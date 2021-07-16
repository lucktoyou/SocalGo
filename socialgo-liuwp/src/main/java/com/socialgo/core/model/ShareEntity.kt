package com.socialgo.core.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.socialgo.core.SocialGo
import com.socialgo.core.utils.SocialGoUtils

/**
 * 分享实体
 */
class ShareEntity(private val entityType: Int) : Parcelable {

    private var title: String? = null
    private var summary: String? = null
    private var image :String? = null
    private var url: String? = null

    constructor(parcel: Parcel) : this(parcel.readInt()) {
        title = parcel.readString()
        summary = parcel.readString()
        image = parcel.readString()
        url = parcel.readString()
    }

    fun getTitle(): String {
        return title ?: ""
    }

    fun getSummary(): String {
        return summary ?: ""
    }

    fun getImagePath(): String {
        return image ?: ""
    }

    fun getUrl(): String {
        return url ?: ""
    }

    fun getEntityType(): Int {
        return entityType
    }

    /**
     * 准备工作，在子线程执行。
     *
     * ①如果图片来自网络，先下载到本地缓存，后续使用缓存的本地图片路径。
     * ②如果图片来自本地，后续直接使用本地图片路径。
     */
    fun prepareImageInBackground(context :Context):Boolean {
        var prepared: Boolean
        try {
            val temp = image
            if (!TextUtils.isEmpty(temp) && SocialGoUtils.isHttpPath(temp)) {
                val file = SocialGo.getRequestAdapter().getFile(temp!!)
                if (SocialGoUtils.isExist(file)) {
                    image = file!!.absolutePath
                } else if (SocialGo.getConfig().getDefImageResId() > 0) {
                    val localPath = SocialGoUtils.mapResId2LocalPath(context, SocialGo.getConfig().getDefImageResId())
                    if (SocialGoUtils.isExist(localPath)) {
                        image = localPath!!
                    }
                }
            }
            prepared = true
        } catch (e: Exception) {
            prepared = false
        }
        return prepared
    }

    override fun toString(): String {
        return "ShareEntity{" +
                "entityType=" + entityType +
                ", title='" + title + '\''.toString() +
                ", summary='" + summary + '\''.toString() +
                ", image='" +  image +'\''.toString() +
                ", url='" + url + '\''.toString() +
                '}'.toString()
    }

    companion object CREATOR : Parcelable.Creator<ShareEntity> {

        const val ENTITY_IS_TEXT = 0x41      // 分享文字
        const val ENTITY_IS_IMAGE = 0x42     // 分享图片
        const val ENTITY_IS_WEB = 0x44       // 分享web

        //分享文字.
        //qq好友不支持，使用intent兼容.
        //qq空间、微信好友、微信朋友圈、微博都支持.
        fun buildText(summary: String): ShareEntity {
            return ShareEntity(ENTITY_IS_TEXT).also {
                it.summary = summary
            }
        }

        // 分享图片
        fun buildImage(imageLocalPathOrUrl: String): ShareEntity {
            return ShareEntity(ENTITY_IS_IMAGE).also {
                it.image = imageLocalPathOrUrl
            }
        }

        // 分享网页
        fun buildWeb(title: String, summary: String, thumbImageLocalPathOrUrl: String, targetUrl: String): ShareEntity {
            return ShareEntity(ENTITY_IS_WEB).also {
                it.title = title
                it.summary = summary
                it.image = thumbImageLocalPathOrUrl
                it.url = targetUrl
            }
        }

        override fun createFromParcel(parcel: Parcel): ShareEntity {
            return ShareEntity(parcel)
        }

        override fun newArray(size: Int): Array<ShareEntity?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(entityType)
        parcel.writeString(title)
        parcel.writeString(summary)
        parcel.writeString(image)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }
}
