package com.socialgo.core

import android.content.Context
import com.socialgo.core.common.SocialConstants
import com.socialgo.core.utils.SocialGoUtils
import java.io.File


/**
 * 第三方平台信息配置
 */
class SocialGoConfig private constructor() {

    private var isDebug: Boolean = false         // 调试配置
    private var appName: String? = null          // 应用名
    private var cacheDir: String? = null         // 存储路径，不允许更改
    private var defImageResId: Int = 0           // 图片默认资源

    private var qqAppId: String? = null          // qq 配置

    private var wxAppId: String? = null          // 微信配置
    private var wxAppSecret: String? = null
    private var isOnlyAuthCode: Boolean = false

    private var wbAppKey: String? = null       // 微博配置
    private var wbRedirectUrl: String? = null
    private var wbScope: String? = null


    fun qq(qqAppId: String): SocialGoConfig {
        this.qqAppId = qqAppId
        return this
    }

    fun wechat(wxAppId: String, wxAppSecret: String): SocialGoConfig {
        this.wxAppSecret = wxAppSecret
        this.wxAppId = wxAppId
        return this
    }

    fun wechat(wxAppId: String, wxAppSecret: String, onlyAuthCode: Boolean): SocialGoConfig {
        this.isOnlyAuthCode = onlyAuthCode
        this.wxAppSecret = wxAppSecret
        this.wxAppId = wxAppId
        return this
    }

    fun weibo(wbAppKey: String): SocialGoConfig {
        this.wbAppKey = wbAppKey
        return this
    }

    fun weiboScope(wbScope: String): SocialGoConfig {
        this.wbScope = wbScope
        return this
    }

    fun weiboRedirectUrl(wbRedirectUrl: String): SocialGoConfig {
        this.wbRedirectUrl = wbRedirectUrl
        return this
    }

    fun defImageResId(defImageResId: Int): SocialGoConfig {
        this.defImageResId = defImageResId
        return this
    }

    fun appName(appName: String): SocialGoConfig {
        this.appName = appName
        return this
    }

    fun debug(debug: Boolean): SocialGoConfig {
        this.isDebug = debug
        return this
    }

    fun getDefImageResId(): Int {
        return defImageResId
    }

    fun getCacheDir(): String? {
        return cacheDir
    }

    fun getAppName(): String? {
        return appName
    }

    fun getWxAppId(): String? {
        return wxAppId
    }

    fun getWxAppSecret(): String? {
        return wxAppSecret
    }

    fun getQqAppId(): String? {
        return qqAppId
    }

    fun getWbAppKey(): String? {
        return wbAppKey
    }

    fun getWbRedirectUrl(): String? {
        return wbRedirectUrl
    }

    fun getWbScope(): String? {
        return wbScope
    }

    fun isDebug(): Boolean {
        return this.isDebug
    }

    fun isOnlyAuthCode(): Boolean {
        return this.isOnlyAuthCode
    }

    override fun toString(): String {
        return "SocialGoConfig{" +
                "appName='" + appName + '\''.toString() +
                ", wxAppId='" + wxAppId + '\''.toString() +
                ", wxAppSecret='" + wxAppSecret + '\''.toString() +
                ", qqAppId='" + qqAppId + '\''.toString() +
                ", wbAppKey='" + wbAppKey + '\''.toString() +
                ", wbRedirectUrl='" + wbRedirectUrl + '\''.toString() +
                ", wbScope='" + wbScope + '\''.toString() +
                ", cacheDir='" + cacheDir + '\''.toString() +
                '}'.toString()
    }

    companion object {

        private const val CACHE_DIR_NAME = "socialgo"

        // 静态工厂
        fun create(context: Context): SocialGoConfig {
            val config = SocialGoConfig()
            val shareDir = File(context.externalCacheDir, CACHE_DIR_NAME)
            if (!SocialGoUtils.isExist(shareDir)) {
                shareDir.mkdirs()
            }
            config.cacheDir = shareDir.absolutePath
            // init
            config.appName = "android_app"
            config.wbRedirectUrl = SocialConstants.WB_REDIRECT_URL
            config.wbScope = SocialConstants.WB_SCOPE
            config.isDebug = false
            return config
        }
    }
}