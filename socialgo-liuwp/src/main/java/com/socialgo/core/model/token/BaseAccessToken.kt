package com.socialgo.core.model.token

import android.content.Context
import android.content.SharedPreferences

import com.socialgo.core.SocialGo
import com.socialgo.core.platform.Target
import com.socialgo.core.utils.SocialGoUtils

/**
 * 登录Token，基类
 */
abstract class BaseAccessToken {

    var openid: String? = null        // 授权用户唯一标识。
    var access_token: String? = null  // 接口调用凭证
    var expires_in: Long = 0          // access_token接口调用凭证超时时间，单位（秒）


    override fun toString(): String {
        return "BaseAccessToken{" +
                "openid='" + openid + '\''.toString() +
                ", access_token='" + access_token + '\''.toString() +
                ", expires_in=" + expires_in +
                '}'.toString()
    }

    companion object {
        // 静态 token 存取
        private const val TOKEN_STORE = "token_store"
        const val WX_TOKEN = "wx_token_key"
        const val WB_TOKEN = "wb_token_key"
        const val QQ_TOKEN = "qq_token_key"

        private fun getSp(context: Context): SharedPreferences {
            return context.getSharedPreferences(TOKEN_STORE, Context.MODE_PRIVATE)
        }

        fun <T> getToken(context: Context?, key: String, tokenClazz: Class<T>): T? {
            if (context == null) {
                return null
            }
            val sp = getSp(context)
            return SocialGoUtils.getObject(sp.getString(key, null), tokenClazz)
        }

        fun saveToken(context: Context?, key: String, token: Any?) {
            if (context != null) {
                SocialGo.getExecutor().execute {
                    try {
                        val sp = getSp(context)
                        val tokenJson = SocialGoUtils.getObject2Json(token)
                        sp.edit().putString(key, tokenJson).apply()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // 清理平台 token
        fun clearToken(context: Context?, @Target.LoginTarget platform: Int) {
            if (context != null) {
                var key: String? = null
                when (platform) {
                    Target.LOGIN_QQ -> key = QQ_TOKEN
                    Target.LOGIN_WB -> key = WB_TOKEN
                    Target.LOGIN_WX -> key = WX_TOKEN
                }
                if (key != null) {
                    val edit = getSp(context).edit()
                    edit.remove(key).apply()
                }
            }
        }
    }
}
