package com.biglabs.mozo.sdk.trans

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

internal class TransactionHistoryRecyclerAdapter(private val histories: List<Models.TransactionHistory>, private val itemClick: ((position: Int) -> Unit)? = null) : RecyclerView.Adapter<TransactionHistoryRecyclerAdapter.ItemViewHolder>() {

    var address: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false))

    override fun getItemCount(): Int = histories.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val history = histories[position]
        holder.bind(history, history.type(address))
        holder.itemView.click { itemClick?.invoke(position) }
    }

    class ItemViewHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(history: Models.TransactionHistory, isSentType: Boolean) {

            val amountSign: String
            val amountColor: Int

            if (isSentType) {
                item_history_type.setText(R.string.mozo_view_text_tx_sent)
                amountSign = "-"
                amountColor = ContextCompat.getColor(itemView.context, R.color.mozo_color_error)
            } else {
                item_history_type.setText(R.string.mozo_view_text_tx_received)
                amountSign = "+"
                amountColor = ContextCompat.getColor(itemView.context, R.color.mozo_color_primary)
            }

            item_history_amount.text = itemView.resources.getString(R.string.mozo_transaction_history_amount_text, amountSign, history.amountDisplay())
            item_history_amount.setTextColor(amountColor)

            item_history_amount_fiat.text = "₩0"
        }
    }
}