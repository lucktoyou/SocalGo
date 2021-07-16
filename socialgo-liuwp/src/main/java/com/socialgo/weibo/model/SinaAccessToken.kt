package com.socialgo.weibo.model

import com.socialgo.core.model.token.BaseAccessToken
import com.sina.weibo.sdk.auth.Oauth2AccessToken

/**
 * 新浪的token
 */
class SinaAccessToken(token: Oauth2AccessToken) : BaseAccessToken() {

    private val refresh_token: String

    init {
        this.openid = token.uid
        this.access_token = token.accessToken
        this.expires_in = token.expiresTime
        this.refresh_token = token.refreshToken
    }
}
