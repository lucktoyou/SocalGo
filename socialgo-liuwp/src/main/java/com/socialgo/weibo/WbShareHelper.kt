package com.socialgo.weibo

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.sina.weibo.sdk.api.*
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.common.UiError
import com.sina.weibo.sdk.openapi.IWBAPI
import com.sina.weibo.sdk.share.WbShareCallback
import com.socialgo.core.SocialGo
import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnShareListener
import com.socialgo.core.model.ShareEntity
import com.socialgo.core.platform.IShareAction
import com.socialgo.core.utils.SocialGoUtils
import org.json.JSONObject
import java.util.*

/**
 * @author Pinger
 * @since 2019/1/31 15:59
 */
class WbShareHelper(private val wbApi: IWBAPI) : IShareAction {

    private var mShareListener: OnShareListener? = null
    private var mLoginHelper: WbLoginHelper? = null

    private fun getWbLoginHelper(): WbLoginHelper {
        if (mLoginHelper == null) {
            mLoginHelper = WbLoginHelper(wbApi)
        }
        return mLoginHelper!!
    }

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        mShareListener = listener
        super.share(activity, target, entity, listener)
    }

    override fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val weiboMultiMessage = WeiboMultiMessage()
        val textObject = TextObject()
        textObject.text = entity.getSummary()
        weiboMultiMessage.textObject = textObject
        wbApi.shareMessage(weiboMultiMessage, false)
    }

    override fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        if (SocialGoUtils.isGifFile(entity.getImagePath())) {
            getWbLoginHelper().justAuth(activity, object : WbAuthListener {
                override fun onComplete(token: Oauth2AccessToken?) {
                    SocialGo.getExecutor().execute {
                        val params = HashMap<String, String>()
                        params["access_token"] = token?.accessToken ?: ""
                        val data = SocialGo.getRequestAdapter().postData("https://api.weibo.com/2/statuses/share.json", params, "pic", entity.getImagePath())
                        SocialGo.getHandler().post {
                            if (TextUtils.isEmpty(data)) {
                                setFailure(SocialError(SocialError.CODE_NETWORK_REQUEST_FAIL))
                            } else {
                                val jsonObject = JSONObject(data)
                                if (jsonObject.has("id") && jsonObject.get("id") != null) {
                                    setSuccess()
                                } else {
                                    setFailure(SocialError(SocialError.CODE_DATA_PARSE_FAIL))
                                }
                            }
                        }
                    }
                }

                override fun onCancel() {
                    setCancel()
                }

                override fun onError(uiError: UiError?) {
                    setFailure(SocialError(SocialError.CODE_SDK_ERROR).append(uiError.toString()))
                }
            })
        } else {
            val weiboMultiMessage = WeiboMultiMessage()
            val imageObject = ImageObject()
            imageObject.setImageData(BitmapFactory.decodeFile(entity.getImagePath()))
            weiboMultiMessage.imageObject = imageObject
            wbApi.shareMessage(weiboMultiMessage, false)
        }
    }

    override fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialGo.getExecutor().execute {
            val thumbData = SocialGoUtils.getStaticSizeBitmapByteByPath(entity.getImagePath())
            SocialGo.getHandler().post {
                if (thumbData == null) {
                    setFailure(SocialError(SocialError.CODE_IMAGE_COMPRESS_FAIL))
                } else {
                    val weiboMultiMessage = WeiboMultiMessage()
                    val webpageObject = WebpageObject()
                    webpageObject.identify = UUID.randomUUID().toString()
                    webpageObject.title = entity.getTitle()
                    webpageObject.description = entity.getSummary()
                    webpageObject.thumbData = thumbData // 注意：最终压缩过的缩略图大小不得超过 32kb。
                    webpageObject.actionUrl = entity.getUrl()
                    webpageObject.defaultText = entity.getSummary()
                    weiboMultiMessage.mediaObject = webpageObject
                    wbApi.shareMessage(weiboMultiMessage, false)
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        wbApi.doResultIntent(data, object : WbShareCallback {
            override fun onComplete() {
                setSuccess()
            }

            override fun onCancel() {
                setCancel()
            }

            override fun onError(uiError: UiError?) {
                setFailure(SocialError(SocialError.CODE_SDK_ERROR).append(uiError.toString()))
            }
        })
    }

    private fun setSuccess() {
        mShareListener?.getFunction()?.onSuccess?.invoke()
    }

    private fun setCancel() {
        mShareListener?.getFunction()?.onCancel?.invoke()
    }

    private fun setFailure(error: SocialError) {
        mShareListener?.getFunction()?.onFailure?.invoke(error)
    }
}