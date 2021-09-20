package com.lavos.features.localshops

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.Pref
import com.lavos.app.domain.AddShopDBModelEntity
import com.lavos.app.domain.OrderDetailsListEntity
import com.lavos.app.utils.AppUtils
import com.lavos.features.location.LocationWizard
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.inflate_nearby_shops.view.*

/**
 * Created by riddhi on 2/1/18.
 */
class LocalShopsListAdapter(context: Context, list: List<AddShopDBModelEntity>, val listener: LocalShopListClickListener) : RecyclerView.Adapter<LocalShopsListAdapter.MyViewHolder>() {
    private val layoutInflater: LayoutInflater
    private var context: Context
    private var mList: List<AddShopDBModelEntity>


    init {
        layoutInflater = LayoutInflater.from(context)
        this.context = context
        mList = list
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindItems(context, mList, listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = layoutInflater.inflate(R.layout.inflate_nearby_shops, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(context: Context, list: List<AddShopDBModelEntity>, listener: LocalShopListClickListener) {
            //Picasso.with(context).load(list[adapterPosition].shopImageLocalPath).into(itemView.shop_image_IV)

            itemView.total_v_TV.text = context.getString(R.string.total_visits)

            if (!TextUtils.isEmpty(list[adapterPosition].shopImageLocalPath)) {
                Picasso.get()
                        .load(list[adapterPosition].shopImageLocalPath)
                        .resize(100, 100)
                        .into(itemView.shop_image_IV)
            }
            itemView.myshop_name_TV.text = list[adapterPosition].shopName
            var address: String = list[adapterPosition].address + ", " + list[adapterPosition].pinCode
            itemView.myshop_address_TV.text = address

            val drawable = TextDrawable.builder()
                    .buildRoundRect(list[adapterPosition].shopName.toUpperCase().take(1), ColorGenerator.MATERIAL.randomColor, 120)

            itemView.shop_IV.setImageDrawable(drawable)

            itemView.shop_image_IV.findViewById<ImageView>(R.id.shop_image_IV).setOnClickListener(View.OnClickListener {
                //                listener.OnNearByShopsListClick(adapterPosition)
            })

            itemView.call_ll.findViewById<LinearLayout>(R.id.call_ll).setOnClickListener(View.OnClickListener {
                //                listener.callClick(adapterPosition)
            })

            itemView.direction_ll.findViewById<LinearLayout>(R.id.direction_ll).setOnClickListener(View.OnClickListener {
                //                listener.mapClick(adapterPosition)
            })

            itemView.add_order_ll.findViewById<LinearLayout>(R.id.add_order_ll).setOnClickListener {
                listener.onOrderClick(list[adapterPosition])
            }

            itemView.order_amt_p_TV.text = " " + context.getString(R.string.zero_order_in_value)
            itemView.total_visited_value_TV.text = " " + list[adapterPosition].totalVisitCount
            itemView.last_visited_date_TV.text = " " + list[adapterPosition].lastVisitedDate

            if (list[adapterPosition].visited) {
                itemView.visit_icon.visibility = View.VISIBLE
                itemView.visit_TV.text = "Revisited Today"
            } else {
                itemView.visit_icon.visibility = View.GONE

                /*if (Pref.isReplaceShopText)
                    itemView.visit_TV.text = "VISIT THIS CUSTOMER"
                else
                    itemView.visit_TV.text = "VISIT THIS SHOP"*/

                itemView.visit_TV.text = "Revisit Now"
            }

            itemView.shop_list_LL.setOnClickListener(View.OnClickListener {
                //                listener.OnNearByShopsListClick(adapterPosition)
            })

            itemView.visit_rl.setOnClickListener(View.OnClickListener {
                if (Pref.isMultipleVisitEnable) {
                    val list_ = AppDatabase.getDBInstance()!!.shopActivityDao().getShopForDay(list[adapterPosition].shop_id, AppUtils.getCurrentDateForShopActi())
                    /*if (list_ == null || list_.isEmpty())
                        listener.visitShop(list[adapterPosition])
                    else {
                        var isDurationCalculated = false
                        for (i in list_.indices) {
                            isDurationCalculated = list_[i].isDurationCalculated
                            if (!list_[i].isDurationCalculated)
                                break

                        }

                        if (isDurationCalculated)
                            listener.visitShop(list[adapterPosition])
                    }*/
                    listener.visitShop(list[adapterPosition])
                }
                else {
                    if (!list[adapterPosition].visited)
                        listener.visitShop(list[adapterPosition])
                }
            })

            itemView.tv_shop_contact_no.text = list[adapterPosition].ownerContactNumber

            if (!TextUtils.isEmpty(list[adapterPosition].assigned_to_dd_id)) {
                itemView.rl_dd.visibility = View.VISIBLE
                itemView.tv_pp_dd_header.text = context.getString(R.string.dist)

                val dd = AppDatabase.getDBInstance()?.ddListDao()?.getSingleValue(list[adapterPosition].assigned_to_dd_id)

                if (!TextUtils.isEmpty(dd?.dd_name) && !TextUtils.isEmpty(dd?.dd_phn_no))
                    itemView.tv_pp_dd_value.text = dd?.dd_name + " (" + dd?.dd_phn_no + ")"
                else if (!TextUtils.isEmpty(dd?.dd_name))
                    itemView.tv_pp_dd_value.text = dd?.dd_name

            } else if (!TextUtils.isEmpty(list[adapterPosition].assigned_to_pp_id)) {
                itemView.rl_dd.visibility = View.VISIBLE
                itemView.tv_pp_dd_header.text = context.getString(R.string.pp_super)

                val pp = AppDatabase.getDBInstance()?.ppListDao()?.getSingleValue(list[adapterPosition].assigned_to_pp_id)

                if (!TextUtils.isEmpty(pp?.pp_name) && !TextUtils.isEmpty(pp?.pp_phn_no))
                    itemView.tv_pp_dd_value.text = pp?.pp_name + " (" + pp?.pp_phn_no + ")"
                else if (!TextUtils.isEmpty(pp?.pp_name))
                    itemView.tv_pp_dd_value.text = pp?.pp_name

            } else {
                itemView.rl_dd.visibility = View.GONE
            }

            try {
                val orderList = AppDatabase.getDBInstance()!!.orderDetailsListDao().getListAccordingToShopId(list[adapterPosition].shop_id) as ArrayList<OrderDetailsListEntity>

                if (orderList != null && orderList.isNotEmpty()) {
                    itemView.order_RL.visibility = View.VISIBLE

                    var amount = 0.0
                    for (i in orderList.indices) {
                        if (!TextUtils.isEmpty(orderList[i].amount))
                            amount += orderList[i].amount?.toDouble()!!
                    }
                    val finalAmount = String.format("%.2f", amount.toFloat())
                    itemView.order_amt_p_TV.text = "\u20B9 $finalAmount"
                } else
                    itemView.order_RL.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                itemView.order_RL.visibility = View.GONE
            }

            itemView.call_ll.setOnClickListener {
                listener.onCallClick(list[adapterPosition])
            }

            itemView.direction_ll.setOnClickListener {
                listener.onLocationClick(list[adapterPosition])
            }

            itemView.add_quot_ll.setOnClickListener {
                listener.onQuationClick(list[adapterPosition])
            }

            if (Pref.isEntityCodeVisible) {
                if (!TextUtils.isEmpty(list[adapterPosition].entity_code)) {
                    itemView.ll_shop_code.visibility = View.VISIBLE
                    itemView.tv_shop_code.text = list[adapterPosition].entity_code
                } else
                    itemView.ll_shop_code.visibility = View.GONE
            } else
                itemView.ll_shop_code.visibility = View.GONE


            if (Pref.isQuotationShow) {
                itemView.add_quot_ll.visibility = View.VISIBLE
                itemView.order_view.visibility = View.VISIBLE
            } else {
                itemView.add_quot_ll.visibility = View.GONE
                itemView.order_view.visibility = View.GONE
            }

            val distance = LocationWizard.getDistance(list[adapterPosition].shopLat, list[adapterPosition].shopLong,
                    Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
            itemView.approx_distance.text = (distance * 1000).toString() + " mtr"

            if (Pref.willShowPartyStatus)
                itemView.rl_party.visibility = View.VISIBLE
            else
                itemView.rl_party.visibility = View.GONE

            if (Pref.willShowEntityTypeforShop && list[adapterPosition].type == "1")
                itemView.rl_entity.visibility = View.VISIBLE
            else
                itemView.rl_entity.visibility = View.GONE

            if (!TextUtils.isEmpty(list[adapterPosition].entity_id)) {
                val entity = AppDatabase.getDBInstance()?.entityDao()?.getSingleItem(list[adapterPosition].entity_id)
                itemView.tv_entity_value.text = entity?.name
            }
            else
                itemView.tv_entity_value.text = "N.A."

            if (!TextUtils.isEmpty(list[adapterPosition].party_status_id)) {
                val partyStatus = AppDatabase.getDBInstance()?.partyStatusDao()?.getSingleItem(list[adapterPosition].party_status_id)
                itemView.tv_party_value.text = partyStatus?.name
            }
            else
                itemView.tv_party_value.text = "N.A."
        }
    }

    fun updateAdapter(mlist: List<AddShopDBModelEntity>) {
        this.mList = mlist
        notifyDataSetChanged()
    }


}