@file:Suppress("UNUSED_PARAMETER")

package com.biglabs.mozo.sdk.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.Button
import com.biglabs.mozo.sdk.R

final class TransferButton : Button {

    private val newWidth: Int
    private val newHeight: Int

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, R.attr.buttonStyle)
    constructor(context: Context, attributes: AttributeSet?, defStyle: Int) : super(context, attributes, defStyle) {

        setText(R.string.mozo_button_transfer)
        setTextColor(Color.WHITE)
        setBackgroundResource(R.drawable.mozo_dr_btn)

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

    // TODO override onCLick event
}