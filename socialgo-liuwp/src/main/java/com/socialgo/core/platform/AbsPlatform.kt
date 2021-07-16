package com.socialgo.core.platform


import android.app.Activity
import android.content.Context
import android.content.Intent
import com.socialgo.core.listener.OnLoginListener
import com.socialgo.core.listener.OnPayListener
import com.socialgo.core.listener.OnShareListener
import com.socialgo.core.model.ShareEntity
import com.socialgo.core.uikit.BaseActionActivity

/**
 * 第三方平台基类
 */
abstract class AbsPlatform(protected var appName: String?) : IPlatform {

    ///////////////////////////////////////////////////////////////////////////
    // IPlatform
    // ///////////////////////////////////////////////////////////////////////

    override fun getActionClazz(): Class<*> {
        return BaseActionActivity::class.java
    }

    override fun login(activity: Activity, listener: OnLoginListener) {

    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {

    }

    override fun doPay(context: Context, params: String, listener: OnPayListener) {
    }

    ///////////////////////////////////////////////////////////////////////////
    // PlatformLifecycle
    // ///////////////////////////////////////////////////////////////////////

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    override fun handleIntent(activity: Activity) {

    }

    override fun onResponse(resp: Any) {

    }

    ///////////////////////////////////////////////////////////////////////////
    // Recyclable
    // ///////////////////////////////////////////////////////////////////////

    override fun recycle() {

    }
}
