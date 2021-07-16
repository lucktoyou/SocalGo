package com.socialgo.core.platform

import android.app.Activity
import com.socialgo.core.listener.OnShareListener
import com.socialgo.core.model.ShareEntity

/**
 * 分享类型
 */
interface IShareAction {

    fun shareText(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun shareImage(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun shareWeb(shareTarget: Int, activity: Activity, entity: ShareEntity)

    fun share(activity: Activity, target: Int, entity: ShareEntity, listener: OnShareListener) {
        when (entity.getEntityType()) {
            ShareEntity.ENTITY_IS_TEXT -> shareText(target, activity, entity)
            ShareEntity.ENTITY_IS_IMAGE -> shareImage(target, activity, entity)
            ShareEntity.ENTITY_IS_WEB -> shareWeb(target, activity, entity)
        }
    }
}