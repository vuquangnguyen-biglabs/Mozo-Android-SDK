package com.biglabs.mozo.sdk.ui.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.services.WalletService
import kotlinx.android.synthetic.main.view_wallet_info_address.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class WalletInfoView : ConstraintLayout {

    private var mViewMode: Int = 0
    private var mShowQRCode = true
    private var mShowCopy = true

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.WalletInfoView, defStyleAttr, 0).apply {
            try {
                mViewMode = getInt(R.styleable.WalletInfoView_viewMode, mViewMode)
                mShowQRCode = getBoolean(R.styleable.WalletInfoView_showQRCode, mShowQRCode)
                mShowCopy = getBoolean(R.styleable.WalletInfoView_showCopy, mShowCopy)
            } finally {
                recycle()
            }
        }

        when (mViewMode) {
            MODE_ONLY_ADDRESS -> inflate(context, R.layout.view_wallet_info_address, this)
            MODE_ONLY_BALANCE -> inflate(context, R.layout.view_wallet_info_balance, this)
            else -> inflate(context, R.layout.view_wallet_info, this)
        }

        when {
            mViewMode != MODE_ONLY_BALANCE -> {
                launch(UI) {
                    mozo_wallet_address.text = WalletService.getInstance().getAddress().await()
                }
            }
        }

        setBackgroundResource(R.drawable.mozo_bg_component)
        minWidth = resources.getDimensionPixelSize(R.dimen.mozo_view_min_width)
        minHeight = resources.getDimensionPixelSize(R.dimen.mozo_view_min_height)

    }

    companion object {
        const val MODE_ADDRESS_BALANCE = 0
        const val MODE_ONLY_ADDRESS = 1
        const val MODE_ONLY_BALANCE = 2
    }
}