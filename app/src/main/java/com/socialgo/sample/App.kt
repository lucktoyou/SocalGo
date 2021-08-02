package com.socialgo.sample

import android.app.Application
import com.socialgo.alipay.AliPlatform
import com.socialgo.core.SocialGo
import com.socialgo.core.SocialGoConfig
import com.socialgo.core.adapter.impl.GsonJsonAdapter
import com.socialgo.core.adapter.impl.OkHttpRequestAdapter
import com.socialgo.qq.QQPlatform
import com.socialgo.wechat.WxPlatform
import com.socialgo.weibo.WbPlatform


/**
 * @author Pinger
 * @since 18-7-20 下午4:30
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initSocialGo()
    }

    private fun initSocialGo() {
        SocialGo.init(SocialGoConfig.create(this)
                .debug(true)
                .qq(AppConstant.QQ_APP_ID)
                .wechat(AppConstant.WX_APP_ID, AppConstant.WX_APP_SECRET)
                .weibo(AppConstant.WEIBO_APP_KEY))
                .registerPlatform(WxPlatform.Creator(), WbPlatform.Creator(), QQPlatform.Creator(), AliPlatform.Creator())
                .setJsonAdapter(GsonJsonAdapter())
                .setRequestAdapter(OkHttpRequestAdapter())
    }
}