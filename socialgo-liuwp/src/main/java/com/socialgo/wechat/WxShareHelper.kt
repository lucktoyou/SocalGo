package com.socialgo.wechat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import com.socialgo.core.SocialGo
import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnShareListener
import com.socialgo.core.model.ShareEntity
import com.socialgo.core.platform.IShareAction
import com.socialgo.core.platform.Target
import com.socialgo.core.utils.SocialGoUtils
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import java.io.File

/**
 * @author Pinger
 * @since 2019/1/31 17:53
 */
class WxShareHelper(private val wxApi: IWXAPI) : IShareAction {

    private var mShareListener: OnShareListener? = null

    override fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        mShareListener = listener
        super.share(activity, target, entity, listener)
    }

    override fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        val textObj = WXTextObject()
        textObj.text = entity.getSummary()
        val msg = WXMediaMessage()
        msg.mediaObject = textObj
        msg.description = entity.getSummary()
        sendMsgToWx(msg, shareTarget, "text")
    }

    override fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialGo.getExecutor().execute {
            val thumbData = SocialGoUtils.getStaticSizeBitmapByteByPath(entity.getImagePath())
            SocialGo.getHandler().post {
                if (thumbData == null) {
                    setFailure(SocialError(SocialError.CODE_IMAGE_COMPRESS_FAIL))
                } else {
                    val localPathCompat = getLocalPathCompat(activity, entity.getImagePath())
                    if (shareTarget == Target.SHARE_WX_FRIENDS && SocialGoUtils.isGifFile(entity.getImagePath())) {
                        toShareEmoji(shareTarget, localPathCompat, thumbData)
                    } else {
                        toShareImage(shareTarget, localPathCompat, thumbData)
                    }
                }
            }
        }
    }

    override fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity) {
        SocialGo.getExecutor().execute {
            val thumbData = SocialGoUtils.getStaticSizeBitmapByteByPath(entity.getImagePath())
            SocialGo.getHandler().post {
                if (thumbData == null) {
                    setFailure(SocialError(SocialError.CODE_IMAGE_COMPRESS_FAIL))
                } else {
                    val webPage = WXWebpageObject()
                    webPage.webpageUrl = entity.getUrl()
                    val msg = WXMediaMessage(webPage)
                    msg.title = entity.getTitle()
                    msg.description = entity.getSummary()
                    msg.thumbData = thumbData
                    sendMsgToWx(msg, shareTarget, "web")
                }
            }
        }
    }

    private fun toShareImage(shareTarget: Int, localPath: String, thumbData: ByteArray) {
        // 文件大小不大于10485760  路径长度不大于10240
        val imgObj = WXImageObject()
        imgObj.imagePath = localPath
        val msg = WXMediaMessage()
        msg.mediaObject = imgObj
        msg.thumbData = thumbData
        sendMsgToWx(msg, shareTarget, "image")
    }

    private fun toShareEmoji(shareTarget: Int, localPath: String, thumbData: ByteArray) {
        val emoji = WXEmojiObject()
        emoji.emojiPath = localPath
        val msg = WXMediaMessage()
        msg.mediaObject = emoji
        msg.thumbData = thumbData
        sendMsgToWx(msg, shareTarget, "emoji")
    }

    private fun getLocalPathCompat(context: Context, localPath: String): String {
        //判断微信版本是否为7.0.13及以上 && 判断Android版本是否7.0及以上
        return if (wxApi.wxAppSupportAPI >= 0x27000D00 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val file = File(localPath)
            val uriForFile = FileProvider.getUriForFile(context, SocialGoUtils.getFileProviderAuthority(context), file)
            // 授权给微信访问路径
            context.grantUriPermission("com.tencent.mm",//这里填微信包名
                    uriForFile, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uriForFile.toString()
        } else {
            localPath
        }
    }

    private fun sendMsgToWx(msg: WXMediaMessage, shareTarget: Int, sign: String) {
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction(sign)
        req.message = msg
        req.scene = getShareToWhere(shareTarget)
        wxApi.sendReq(req)
    }

    private fun buildTransaction(type: String?): String {
        return if (type == null) System.currentTimeMillis().toString() else type + System.currentTimeMillis()
    }

    private fun getShareToWhere(shareTarget: Int): Int {
        var where = SendMessageToWX.Req.WXSceneSession
        when (shareTarget) {
            Target.SHARE_WX_FRIENDS -> where = SendMessageToWX.Req.WXSceneSession
            Target.SHARE_WX_ZONE -> where = SendMessageToWX.Req.WXSceneTimeline
        }
        return where
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