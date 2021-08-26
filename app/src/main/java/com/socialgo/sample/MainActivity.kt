package com.socialgo.sample

import android.app.ProgressDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.socialgo.core.SocialGo
import com.socialgo.core.model.ShareEntity
import com.socialgo.core.platform.Target

class MainActivity : AppCompatActivity(){

    private val mStringBuilder = StringBuilder()
    private val imageUrl = "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=4206269753,120347069&fm=26&gp=0.jpg"
    private val webUrl = "https://www.baidu.com/"
    private var shareTarget : Int = 0
    private lateinit var shareEntity :ShareEntity
    private lateinit var mProgressDialog :ProgressDialog
    private lateinit var containerType : RadioGroup
    private lateinit var containerPlatform : RadioGroup
    private lateinit var tvConsole : TextView
    private lateinit var rbTypeText : RadioButton
    private lateinit var rbPlatformQQ : RadioButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mProgressDialog = ProgressDialog(this)
        containerType = findViewById(R.id.containerType)
        containerPlatform = findViewById(R.id.containerPlatform)
        tvConsole = findViewById(R.id.tvConsole)
        rbTypeText = findViewById(R.id.rbTypeText)
        rbPlatformQQ = findViewById(R.id.rbPlatformQQ)
        initEvent()
        initSet()
    }

    private fun initEvent() {
        containerType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTypeText -> {
                    shareEntity = ShareEntity.buildText("上单无敌！")
                }
                R.id.rbTypeImage -> {
                    shareEntity = ShareEntity.buildImage(imageUrl)
                }
                R.id.rbTypeLink -> {
                    shareEntity = ShareEntity.buildWeb(getString(R.string.share_title), getString(R.string.share_text), imageUrl, webUrl)
                }
            }
        }
        containerPlatform.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPlatformQQ -> {
                    shareTarget = Target.SHARE_QQ_FRIENDS
                }
                R.id.rbPlatformQzon -> {
                    shareTarget = Target.SHARE_QQ_ZONE
                }
                R.id.rbPlatformWx -> {
                    shareTarget = Target.SHARE_WX_FRIENDS
                }
                R.id.rbPlatformWxFriend -> {
                    shareTarget = Target.SHARE_WX_ZONE
                }
                R.id.rbPlatformSina -> {
                    shareTarget = Target.SHARE_WB
                }
            }
        }
    }

    private fun initSet() {
        tvConsole.movementMethod = ScrollingMovementMethod.getInstance()
        rbTypeText.isChecked = true
        rbPlatformQQ.isChecked = true
    }

    fun onQQLogin(view: View) {
        doLogin(Target.LOGIN_QQ)
    }

    fun onWxLogin(view: View) {
        doLogin(Target.LOGIN_WX)
    }

    fun onSinaLogin(view: View) {
        doLogin(Target.LOGIN_WB)
    }

    private fun doLogin(@Target.LoginTarget loginTarget: Int) {
        SocialGo.doLogin(this, loginTarget) {
            onStart {
                mProgressDialog.show()
                printMsg("登录开始")
            }
            onSuccess {
                mProgressDialog.dismiss()
                printMsg("${it.socialUser?.toString()}")
            }
            onCancel {
                mProgressDialog.dismiss()
                printMsg("登录取消")
            }
            onFailure {
                mProgressDialog.dismiss()
                printMsg("登录失败 -> $it")
            }
        }
    }

    fun onShare(view: View) {
        SocialGo.doShare(this, shareTarget, shareEntity) {
            onStart { _, obj ->
                mProgressDialog.show()
                printMsg("分享开始 -> $obj")
            }
            onSuccess {
                mProgressDialog.dismiss()
                printMsg("分享成功")
            }
            onCancel {
                mProgressDialog.dismiss()
                printMsg("分享取消")
            }
            onFailure {
                mProgressDialog.dismiss()
                printMsg("分享失败 -> $it")
            }
        }
    }

    fun onPayWx(view: View) {
        doPay(Target.PAY_WX)
    }

    fun onPayAli(view: View) {
        doPay(Target.PAY_ALI)
    }

    private fun doPay(@Target.PayTarget payTarget: Int) {
        SocialGo.doPay(this, "xxxxx", payTarget) {
            onStart {
                printMsg("支付开始")
            }
            onSuccess {
                printMsg("支付成功")
            }
            onDealing {
                printMsg("onDealing")
            }
            onCancel {
                printMsg("支付取消")
            }
            onFailure {
                printMsg("支付失败 -> $it")
            }

        }
    }

    private fun printMsg(content: String) {
        mStringBuilder.insert(0, "${content}\n\n")
        tvConsole.text = mStringBuilder.toString()
    }
}
