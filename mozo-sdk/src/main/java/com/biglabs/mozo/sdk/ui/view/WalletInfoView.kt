package com.biglabs.mozo.sdk.ui.view

import android.content.Context
import android.support.annotation.IntDef
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.services.WalletService
import com.biglabs.mozo.sdk.ui.dialog.QRCodeDialog
import com.biglabs.mozo.sdk.utils.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class WalletInfoView : ConstraintLayout {

    private var mViewMode: Int = 0
    private var mShowQRCode = true
    private var mShowCopy = true

    private var mAddress: String? = null

    private var mWalletAddressView: TextView? = null
    private var fragmentManager: FragmentManager? = null

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

        setBackgroundResource(R.drawable.mozo_bg_component)
        minWidth = resources.getDimensionPixelSize(R.dimen.mozo_view_min_width)
        minHeight = resources.getDimensionPixelSize(R.dimen.mozo_view_min_height)

        inflateLayout()

        launch(UI) {
            mAddress = WalletService.getInstance().getAddress().await()
            mWalletAddressView?.text = mAddress
        }

        if (context is FragmentActivity) {
            fragmentManager = context.supportFragmentManager
        } else if (context is Fragment) {
            fragmentManager = context.fragmentManager
        }
    }

    private fun inflateLayout() {
        removeAllViews()
        when (mViewMode) {
            MODE_ONLY_ADDRESS -> inflate(context, R.layout.view_wallet_info_address, this)
            MODE_ONLY_BALANCE -> inflate(context, R.layout.view_wallet_info_balance, this)
            else -> inflate(context, R.layout.view_wallet_info, this)
        }

        updateUI()
    }

    private fun updateUI() {
        val balanceRate: TextView?

        if (mViewMode == MODE_ONLY_BALANCE) {
            balanceRate = find(R.id.mozo_wallet_balance_rate_side)

        } else {
            mWalletAddressView = find(R.id.mozo_wallet_address)
            mAddress?.let { mWalletAddressView?.text = it }

            balanceRate = find(R.id.mozo_wallet_balance_rate_bottom)
            find<View>(R.id.mozo_wallet_btn_show_qr)?.apply {
                if (mShowQRCode) {
                    visible()
                    click { showQRCodeDialog() }
                } else gone()
            }

            find<View>(R.id.mozo_wallet_btn_copy)?.apply {
                if (mShowCopy) {
                    visible()
                    click { context.copyWithToast(mAddress) }
                } else gone()
            }
        }

        balanceRate?.apply {
            visible()
            text = "₩000"
        }
    }

    private fun showQRCodeDialog() {
        if (fragmentManager != null) {
            QRCodeDialog.show(mAddress!!, fragmentManager!!)
        } else {
            "Cannot show QR Code dialog on this context".logAsError("WalletInfoView")
        }
    }


    fun setViewMode(@ViewMode mode: Int) {
        if (mViewMode != mode) {
            mViewMode = mode
            inflateLayout()
        }
    }

    fun setShowQRCode(isShow: Boolean) {
        mShowQRCode = isShow
        updateUI()
    }

    fun setShowCopy(isShow: Boolean) {
        mShowCopy = isShow
        updateUI()
    }

    companion object {
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(MODE_ADDRESS_BALANCE, MODE_ONLY_ADDRESS, MODE_ONLY_BALANCE)
        annotation class ViewMode

        const val MODE_ADDRESS_BALANCE = 0
        const val MODE_ONLY_ADDRESS = 1
        const val MODE_ONLY_BALANCE = 2
    }
}