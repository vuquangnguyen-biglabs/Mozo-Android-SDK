package com.biglabs.mozo.sdk.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.Button
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.services.AuthService

final class LoginButton : Button {

    private val newWidth: Int
    private val newHeight: Int

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, R.attr.buttonStyle)
    constructor(context: Context, attributes: AttributeSet?, defStyle: Int) : super(context, attributes, defStyle) {
        super.setText(R.string.mozo_button_login)
        super.setTextColor(Color.WHITE)
        super.setBackgroundResource(R.drawable.mozo_dr_btn)
        super.setOnClickListener {
            AuthService.getInstance().signIn()
        }

        newWidth = resources.getDimensionPixelSize(R.dimen.mozo_btn_width)
        newHeight = resources.getDimensionPixelSize(R.dimen.mozo_btn_height)
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

//    override fun setOnClickListener(l: OnClickListener?) {
//        // ignore
//    }
//
//    override fun setBackground(background: Drawable?) {
//        // ignore
//    }
//
//    override fun setBackgroundColor(color: Int) {
//        // ignore
//    }
//
//    override fun setBackgroundResource(resid: Int) {
//        // ignore
//    }
//
//    override fun setBackgroundTintList(tint: ColorStateList?) {
//        // ignore
//    }
//
//    override fun setBackgroundTintMode(tintMode: PorterDuff.Mode?) {
//        // ignore
//    }
//
//    override fun setTextColor(color: Int) {
//        // ignore
//    }
//
//    override fun setTextColor(colors: ColorStateList?) {
//        // ignore
//    }
}