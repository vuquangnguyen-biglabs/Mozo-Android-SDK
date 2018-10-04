package com.biglabs.mozo.sdk.trans

import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.core.Models
import com.biglabs.mozo.sdk.utils.click
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_history.*
import java.text.SimpleDateFormat
import java.util.*

internal class TransactionHistoryRecyclerAdapter(private val histories: List<Models.TransactionHistory>, private val itemClick: ((position: Int) -> Unit)? = null) : RecyclerView.Adapter<TransactionHistoryRecyclerAdapter.ItemViewHolder>() {

    var address: String? = null
    private var dataFilter: List<Models.TransactionHistory>? = null
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm aa", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false))

    override fun getItemCount(): Int = getData().size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val history = getData()[position]
        holder.bind(history, history.type(address), dateFormat.format(Date(history.time * 1000L)))
        holder.itemView.click { itemClick?.invoke(position) }
    }

    private fun getData() = dataFilter ?: histories

    fun filter(@FilterMode mode: Int) {
        dataFilter = when (mode) {
            FILTER_RECEIVED -> histories.filter { it.type(address) == false }
            FILTER_SENT -> histories.filter { it.type(address) == true }
            else -> null
        }

        notifyDataSetChanged()
    }

    class ItemViewHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(history: Models.TransactionHistory, isSentType: Boolean, dateTime: String) {

            val amountSign: String
            val amountColor: Int

            if (isSentType) {
                item_history_type.setText(R.string.mozo_view_text_tx_sent)
                amountSign = "-"
                amountColor = ContextCompat.getColor(itemView.context, R.color.mozo_color_title)
            } else {
                item_history_type.setText(R.string.mozo_view_text_tx_received)
                amountSign = "+"
                amountColor = ContextCompat.getColor(itemView.context, R.color.mozo_color_primary)
            }

            item_history_amount.text = itemView.resources.getString(R.string.mozo_transaction_history_amount_text, amountSign, history.amountDisplay())
            item_history_amount.setTextColor(amountColor)

            item_history_amount_fiat.text = "₩0"

            item_history_time.text = dateTime
        }
    }

    companion object {
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(FILTER_ALL, FILTER_RECEIVED, FILTER_SENT)
        annotation class FilterMode

        const val FILTER_ALL = 0x1
        const val FILTER_RECEIVED = 0x2
        const val FILTER_SENT = 0x3
    }
}