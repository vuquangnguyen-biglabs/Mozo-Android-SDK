package com.biglabs.mozo.sdk.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.common.MessageEvent
import com.biglabs.mozo.sdk.services.AuthService
import com.biglabs.mozo.sdk.trans.MozoTrans
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SendButton : Button {

    private val icSignIn: Drawable?
    private val icSignOut: Drawable?

    private var needToContinue = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, R.attr.buttonStyle)
    constructor(context: Context, attributes: AttributeSet?, defStyle: Int) : super(context, attributes, defStyle) {
        super.setAllCaps(false)
        super.setTextColor(Color.WHITE)
        super.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        super.setTypeface(Typeface.DEFAULT_BOLD)
        super.setText(R.string.mozo_button_transfer)
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
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, resources.displayMetrics).toInt()
        super.setPaddingRelative(padding, padding, padding, padding)
        val drawablePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        super.setCompoundDrawablePadding(drawablePadding)

        icSignIn = ContextCompat.getDrawable(context, R.drawable.ic_btn_sign_in)
        icSignOut = ContextCompat.getDrawable(context, R.drawable.ic_btn_sign_out)

        if (isInEditMode) {
            super.setCompoundDrawablesWithIntrinsicBounds(icSignIn, null, null, null)
        } //else updateUI()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(measuredWidth, measuredHeight)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!isInEditMode) {
            EventBus.getDefault().unregister(this)
        }
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

    override fun setOnClickListener(l: OnClickListener?) {
        // ignore
    }
}