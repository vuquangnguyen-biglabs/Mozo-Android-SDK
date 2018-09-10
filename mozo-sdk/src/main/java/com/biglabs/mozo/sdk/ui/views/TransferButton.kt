package com.biglabs.mozo.sdk.ui.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.Button
import com.biglabs.mozo.sdk.R

class TransferButton : Button {

    private val newWidth: Int
    private val newHeight: Int

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)
    constructor(context: Context, attributes: AttributeSet?, def: Int) : super(context, null, R.attr.buttonStyle)

    init {
        setText(R.string.mozo_button_transfer)
        setTextColor(Color.WHITE)
        setBackgroundResource(R.drawable.mozo_dr_btn_primary)

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