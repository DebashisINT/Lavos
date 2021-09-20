package com.lavos.features.viewAllOrder.orderNew

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lavos.CustomStatic
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.NetworkConstant
import com.lavos.app.Pref
import com.lavos.app.domain.NewOrderScrOrderEntity
import com.lavos.app.types.FragType
import com.lavos.app.uiaction.IntentActionable
import com.lavos.app.utils.AppUtils
import com.lavos.base.presentation.BaseActivity
import com.lavos.base.presentation.BaseFragment
import com.lavos.features.commondialog.presentation.CommonDialog
import com.lavos.features.commondialog.presentation.CommonDialogClickListener
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.stockAddCurrentStock.UpdateShopStockFragment
import com.lavos.features.stockCompetetorStock.api.AddCompStockProvider
import com.lavos.features.stockCompetetorStock.api.AddCompStockRepository
import com.lavos.features.viewAllOrder.api.addorder.AddOrderRepoProvider
import com.lavos.features.viewAllOrder.presentation.NewOrderCartAdapter
import com.lavos.features.viewAllOrder.interf.NewOrderorderCount
import com.lavos.features.viewAllOrder.model.NewOrderCartModel
import com.lavos.features.viewAllOrder.model.NewOrderSaveApiModel
import com.lavos.features.viewAllOrder.presentation.NewOrderCartAdapterNew
import com.lavos.widgets.AppCustomTextView
import com.elvishew.xlog.XLog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_new_order_scr_cart.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class NeworderScrCartFragment : BaseFragment(), View.OnClickListener {

    private lateinit var rv_order: RecyclerView
    private var newOrderCartAdapter: NewOrderCartAdapter? = null
    private var newOrderCartAdapterNew: NewOrderCartAdapterNew? = null

    private lateinit var tv_name:TextView
    private lateinit var tv_orderID:TextView
    private lateinit var tv_orderDate:TextView
    private lateinit var iv_call:ImageView
    private lateinit var tv_phone:TextView
    private lateinit var ll_odrDtlsRoot:LinearLayout
    private var shop_phone:String=""

    private lateinit var mContext: Context

    private lateinit var btnSaveDB: Button
    var ordID:String=""

//    var simpleDialog = Dialog(mContext)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        var cartOrder: ArrayList<NewOrderCartModel>? = null
        fun getInstance(objects: Any): NeworderScrCartFragment {
            val Fragment = NeworderScrCartFragment()
            cartOrder = objects as ArrayList<NewOrderCartModel>
            return Fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_new_order_scr_cart, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View?) {
        rv_order = view!!.findViewById(R.id.rv_frag_new_order_cart)

        tv_name = view!!.findViewById(R.id.tv_frag_new_order_scr_cart_name)
        tv_orderID = view!!.findViewById(R.id.tv_frag_new_order_scr_cart_order_id)
        tv_orderDate = view!!.findViewById(R.id.tv_frag_new_order_scr_cart_order_date)
        iv_call = view!!.findViewById(R.id.iv_frag_new_order_scr_cart_phone)
        tv_phone = view!!.findViewById(R.id.tv_frag_new_order_scr_cart_phone)
        ll_odrDtlsRoot = view!!.findViewById(R.id.ll_frag_new_order_scr_root)

        btnSaveDB = view!!.findViewById(R.id.btn_new_order_save_db)
        btnSaveDB.setOnClickListener(this)


        tv_orderID.text=CustomStatic.IsFromViewNewOdrScrOrderID

        tv_orderDate.text= AppUtils.convertToCommonFormat(CustomStatic.IsFromViewNewOdrScrOrderDate)
        iv_call.setOnClickListener(this)
        tv_phone.setOnClickListener(this)

        if( CustomStatic.IsFromViewNewOdrScr==true){
            ll_odrDtlsRoot.visibility=View.VISIBLE
            btnSaveDB.visibility=View.GONE
            tv_name.text=AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(NewOrderScrOrderDetailsFragment.shop_id).shopName!!
            shop_phone=AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(NewOrderScrOrderDetailsFragment.shop_id).ownerContactNumber!!
        }else{
            ll_odrDtlsRoot.visibility=View.GONE
            btnSaveDB.visibility=View.VISIBLE
        }
        tv_phone.text=shop_phone

        showCartDetails()
    }


    /*    private fun showCartDetails(){
            newOrderCartAdapter=NewOrderCartAdapter(mContext,cartOrder!!,object: NewOrderorderCount{
                override fun getOrderCount(orderCount: Int) {
                    (mContext as DashboardActivity).tv_cart_count.text = orderCount.toString()
                    (mContext as DashboardActivity).tv_cart_count.visibility = View.VISIBLE
                    //(mContext as DashboardActivity).showSnackMessage(getString(R.string.add_product_cart))
                }
            })
            rv_order.adapter=newOrderCartAdapter
        }*/
    fun showCheckAlert() {
        var simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_new_order_confirmed)
        val dialogHeader = simpleDialog.findViewById(R.id.dialog_new_order_confirmed_header_TV) as AppCustomTextView
        val dialog_yes_no_headerTV = simpleDialog.findViewById(R.id.dialog_new_order_confirmed_headerTV) as AppCustomTextView
        dialog_yes_no_headerTV.text = "Order Confirmation"
        dialogHeader.text = "Do you want to recheck the order?"
        val dialogYes = simpleDialog.findViewById(R.id.tv_message_yes) as AppCustomTextView
        val dialogNo = simpleDialog.findViewById(R.id.tv_message_no) as AppCustomTextView
        dialogYes.setOnClickListener({ view ->
            simpleDialog.cancel()
        })
        dialogNo.setOnClickListener({ view ->
            simpleDialog.cancel()
            if (cartOrder!!.size > 0)
                saveToDB()
        })
        simpleDialog.show()
//        CommonDialog.getInstance(header, title, getString(R.string.no), getString(R.string.yes), false, object : CommonDialogClickListener {
//            override fun onLeftClick() {
//                if (cartOrder!!.size > 0)
//                    saveToDB()
//            }
//
//            override fun onRightClick(editableData: String) {
//                // Api called
//            }
//
//        }).show((mContext as DashboardActivity).supportFragmentManager, "")
    }

    private fun showCartDetails() {
        newOrderCartAdapterNew = NewOrderCartAdapterNew(mContext, cartOrder!!, object : NewOrderorderCount {
            override fun getOrderCount(orderCount: Int) {
                (mContext as DashboardActivity).tv_cart_count.text = orderCount.toString()
                (mContext as DashboardActivity).tv_cart_count.visibility = View.VISIBLE
                //(mContext as DashboardActivity).showSnackMessage(getString(R.string.add_product_cart))
            }
        })
        rv_order.adapter = newOrderCartAdapterNew
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.btn_new_order_save_db -> {
                    if (cartOrder!!.size > 0)
                        showCheckAlert()
                        //saveToDB()
                }
                R.id.iv_frag_new_order_scr_cart_phone -> {
                    IntentActionable.initiatePhoneCall(mContext, shop_phone)
                }
                R.id.tv_frag_new_order_scr_cart_phone -> {
                    IntentActionable.initiatePhoneCall(mContext, shop_phone)
                }
            }
        }
    }





    var newOrderRoomDataList:ArrayList<NewOrderRoomData> = ArrayList()

    fun saveToDB(){
        newOrderRoomDataList.clear()
        ordID= Pref.user_id + AppUtils.getCurrentDateMonth() + AppUtils.getRandomNumber(6)
        for(i in 0..cartOrder!!.size-1){
            for(j in 0..cartOrder!!.get(i).color_list.size-1){
                for(k in 0..cartOrder!!.get(i).color_list.get(j).order_list.size-1){
                    var newOrderRoomData=NewOrderRoomData(ordID,cartOrder!!.get(i).product_id.toString(),cartOrder!!.get(i).product_name.toString(),cartOrder!!.get(i).gender.toString(),
                            cartOrder!!.get(i).color_list.get(j).color_id,cartOrder!!.get(i).color_list.get(j).color_name,cartOrder!!.get(i).color_list.get(j).order_list.get(k).size, cartOrder!!.get(i).color_list.get(j).order_list.get(k).qty)

                    newOrderRoomDataList.add(newOrderRoomData)

                    var obj:NewOrderScrOrderEntity= NewOrderScrOrderEntity()
                    obj.order_id=ordID
                    obj.product_id=newOrderRoomData.product_id
                    obj.product_name=newOrderRoomData.product_name
                    obj.gender=newOrderRoomData.gender
                    obj.size=newOrderRoomData.size
                    obj.qty=newOrderRoomData.qty
                    obj.order_date=AppUtils.getCurrentDateyymmdd()
                    obj.shop_id=NewOrderScrActiFragment.shop_id!!
                    obj.color_id=newOrderRoomData.color_id
                    obj.color_name=newOrderRoomData.color_name
                    obj.isUploaded=false
                    AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.insert(obj)

                    XLog.d("NeworderScrCartFragment ITEM : "  + AppUtils.getCurrentDateTime().toString()+"\n"+
                    "ordID:"+ordID+"~product_id:"+obj.product_id+"~gender:"+obj.gender+"~size:"+obj.size+"~qty:"+obj.qty+"~order_date:"+obj.order_date+"~shop_id:"+obj.shop_id+
                    "~color_id:"+obj.color_id+"~color_name:"+obj.color_name+"\n")
                }
            }
        }

        if(AppUtils.isOnline(mContext)){
            sendToApi(ordID)

            //showConfirmationDialog()

        }else{
            showConfirmationDialog()
        }

    }

    data class NewOrderRoomData(var order_id:String,var product_id:String,var product_name:String,var gender:String,var color_id:String,var color_name:String ,var size:String,var qty:String)


    private fun sendToApi(ordID:String){
        var newOrderSaveApiModel: NewOrderSaveApiModel=NewOrderSaveApiModel()
        newOrderSaveApiModel.user_id=Pref.user_id
        newOrderSaveApiModel.session_token=Pref.session_token
        newOrderSaveApiModel.order_id=ordID
        newOrderSaveApiModel.shop_id=NewOrderScrActiFragment.shop_id!!  //// test needed
        newOrderSaveApiModel.order_date=AppUtils.getCurrentDateyymmdd()
        newOrderSaveApiModel.product_list=newOrderRoomDataList

        val repository = AddOrderRepoProvider.provideAddOrderRepository()
        BaseActivity.compositeDisposable.add(
                repository.addOrderNewOrderScr(newOrderSaveApiModel)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            XLog.d("NewOrderScrCartFrag OrderWithProductAttribute/OrderWithProductAttribute : RESPONSE " + result.status)
                            if (result.status == NetworkConstant.SUCCESS){

                                doAsync {

                                    AppDatabase.getDBInstance()?.newOrderScrOrderDao()?.syncNewOrder(ordID)

                                    uiThread {
                                        showConfirmationDialog()
                                    }
                                }


                            }
                        },{error ->
                            if (error == null) {
                                XLog.d("NewOrderScrCartFrag OrderWithProductAttribute/OrderWithProductAttribute : ERROR " )
                            } else {
                                XLog.d("NewOrderScrCartFrag OrderWithProductAttribute/OrderWithProductAttribute : ERROR " + error.localizedMessage)
                                error.printStackTrace()
                            }
                        })
        )

    }

    private fun showConfirmationDialog(){
        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_message_new)

        val dialog_yes_no_headerTV = simpleDialog.findViewById(R.id.dialog_message_headerTV_new) as AppCustomTextView
        val dialogHeader = simpleDialog.findViewById(R.id.dialog_message_header_TV_new) as AppCustomTextView

        dialog_yes_no_headerTV.text = "Congrats!"
        dialogHeader.text = AppUtils.hiFirstNameText() + " , Your order has been placed successfully. Order No is "+ ordID.toString()+"."


        val dialogYes = simpleDialog.findViewById(R.id.tv_message_ok_new) as AppCustomTextView
        dialogYes.setOnClickListener({ view ->
            simpleDialog.cancel()
            voiceAttendanceMsg( AppUtils.hiFirstNameText() + " , Your order has been placed successfully.")
        })
        simpleDialog.show()
    }

    private fun voiceAttendanceMsg(msg: String) {
        if (Pref.isVoiceEnabledForAttendanceSubmit) {
            val speechStatus = (mContext as DashboardActivity).textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null)
            if (speechStatus == TextToSpeech.ERROR)
                Log.e("Speeh Error", "NewOrderScrCartFragment");
        }

        (mContext as DashboardActivity).loadFragment(FragType.NewOrderScrOrderDetailsFragment, false, NewOrderScrActiFragment.shop_id!! )
    }

}