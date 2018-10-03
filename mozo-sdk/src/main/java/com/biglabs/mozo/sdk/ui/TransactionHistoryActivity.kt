package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.MenuCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.DefaultItemAnimator
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.core.MozoApiService
import com.biglabs.mozo.sdk.services.WalletService
import com.biglabs.mozo.sdk.trans.TransactionHistoryRecyclerAdapter
import com.biglabs.mozo.sdk.utils.click
import kotlinx.android.synthetic.main.view_transaction_history.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

internal class TransactionHistoryActivity : AppCompatActivity() {

    private val walletService: WalletService by lazy { WalletService.getInstance() }

    private val histories = arrayListOf<Models.TransactionHistory>()

    private val onItemClick = { position: Int ->

    }
    private var historyAdapter = TransactionHistoryRecyclerAdapter(histories, onItemClick)
    private var currentAddress: String? = null

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

        list_history.setHasFixedSize(true)
        list_history.itemAnimator = DefaultItemAnimator()
        list_history.adapter = historyAdapter

//        menuInflater.inflate(R.menu.menu_transaction_type)
//        button_filter.createContextMenu()

        registerForContextMenu(button_filter)

        button_filter.click { openContextMenu(it) }
    }

    override fun onStart() {
        super.onStart()
        fetchData()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        menuInflater.inflate(R.menu.menu_transaction_type, menu)
        if (button_filter_text.tag != null) {
            button_filter_text.tag.toString().toIntOrNull()?.let {
                menu?.findItem(it)?.isChecked = true
            }
        } else {
            menu?.getItem(0)?.isChecked = true
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        button_filter_text.text = item?.title
        button_filter_text.tag = item?.itemId
        return super.onContextItemSelected(item)
    }

    private fun fetchData() = async {
        if (currentAddress == null) {
            currentAddress = walletService.getAddress().await() ?: return@async
            historyAdapter.address = currentAddress
        }
        val response = MozoApiService.getInstance(this@TransactionHistoryActivity)
                .getTransactionHistory(currentAddress ?: "", 0, 0)
                .await()

        if (response.isSuccessful && response.body() != null) {
            histories.clear()
            histories.addAll(response.body()!!)
        }

        launch(UI) {
            list_history_refresh.isRefreshing = false
            historyAdapter.notifyDataSetChanged()
        }
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