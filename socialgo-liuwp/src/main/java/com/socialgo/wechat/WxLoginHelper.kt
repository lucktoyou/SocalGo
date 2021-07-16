package com.socialgo.wechat

import android.content.Context

import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnLoginListener
import com.socialgo.core.model.LoginResult
import com.socialgo.core.model.token.BaseAccessToken
import com.socialgo.core.platform.Target
import com.socialgo.wechat.model.WeChatAccessToken
import com.socialgo.wechat.model.WxUser
import com.socialgo.core.utils.SocialGoUtils
import com.socialgo.core.utils.SocialLogUtils
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI

import java.lang.ref.WeakReference

/**
 * 微信登录助手
 */
class WxLoginHelper(context: Context, private val wxapi: IWXAPI?, private val wxAppId: String?, private val wxAppSecret: String?) {

    private val mLoginType: Int = Target.LOGIN_WX
    private val mContextRef: WeakReference<Context> = WeakReference(context)
    private var mLoginListener: OnLoginListener? = null

    /**
     * 开始登录
     *  ① 检测本地是否有token。
     *  ② 本地有token，检测是否还有效。
     *  ③ 本地没有token，发起请求，回调后将会获得code，接着获取access_token。
     */
    fun login(listener: OnLoginListener) {
        this.mLoginListener = listener
        val storeToken = getToken()
        if (storeToken != null) {
            checkAccessTokenValid(storeToken)
        } else {
            sendAuthReq()
        }
    }

    /**
     * 发起申请
     */
    private fun sendAuthReq() {
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "carjob_wx_login"
        wxapi?.sendReq(req)
    }

    /**
     * 刷新token,当access_token失效时使用,使用refresh_token获取新的token
     *
     * @param token 用来放 refresh_token
     */
    private fun refreshToken(token: WeChatAccessToken) {
        SocialGoUtils.startJsonRequest(buildRefreshTokenUrl(token), WeChatAccessToken::class.java, object : SocialGoUtils.Callback<WeChatAccessToken> {
            override fun onSuccess(data: WeChatAccessToken?) {
                SocialLogUtils.e("成功刷新wx token：$data")
                if (data?.isNoError == true) {
                    saveToken(data)
                    getUserInfoByValidToken(data)
                } else {
                    sendAuthReq()
                }
            }

            override fun onFailure(e: SocialError) {
                SocialLogUtils.e("刷新wx token失败：$e")
                setFailure(e)
            }
        })
    }

    /**
     * 根据code获取access_token
     *
     * @param code code
     */
    fun getAccessTokenByCode(code: String) {
        SocialGoUtils.startJsonRequest(buildGetTokenUrl(code), WeChatAccessToken::class.java, object : SocialGoUtils.Callback<WeChatAccessToken> {
            override fun onSuccess(data: WeChatAccessToken?) {
                SocialLogUtils.e("获取到wx token：$data")
                if (data?.isNoError == true) {
                    saveToken(data)
                    getUserInfoByValidToken(data)
                } else {
                    setFailure(SocialError(SocialError.CODE_NETWORK_REQUEST_FAIL))
                }
            }

            override fun onFailure(e: SocialError) {
                SocialLogUtils.e("获取wx token失败：$e")
                setFailure(e)
            }
        })
    }

    /**
     * 检测token有效性
     *
     * @param token 用来拿access_token
     */
    private fun checkAccessTokenValid(token: WeChatAccessToken) {
        SocialGoUtils.startJsonRequest(buildCheckAccessTokenValidUrl(token), TokenValidResp::class.java, object : SocialGoUtils.Callback<TokenValidResp> {
            override fun onSuccess(data: TokenValidResp?) {
                SocialLogUtils.e("成功检测wx token有效性：$data")
                if (data?.isNoError == true) {
                    getUserInfoByValidToken(token)
                } else {
                    refreshToken(token)
                }
            }

            override fun onFailure(e: SocialError) {
                SocialLogUtils.e("检测wx token有效性失败：$e")
                setFailure(e)
            }
        })
    }

    /**
     * token是ok的，获取用户信息
     *
     * @param token 用来拿access_token
     */
    private fun getUserInfoByValidToken(token: WeChatAccessToken) {
        SocialGoUtils.startJsonRequest(buildFetchUserInfoUrl(token), WxUser::class.java, object : SocialGoUtils.Callback<WxUser> {
            override fun onSuccess(data: WxUser?) {
                SocialLogUtils.e("成功获取wx user info：$data")
                if (data?.isNoError == true) {
                    setSuccess(LoginResult(mLoginType, data, token))
                } else {
                    setFailure(SocialError(SocialError.CODE_NETWORK_REQUEST_FAIL))
                }
            }

            override fun onFailure(e: SocialError) {
                SocialLogUtils.e("获取wx user info失败：$e")
                setFailure(e)
            }
        })
    }

    fun setSuccess(loginResult: LoginResult) {
        mLoginListener?.getFunction()?.onLoginSuccess?.invoke(loginResult)
    }

    fun setCancel() {
        mLoginListener?.getFunction()?.onCancel?.invoke()
    }

    fun setFailure(error: SocialError) {
        mLoginListener?.getFunction()?.onFailure?.invoke(error)
    }

    private fun getToken(): WeChatAccessToken? {
        return BaseAccessToken.getToken(mContextRef.get(), BaseAccessToken.WX_TOKEN, WeChatAccessToken::class.java)
    }

    private fun saveToken(data: WeChatAccessToken?) {
        BaseAccessToken.saveToken(mContextRef.get(), BaseAccessToken.WX_TOKEN, data)
    }

    private fun buildRefreshTokenUrl(token: WeChatAccessToken): String {
        return (BASE_URL
                + "/oauth2/refresh_token"
                + "?appid=" + wxAppId
                + "&grant_type=" + "refresh_token"
                + "&refresh_token=" + token.access_token)
    }

    private fun buildGetTokenUrl(code: String): String {
        return (BASE_URL
                + "/oauth2/access_token"
                + "?appid=" + wxAppId
                + "&secret=" + wxAppSecret
                + "&code=" + code
                + "&grant_type=" + "authorization_code")
    }

    private fun buildCheckAccessTokenValidUrl(token: WeChatAccessToken): String {
        return (BASE_URL
                + "/auth"
                + "?access_token=" + token.access_token
                + "&openid=" + token.openid)
    }

    private fun buildFetchUserInfoUrl(token: WeChatAccessToken): String {
        return (BASE_URL
                + "/userinfo"
                + "?access_token=" + token.access_token
                + "&openid=" + token.openid)
    }

    /**
     * 检测token有效性的resp
     */
    private class TokenValidResp {
        var errcode: Int = 0
        var errmsg: String? = null

        val isNoError: Boolean
            get() = errcode == 0

        override fun toString(): String {
            return "TokenValidResp{" +
                    "errcode=" + errcode +
                    ", errmsg='" + errmsg + '\''.toString() +
                    '}'.toString()
        }
    }

    companion object {
        private const val BASE_URL = "https://api.weixin.qq.com/sns"
    }
}
