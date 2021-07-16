package com.socialgo.qq

import android.app.Activity
import android.content.Intent
import com.socialgo.core.common.SocialError
import com.socialgo.core.listener.OnLoginListener
import com.socialgo.core.model.LoginResult
import com.socialgo.core.model.token.BaseAccessToken
import com.socialgo.core.platform.Target
import com.socialgo.core.utils.SocialGoUtils
import com.socialgo.core.utils.SocialLogUtils
import com.socialgo.qq.model.QQAccessToken
import com.socialgo.qq.model.QQUser
import com.tencent.connect.UserInfo
import com.tencent.tauth.DefaultUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * QQ登录助手
 */
class QQLoginHelper(activity: Activity, private val tencentApi: Tencent) {

    private val mLoginType: Int = Target.LOGIN_QQ
    private val mActivityReference: WeakReference<Activity> = WeakReference(activity)
    private var mLoginListener: OnLoginListener? = null
    private var mUiListenerWrap: UiListenerWrap? = null

    fun login(listener: OnLoginListener) {
        mLoginListener = listener
        val qqToken = getToken()
        if (qqToken != null) {
            tencentApi.setAccessToken(qqToken.access_token, qqToken.expires_in.toString() + "")
            tencentApi.openId = qqToken.openid
            if (tencentApi.isSessionValid) {
                getUserInfo(qqToken)
            } else {
                mUiListenerWrap = UiListenerWrap()
                tencentApi.login(mActivityReference.get(), "all", mUiListenerWrap)
            }
        } else {
            mUiListenerWrap = UiListenerWrap()
            tencentApi.login(mActivityReference.get(), "all", mUiListenerWrap)
        }
    }

    private inner class UiListenerWrap : DefaultUiListener() {
        override fun onComplete(o: Any?) {
            SocialLogUtils.e("获取到qq token:${o.toString()}")
            val jsonResponse = o as JSONObject
            val qqToken = SocialGoUtils.getObject(jsonResponse.toString(), QQAccessToken::class.java)
            if (qqToken == null) {
                setFailure(SocialError(SocialError.CODE_DATA_PARSE_FAIL))
            } else {
                saveToken(qqToken)
                tencentApi.setAccessToken(qqToken.access_token, qqToken.expires_in.toString() + "")
                tencentApi.openId = qqToken.openid
                getUserInfo(qqToken)
            }
        }

        override fun onCancel() {
            setCancel()
        }

        override fun onError(uiError: UiError?) {
            SocialLogUtils.e("获取qq tokens失败:${uiError.toString()}")
            setFailure(SocialError(SocialError.CODE_SDK_ERROR).append(uiError.toString()))
        }
    }

    private fun getUserInfo(qqToken: QQAccessToken?) {
        val info = UserInfo(mActivityReference.get(), tencentApi.qqToken)
        info.getUserInfo(object : DefaultUiListener() {
            override fun onComplete(any: Any?) {
                SocialLogUtils.e("获取到qq user info:${any.toString()}")
                val qqUserInfo = SocialGoUtils.getObject(any.toString(), QQUser::class.java)
                if (qqUserInfo == null) {
                    setFailure(SocialError(SocialError.CODE_DATA_PARSE_FAIL))
                } else {
                    qqUserInfo.setOpenId(tencentApi.openId)
                    setSuccess(LoginResult(mLoginType, qqUserInfo, qqToken))
                }
            }

            override fun onCancel() {
                setCancel()
            }

            override fun onError(uiError: UiError?) {
                SocialLogUtils.e("获取qq user info失败:${uiError.toString()}")
                setFailure(SocialError(SocialError.CODE_SDK_ERROR).append(uiError.toString()))
            }
        })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Tencent.handleResultData(data, mUiListenerWrap)
    }

    private fun setSuccess(loginResult: LoginResult) {
        mLoginListener?.getFunction()?.onLoginSuccess?.invoke(loginResult)
    }

    private fun setCancel() {
        mLoginListener?.getFunction()?.onCancel?.invoke()
    }

    private fun setFailure(error: SocialError) {
        mLoginListener?.getFunction()?.onFailure?.invoke(error)
    }

    private fun getToken(): QQAccessToken? {
        return BaseAccessToken.getToken(mActivityReference.get(), BaseAccessToken.QQ_TOKEN, QQAccessToken::class.java)
    }

    private fun saveToken(qqToken: QQAccessToken?) {
        BaseAccessToken.saveToken(mActivityReference.get(), BaseAccessToken.QQ_TOKEN, qqToken)
    }
}
