package com.socialgo.weibo

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.openapi.IWBAPI
import com.sina.weibo.sdk.openapi.WBAPIFactory
import com.socialgo.core.SocialGo
import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnLoginListener
import com.socialgo.core.listener.OnShareListener
import com.socialgo.core.model.ShareEntity
import com.socialgo.core.platform.AbsPlatform
import com.socialgo.core.platform.IPlatform
import com.socialgo.core.platform.PlatformCreator
import com.socialgo.core.utils.SocialGoUtils
import com.socialgo.weibo.uikit.WbActionActivity

/**
 * 新浪微博平台
 * [移动应用接入文档](https://open.weibo.com/wiki/%E7%A7%BB%E5%8A%A8%E5%BA%94%E7%94%A8%E4%BB%8B%E7%BB%8D)
 */
class WbPlatform private constructor(context: Context, var wbAppKey: String?, var wbRedirectUrl: String?, var wbScope: String?, appName: String?) : AbsPlatform(appName) {

    private var mLoginHelper: WbLoginHelper? = null
    private var mShareHelper: WbShareHelper? = null
    private var wbApi: IWBAPI = WBAPIFactory.createWBAPI(context)

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform {
            val config = SocialGo.getConfig()
            if (SocialGoUtils.isAnyEmpty(config.getWbAppKey(), config.getWbRedirectUrl(), config.getWbScope())) {
                throw IllegalArgumentException(SocialError.MSG_WB_APPKEY_OR_REDIRECTURL_OR_SCOPE_NULL)
            }
            return WbPlatform(context, config.getWbAppKey(), config.getWbRedirectUrl(), config.getWbScope(), config.getAppName())
        }
    }

    override fun getActionClazz(): Class<*> {
        return WbActionActivity::class.java
    }

    override fun isInstall(context: Context): Boolean {
        return wbApi.isWBAppInstalled
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mLoginHelper?.onActivityResult(requestCode, resultCode, data)
        mShareHelper?.onActivityResult(requestCode, resultCode, data)
    }

    override fun recycle() {
        mLoginHelper = null
        mShareHelper = null
    }

    override fun login(activity: Activity, listener: OnLoginListener) {
        if (mLoginHelper == null) {
            wbApi = WBAPIFactory.createWBAPI(activity)
            wbApi.registerApp(activity, AuthInfo(activity, wbAppKey, wbRedirectUrl, wbScope))
            mLoginHelper = WbLoginHelper(wbApi)
        }
        mLoginHelper!!.login(activity, listener)
    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        if (mShareHelper == null) {
            wbApi = WBAPIFactory.createWBAPI(activity)
            wbApi.registerApp(activity, AuthInfo(activity, wbAppKey, wbRedirectUrl, wbScope))
            mShareHelper = WbShareHelper(wbApi)
        }
        mShareHelper!!.share(activity, target, entity, listener)
    }
}
