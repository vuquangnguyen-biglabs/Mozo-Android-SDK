package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.biglabs.mozo.sdk.R
import kotlinx.android.synthetic.main.view_transaction_history.*

class TransactionHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_transaction_history)

        list_history_refresh?.apply {
            val offset = resources.getDimensionPixelSize(R.dimen.mozo_refresh_progress_offset)
            setProgressViewOffset(true, progressViewStartOffset + offset, progressViewEndOffset + offset)
            setColorSchemeResources(R.color.mozo_color_primary)
            isRefreshing = true
            setOnRefreshListener { fetchData() }
        }
    }

    private fun fetchData() {

    }

    companion object {
        fun start(context: Context) {
            Intent(context, TransactionHistoryActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                context.startActivity(this)
            }
        }
    }
}