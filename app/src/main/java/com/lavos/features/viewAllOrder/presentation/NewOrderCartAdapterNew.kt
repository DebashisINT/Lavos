package com.lavos.features.viewAllOrder.presentation

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lavos.CustomStatic
import com.lavos.R
import com.lavos.app.types.FragType
import com.lavos.app.utils.Toaster
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.viewAllOrder.interf.ColorListOnCLick
import com.lavos.features.viewAllOrder.interf.EmptyProductOnClick
import com.lavos.features.viewAllOrder.interf.NewOrderSizeQtyDelOnClick
import com.lavos.features.viewAllOrder.interf.NewOrderorderCount
import com.lavos.features.viewAllOrder.model.NewOrderCartModel
import com.lavos.features.viewAllOrder.model.ProductOrder
import com.lavos.mappackage.SendBrod
import com.lavos.widgets.AppCustomTextView
import kotlinx.android.synthetic.main.row_new_order_cart_details_new.view.*
import kotlinx.android.synthetic.main.row_new_order_color_list.view.*
import java.util.logging.Handler

class NewOrderCartAdapterNew(var context: Context,var cartOrderList:List<NewOrderCartModel>,var listner: NewOrderorderCount) :
    RecyclerView.Adapter<NewOrderCartAdapterNew.NewOrderCartViewHolderNew>(){

    private var sizw_qty_list:ArrayList<ProductOrder> = ArrayList()

    private var sizeQtyAdapter: OrderSizeQtyDetailsAdapter?=null
    private var sizeQtyDialogAdapter: OrderSizeQtyDetailsDelAdapter?=null

    private var adapterColorListNewOrder: AdapterColorListNewOrder?=null

    private lateinit var cartOrderList_Temp:MutableList<NewOrderCartModel>

    var mProductName:String=""

    var mGender:String=""

    init {
        cartOrderList_Temp = cartOrderList as MutableList<NewOrderCartModel>
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewOrderCartViewHolderNew {
        val view=LayoutInflater.from(context).inflate(R.layout.row_new_order_cart_details_new,parent,false)
        return NewOrderCartViewHolderNew(view)
    }

    override fun getItemCount(): Int {
        return cartOrderList!!.size
    }

    override fun onBindViewHolder(holder: NewOrderCartViewHolderNew, position: Int) {
        holder.productName.text=cartOrderList.get(position).product_name.toString()
        holder.gender.text=cartOrderList.get(position).gender.toString()

/*        adapterColorListNewOrder=AdapterColorListNewOrder(context,cartOrderList.get(position).color_list,object: ColorListOnCLick{
            override fun colorListOnCLick(size_qty_list: ArrayList<ProductOrder>, adpPosition: Int) {
                var art=size_qty_list
            }
        })*/
        mProductName = cartOrderList.get(position).product_name
        mGender = cartOrderList.get(position).gender

        adapterColorListNewOrder=AdapterColorListNewOrder(context,cartOrderList.get(position).color_list,mProductName,mGender,object: EmptyProductOnClick {
            override fun emptyProductOnCLick(emptyFound: Boolean) {
                for(i in 0..cartOrderList.size-1){
                    if(cartOrderList.get(i).color_list.size==0){
                        cartOrderList_Temp.removeAt(i)
                        break
                    }
                }
                notifyDataSetChanged()

                //android.os.Handler(Looper.getMainLooper()).postDelayed({
                    if(cartOrderList.size==0){
                        android.os.Handler(Looper.getMainLooper()).postDelayed({
                            (context as DashboardActivity).loadFragment(FragType.NewOrderScrActiFragment, false, "")
                        }, 8000)

                    }
                //}, 5000)


            }
        })




        holder.rv_color_list.adapter=adapterColorListNewOrder

    }





    inner class NewOrderCartViewHolderNew(itemView: View):RecyclerView.ViewHolder(itemView){
        val productName=itemView.tv_row_new_order_product_name_new
        val gender=itemView.tv_row_new_order_gender_new
        val rv_color_list=itemView.rv_color_details_new

    }
}