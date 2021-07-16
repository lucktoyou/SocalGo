package com.socialgo.wechat.model

import com.socialgo.core.model.user.BaseSocialUser

/**
 * 微信用户数据
 */
class WxUser : BaseSocialUser() {

    private val unionid: String? = null
    private val openid: String? = null
    private val nickname: String? = null
    private val sex: Int = 0
    private val province: String? = null
    private val city: String? = null
    private val country: String? = null
    private val headimgurl: String? = null
    private val privilege: List<String>? = null
    val errcode: Int = 0
    val errmsg: String? = null

    val isNoError: Boolean
        get() = errcode == 0

    override fun toString(): String {
        return "WxUserInfo{" +
                "openid='" + openid + '\''.toString() +
                ", nickname='" + nickname + '\''.toString() +
                ", sex=" + sex +
                ", province='" + province + '\''.toString() +
                ", city='" + city + '\''.toString() +
                ", country='" + country + '\''.toString() +
                ", headimgurl='" + headimgurl + '\''.toString() +
                ", privilege=" + privilege +
                ", unionid='" + unionid + '\''.toString() +
                '}'.toString()
    }

    override fun getUserId(): String {
        return unionid ?: ""
    }

    override fun getUserNickName(): String {
        return nickname ?: ""
    }

    override fun getUserGender(): Int {
        return when (sex) {
            1 -> BaseSocialUser.GENDER_BOY
            2 -> BaseSocialUser.GENDER_GIRL
            else -> BaseSocialUser.GENDER_UNKONW
        }
    }

    override fun getUserProvince(): String {
        return province ?: ""
    }

    override fun getUserCity(): String {
        return city ?: ""
    }

    override fun getUserHeadUrl(): String {
        return headimgurl ?: ""
    }

    override fun getUserHeadUrlLarge(): String {
        return headimgurl ?: ""
    }
}