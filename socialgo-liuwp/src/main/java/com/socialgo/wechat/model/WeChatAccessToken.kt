package com.socialgo.wechat.model

import com.socialgo.core.model.token.BaseAccessToken

/**
 * 微信的AccessToken
 */
class WeChatAccessToken : BaseAccessToken() {

    val refresh_token: String? = null//用户刷新access_token。
    val scope: String? = null//用户授权的作用域，使用逗号（,）分隔
    val errcode: Int = 0
    val errmsg: String? = null

    val isNoError: Boolean
        get() = errcode == 0
}