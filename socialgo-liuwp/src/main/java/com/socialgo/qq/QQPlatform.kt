package com.socialgo.qq

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.socialgo.core.SocialGo
import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnLoginListener
import com.socialgo.core.listener.OnShareListener
import com.socialgo.core.model.ShareEntity
import com.socialgo.core.platform.AbsPlatform
import com.socialgo.core.platform.IPlatform
import com.socialgo.core.platform.PlatformCreator
import com.socialgo.qq.uikit.QQActionActivity
import com.socialgo.core.utils.SocialGoUtils
import com.tencent.connect.common.Constants
import com.tencent.tauth.Tencent

/**
 * QQ平台
 * [登录分享文档](https://wiki.open.qq.com/index.php?title=Android_SDK%E5%8A%9F%E8%83%BD%E5%88%97%E8%A1%A8)
 *
 * 问题汇总：com.mTencentApi.tauth.AuthActivity需要添加（ <data android:scheme="tencent110557146"></data>）否则会一直返回分享取消
 * qq空间支持本地视频分享，网络视频使用web形式分享
 * qq好友不支持本地视频分享，支持网络视频分享
 */
class QQPlatform private constructor(context: Context, qqAppId: String?, appName: String?) : AbsPlatform(appName) {

    private var mQQLoginHelper: QQLoginHelper? = null
    private var mQQShareHelper: QQShareHelper? = null
    private var tencentApi: Tencent = Tencent.createInstance(qqAppId, context, SocialGoUtils.getFileProviderAuthority(context))

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform {
            val config = SocialGo.getConfig()
            if (SocialGoUtils.isAnyEmpty(config.getQqAppId())) {
                throw IllegalArgumentException(SocialError.MSG_QQ_APPID_NULL)
            }
            return QQPlatform(context, config.getQqAppId(), config.getAppName())
        }
    }

    override fun getActionClazz(): Class<*> {
        return QQActionActivity::class.java
    }

    override fun isInstall(context: Context): Boolean {
        return tencentApi.isQQInstalled(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_QQ_SHARE || requestCode == Constants.REQUEST_QZONE_SHARE) {
            mQQShareHelper?.onActivityResult(requestCode, resultCode, data)
        } else if (requestCode == Constants.REQUEST_LOGIN) {
            mQQLoginHelper?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun recycle() {
        mQQLoginHelper = null
        mQQShareHelper = null
    }

    override fun login(activity: Activity, listener: OnLoginListener) {
        if (!tencentApi.isSupportSSOLogin(activity)) {
            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_VERSION_LOW))
            return
        }
        if (mQQLoginHelper == null) {
            mQQLoginHelper = QQLoginHelper(activity, tencentApi)
        }
        mQQLoginHelper!!.login(listener)
    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        if (mQQShareHelper == null) {
            mQQShareHelper = QQShareHelper(tencentApi, appName)
        }
        mQQShareHelper!!.share(activity, target, entity, listener)
    }
}
