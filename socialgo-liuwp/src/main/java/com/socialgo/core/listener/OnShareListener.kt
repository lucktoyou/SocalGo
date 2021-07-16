package com.socialgo.core.listener

import com.socialgo.core.common.SocialError
import com.socialgo.core.model.ShareEntity

/**
 * 分享的回调
 */
interface OnShareListener {

    fun getFunction(): FunctionListener

    fun onStart(start: (shareTarget: Int, obj: ShareEntity) -> Unit) {
        getFunction().onShareStart = start
    }

    fun onSuccess(success: () -> Unit) {
        getFunction().onSuccess = success
    }

    fun onCancel(cancel: () -> Unit) {
        getFunction().onCancel = cancel
    }

    fun onFailure(failure: (error: SocialError) -> Unit) {
        getFunction().onFailure = failure
    }
}
