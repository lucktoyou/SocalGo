package com.socialgo.wechat

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.socialgo.core.SocialGo
import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnLoginListener
import com.socialgo.core.listener.OnPayListener
import com.socialgo.core.listener.OnShareListener
import com.socialgo.core.model.LoginResult
import com.socialgo.core.model.ShareEntity
import com.socialgo.core.platform.AbsPlatform
import com.socialgo.core.platform.IPlatform
import com.socialgo.core.platform.PlatformCreator
import com.socialgo.core.platform.Target
import com.socialgo.core.utils.SocialGoUtils
import com.socialgo.wechat.uikit.WxActionActivity
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.json.JSONException
import org.json.JSONObject

/**
 * 微信平台
 * [分享与收藏文档](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317340&token=&lang=zh_CN)
 * [微信登录文档](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317851&token=&lang=zh_CN)
 *
 * 缩略图不超过 32kb
 * 源文件不超过 10M
 */
class WxPlatform private constructor(context: Context, private val wxAppId: String?, private val wxAppSecret: String?, appName: String?) : AbsPlatform(appName) {

    private var mLoginHelper: WxLoginHelper? = null
    private var mShareHelper: WxShareHelper? = null
    private var mPayListener: OnPayListener? = null
    private var wxApi: IWXAPI = WXAPIFactory.createWXAPI(context, wxAppId, true)

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform {
            val config = SocialGo.getConfig()
            if (SocialGoUtils.isAnyEmpty(config.getWxAppId(), config.getWxAppSecret())) {
                throw IllegalArgumentException(SocialError.MSG_WX_APPID_OR_APPSECRET_NULL)
            }
            return WxPlatform(context, config.getWxAppId(), config.getWxAppSecret(), config.getAppName())
        }
    }

    init {
        wxApi.registerApp(wxAppId)
    }

    override fun getActionClazz(): Class<*> {
        return WxActionActivity::class.java
    }

    override fun isInstall(context: Context): Boolean {
        return wxApi.isWXAppInstalled
    }

    override fun handleIntent(activity: Activity) {
        if (activity is IWXAPIEventHandler) {
            wxApi.handleIntent(activity.intent, activity as IWXAPIEventHandler)
        }
    }

    override fun onResponse(resp: Any) {
        if (resp !is BaseResp) {
            return
        }
        when (resp.type) {
            //登录
            ConstantsAPI.COMMAND_SENDAUTH -> {
                when (resp.errCode) {
                    BaseResp.ErrCode.ERR_OK -> {// 用户同意 authResp.country;  authResp.lang;  authResp.state;
                        val authResp = resp as SendAuth.Resp
                        // 这个code如果需要使用微信充值功能的话，服务端需要使用
                        // 这里为了安全暂时不提供出去
                        val authCode = authResp.code
                        if (SocialGo.getConfig().isOnlyAuthCode()) {
                            mLoginHelper?.setSuccess(LoginResult(Target.LOGIN_WX, authCode))
                        } else {
                            mLoginHelper?.getAccessTokenByCode(authCode)
                        }
                    }
                    BaseResp.ErrCode.ERR_USER_CANCEL -> {// 用户取消
                        mLoginHelper?.setCancel()
                    }
                    BaseResp.ErrCode.ERR_AUTH_DENIED -> {// 用户拒绝授权
                        mLoginHelper?.setFailure(SocialError(SocialError.CODE_AUTH_REJECTED))
                    }
                }
            }
            //分享
            ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX -> {
                when (resp.errCode) {
                    BaseResp.ErrCode.ERR_OK -> {            // 分享成功
                        mShareHelper?.setSuccess()
                    }
                    BaseResp.ErrCode.ERR_USER_CANCEL -> {   // 分享取消
                        mShareHelper?.setCancel()
                    }
                    BaseResp.ErrCode.ERR_SENT_FAILED -> {   // 分享失败
                        mShareHelper?.setFailure(SocialError(SocialError.CODE_SHARE_FAIL))
                    }
                    BaseResp.ErrCode.ERR_AUTH_DENIED -> {   // 分享被拒绝
                        mShareHelper?.setFailure(SocialError(SocialError.CODE_SHARE_REJECTED))
                    }
                }
            }
            //支付
            ConstantsAPI.COMMAND_PAY_BY_WX -> {
                onPayResp(resp.errCode)
            }
        }
    }

    override fun recycle() {
        wxApi.detach()
        mLoginHelper = null
        mShareHelper = null
        mPayListener = null
    }

    override fun login(activity: Activity, listener: OnLoginListener) {
        if (mLoginHelper == null) {
            mLoginHelper = WxLoginHelper(activity, wxApi, wxAppId, wxAppSecret)
        }
        mLoginHelper!!.login(listener)
    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        if (mShareHelper == null) {
            mShareHelper = WxShareHelper(wxApi)
        }
        mShareHelper!!.share(activity, target, entity, listener)
    }


    override fun doPay(context: Context, params: String, listener: OnPayListener) {
        // 判断微信当前版本是否支持支付
        if (wxApi.wxAppSupportAPI < Build.PAY_SUPPORTED_SDK_INT) {
            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_VERSION_LOW))
            return
        }
        mPayListener = listener
        val json: JSONObject
        try {
            json = JSONObject(params)
        } catch (e: JSONException) {
            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_EXCEPTION).append(e.toString()))
            return
        }
        if (TextUtils.isEmpty(json.optString("appid")) || TextUtils.isEmpty(json.optString("partnerid"))
                || TextUtils.isEmpty(json.optString("prepayid")) || TextUtils.isEmpty(json.optString("package")) ||
                TextUtils.isEmpty(json.optString("noncestr")) || TextUtils.isEmpty(json.optString("timestamp")) ||
                TextUtils.isEmpty(json.optString("sign"))) {
            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_PAY_PARAM_INCOMPLETE))
            return
        }

        val req = PayReq()
        req.appId = json.optString("appid")
        req.partnerId = json.optString("partnerid")
        req.prepayId = json.optString("prepayid")
        req.packageValue = json.optString("package")
        req.nonceStr = json.optString("noncestr")
        req.timeStamp = json.optString("timestamp")
        req.sign = json.optString("sign")
        wxApi.sendReq(req)
    }

    /**
     * 支付回调响应
     */
    private fun onPayResp(code: Int) {
        when (code) {
            0 -> //成功
                mPayListener?.getFunction()?.onSuccess?.invoke()
            -1 -> //错误
                mPayListener?.getFunction()?.onFailure?.invoke(SocialError(SocialError.CODE_PAY_ERROR))
            -2 -> //取消
                mPayListener?.getFunction()?.onCancel?.invoke()
        }
    }
}
