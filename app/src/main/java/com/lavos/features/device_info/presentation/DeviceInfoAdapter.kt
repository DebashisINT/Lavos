package com.lavos.features.device_info.presentation

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lavos.R
import com.lavos.app.domain.BatteryNetStatusEntity
import com.lavos.app.utils.AppUtils
import kotlinx.android.synthetic.main.inflate_device_info_item.view.*

class DeviceInfoAdapter(private val mContext: Context, private val list: ArrayList<BatteryNetStatusEntity>?,
                        private val onSyncClick: (BatteryNetStatusEntity) -> Unit) : RecyclerView.Adapter<DeviceInfoAdapter.MyViewHolder>() {

    private val layoutInflater: LayoutInflater by lazy {
        LayoutInflater.from(mContext)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindItems()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = layoutInflater.inflate(R.layout.inflate_device_info_item, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list?.size!!
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems() {
            itemView.apply {
                if (!TextUtils.isEmpty(list?.get(adapterPosition)?.date_time))
                    tv_date.text = AppUtils.convertToNotificationDateTime(list?.get(adapterPosition)?.date_time!!)
                else
                    tv_date.text = "N.A."

                tv_battery.text = list?.get(adapterPosition)?.bat_level + "%"

                if (!TextUtils.isEmpty(list?.get(adapterPosition)?.net_type)) {
                    tv_network.text = "Online"

                    if (list?.get(adapterPosition)?.net_type?.equals("Wifi", ignoreCase = true)!!)
                        tv_net_type.text = list[adapterPosition].net_type
                    else
                        tv_net_type.text = list[adapterPosition].net_type + " " + list[adapterPosition].mob_net_type
                }
                else {
                    tv_network.text = "Offline"
                    tv_net_type.text = "N.A."
                }

                if (list?.get(adapterPosition)?.isUploaded!!)
                    sync_icon.setImageResource(R.drawable.ic_registered_shop_sync)
                else {
                    sync_icon.setImageResource(R.drawable.ic_registered_shop_not_sync)
                    sync_icon.setOnClickListener {
                        onSyncClick(list?.get(adapterPosition))
                    }
                }
            }
        }
    }
}