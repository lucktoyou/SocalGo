package com.socialgo.qq

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.socialgo.core.common.SocialConstants
import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnShareListener
import com.socialgo.core.model.ShareEntity
import com.socialgo.core.platform.IShareAction
import com.socialgo.core.platform.Target
import com.socialgo.core.utils.SocialGoUtils
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzonePublish
import com.tencent.connect.share.QzoneShare
import com.tencent.tauth.DefaultUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import java.util.*

/**
 * @author Pinger
 * @since 2019/1/31 18:19
 */
class QQShareHelper(private val tencentApi: Tencent, private val appName: String?) : IShareAction {

    private var mShareListener: OnShareListener? = null
    private var mUiListenerWrap: UiListenerWrap? = null

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        mShareListener = listener
        if (mUiListenerWrap == null) {
            mUiListenerWrap = UiListenerWrap()
        }
        super.share(activity, target, entity, listener)
    }

    override fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        when (shareTarget) {
            Target.SHARE_QQ_FRIENDS -> {
                //使用intent兼容分享文本
                val pkg = SocialConstants.QQ_PKG;
                val page = SocialConstants.QQ_FRIENDS_PAGE;
                val result = SocialGoUtils.shareText(activity, "", entity.getSummary(), pkg, page)
                if (result)
                    setSuccess()
                else
                    setFailure(SocialError(SocialError.CODE_SHARE_BY_INTENT_FAIL).append("shareText by intent($pkg -> $page) failure"))
            }
            Target.SHARE_QQ_ZONE -> {
                val params = Bundle()
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
                params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, entity.getSummary())
                tencentApi.publishToQzone(activity, params, mUiListenerWrap)
            }
        }
    }

    override fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        when (shareTarget) {
            Target.SHARE_QQ_FRIENDS -> {
                val params = Bundle()
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, entity.getImagePath())
                tencentApi.shareToQQ(activity, params, mUiListenerWrap)
            }
            Target.SHARE_QQ_ZONE -> {
                val params = Bundle()
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
                params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, ArrayList<String>().also { it.add(entity.getImagePath()) })
                tencentApi.publishToQzone(activity, params, mUiListenerWrap)
            }
        }
    }

    override fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        when (shareTarget) {
            Target.SHARE_QQ_FRIENDS -> {
                val params = Bundle()
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
                params.putString(QQShare.SHARE_TO_QQ_TITLE, entity.getTitle())
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, entity.getSummary())
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, entity.getImagePath())
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, entity.getUrl())
                tencentApi.shareToQQ(activity, params, mUiListenerWrap)
            }
            Target.SHARE_QQ_ZONE -> {
                val params = Bundle()
                params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
                params.putString(QzoneShare.SHARE_TO_QQ_APP_NAME, appName)
                params.putString(QzoneShare.SHARE_TO_QQ_TITLE, entity.getTitle())
                params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, entity.getSummary())
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, ArrayList<String>().also { it.add(entity.getImagePath()) })
                params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, entity.getUrl())
                tencentApi.shareToQzone(activity, params, mUiListenerWrap)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Tencent.handleResultData(data, mUiListenerWrap)
    }

    private inner class UiListenerWrap : DefaultUiListener() {
        override fun onComplete(o: Any) {
            setSuccess()
        }

        override fun onCancel() {
            setCancel()
        }

        override fun onError(uiError: UiError?) {
            setFailure(SocialError(SocialError.CODE_SDK_ERROR).append(uiError.toString()))
        }
    }

    fun setSuccess() {
        mShareListener?.getFunction()?.onSuccess?.invoke()
    }

    fun setCancel() {
        mShareListener?.getFunction()?.onCancel?.invoke()
    }

    fun setFailure(error: SocialError) {
        mShareListener?.getFunction()?.onFailure?.invoke(error)
    }
}