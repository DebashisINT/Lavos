package com.lavos.features.viewAllOrder.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.utils.AppUtils
import com.lavos.features.viewAllOrder.interf.ViewNewOrdScrDetailsOnCLick
import com.lavos.features.viewAllOrder.orderNew.NewOdrScrListFragment
import kotlinx.android.synthetic.main.row_new_odr_scr_list_details.view.*

class NewOdrScrListAdapter (var context: Context,var list:List<NewOdrScrListFragment.ViewDataNewOdrScrDetails>,var listner: ViewNewOrdScrDetailsOnCLick):
        RecyclerView.Adapter<NewOdrScrListAdapter.OdrScrListViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OdrScrListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_new_odr_scr_list_details,parent,false)
        return OdrScrListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: OdrScrListViewHolder, position: Int) {
        holder.tv_orderID.text="Order ID : "+list.get(position).order_id
        holder.tv_orderDate.text= AppUtils.convertToCommonFormat(list.get(position).order_date)
        holder.tv_shopName.text=list.get(position).shop_name
        holder.tv_shopAddr.text=list.get(position).shop_addr

        holder.btn_viewDetails.setOnClickListener { listner.getOrderID(list.get(holder.adapterPosition).order_id,list.get(holder.adapterPosition).order_date,
                list.get(holder.adapterPosition).shop_id) }

        holder.tv_orderID.setOnClickListener { listner.getOrderID(list.get(holder.adapterPosition).order_id,list.get(holder.adapterPosition).order_date,
                list.get(holder.adapterPosition).shop_id) }

        var qty_Order: Int = 0
        var qtty_list = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getShopOrderQtyOrderIDWise(list.get(position).order_id)
        for (i in 0..qtty_list!!.size - 1) {
            qty_Order = qty_Order + qtty_list.get(i).toString().toInt()
        }
        holder.tv_qty.text="Qty         : "+qty_Order.toString()
    }

    inner class OdrScrListViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var tv_orderID = itemView.tv_row_new_odr_scr_dtls_order_id
        var tv_orderDate = itemView.tv_row_new_odr_scr_dtls_order_date
        var tv_shopName = itemView.tv_row_new_odr_scr_dtls_order_shop_name
        var tv_shopAddr = itemView.tv_row_new_odr_scr_dtls_order_shop_addr
        var tv_qty = itemView.tv_row_new_odr_scr_dtls_order_qty
        var btn_viewDetails = itemView.btn_row_new_odr_scr_dtls_view_details
    }

}