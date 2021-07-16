package com.socialgo.weibo

import android.app.Activity
import android.content.Intent
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.common.UiError
import com.sina.weibo.sdk.openapi.IWBAPI
import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnLoginListener
import com.socialgo.core.model.LoginResult
import com.socialgo.core.model.token.BaseAccessToken
import com.socialgo.core.platform.Target
import com.socialgo.core.utils.SocialGoUtils
import com.socialgo.core.utils.SocialLogUtils
import com.socialgo.weibo.model.SinaAccessToken
import com.socialgo.weibo.model.SinaUser


/**
 * 微博登陆辅助工具类
 */
class WbLoginHelper(private val wbApi: IWBAPI) {

    private val mLoginType: Int = Target.LOGIN_WB
    private var mLoginListener: OnLoginListener? = null

    fun login(activity: Activity, listener: OnLoginListener) {
        mLoginListener = listener
        justAuth(activity, object : WbAuthListener {
            override fun onComplete(token: Oauth2AccessToken?) {
                getUserInfo(token)
            }

            override fun onCancel() {
                setCancel()
            }

            override fun onError(uiError: UiError?) {
                setFailure(SocialError(SocialError.CODE_SDK_ERROR).append(uiError.toString()))
            }
        })
    }

    fun justAuth(activity: Activity, wbAuthListener: WbAuthListener) {
        val token = BaseAccessToken.getToken(activity, BaseAccessToken.WB_TOKEN, Oauth2AccessToken::class.java)
        if (token != null && token.isSessionValid && token.expiresTime > System.currentTimeMillis()) {
            wbAuthListener.onComplete(token)
        } else {
            wbApi.authorize(object : WbAuthListener {
                override fun onComplete(oauth2AccessToken: Oauth2AccessToken?) {
                    SocialLogUtils.e("成功获取wb token：accessToken =${token?.accessToken}")
                    BaseAccessToken.saveToken(activity, BaseAccessToken.WB_TOKEN, oauth2AccessToken)
                    wbAuthListener.onComplete(oauth2AccessToken)
                }

                override fun onCancel() {
                    wbAuthListener.onCancel()
                }

                override fun onError(uiError: UiError?) {
                    SocialLogUtils.e("获取wb token失败：${uiError.toString()}")
                    wbAuthListener.onError(uiError)
                }
            })
        }
    }

    /**
     * 获取用户信息
     */
    private fun getUserInfo(token: Oauth2AccessToken?) {
        SocialGoUtils.startJsonRequest("https://api.weibo.com/2/users/show.json?access_token=" + token?.accessToken + "&uid=" + token?.uid, SinaUser::class.java, object : SocialGoUtils.Callback<SinaUser> {
            override fun onSuccess(data: SinaUser?) {
                SocialLogUtils.e("成功获取wb user info：$data")
                if (data != null && token != null) {
                    setSuccess(LoginResult(mLoginType, data, SinaAccessToken(token)))
                } else {
                    onFailure(SocialError(SocialError.CODE_NETWORK_REQUEST_FAIL))
                }
            }

            override fun onFailure(e: SocialError) {
                SocialLogUtils.e("获取wb user info失败：$e")
                setFailure(e)
            }
        })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        wbApi.authorizeCallback(requestCode, resultCode, data)
    }

    private fun setSuccess(loginResult: LoginResult) {
        mLoginListener?.getFunction()?.onLoginSuccess?.invoke(loginResult)
    }

    private fun setCancel() {
        mLoginListener?.getFunction()?.onCancel?.invoke()
    }

    private fun setFailure(error: SocialError) {
        mLoginListener?.getFunction()?.onFailure?.invoke(error)
    }
}
