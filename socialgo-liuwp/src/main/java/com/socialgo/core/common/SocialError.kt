package com.socialgo.core.common

import android.text.TextUtils

/**
 * 错误信息描述
 */
class SocialError(errorCode: Int) {

    var errorMsg: String? = null

    init {
        when (errorCode) {
            CODE_EXCEPTION -> append("异常")
            CODE_AUTH_REJECTED -> append("用户拒绝授权")
            CODE_DATA_PARSE_FAIL -> append("数据解析失败")
            CODE_IMAGE_COMPRESS_FAIL -> append("图片压缩失败")
            CODE_NETWORK_REQUEST_FAIL -> append("网络请求失败")
            CODE_NETWORK_CONNECT_FAIL -> append("网络连接失败")

            CODE_NOT_INSTALL -> append("平台应用未安装")
            CODE_VERSION_LOW -> append("平台应用版本过低，需要更高版本")
            CODE_SDK_ERROR -> append("平台sdk返回错误")

            CODE_SHARE_ENTITY_INVALID -> append("分享实体无效")
            CODE_SHARE_BY_INTENT_FAIL -> append("使用intent分享失败")
            CODE_SHARE_REJECTED -> append("分享被拒绝")
            CODE_SHARE_FAIL -> append("分享失败")

            CODE_PAY_PARAM_INCOMPLETE -> append("支付参数不全")
            CODE_PAY_ERROR -> append("支付错误")
            CODE_PAY_RESULT_NULL -> append("支付结果返回为空")
        }
    }

    fun append(msg: String?): SocialError {
        if (!TextUtils.isEmpty(errorMsg)) {
            this.errorMsg = errorMsg.toString() + "." + msg
        } else {
            this.errorMsg = msg
        }
        return this
    }

    override fun toString(): String {
        val sb = StringBuilder()
                .append("SocialGoErrorMessage = ").append(errorMsg)
        return sb.toString()
    }

    companion object {
        const val CODE_EXCEPTION = 101 //异常
        const val CODE_AUTH_REJECTED = 102 //用户拒绝授权
        const val CODE_DATA_PARSE_FAIL = 103 //数据解析失败
        const val CODE_IMAGE_COMPRESS_FAIL = 104 //图片压缩失败
        const val CODE_NETWORK_REQUEST_FAIL = 105 //网络请求失败
        const val CODE_NETWORK_CONNECT_FAIL = 106 //网络连接失败

        const val CODE_NOT_INSTALL = 201 //平台应用未安装
        const val CODE_VERSION_LOW = 202 //平台应用版本过低，需要更高版本
        const val CODE_SDK_ERROR = 203 //平台sdk返回错误

        const val CODE_SHARE_ENTITY_INVALID = 301 //分享实体无效
        const val CODE_SHARE_BY_INTENT_FAIL = 302 //使用intent分享失败
        const val CODE_SHARE_REJECTED = 303 //分享被拒绝
        const val CODE_SHARE_FAIL = 304 //分享失败

        const val CODE_PAY_PARAM_INCOMPLETE = 401 //支付参数不全
        const val CODE_PAY_RESULT_NULL = 402 //支付结果返回为空
        const val CODE_PAY_ERROR = 403 //支付错误


        const val MSG_QQ_APPID_NULL = "请先配置好QQ的AppID"
        const val MSG_WX_APPID_OR_APPSECRET_NULL = "请先配置好微信的AppID和AppSecret"
        const val MSG_WB_APPKEY_OR_REDIRECTURL_OR_SCOPE_NULL = "请先配置好微博的AppKey、RedirectUrl和Scope"
    }
}
