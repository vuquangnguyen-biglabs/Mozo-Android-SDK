package com.biglabs.mozo.sdk.ui.views

import android.app.Activity
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.biglabs.mozo.sdk.R
import kotlinx.android.synthetic.main.view_toolbar.view.*

internal class MozoToolbar : ConstraintLayout {

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

        screen_title.text = mTitle
        button_back.visibility = if (mShowBack) View.VISIBLE else View.GONE
        button_close.visibility = if (mShowClose) View.VISIBLE else View.GONE

        button_back.setOnClickListener {
            if (onBackPress != null) onBackPress
            else (context as? Activity)?.onBackPressed()
        }
        button_close.setOnClickListener {
            if (onClosePress != null) onClosePress
            else (context as? Activity)?.finish()
        }
    }
}