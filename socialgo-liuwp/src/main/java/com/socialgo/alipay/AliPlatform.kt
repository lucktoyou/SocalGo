package com.socialgo.alipay

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.alipay.sdk.app.PayTask
import com.socialgo.alipay.uikit.AliActionActivity
import com.socialgo.core.SocialGo
import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnPayListener
import com.socialgo.core.platform.AbsPlatform
import com.socialgo.core.platform.IPlatform
import com.socialgo.core.platform.PlatformCreator

/**
 * 支付宝支付平台
 *
 * App支付文档 https://opendocs.alipay.com/open/204/105051
 */
class AliPlatform private constructor(appName: String?) : AbsPlatform(appName) {

    class Creator : PlatformCreator {
        override fun create(context: Context, target: Int): IPlatform {
            return AliPlatform(SocialGo.getConfig().getAppName())
        }
    }

    override fun getActionClazz(): Class<*> {
        return AliActionActivity::class.java
    }

    override fun isInstall(context: Context): Boolean {
        // 商家App调用支付宝提供的SDK，SDK再调用支付宝App内的支付模块。
        // 如果用户已安装支付宝App，商家App会跳转到支付宝中完成支付，支付完后跳回到商家App内，最后展示支付结果。
        // 如果用户没有安装支付宝App，商家App内会调起支付宝网页支付收银台，用户登录支付宝账户，支付完后展示支付结果。
        return true
    }

    override fun doPay(context: Context, params: String, listener: OnPayListener) {
        val payTask = PayTask(context as Activity)
        SocialGo.getExecutor().execute {
            val payResult = payTask.payV2(params, true)
            SocialGo.getHandler().post {
                if (payResult != null) {
                    val resultStatus = payResult["resultStatus"]
                    when {
                        TextUtils.equals(resultStatus, "9000") -> //支付成功
                            listener.getFunction().onSuccess?.invoke()
                        TextUtils.equals(resultStatus, "8000") -> //支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                            listener.getFunction().onDealing?.invoke()
                        TextUtils.equals(resultStatus, "6001") -> //支付取消
                            listener.getFunction().onCancel?.invoke()
                        TextUtils.equals(resultStatus, "6002") -> //网络连接出错
                            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_NETWORK_CONNECT_FAIL))
                        TextUtils.equals(resultStatus, "4000") -> //支付错误
                            listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_PAY_ERROR))
                    }
                } else {
                    listener.getFunction().onFailure?.invoke(SocialError(SocialError.CODE_PAY_RESULT_NULL))
                }
            }
        }
    }
}
