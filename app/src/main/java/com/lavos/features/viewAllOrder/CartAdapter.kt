package com.lavos.features.viewAllOrder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.Pref
import com.lavos.app.domain.ProductListEntity
import com.lavos.app.domain.ProductRateEntity
import com.lavos.app.utils.AppUtils
import com.lavos.app.utils.CustomSpecialTextWatcher
import com.lavos.features.dashboard.presentation.DashboardActivity
import kotlinx.android.synthetic.main.cart_adapter_body_layout.view.*
import kotlinx.android.synthetic.main.item_right_menu.view.*


/**
 * Created by Saikat on 09-11-2018.
 */
class CartAdapter(private val context: Context, private val selectedProductList: ArrayList<ProductListEntity>?, private val listener: OnProductClickListener) :
        RecyclerView.Adapter<CartAdapter.MyViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    /*init {
        (context as DashboardActivity).totalPrice.clear()
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = layoutInflater.inflate(R.layout.inflate_cart_new, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindItems(context, selectedProductList, listener)
    }

    override fun getItemCount(): Int {
        return selectedProductList?.size!!
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(context: Context, categoryList: ArrayList<ProductListEntity>?, listener: OnProductClickListener) {

            var previousQty = ""
            var previousRate = ""

            /*if (adapterPosition % 2 == 0)
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.report_screen_bg))
            else
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))*/

            try {
//                itemView.iv_product_edit_icon.setOnClickListener({
//                    /*if (itemView.iv_product_check.isSelected) {
//                        itemView.iv_product_check.isSelected = false
//                        listener.onProductClick(categoryList?.get(adapterPosition), adapterPosition, false)
//                    } else {
//                        itemView.iv_product_check.isSelected = true
//                        listener.onProductClick(categoryList?.get(adapterPosition), adapterPosition, true)
//                    }*/
//
//                    listener.onEdit(adapterPosition)
//
//                })

                itemView.tv_post_list_item_delete.setOnClickListener {
                    listener.onDelete(adapterPosition)
                }

                itemView.tv_particular_item.text = categoryList?.get(adapterPosition)?.product_name

                /*if ((context as DashboardActivity).rateList[adapterPosition].contains("."))
                    itemView.tv_brand_item.text = "Rate : ₹ " + (context as DashboardActivity).rateList[adapterPosition] + ".00"
                else if ((context as DashboardActivity).rateList[adapterPosition].substring((context as DashboardActivity).rateList[adapterPosition].indexOf("."),
                        (context as DashboardActivity).rateList[adapterPosition].length).length == 1)
                    itemView.tv_brand_item.text = "Rate : ₹ " + (context as DashboardActivity).rateList[adapterPosition].substring((context as DashboardActivity).rateList[adapterPosition].indexOf("."),
                            (context as DashboardActivity).rateList[adapterPosition].length) + ".0"
                else*/
                //itemView.tv_brand_item.text = "Rate: ₹ " + String.format("%.2f", (context as DashboardActivity).rateList[adapterPosition].toFloat())

                //itemView.tv_category_item.text = "Quantity: " + (context as DashboardActivity).qtyList[adapterPosition] //categoryList?.get(adapterPosition)?.category

                //val totalPrice = DecimalFormat("##.##").format((context as DashboardActivity).rateList[adapterPosition].toDouble() * (context as DashboardActivity).qtyList[adapterPosition].toInt())

                /*val totalPrice = String.format("%.2f", ((context as DashboardActivity).rateList[adapterPosition].toFloat() * (context as DashboardActivity).qtyList[adapterPosition].toInt()).toFloat())
                itemView.tv_watt_item.text = "Total Price: ₹ " + totalPrice.toString()*/  //categoryList?.get(adapterPosition)?.watt

                //itemView.et_rate.filters = arrayOf<InputFilter>(InputFilterDecimal(8, 2))

                if (Pref.isRateOnline) {
                    val list = AppUtils.loadSharedPreferencesProductRateList(context)

                    if (AppUtils.stockStatus == 0) {
                        if (Pref.isRateNotEditable && list != null && list.size > 0) {
                            itemView.et_rate.isEnabled = false
                            itemView.et_rate.setText(((context as DashboardActivity).rateList[adapterPosition]))
                        } else {
                            itemView.et_rate.isEnabled = true

                            if ((context as DashboardActivity).rateList[adapterPosition] > "0.00")
                                itemView.et_rate.setText(((context as DashboardActivity).rateList[adapterPosition]/*.toFloat().toInt()*/).toString())
                            else
                                itemView.et_rate.setText("")
                        }
                    } else if (AppUtils.stockStatus == 1) {
                        itemView.et_rate.isEnabled = true

                        if ((context as DashboardActivity).rateList[adapterPosition] > "0.00")
                            itemView.et_rate.setText(((context as DashboardActivity).rateList[adapterPosition]/*.toFloat().toInt()*/).toString())
                        else
                            itemView.et_rate.setText("")
                    }
                }
                else {
                    val list = AppDatabase.getDBInstance()?.productRateDao()?.getAll() as ArrayList<ProductRateEntity>?

                    if (AppUtils.stockStatus == 0) {
                        if (Pref.isRateNotEditable && list != null && list.size > 0) {
                            itemView.et_rate.isEnabled = false
                            itemView.et_rate.setText(((context as DashboardActivity).rateList[adapterPosition]))
                        } else {
                            itemView.et_rate.isEnabled = true

                            if ((context as DashboardActivity).rateList[adapterPosition] > "0.00")
                                itemView.et_rate.setText(((context as DashboardActivity).rateList[adapterPosition]/*.toFloat().toInt()*/).toString())
                            else
                                itemView.et_rate.setText("")
                        }
                    } else if (AppUtils.stockStatus == 1) {
                        itemView.et_rate.isEnabled = true

                        if ((context as DashboardActivity).rateList[adapterPosition] > "0.00")
                            itemView.et_rate.setText(((context as DashboardActivity).rateList[adapterPosition]/*.toFloat().toInt()*/).toString())
                        else
                            itemView.et_rate.setText("")
                    }
                }


                /*itemView.et_rate.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                        var rate = itemView.et_rate.text.toString().trim()
//                        if (rate < "1") {
//                            rate = "1"
//                            itemView.et_rate.setText(rate)
//                        }
                        if (!TextUtils.isEmpty(rate)) {

                            if (rate.contains(".")) {
                                val index = rate.indexOf(".")
                                val subStr1 = rate.substring(0, index)
                                val subStr = rate.substring(index, rate.length)

                                if (subStr1.length > 4) {
                                    if (subStr.length > 3) {
                                        itemView.et_rate.setText(rate.substring(0, 1) + rate.substring(1, 2) + rate.substring(2, 3) + rate.substring(3, 4)+
                                                rate.substring(index, index + 3))
                                    }
                                    else {
                                        itemView.et_rate.setText(rate.substring(0, 1) + rate.substring(1, 2) + rate.substring(2, 3) + rate.substring(3, 4)+
                                                rate.substring(index, rate.length))
                                    }
                                }
                                else {
                                    if (subStr.length > 3 *//*&& subStr1.length > 2*//*) {
                                        itemView.et_rate.setText(rate.substring(0, index) + rate.substring(index, index + 3))
                                    }
                                }
                            } else {
                                val substr = rate.substring(0, rate.length)
                                if (substr.length > 4) {
                                    itemView.et_rate.setText(rate.substring(0, 1) + rate.substring(1, 2) + rate.substring(2, 3) +
                                            rate.substring(3, 4))
                                }
                            }

                            rate = itemView.et_rate.text.toString().trim()
                            //val finalRate = String.format("%.2f", rate.toFloat())
                            (context as DashboardActivity).rateList[adapterPosition] = rate
                            listener.onEdit(adapterPosition)
                        }
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }
                })*/


                itemView.et_rate.addTextChangedListener(CustomSpecialTextWatcher(itemView.et_rate, 5, 2, object : CustomSpecialTextWatcher.GetCustomTextChangeListener {
                    override fun beforeTextChange(text: String) {
                        previousRate = text
                    }

                    override fun customTextChange(text: String) {
                        val rate = text //itemView.et_rate.text.toString().trim()
                        //val finalRate = String.format("%.2f", rate.toFloat())

                        if (!Pref.isShowAllProduct && AppUtils.stockStatus == 0) {
                            (context as DashboardActivity).rateList[adapterPosition] = rate
                            listener.onEdit(adapterPosition)
                        } else {
                            if (!TextUtils.isEmpty(rate)) {
                                try {
                                    /*if (TextUtils.isEmpty((context as DashboardActivity).rateList[adapterPosition]))
                                    (context as DashboardActivity).rateList.add(rate)
                                else*/
                                    (context as DashboardActivity).rateList[adapterPosition] = rate
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    //(context as DashboardActivity).rateList.add(rate)
                                }
                                listener.onEdit(adapterPosition)
                            } else {
                                try {
                                    if (!TextUtils.isEmpty((context as DashboardActivity).rateList[adapterPosition])) {
                                        (context as DashboardActivity).rateList[adapterPosition] = "0.00"
                                        listener.onEdit(adapterPosition)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()

                                    for (i in (context as DashboardActivity).rateList.indices) {
                                        if ((context as DashboardActivity).rateList[i] == previousRate) {
                                            (context as DashboardActivity).rateList[i] = "0.00"
                                            listener.onEdit(i)
                                            break
                                        }
                                    }
                                }
                            }
                        }
                        /*(context as DashboardActivity).rateList[adapterPosition] = rate
                        listener.onEdit(adapterPosition)*/
                    }
                }))

                if ((context as DashboardActivity).qtyList[adapterPosition] >= "1")
                    itemView.et_qty.setText((context as DashboardActivity).qtyList[adapterPosition])
                else
                    itemView.et_qty.setText("")

                itemView.et_qty.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                        val qty = itemView.et_qty.text.toString().trim()
                        /*if (qty < "1") {
                            qty = "1"
                            itemView.et_qty.setText(qty)
                        }*/

                        if (!Pref.isShowAllProduct && AppUtils.stockStatus == 0) {
                            //if (!TextUtils.isEmpty(qty)) {
                            (context as DashboardActivity).qtyList[adapterPosition] = qty
                            listener.onEdit(adapterPosition)
                            //}
                        } else {
                            if (!TextUtils.isEmpty(qty)) {
                                try {
                                    /*if (TextUtils.isEmpty((context as DashboardActivity).qtyList[adapterPosition]))
                                    (context as DashboardActivity).qtyList.add(qty)
                                else*/
                                    (context as DashboardActivity).qtyList[adapterPosition] = qty
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    //(context as DashboardActivity).qtyList.add(qty)
                                }
                                listener.onEdit(adapterPosition)
                            } else {
                                try {
                                    if (!TextUtils.isEmpty((context as DashboardActivity).qtyList[adapterPosition])) {
                                        (context as DashboardActivity).qtyList[adapterPosition] = "0"
                                        listener.onEdit(adapterPosition)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    //(context as DashboardActivity).qtyList[adapterPosition] = "0"

                                    for (i in (context as DashboardActivity).qtyList.indices) {
                                        if ((context as DashboardActivity).qtyList[i] == previousQty) {
                                            (context as DashboardActivity).qtyList[i] = "0"
                                            listener.onEdit(i)
                                            break
                                        }
                                    }
                                }
                            }
                        }
                        /*if (!TextUtils.isEmpty(qty)) {
                            (context as DashboardActivity).qtyList[adapterPosition] = qty
                            listener.onEdit(adapterPosition)
                        }*/
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        previousQty = p0.toString()
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }
                })


                if (adapterPosition != categoryList?.size!! - 1) {
                    itemView.et_qty.imeOptions = EditorInfo.IME_ACTION_NEXT
                } else
                    itemView.et_qty.imeOptions = EditorInfo.IME_ACTION_DONE


                /*itemView.et_rate.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {

                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            itemView.et_qty.requestFocus()
                            itemView.et_rate.clearFocus()
                        }

                        return true
                    }
                })*/


                /*itemView.et_qty.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {

                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            itemView.et_qty.clearFocus()
                            itemView.et_rate.requestFocus()
                        }
                        else if (actionId == EditorInfo.IME_ACTION_DONE) {
                            itemView.et_qty.clearFocus()
                            AppUtils.hideSoftKeyboard(context)
                        }

                        return true
                    }
                })*/

                //(context as DashboardActivity).totalPrice.add(totalPrice.toDouble())

            } catch (e: Exception) {
                e.printStackTrace()
            }
            //itemView.iv_check.isSelected = workTypeList?.get(adapterPosition)?.isSelected!!
            //itemView.iv_check.visibility = View.GONE
            /*itemView.setOnClickListener {
                listener.onProductClick(categoryList?.get(adapterPosition), adapterPosition)
            }*/
        }
    }

    interface OnProductClickListener {
        fun onProductClick(brand: ProductListEntity?, adapterPosition: Int, isSelected: Boolean)

        fun onEdit(adapterPosition: Int)

        fun onDelete(adapterPosition: Int)
    }
}