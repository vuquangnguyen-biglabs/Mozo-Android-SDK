@file:Suppress("UNUSED_PARAMETER")

package com.biglabs.mozo.sdk.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.Button
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.services.AuthService
import com.biglabs.mozo.sdk.trans.MozoTrans
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class TransferButton : Button {

    private val newWidth: Int
    private val newHeight: Int

    private var needToContinue = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, R.attr.buttonStyle)
    constructor(context: Context, attributes: AttributeSet?, defStyle: Int) : super(context, attributes, defStyle) {
        super.setText(R.string.mozo_button_transfer)
        super.setTextColor(Color.WHITE)
        super.setBackgroundResource(R.drawable.mozo_dr_btn)
        super.setOnClickListener {
            AuthService.getInstance().run {
                if (isSignedIn())
                    doTransfer()
                else {
                    needToContinue = true
                    signIn()
                }
            }
        }

        newWidth = resources.getDimensionPixelSize(R.dimen.mozo_btn_width)
        newHeight = resources.getDimensionPixelSize(R.dimen.mozo_btn_height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe
    internal fun onAuthorizeChanged(auth: MessageEvent.Auth) {
        if (needToContinue) {
            needToContinue = false
            doTransfer()
        }
    }

    private fun doTransfer() {
        MozoTrans.getInstance().transfer()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = newWidth
        height = newHeight
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        width = newWidth
        height = newHeight
    }

    override fun setOnClickListener(l: OnClickListener?) {
        // ignore
    }
}