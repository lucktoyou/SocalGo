### 功能预览
<img src="/preview/main.jpg" width="380px"/>

# 项目说明
Android第三方登录，分享，支付组件。包括QQ，微信，微博和支付宝。

### 优点
* 支持分享登录支付功能。
* 不需要手动添加activity和更改清单文件。
* 本地保存登录Token，可随时清除。
* 请求用户信息接口可随时迁移到服务器。
* 使用Androidx和Kotlin开发。
* 一行代码实现分享登录支付的调用。

### 使用流程
* 在Application中初始化第三方平台和配置各自的appkey等.
                 
        SocialGo.init(SocialGoConfig.create(this)
                .debug(true)
                .qq(AppConstant.QQ_APP_ID)
                .wechat(AppConstant.WX_APP_ID, AppConstant.WX_APP_SECRET)
                .weibo(AppConstant.WEIBO_APP_KEY))
                .registerPlatform(WxPlatform.Creator(), WbPlatform.Creator(), QQPlatform.Creator(), AliPlatform.Creator())
                .setJsonAdapter(GsonJsonAdapter())
                .setRequestAdapter(OkHttpRequestAdapter())            

* 登录

        SocialGo.doLogin(this, Target.LOGIN_QQ) {
            onStart {
                mProgressDialog.show()
                tvConsole?.text = "登录开始"
            }

            onSuccess {
                mProgressDialog.dismiss()
                tvConsole?.text = it.socialUser?.toString()
            }

            onCancel {
                mProgressDialog.dismiss()
                tvConsole?.text = "登录取消"
            }

            onFailure {
                mProgressDialog.dismiss()
                tvConsole?.text = "登录异常 + ${it?.errorMsg}"
            }
        }

* 分享

         SocialGo.doShare(this, platformType, shareMedia) {
            onStart { _, _ ->
                mProgressDialog.show()
                tvConsole?.text = "分享开始"
            }
            onSuccess {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享成功"
            }
            onFailure {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享失败"
            }
            onCancel {
                mProgressDialog.dismiss()
                tvConsole?.text = "分享取消"
            }
        }


* 支付

         SocialGo.doPay(this, params, Target.PAY_WX) {
            onStart {
                tvConsole?.text = "支付开始"
            }
            onSuccess {
                tvConsole?.text = "支付成功"
            }
            onDealing {
                tvConsole?.text = "支付Dealing"
            }
            onFailure {
                tvConsole?.text = "支付异常：${it?.errorMsg}"
            }
            onCancel {
                tvConsole?.text = "支付取消"
            }
        }

### 第三方底层SDK版本
* QQ：`open_sdk_3.5.4.11_r3f56456_lite.jar`
* 微信：`com.tencent.mm.opensdk:wechat-sdk-android-without-mta:6.7.0`
* 微博：`io.github.sinaweibosdk:core:11.6.0@aar`
* 支付宝：`alipaySdk-15.5.9-20181123210601.jar`
