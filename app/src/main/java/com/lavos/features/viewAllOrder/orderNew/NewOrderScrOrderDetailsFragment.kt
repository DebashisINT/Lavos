package com.lavos.features.viewAllOrder.orderNew

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lavos.CustomStatic
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.Pref
import com.lavos.app.domain.AddShopDBModelEntity
import com.lavos.app.types.FragType
import com.lavos.app.utils.AppUtils
import com.lavos.app.utils.FTStorageUtils
import com.lavos.base.presentation.BaseFragment
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.stockCompetetorStock.CompetetorStockFragment
import com.lavos.features.viewAllOrder.interf.NewOrdScrShowDetaisOnCLick
import com.lavos.features.viewAllOrder.model.ColorList
import com.lavos.features.viewAllOrder.model.NewOrderCartModel
import com.lavos.features.viewAllOrder.model.ProductOrder
import com.lavos.features.viewAllOrder.presentation.AdapterNewOrdScrOrdList
import com.lavos.widgets.AppCustomTextView
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class NewOrderScrOrderDetailsFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context

    private lateinit var mRv_orderDetails: RecyclerView
    private lateinit var ll_Add: LinearLayout

    private lateinit var myshop_name_TV: AppCustomTextView
    private lateinit var myshop_addr_TV: AppCustomTextView
    private lateinit var myshop_contact_TV: AppCustomTextView

    private var adapterNewOrdScrOrdList: AdapterNewOrdScrOrdList? = null

    private lateinit var share_Icon: LinearLayout

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        var mAddShopDataObj: AddShopDBModelEntity? = null
        var shop_id: String = ""
        fun getInstance(objects: Any): NewOrderScrOrderDetailsFragment {
            val Fragment = NewOrderScrOrderDetailsFragment()
            if (!TextUtils.isEmpty(objects.toString())) {
                shop_id = objects.toString()
                mAddShopDataObj = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(shop_id)
            }
            return Fragment
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.activity_new_order_scr_order_details_fragment, container, false)
        initView(view)
        return view
    }


    override fun onResume() {
        super.onResume()
    }


    fun onShareClick() {
        var qty_Order: Int = 0
        val heading = "ORDER SUMMARY"


        var pdfBody = "\n\n\n\nShop Name: " +  mAddShopDataObj?.shopName + "\n\nShop Address: " + mAddShopDataObj?.address +
                "\n\nShop Contact: "+  mAddShopDataObj?.ownerContactNumber

        val orderList =  AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getShopOrderAll(shop_id)



        orderList?.forEach {it1 ->
            pdfBody = pdfBody + "\n\n-----------------------------------------------------------------------------\n\n"+
                    "Date: " + it1.order_date + "Order#: " + it1.order_id +""
                 var qtty_list = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getShopOrderQtyOrderIDWise(it1.order_id!!)
                      for (i in 0..qtty_list!!.size - 1) {
                           qty_Order = qty_Order + qtty_list.get(i).toString().toInt()
                       }

            }
            pdfBody = pdfBody +
                    "  QTY: " + qty_Order + "\n\n"




        val image = BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher)

        val path = FTStorageUtils.stringToPdf(pdfBody, mContext, "OrderSummary_" +
                "_" + Pref.user_id + ".pdf", image, heading, 3.7f)

        if (!TextUtils.isEmpty(path)) {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                val fileUrl = Uri.parse(path)

                val file = File(fileUrl.path)
                val uri = Uri.fromFile(file)
                shareIntent.type = "image/png"
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(shareIntent, "Share pdf using"));
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else
            (mContext as DashboardActivity).showSnackMessage("Pdf can not be sent.")


    }

    private fun initView(view: View?) {
        mRv_orderDetails = view!!.findViewById(R.id.rv_new_order_list)
        mRv_orderDetails.layoutManager = LinearLayoutManager(mContext)
        ll_Add = view!!.findViewById(R.id.ll_frag_new_order_detalis_add)
        ll_Add.setOnClickListener(this)

        myshop_name_TV = view!!.findViewById(R.id.myshop_name_TV)
        myshop_addr_TV = view!!.findViewById(R.id.myshop_address_TV)
        myshop_contact_TV = view!!.findViewById(R.id.tv_contact_number)
        share_Icon = view!!.findViewById(R.id.ll_frag_new_order_detalis_share)


        myshop_name_TV.text = mAddShopDataObj?.shopName
        myshop_addr_TV.text = mAddShopDataObj?.address
        myshop_contact_TV.text = "Owner Contact Number: " + mAddShopDataObj?.ownerContactNumber.toString()

        share_Icon.setOnClickListener(this)

        getOrderList()
    }

    private var final_order_list: ArrayList<NewOrderCartModel> = ArrayList()
    private var newOrderCartModel1: NewOrderCartModel? = null
    private var newOrderCartModel2: NewOrderCartModel? = null


    data class OrderIDDateStatus(var order_date: String, var isUploaded: Boolean)
    data class OrderIDDateViewStatus(var order_id: String, var order_date: String, var isUploaded: Boolean)

    var qty_list: ArrayList<String> = ArrayList()
    var rv_data: ArrayList<OrderIDDateViewStatus> = ArrayList()

    private fun getOrderList() {
        qty_list.clear()
        var objOrderDistList = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getShopOrderDistinct(shop_id)
        try {
            for (i in 0..objOrderDistList!!.size - 1) {
                var qty_Order: Int = 0
                var qtty_list = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getShopOrderQtyOrderIDWise(objOrderDistList.get(i))
                for (i in 0..qtty_list!!.size - 1) {
                    qty_Order = qty_Order + qtty_list.get(i).toString().toInt()
                }


                var objDateUploadList = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getOrderIdDateStatus(objOrderDistList!!.get(i))
                var obj: OrderIDDateViewStatus = OrderIDDateViewStatus(objOrderDistList.get(i), objDateUploadList!!.order_date, objDateUploadList.isUploaded)

                qty_list.add(qty_Order.toString())
                rv_data.add(obj)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }


        adapterNewOrdScrOrdList = AdapterNewOrdScrOrdList(mContext, rv_data, object : NewOrdScrShowDetaisOnCLick {
            override fun getOrderID(orderID: String, orderDate: String) {
                getOrderDetailsList(orderID, orderDate)
            }
        })
        mRv_orderDetails.adapter = adapterNewOrdScrOrdList

    }


    private fun getOrderDetailsList(orderID: String, orderDate: String) {
        var orderAllList = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getShopOrderAll(shop_id)
        //var orderIdList=AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getShopOrderDistinct(shop_id)
        var orderIdList: ArrayList<String> = ArrayList()
        orderIdList.add(orderID)
        final_order_list.clear()

        for (i in 0..orderIdList!!.size - 1) {
            newOrderCartModel1 = NewOrderCartModel()
            newOrderCartModel2 = NewOrderCartModel()
            //var uniqGender=AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getUniqGenderForOrderID(orderIdList.get(i))

            var productIDList = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getProductCodeDistinctByOrderID(orderIdList.get(i))
            //


            for (j in 0..productIDList!!.size - 1) {

                if (j != 0) {
                    if (newOrderCartModel1 != null) {
                        if (newOrderCartModel1!!.color_list.size > 0)
                            final_order_list.add(newOrderCartModel1!!)
                    }
                    if (newOrderCartModel2 != null) {
                        if (newOrderCartModel2!!.color_list.size > 0)
                            final_order_list.add(newOrderCartModel2!!)
                    }
                }

                newOrderCartModel1 = NewOrderCartModel()
                newOrderCartModel2 = NewOrderCartModel()

                //newOrderCartModel1!!.product_id=productIDList!!.get(j)
                //newOrderCartModel2!!.product_id=productIDList!!.get(j)
                //newOrderCartModel1!!.product_name=AppDatabase.getDBInstance()?.newOrderProductDao()?.getNewOrderProductName(productIDList!!.get(j))!!
                //newOrderCartModel2!!.product_name=AppDatabase.getDBInstance()?.newOrderProductDao()?.getNewOrderProductName(productIDList!!.get(j))!!

                var colorIDListForProduct = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getColorIDDistinctByOrderID(orderIdList.get(i), productIDList.get(j))
                for (k in 0..colorIDListForProduct!!.size - 1) {
                    var sizeQtyListMale = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getSizeQtyByProductColorIDMale(orderIdList!!.get(i), productIDList!!.get(j), colorIDListForProduct!!.get(k))
                    var sizeQtyListFeMale = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getSizeQtyByProductColorIDFemale(orderIdList!!.get(i), productIDList!!.get(j), colorIDListForProduct!!.get(k))
                    if (sizeQtyListMale!!.size > 0) {
                        newOrderCartModel1!!.product_id = productIDList!!.get(j)
                        newOrderCartModel1!!.product_name = AppDatabase.getDBInstance()?.newOrderProductDao()?.getNewOrderProductName(productIDList!!.get(j))!!

                        newOrderCartModel1!!.gender = "MALE"

                        var colorSel = AppDatabase.getDBInstance()?.newOrderColorDao()?.getNewOrderColorName(colorIDListForProduct.get(k))
                        var colorList: ColorList = ColorList(colorSel!!, colorIDListForProduct.get(k), sizeQtyListMale as ArrayList<ProductOrder>)
                        newOrderCartModel1!!.color_list.add(colorList)
                    }

                    if (sizeQtyListFeMale!!.size > 0) {
                        newOrderCartModel2!!.product_id = productIDList!!.get(j)
                        newOrderCartModel2!!.product_name = AppDatabase.getDBInstance()?.newOrderProductDao()?.getNewOrderProductName(productIDList!!.get(j))!!

                        newOrderCartModel2!!.gender = "FEMALE"

                        var colorSel = AppDatabase.getDBInstance()?.newOrderColorDao()?.getNewOrderColorName(colorIDListForProduct.get(k))
                        var colorList: ColorList = ColorList(colorSel!!, colorIDListForProduct.get(k), sizeQtyListFeMale as ArrayList<ProductOrder>)
                        newOrderCartModel2!!.color_list.add(colorList)
                    }


                }

            }

            if (newOrderCartModel1 != null) {
                if (newOrderCartModel1!!.color_list.size > 0)
                    final_order_list.add(newOrderCartModel1!!)
            }
            if (newOrderCartModel2 != null) {
                if (newOrderCartModel2!!.color_list.size > 0)
                    final_order_list.add(newOrderCartModel2!!)
            }

        }

        CustomStatic.IsFromViewNewOdrScr = true
        CustomStatic.IsFromViewNewOdrScrOrderID = orderID
        CustomStatic.IsFromViewNewOdrScrOrderDate = orderDate
        (mContext as DashboardActivity).loadFragment(FragType.NeworderScrCartFragment, true, final_order_list)

    }

    private fun arrangProduct() {


    }


    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.ll_frag_new_order_detalis_add -> {
                    (mContext as DashboardActivity).loadFragment(FragType.NewOrderScrActiFragment, true, shop_id)
                }

                R.id.ll_frag_new_order_detalis_share->{
                    //onShareClick()
                    onShareClickNew()
                }
            }
        }
    }


    fun onShareClickNew(){
        var heading = "ORDER SUMMARY"
        var pdfBody: String = "\n\n"

        pdfBody=pdfBody+"Name : "+mAddShopDataObj?.shopName.toString()+"\n\n"
        pdfBody=pdfBody+"Address : "+mAddShopDataObj?.address.toString()+"\n\n"
        pdfBody=pdfBody+"Phone : "+mAddShopDataObj?.ownerContactNumber.toString()+"\n\n"

        pdfBody=pdfBody+"\n\n"+"Order Date"+"       "+"           Order ID"+"       "+"                 QTY"+"\n"

        for(i in 0..rv_data!!.size-1){
            var qty_Order: Int = 0
            var qtty_list = AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.getShopOrderQtyOrderIDWise(rv_data!!.get(i).order_id)
            for (j in 0..qtty_list!!.size - 1) {
                qty_Order = qty_Order + qtty_list.get(j).toString().toInt()
            }
            var content="\n\n"+rv_data.get(i).order_date+"        "+rv_data.get(i).order_id+"           "+qty_Order.toString()+"\n"+
                    "\n____________________________________________________________"
            pdfBody=pdfBody+content
        }


        val image = BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher)

        val path = FTStorageUtils.stringToPdf(pdfBody, mContext, "OrderDtls" +
                "_" + Pref.user_id+AppUtils.getCurrentDateTime().toString().replace(" ","R").replace(":","_") + ".pdf", image, heading, 3.7f)




        if (!TextUtils.isEmpty(path)) {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                val fileUrl = Uri.parse(path)

                val file = File(fileUrl.path)
                val uri = Uri.fromFile(file)
                shareIntent.type = "image/png"
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                startActivity(Intent.createChooser(shareIntent, "Share pdf using"));
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else
            (mContext as DashboardActivity).showSnackMessage("Pdf can not be sent.")


    }


}