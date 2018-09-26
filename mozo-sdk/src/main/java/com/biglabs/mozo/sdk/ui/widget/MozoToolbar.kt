package com.biglabs.mozo.sdk.ui.widget

import android.app.Activity
import android.content.Context
import android.support.annotation.StringRes
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.biglabs.mozo.sdk.R

internal class MozoToolbar : ConstraintLayout {

    private var viewScreenTitle: TextView? = null
    private var viewButtonBack: View? = null
    private var viewButtonClose: View? = null

    private var mTitle: String? = null
    private var mShowBack = false
    private var mShowClose = false

    var onBackPress: (() -> Unit)? = null
    var onClosePress: (() -> Unit)? = null

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.MozoToolbar, defStyleAttr, 0).apply {
            try {
                mTitle = getString(R.styleable.MozoToolbar_title)
                mShowBack = getBoolean(R.styleable.MozoToolbar_buttonBack, mShowBack)
                mShowClose = getBoolean(R.styleable.MozoToolbar_buttonClose, mShowClose)
            } finally {
                recycle()
            }
        }

        inflate(context, R.layout.view_toolbar, this)
        viewScreenTitle = this.findViewById(R.id.screen_title)
        viewButtonBack = this.findViewById(R.id.button_back)
        viewButtonClose = this.findViewById(R.id.button_close)

        updateUI()

        viewButtonBack?.setOnClickListener {
            if (onBackPress != null) onBackPress?.invoke()
            else (context as? Activity)?.onBackPressed()
        }
        viewButtonClose?.setOnClickListener {
            if (onClosePress != null) onClosePress?.invoke()
            else (context as? Activity)?.finish()
        }

        if (isInEditMode) {
            maxHeight = 100
        }
    }

    private fun updateUI() {
        viewScreenTitle?.text = mTitle
        viewButtonBack?.visibility = if (mShowBack) View.VISIBLE else View.GONE
        viewButtonClose?.visibility = if (mShowClose) View.VISIBLE else View.GONE
    }

    fun showBackButton(isShow: Boolean) {
        mShowBack = isShow
        updateUI()
    }

    fun showCloseButton(isShow: Boolean) {
        mShowClose = isShow
        updateUI()
    }

    fun setTitle(title: String?) {
        mTitle = title
        updateUI()
    }

    fun setTitle(@StringRes id: Int) {
        mTitle = context.getString(id)
        updateUI()
    }
}