package com.lavos.features.contacts

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.NetworkConstant
import com.lavos.app.Pref
import com.lavos.app.domain.AddShopDBModelEntity
import com.lavos.app.domain.CallHisEntity
import com.lavos.app.domain.CompanyMasterEntity
import com.lavos.app.domain.ContactActivityEntity
import com.lavos.app.domain.ShopActivityEntity
import com.lavos.app.types.FragType
import com.lavos.app.uiaction.IntentActionable
import com.lavos.app.utils.AppUtils
import com.lavos.app.utils.PermissionUtils
import com.lavos.app.utils.Toaster
import com.lavos.app.widgets.MovableFloatingActionButton
import com.lavos.base.BaseResponse
import com.lavos.base.presentation.BaseActivity
import com.lavos.base.presentation.BaseFragment
import com.lavos.features.addshop.api.AddShopRepositoryProvider
import com.lavos.features.addshop.model.AddShopRequestData
import com.lavos.features.addshop.model.AddShopResponse
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.location.LocationWizard
import com.lavos.features.location.SingleShotLocationProvider
import com.lavos.features.nearbyshops.api.ShopListRepositoryProvider
import com.lavos.features.nearbyshops.model.ShopData
import com.lavos.features.nearbyshops.model.ShopListResponse
import com.lavos.features.nearbyshops.presentation.AdapterCallLogL
import com.lavos.features.nearbyshops.presentation.UpdateShopStatusDialog
import com.lavos.features.shopdetail.presentation.api.EditShopRepoProvider
import com.lavos.widgets.AppCustomEditText
import com.lavos.widgets.AppCustomTextView
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.itextpdf.text.BadElementException
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.VerticalPositionMark
import com.pnikosis.materialishprogress.ProgressWheel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import java.util.Random


class ContactsFrag : BaseFragment(), View.OnClickListener {

    private lateinit var mContext: Context
    private lateinit var mFab: MovableFloatingActionButton
    private lateinit var tvNodata: TextView
    private lateinit var ivContactSync: ImageView
    private lateinit var ivContactSyncDel: ImageView
    private lateinit var iv_click_scheduler: ImageView
    private lateinit var adapterContGr:AdapterContactGr
    private lateinit var adapterContName:AdapterContactName
    private lateinit var progress_wheel: ProgressWheel
    private lateinit var rvContactL: RecyclerView
    private lateinit var tv_syncAll: TextView

    private lateinit var et_search: AppCustomEditText
    private lateinit var iv_search: ImageView
    private lateinit var iv_mic: ImageView

    private var locationStr_lat:String = ""
    private var locationStr_long:String = ""
    private var locationStr:String = ""
    private var location_pinStr:String = ""

    private lateinit var adapterContactList: AdapterContactList

    private var permissionUtils: PermissionUtils? = null
    private var contGrDialog: Dialog? = null

    private lateinit var floating_fab: FloatingActionMenu

    private lateinit var adapterCallLogL : AdapterCallLogL
    private lateinit var getFloatingVal: ArrayList<String>
    private lateinit var programFab1: FloatingActionButton
    private lateinit var programFab2: FloatingActionButton
    private lateinit var programFab3: FloatingActionButton

    lateinit var simpleDialogProcess : Dialog
    lateinit var dialogHeaderProcess: AppCustomTextView
    lateinit var dialog_yes_no_headerTVProcess: AppCustomTextView
    lateinit var dialog_tv_message_ok: AppCustomTextView
    lateinit var dialog_pg: ProgressWheel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private var myCalendar = Calendar.getInstance()
    private var selectedDate = ""
    private var selectedShopIdForActivity = ""
    val dates = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, monthOfYear)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        selectedDate = AppUtils.getFormattedDateForApi(myCalendar.time)
        updateLabel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.frag_contacts, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        progress_wheel = view.findViewById(R.id.progress_wheel_frag_add_cont)
        tvNodata = view.findViewById(R.id.tv_frag_add_cont_noData)
        rvContactL = view.findViewById(R.id.rv_frag_contacts_list)
        mFab = view.findViewById(R.id.fab_frag_contacts_add_contacts)
        ivContactSync = view.findViewById(R.id.iv_frag_contacts_dialog)
        ivContactSyncDel = view.findViewById(R.id.iv_frag_contacts_dialog_del)
        iv_click_scheduler = view.findViewById(R.id.iv_click_scheduler)

        iv_mic = view.findViewById(R.id.iv_frag_contacts_mic)
        et_search = view.findViewById(R.id.et_frag_contacts_search)
        iv_search = view.findViewById(R.id.iv_frag_contacts_search)
        floating_fab = view.findViewById(R.id.floating_fab_contact_frag)

        tv_syncAll = view.findViewById(R.id.tv_frag_contact_sync_all)

        floating_fab.menuIconView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_dashboard_filter_icon))
        floating_fab.menuButtonColorNormal = mContext.resources.getColor(R.color.colorAccent)
        floating_fab.menuButtonColorPressed = mContext.resources.getColor(R.color.colorPrimaryDark)
        floating_fab.menuButtonColorRipple = mContext.resources.getColor(R.color.colorPrimary)
        floating_fab.isIconAnimated = false
        floating_fab.setClosedOnTouchOutside(true)

        getFloatingVal = ArrayList()
        getFloatingVal.add("Alphabetically")
        getFloatingVal.add("Added Date")
        //getFloatingVal.add("Most Visited")
        var preid = 100

        for (i in getFloatingVal.indices) {
            if (i == 0) {
                programFab1 = FloatingActionButton(activity)
                programFab1.buttonSize = FloatingActionButton.SIZE_MINI
                programFab1.id = preid + i
                programFab1.colorNormal = mContext.resources.getColor(R.color.colorPrimaryDark)
                programFab1.colorPressed = mContext.resources.getColor(R.color.delivery_status_green)
                programFab1.colorRipple = mContext.resources.getColor(R.color.delivery_status_green)
                programFab1.labelText = getFloatingVal[0]
                floating_fab.addMenuButton(programFab1)
                programFab1.setOnClickListener(this)
            }
            if (i == 1) {
                programFab2 = FloatingActionButton(activity)
                programFab2.buttonSize = FloatingActionButton.SIZE_MINI
                programFab2.id = preid + i
                programFab2.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                programFab2.colorPressed = mContext.resources.getColor(R.color.delivery_status_green)
                programFab2.colorRipple = mContext.resources.getColor(R.color.delivery_status_green)
                programFab2.labelText = getFloatingVal[1]
                floating_fab.addMenuButton(programFab2)
                programFab2.setOnClickListener(this)
            }
            if (i == 2) {
                programFab3 = FloatingActionButton(activity)
                programFab3.buttonSize = FloatingActionButton.SIZE_MINI
                programFab3.id = preid + i
                programFab3.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                programFab3.colorPressed = mContext.resources.getColor(R.color.delivery_status_green)
                programFab3.colorRipple = mContext.resources.getColor(R.color.delivery_status_green)
                programFab3.labelText = getFloatingVal[2]
                floating_fab.addMenuButton(programFab3)
                programFab3.setOnClickListener(this)
            }
            if (i == 0) {
                programFab1.setImageResource(R.drawable.ic_tick_float_icon)
                programFab1.colorNormal = mContext.resources.getColor(R.color.delivery_status_green)
            } else if (i == 1) {
                programFab2.setImageResource(R.drawable.ic_tick_float_icon_gray)
            } else if(i==3) {
                //programFab3.setImageResource(R.drawable.ic_tick_float_icon_gray)
            }
        }

        mFab.setOnClickListener(this)
        ivContactSync.setOnClickListener(this)
        iv_click_scheduler.setOnClickListener(this)
        ivContactSyncDel.setOnClickListener(this)
        iv_search.setOnClickListener(this)
        iv_mic.setOnClickListener(this)
        tv_syncAll.setOnClickListener(this)
        mFab.setCustomClickListener {
            (mContext as DashboardActivity).loadFragment(FragType.ContactsAddFrag, true, "")
        }
        initPermissionCheckOne()
        if(AppUtils.isOnline(mContext)){
            singleLocation()
        }else{
            locationStr = LocationWizard.getNewLocationName(mContext, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
            location_pinStr = LocationWizard.getPostalCode(mContext, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
            locationStr_lat =Pref.current_latitude.toDouble().toString()
            locationStr_long =Pref.current_longitude.toDouble().toString()
            if(location_pinStr.equals("")){
                location_pinStr = Pref.current_pincode
            }
            progress_wheel.stopSpinning()
        }
        shopContactList("")

        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString().length == 0){
                    shopContactList("")
                }
            }
        })

        simpleDialogProcess = Dialog(mContext)
        simpleDialogProcess.setCancelable(false)
        simpleDialogProcess.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialogProcess.setContentView(R.layout.dialog_message)
        dialogHeaderProcess = simpleDialogProcess.findViewById(R.id.dialog_message_header_TV) as AppCustomTextView
        dialog_yes_no_headerTVProcess = simpleDialogProcess.findViewById(R.id.dialog_message_headerTV) as AppCustomTextView
        dialog_tv_message_ok = simpleDialogProcess.findViewById(R.id.tv_message_ok) as AppCustomTextView
        var ll_dialog_msg_progress_root = simpleDialogProcess.findViewById(R.id.ll_dialog_msg_progress_root) as LinearLayout
        dialog_pg = simpleDialogProcess.findViewById(R.id.pw_dialog_msg_progress) as ProgressWheel
        ll_dialog_msg_progress_root.visibility = View.VISIBLE

        dialog_tv_message_ok.visibility = View.GONE
        dialog_yes_no_headerTVProcess.text = AppUtils.hiFirstNameText()
        dialogHeaderProcess.text = "Please wait while fetching contacts ........."
    }

    fun initPermissionCheckOne() {
        var permissionList = arrayOf<String>( Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_CONTACTS)
        permissionUtils = PermissionUtils(mContext as Activity, object : PermissionUtils.OnPermissionListener {
            @TargetApi(Build.VERSION_CODES.M)
            override fun onPermissionGranted() {

            }
            override fun onPermissionNotGranted() {

            }
        },permissionList)
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tv_frag_contact_sync_all->{
                if(AppUtils.isOnline(mContext)){
                    syncCompanyMaster("")
                    Handler().postDelayed(Runnable {
                        syncShopAll()
                    }, 1900)
                }else{
                    Toaster.msgShort(mContext,"Please connect to internet.")
                }
            }
            R.id.iv_frag_contacts_dialog_del->{
                AppDatabase.getDBInstance()!!.addShopEntryDao().del99()
                shopContactList("")
            }
            R.id.iv_click_scheduler->{
                    (mContext as DashboardActivity).loadFragment(FragType.SchedulerViewFrag, true, "")
            }
            R.id.iv_frag_contacts_dialog -> {
                contGrDialog = Dialog(mContext)
                contGrDialog!!.setCancelable(false)
                contGrDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                contGrDialog!!.setContentView(R.layout.dialog_contact_gr)
                val tvHeader = contGrDialog!!.findViewById(R.id.dialog_contact_gr_header) as TextView
                val rvContactGrName = contGrDialog!!.findViewById(R.id.rv_dialog_cont_gr) as RecyclerView
                val iv_close = contGrDialog!!.findViewById(R.id.iv_dialog_generic_list_close_icon) as ImageView

                tvHeader.text = "Select contact group"
                contGrDialog!!.show()

                doAsync {
                    progress_wheel.spin()
                    var grNameL = AppUtils.getPhoneBookGroups(mContext) as ArrayList<ContactGr>
                    uiThread {
                        progress_wheel.stopSpinning()
                        if(grNameL.size>0){
                            adapterContGr = AdapterContactGr(mContext,grNameL,object :AdapterContactGr.onClick
                            {
                                override fun onGrClick(obj: ContactGr) {
                                    // contGrDialog.dismiss()
                                    showContactNameL(obj)
                                }
                            })
                            rvContactGrName.adapter = adapterContGr
                        }
                    }
                }
                iv_close.setOnClickListener {
                    contGrDialog!!.dismiss()
                    progress_wheel.stopSpinning()
                }
            }
            R.id.iv_frag_contacts_search->{
                shopContactList(et_search.text.toString())
            }
            R.id.iv_frag_contacts_mic->{
                progress_wheel.spin()
                val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                Handler().postDelayed(Runnable {
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"hi")
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?")
                }, 1000)
                try {
                    startActivityForResult(intent, 7009)
                    Handler().postDelayed(Runnable {
                        progress_wheel.stopSpinning()
                    }, 3000)

                } catch (a: ActivityNotFoundException) {
                    a.printStackTrace()
                }
            }
            100->{
                shopContactList("")
                floating_fab.close(true)
                programFab1.colorNormal = mContext.resources.getColor(R.color.delivery_status_green)
                programFab2.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                //programFab3.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                programFab1.setImageResource(R.drawable.ic_tick_float_icon)
                programFab2.setImageResource(R.drawable.ic_tick_float_icon_gray)
                //programFab3.setImageResource(R.drawable.ic_tick_float_icon_gray)
            }
            101 -> {
                shopContactList("","addedDate")
                floating_fab.close(true)
                programFab1.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                programFab2.colorNormal = mContext.resources.getColor(R.color.delivery_status_green)
                //programFab3.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                programFab1.setImageResource(R.drawable.ic_tick_float_icon_gray)
                programFab2.setImageResource(R.drawable.ic_tick_float_icon)
                //programFab3.setImageResource(R.drawable.ic_tick_float_icon_gray)
            }
            /*102 -> {
                floating_fab.close(true)
                programFab1.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                programFab2.colorNormal = mContext.resources.getColor(R.color.colorAccent)
                programFab3.colorNormal = mContext.resources.getColor(R.color.delivery_status_green)
                programFab1.setImageResource(R.drawable.ic_tick_float_icon_gray)
                programFab2.setImageResource(R.drawable.ic_tick_float_icon_gray)
                programFab3.setImageResource(R.drawable.ic_tick_float_icon)
            }*/
        }
    }

    fun syncShopAll(){
        var allUnSyncContact = AppDatabase.getDBInstance()!!.addShopEntryDao().getContatcUnsyncList(false) as ArrayList<AddShopDBModelEntity>
        if(allUnSyncContact.size>0){
            for(i in 0..allUnSyncContact.size-1){
                var obj = allUnSyncContact.get(i)
                var compID = "0"
                if(!obj.companyName.equals("")){
                    compID = AppDatabase.getDBInstance()!!.companyMasterDao().getInfoByName(obj.companyName).company_id.toString()
                }
                obj.companyName_id = compID
                AppDatabase.getDBInstance()!!.addShopEntryDao().updateCompanyID(obj.companyName_id,obj.shop_id)
                if(i==allUnSyncContact.size-1){
                    Handler().postDelayed(Runnable {
                        syncContact(obj,true,true)
                    }, 800)
                }else{
                    Handler().postDelayed(Runnable {
                        syncContact(obj,true,false)
                    }, 800)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 7009){
            try {
                val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                et_search.setText(result!![0].toString())
            }catch (ex:Exception){
                ex.printStackTrace()
            }
        }
    }

    private fun singleLocation() {
        try{
            //progress_wheel.spin()
            SingleShotLocationProvider.requestSingleUpdate(mContext,
                object : SingleShotLocationProvider.LocationCallback {
                    override fun onStatusChanged(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderDisabled(status: String) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onNewLocationAvailable(location: Location) {
                        if(location!=null){
                            locationStr = LocationWizard.getNewLocationName(mContext, location.latitude, location.longitude)
                            location_pinStr = LocationWizard.getPostalCode(mContext, location.latitude, location.longitude)
                            locationStr_lat =location.latitude.toString()
                            locationStr_long =location.longitude.toString()
                        } else{
                            var lloc: Location = Location("")
                            lloc.latitude=Pref.current_latitude.toDouble()
                            lloc.longitude=Pref.current_longitude.toDouble()
                            locationStr_lat =lloc.latitude.toString()
                            locationStr_long =lloc.longitude.toString()
                            locationStr = LocationWizard.getNewLocationName(mContext, lloc.latitude, lloc.longitude)
                            location_pinStr = LocationWizard.getPostalCode(mContext, lloc.latitude, lloc.longitude)
                        }
                        //progress_wheel.stopSpinning()
                    }

                })
        }catch (ex:Exception){
            ex.printStackTrace()
            //progress_wheel.stopSpinning()
        }
    }

    fun showContactNameL(obj:ContactGr){

        doAsync {
            //progress_wheel.spin()
            var contactL : ArrayList<ContactDtls> = ArrayList()
            try{
                (mContext!! as Activity).runOnUiThread {
                    simpleDialogProcess.show()
                    dialog_pg.spin()
                }
                println("tag_cont selected gr ${obj.gr_name} start")
                contactL = AppUtils.getContactsFormGroup(obj.gr_id,obj.gr_name,mContext)
                println("tag_cont selected gr ${obj.gr_name} end")
            }catch (ex:Exception){
                ex.printStackTrace()
                (mContext as DashboardActivity).showSnackMessage("Something went wrong.")
                simpleDialogProcess.dismiss()
            }

            uiThread {
                //progress_wheel.stopSpinning()
                if(contactL.size>0){
                    var myShopContactL = AppDatabase.getDBInstance()!!.addShopEntryDao().getAllOwnerContact() as ArrayList<String>
                    var contactLFinal = contactL.clone() as  ArrayList<ContactDtls>
                    for (i in 0..contactL.size-1){
                        if(myShopContactL.contains(contactL.get(i).number)){
                            contactLFinal.remove(contactL.get(i))
                        }
                    }
                    contactL = contactLFinal
                }else{
                    simpleDialogProcess.dismiss()
                    Toaster.msgShort(mContext,"No contact avaliable")
                }

                Handler().postDelayed(Runnable {
                    simpleDialogProcess.dismiss()
                }, 1500)

                if(contactL.size>0){
                    var contactLTemp : ArrayList<ContactDtls> = contactL.clone() as ArrayList<ContactDtls>
                    //contactLTemp.addAll(contactL)

                    val contactDialog = Dialog(mContext)
                    contactDialog.setCancelable(true)
                    contactDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    contactDialog.setContentView(R.layout.dialog_cont_select)
                    val rvContactL = contactDialog.findViewById(R.id.rv_dialog_cont_list) as RecyclerView
                    val tvHeader = contactDialog.findViewById(R.id.tv_dialog_cont_sel_header) as TextView
                    val submit = contactDialog.findViewById(R.id.tv_dialog_cont_list_submit) as TextView
                    val et_contactNameSearch = contactDialog.findViewById(R.id.et_dialog_contact_search) as AppCustomEditText
                    val cb_selectAll = contactDialog.findViewById(R.id.cb_dialog_cont_select_all) as CheckBox
                    val iv_close = contactDialog.findViewById(R.id.iv_dialog_generic_list_close_icon) as ImageView
                    tvHeader.text = "Select Contact(s)"


                    iv_close.setOnClickListener {
                        contactDialog.dismiss()
                        progress_wheel.stopSpinning()
                    }

                    for(i in 0..contactL.size-1){
                        var ob = contactL.get(i)
                        var  isPresentInDb= (AppDatabase.getDBInstance()!!.addShopEntryDao().getCustomData(ob.name,ob.number) as ArrayList<AddShopDBModelEntity>).size
                        if(isPresentInDb!=0){
                            contactLTemp.remove(ob)
                        }
                    }

                    if(contactLTemp.size>0){
                        var finalL : ArrayList<ContactDtls> = ArrayList()
                        try {
                            if(contactLTemp.size>2){
                                finalL = contactLTemp.sortedByDescending { it.name }.reversed() as ArrayList<ContactDtls>
                            }else{
                                finalL = contactLTemp.clone() as ArrayList<ContactDtls>
                            }
                        }catch (ex:Exception){
                            ex.printStackTrace()
                        }
                        var contactTickL : ArrayList<ContactDtls> = ArrayList()
                        //rvContactL.layoutManager = LinearLayoutManager(mContext)
                        tvHeader.text = "Select Contact(s): ${finalL.size}"
                        adapterContName = AdapterContactName(mContext,finalL,object :AdapterContactName.onClick{
                            override fun onTickUntick(obj: ContactDtls, isTick: Boolean) {
                                if(isTick){
                                    contactTickL.add(obj)
                                    finalL.filter { it.name.equals(obj.name) }.first().isTick = true
                                    tvHeader.text = "Selected Contact(s) : (${contactTickL.size})"


                                }else{
                                    contactTickL.remove(obj)
                                    finalL.filter { it.name.equals(obj.name) }.first().isTick = false
                                    tvHeader.text = "Selected Contact(s) : (${contactTickL.size})"

                                }
                            }
                        },{
                            it
                        })
                        /*   cb_selectAll.setOnClickListener {
                               adapterContName.selectAll()
                           }*/

                        cb_selectAll.setOnCheckedChangeListener { compoundButton, b ->

                            if (compoundButton.isChecked) {
                                adapterContName.selectAll()
                                cb_selectAll.setText("Deselect All")
                            }
                            else{

                                adapterContName.deselectAll()
                                cb_selectAll.setText("Select All")

                            }
                        }

                        et_contactNameSearch.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(p0: Editable?) {
                            }

                            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            }

                            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                adapterContName!!.getFilter().filter(et_contactNameSearch.text.toString().trim())
                            }
                        })

                        submit.setOnClickListener {
                            if(contactTickL.size>0){
                                contactDialog.dismiss()
                                contGrDialog!!.dismiss()
                                submitContact(contactTickL)
                            }else{
                                contactDialog.dismiss()
                                contGrDialog!!.dismiss()
                                Toaster.msgShort(mContext,"Select Contact(s)")
                            }
                        }
                        rvContactL.adapter = adapterContName
                        contactDialog.show()
                        //contGrDialog!!.dismiss()

                    }else{
                        Toaster.msgShort(mContext,"No contact avaliable")
                    }
                }
            }
        }
    }


    fun submitContact(contactTickL : ArrayList<ContactDtls>){
        doAsync {
            progress_wheel.spin()
            for(i in 0..contactTickL.size-1){
                var shopObj:AddShopDBModelEntity = AddShopDBModelEntity()
                val random = Random()
                shopObj.shop_id = Pref.user_id + "_" + System.currentTimeMillis().toString() +  (random.nextInt(999 - 100) + 100).toString()
                shopObj.shopName = contactTickL.get(i).name
                shopObj.ownerName = contactTickL.get(i).name
                shopObj.crm_firstName =  contactTickL.get(i).name
                shopObj.crm_lastName = ""
                shopObj.companyName_id = ""
                shopObj.companyName = ""
                shopObj.jobTitle = ""
                shopObj.ownerEmailId = ""
                shopObj.ownerContactNumber = contactTickL.get(i).number
                shopObj.address = locationStr
                shopObj.pinCode = location_pinStr
                shopObj.shopLat = locationStr_lat.toDouble()
                shopObj.shopLong = locationStr_long.toDouble()
                shopObj.crm_assignTo = Pref.user_name.toString()
                shopObj.crm_assignTo_ID = Pref.user_id
                shopObj.crm_type = ""
                shopObj.crm_type_ID = ""
                shopObj.crm_status=""
                shopObj.crm_status_ID=""
                shopObj.crm_source=""
                shopObj.crm_source_ID=""
                shopObj.crm_reference=""
                shopObj.crm_reference_ID=""
                shopObj.crm_reference_ID_type=""
                shopObj.remarks = ""
                shopObj.amount = ""
                shopObj.crm_stage=""
                shopObj.crm_stage_ID=""
                shopObj.crm_reference = ""
                shopObj.crm_reference_ID = ""
                shopObj.type = "99"
                shopObj.added_date = AppUtils.getCurrentDateTime()
                shopObj.crm_saved_from = "Phone Book"
                shopObj.isUploaded = false
                AppDatabase.getDBInstance()!!.addShopEntryDao().insert(shopObj)

                //shop activity work begin
                val shopActivityEntity = ShopActivityEntity()
                shopActivityEntity.shopid = shopObj.shop_id
                shopActivityEntity.shop_name = shopObj.ownerName
                shopActivityEntity.shop_address = shopObj.address
                shopActivityEntity.date = AppUtils.getCurrentDateForShopActi()
                shopActivityEntity.duration_spent = "00:00:00"
                shopActivityEntity.visited_date = AppUtils.getCurrentISODateTime()
                shopActivityEntity.isUploaded = false
                shopActivityEntity.isVisited = true
                shopActivityEntity.isDurationCalculated = true
                shopActivityEntity.startTimeStamp = System.currentTimeMillis().toString()
                shopActivityEntity.next_visit_date = ""
                shopActivityEntity.distance_travelled = "0"

                val todaysVisitedShop = AppDatabase.getDBInstance()!!.shopActivityDao().getTotalShopVisitedForADay(AppUtils.getCurrentDateForShopActi())
                if (todaysVisitedShop == null || todaysVisitedShop.isEmpty()) {
                    shopActivityEntity.isFirstShopVisited = true

                    if (!TextUtils.isEmpty(Pref.home_latitude) && !TextUtils.isEmpty(Pref.home_longitude)) {
                        shopActivityEntity.distance_from_home_loc = "0.0"
                    } else
                        shopActivityEntity.distance_from_home_loc = "0.0"
                } else {
                    shopActivityEntity.isFirstShopVisited = false
                    shopActivityEntity.distance_from_home_loc = ""
                }
                shopActivityEntity.in_time = AppUtils.getCurrentTimeWithMeredian()
                shopActivityEntity.in_loc = shopObj.actual_address
                shopActivityEntity.agency_name = shopObj.ownerName
                shopActivityEntity.updated_by=Pref.user_id
                shopActivityEntity.updated_on= AppUtils.getCurrentDateForShopActi()
                shopActivityEntity.shop_revisit_uniqKey = ""
                shopActivityEntity.pros_id = ""
                shopActivityEntity.agency_name = ""
                shopActivityEntity.isnewShop = true

                AppDatabase.getDBInstance()!!.shopActivityDao().insertAll(shopActivityEntity)
                //shop activity work end
            }
            uiThread {
                Handler().postDelayed(Runnable {
                    progress_wheel.stopSpinning()

                    if (AppUtils.isOnline(mContext)){
                        syncShopAll()
                    }else{
                        voiceMsg("Contact added successfully")
                        Toaster.msgShort(mContext,"Contact added successfully.")
                        shopContactList("")
                    }
                }, 1500)
            }
        }
    }

    fun shopContactList(searchObj:String,sortBy:String=""){
        var contL : ArrayList<AddShopDBModelEntity> = ArrayList()
        contL = AppDatabase.getDBInstance()!!.addShopEntryDao().getContatcShops() as ArrayList<AddShopDBModelEntity>

        if(sortBy.equals("addedDate")){
            contL = AppDatabase.getDBInstance()!!.addShopEntryDao().getContatcShopsByAddedDate() as ArrayList<AddShopDBModelEntity>
        }

        if(!searchObj.equals("")){
            var searchL = contL.filter { it.shopName.contains(searchObj, ignoreCase = true) || it.ownerContactNumber.contains(searchObj, ignoreCase = true) ||
                    it.crm_stage.contains(searchObj, ignoreCase = true) || it.crm_source.contains(searchObj, ignoreCase = true) ||
                    it.crm_status.contains(searchObj, ignoreCase = true) || it.crm_type.contains(searchObj, ignoreCase = true) || it.crm_saved_from.contains(searchObj, ignoreCase = true)} as ArrayList<AddShopDBModelEntity>
            contL = searchL
        }
        if(contL.size>0){
            tvNodata.visibility = View.GONE
            (mContext as DashboardActivity).setTopBarTitle("Contact(s) : ${contL.size}")
            adapterContactList = AdapterContactList(mContext,contL,et_search.text.toString(),object :AdapterContactList.onClick{
                override fun onCallClick(obj: AddShopDBModelEntity) {
                    IntentActionable.initiatePhoneCall(mContext, obj.ownerContactNumber)
                }

                override fun onWhatsClick(obj: AddShopDBModelEntity) {
                    val url = "https://api.whatsapp.com/send?phone=+91${obj.ownerContactNumber}"
                    try {
                        val pm = mContext.packageManager
                        pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        startActivity(i)
                    } catch (e: PackageManager.NameNotFoundException ) {
                        e.printStackTrace()
                        (mContext as DashboardActivity).showSnackMessage("Whatsapp app not installed in your phone.")
                    }
                    catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        (mContext as DashboardActivity).showSnackMessage("This is not whatsApp no.")
                    }
                }

                override fun onEmailClick(obj: AddShopDBModelEntity) {
                    //IntentActionable.sendMail(mContext, obj.ownerEmailId, "")

                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.setData(Uri.parse("mailto:")) // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("${obj.ownerEmailId}"))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Hi ${obj.ownerName}")
                    intent.putExtra(Intent.EXTRA_TEXT, "Welcome")
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        startActivity(intent)
                    }
                }

                override fun onInfoClick(obj: AddShopDBModelEntity) {
                    saveCallHisToDB(obj)
                }

                override fun onSyncUnsyncClick(obj: AddShopDBModelEntity) {
                    if(AppUtils.isOnline(mContext)){
                        if(!obj.companyName.equals("")){
                            if(AppDatabase.getDBInstance()!!.companyMasterDao().getInfoByName(obj.companyName).isUploaded == false){
                                syncCompanyMaster(obj.companyName)
                            }
                        }
                        Handler().postDelayed(Runnable {
                            var compID = "0"
                            if(!obj.companyName.equals("")){
                                compID = AppDatabase.getDBInstance()!!.companyMasterDao().getInfoByName(obj.companyName).company_id.toString()
                            }
                            obj.companyName_id = compID
                            AppDatabase.getDBInstance()!!.addShopEntryDao().updateCompanyID(obj.companyName_id,obj.shop_id)
                            syncContact(obj)
                        }, 1900)
                    }else{
                        (this as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
                    }
                }
                override fun onEditClick(obj: AddShopDBModelEntity) {
                    (mContext as DashboardActivity).loadFragment(FragType.ContactsAddFrag, true, obj.shop_id)
                }
                override fun onUpdateStatusClick(obj: AddShopDBModelEntity) {
                    UpdateShopStatusDialog.getInstance(obj.shopName!!, "Cancel", "Confirm", true,"",obj.user_id.toString()!!,"Select Contact Status",
                        object : UpdateShopStatusDialog.OnDSButtonClickListener {
                            override fun onLeftClick() {

                            }
                            override fun onRightClick(status: String) {
                                if(!status.equals("")){
                                    if(status.equals("Inactive")){
                                        AppDatabase.getDBInstance()!!.addShopEntryDao().updateShopStatus(obj.shop_id,"0")
                                        AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsEditUploaded(0,obj.shop_id)
                                        if(AppUtils.isOnline(mContext)) {
                                            convertToReqAndApiCallForShopStatus(AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(obj.shop_id!!))
                                        } else{
                                            shopContactList("")
                                            (mContext as DashboardActivity).showSnackMessage("Status updated successfully")
                                        }
                                    }
                                    if(status.equals("Active")){
                                        shopContactList("")
                                        AppDatabase.getDBInstance()!!.addShopEntryDao().updateShopStatus(obj.shop_id,"1")
                                    }
                                }
                            }
                        }).show((mContext as DashboardActivity).supportFragmentManager, "")
                }

                override fun onDtlsShareClick(obj: AddShopDBModelEntity) {
                    generateContactDtlsPdf(obj)
                }

                override fun onAutoActivityClick(obj: AddShopDBModelEntity) {

                    selectedShopIdForActivity = obj.shop_id

                    val datePicker = DatePickerDialog(mContext, R.style.DatePickerTheme, dates, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                    datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
                    datePicker.show()
                }
            })
            rvContactL.adapter = adapterContactList
            rvContactL.visibility = View.VISIBLE
        }else{
            (mContext as DashboardActivity).setTopBarTitle("Contact(s)")
            tvNodata.visibility = View.VISIBLE
            rvContactL.visibility = View.GONE
        }
    }

    private fun syncCompanyMaster(compName:String){
        progress_wheel.spin()
        //var unsyncL = AppDatabase.getDBInstance()!!.companyMasterDao().getUnSync(false,compName)

        var unsyncL = AppDatabase.getDBInstance()!!.companyMasterDao().getUnSyncList(false) as ArrayList<CompanyMasterEntity>
        var compReq :CompanyReqData = CompanyReqData()
        if(unsyncL.size>0){
            compReq.created_by = Pref.user_id.toString()
            compReq.session_token = Pref.session_token.toString()
            for(i in 0..unsyncL.size-1){
                compReq.company_name_list.add(CompanyName(unsyncL.get(i).company_name.toString()))
            }
            val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                repository.saveCompanyMasterNw(compReq)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val resp = result as BaseResponse
                        progress_wheel.stopSpinning()
                        if(resp.status == NetworkConstant.SUCCESS){
                            callCRMCompanyMasterApi()
                        }else{
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        }
                    }, { error ->
                        error.printStackTrace()
                        progress_wheel.stopSpinning()
                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        Timber.d("AddShop err : ${error.message}")
                    })
            )
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun callCRMCompanyMasterApi(){
        progress_wheel.spin()
        val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
        BaseActivity.compositeDisposable.add(
            repository.callCompanyMaster(Pref.session_token.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val resp = result as ContactMasterRes
                    progress_wheel.stopSpinning()
                    if(resp.status == NetworkConstant.SUCCESS){
                        AppDatabase.getDBInstance()?.companyMasterDao()?.deleteAll()
                        AppDatabase.getDBInstance()?.companyMasterDao()?.insertAll(resp.company_list)
                    }else{
                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                    }
                }, { error ->
                    error.printStackTrace()
                    progress_wheel.stopSpinning()
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                    Timber.d("AddShop err : ${error.message}")
                })
        )

    }

    private fun voiceMsg(msg: String) {
        if (Pref.isVoiceEnabledForAttendanceSubmit) {
            val speechStatus = (mContext as DashboardActivity).textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null)
            if (speechStatus == TextToSpeech.ERROR)
                Log.e("Add Day Start", "TTS error in converting Text to Speech!");
        }
    }

    fun saveCallHisToDB(obj:AddShopDBModelEntity){
        try{
            println("cont_frag call his saveCallHisToDB")
            progress_wheel.spin()
            doAsync {
                //var callHisL = AppUtils.obtenerDetallesLlamadas(mContext) as java.util.ArrayList<AppUtils.Companion.PhoneCallDtls>
                var callHisL = AppUtils.obtenerDetallesLlamadasByNumber(mContext,obj.ownerContactNumber) as java.util.ArrayList<AppUtils.Companion.PhoneCallDtls>
                println("cont_frag call his size ${callHisL.size}")
                if(callHisL.size>0){
                    for(i in 0..callHisL.size-1){
                        try{
                            var obj: CallHisEntity = CallHisEntity()
                            var callNo = if(callHisL.get(i).number!!.length>10) callHisL.get(i).number!!.replace("+","").removeRange(0,2) else callHisL.get(i).number!!
                            var isMyShop = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByPhone(callNo) as ArrayList<AddShopDBModelEntity>
                            if(isMyShop.size>0){
                                obj.apply {
                                    shop_id = isMyShop.get(0).shop_id.toString()
                                    call_number = callNo
                                    call_date = callHisL.get(i).callDateTime!!.split(" ").get(0)
                                    call_time = callHisL.get(i).callDateTime!!.split(" ").get(1)
                                    call_date_time = callHisL.get(i).callDateTime!!
                                    call_type = callHisL.get(i).type!!
                                    if(call_type.equals("MISSED",ignoreCase = true)){
                                        call_duration_sec = "0"
                                    }else{
                                        call_duration_sec = callHisL.get(i).callDuration!!
                                    }
                                    call_duration = AppUtils.getMMSSfromSeconds(call_duration_sec.toInt())
                                }
                                var isPresent = (AppDatabase.getDBInstance()!!.callhisDao().getFilterData(obj.call_number,obj.call_date,obj.call_time,obj.call_type,obj.call_duration_sec) as ArrayList<CallHisEntity>).size
                                if(isPresent==0){
                                    println("cont_frag call his insert ${obj.call_number}")
                                    Timber.d("tag_log_insert ${obj.call_number} ${obj.call_duration}")
                                    AppDatabase.getDBInstance()!!.callhisDao().insert(obj)
                                }
                            }
                        }catch (ex:Exception){
                            ex.printStackTrace()
                            println("cont_frag call his err inner ${ex.message}")
                        }
                    }
                }
                uiThread {
                    progress_wheel.stopSpinning()
                    showCallInfo(obj)
                }
            }
        }catch (ex:Exception){
            ex.printStackTrace()
            println("cont_frag call his err ${ex.message}")
        }
    }

    fun showCallInfo(obj:AddShopDBModelEntity){
        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_info_1)
        val dialogHeader = simpleDialog.findViewById(R.id.tv_dialog_info_1_header) as TextView
        val rvList = simpleDialog.findViewById(R.id.rv_dialog_info_1_info) as RecyclerView
        val dialogCross = simpleDialog.findViewById(R.id.iv_dialog_info_1_cross) as ImageView
        val tv_noData = simpleDialog.findViewById(R.id.tv_dialog_info_1_info_na) as TextView
        val tv_totalCallDuration = simpleDialog.findViewById(R.id.tv_dialog_info_1_info_total_call_duraton) as TextView
        val tv_totalCallCount = simpleDialog.findViewById(R.id.tv_dialog_info_1_info_total_call_count) as TextView
        val ll_countRoot = simpleDialog.findViewById(R.id.ll_dialog_info_1_count_root) as LinearLayout

        dialogHeader.text = obj.ownerName

        dialogCross.setOnClickListener({ view ->
            simpleDialog.cancel()
        })
        simpleDialog.show()

        var callL = AppDatabase.getDBInstance()!!.callhisDao().getCallListByPhone(obj.ownerContactNumber!!) as ArrayList<CallHisEntity>
        //var callL = AppDatabase.getDBInstance()!!.callhisDao().getCallListByPhone("9830916971") as ArrayList<CallHisEntity>
        if(callL.size>0){

            dialogHeader.text = obj.ownerName+" (Count : ${callL.size})"

            tv_noData.visibility = View.GONE
            rvList.visibility = View.VISIBLE
            ll_countRoot.visibility = View.VISIBLE
            adapterCallLogL = AdapterCallLogL(mContext,callL,false,object :AdapterCallLogL.onClick{
                override fun onSyncClick(obj: CallHisEntity) {

                }
            })
            rvList.adapter=adapterCallLogL

            var totalCallDurationInSec: Int = callL.sumOf { it.call_duration_sec.toInt() }
            tv_totalCallDuration.text = "Total Call duration : ${AppUtils.getMMSSfromSeconds(totalCallDurationInSec.toInt())}"
            tv_totalCallCount.text = "Total Call count : ${callL.size}"

        }else{
            tv_noData.visibility = View.VISIBLE
            rvList.visibility = View.GONE
            ll_countRoot.visibility = View.GONE
        }
    }

    private fun syncContact(shopObj: AddShopDBModelEntity,isFromSyncAll:Boolean = false,isFromSyncAllLast:Boolean=false){
        var addShopRequestData: AddShopRequestData = AddShopRequestData()
        addShopRequestData.user_id = Pref.user_id
        addShopRequestData.session_token = Pref.session_token
        addShopRequestData.shop_id = shopObj.shop_id
        addShopRequestData.shop_name = shopObj.shopName
        addShopRequestData.address = shopObj.address
        addShopRequestData.actual_address = shopObj.address
        addShopRequestData.pin_code = shopObj.pinCode
        addShopRequestData.type = shopObj.type
        addShopRequestData.shop_lat = shopObj.shopLat.toString()
        addShopRequestData.shop_long = shopObj.shopLong.toString()
        addShopRequestData.owner_email = shopObj.ownerEmailId.toString()
        addShopRequestData.owner_name = shopObj.shopName.toString()
        addShopRequestData.owner_contact_no = shopObj.ownerContactNumber.toString()
        addShopRequestData.amount = shopObj.amount.toString()

        addShopRequestData.shop_firstName=  shopObj.crm_firstName.toString()
        addShopRequestData.shop_lastName=  shopObj.crm_lastName.toString()
        addShopRequestData.crm_companyID=  if(shopObj.companyName_id.equals("")) "0" else shopObj.companyName_id
        addShopRequestData.crm_jobTitle=  shopObj.jobTitle
        addShopRequestData.crm_typeID=  if(shopObj.crm_type_ID.equals("")) "0" else shopObj.crm_type_ID
        addShopRequestData.crm_statusID=  if(shopObj.crm_status_ID.equals("")) "0" else shopObj.crm_status_ID
        addShopRequestData.crm_sourceID= if(shopObj.crm_source_ID.equals("")) "0" else shopObj.crm_source_ID
        addShopRequestData.crm_reference=  shopObj.crm_reference
        addShopRequestData.crm_referenceID=  if(shopObj.crm_reference_ID.equals("")) "0" else shopObj.crm_reference_ID
        addShopRequestData.crm_referenceID_type=  shopObj.crm_reference_ID_type
        addShopRequestData.crm_stage_ID=  if(shopObj.crm_stage_ID.equals("")) "0" else shopObj.crm_stage_ID
        addShopRequestData.assign_to=  shopObj.crm_assignTo_ID
        addShopRequestData.saved_from_status=  shopObj.crm_saved_from

        progress_wheel.spin()
        val repository = AddShopRepositoryProvider.provideAddShopWithoutImageRepository()
        BaseActivity.compositeDisposable.add(
            repository.addShop(addShopRequestData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val addShopResult = result as AddShopResponse
                    Timber.d("AddShop : " + ", SHOP: " + addShopRequestData.shop_name + ", RESPONSE:" + result.message)
                    progress_wheel.stopSpinning()
                    if (addShopResult.status == NetworkConstant.SUCCESS) {
                        AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsUploaded(true, addShopRequestData.shop_id)
                        if(isFromSyncAll){
                            if(isFromSyncAllLast){
                                Toaster.msgShort(mContext,"Sync Successfully done. Thanks.")
                                shopContactList("")
                            }
                        }else{
                            Toaster.msgShort(mContext,"Sync Successfully done. Thanks.")
                            shopContactList("")
                        }

                    }
                    else {
                        (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                    }
                }, { error ->
                    progress_wheel.stopSpinning()
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                    Timber.d("AddShop err : ${error.message}")
                })
        )

    }

    //Refresh contact list
    fun checkModifiedShop() {
        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }
        progress_wheel.spin()
        callCRMCompanyMasterApiRefresh()
    }

    fun callCRMCompanyMasterApiRefresh() {
            val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                repository.callCompanyMaster(Pref.session_token.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val resp = result as ContactMasterRes
                        if(resp.status == NetworkConstant.SUCCESS){
                            AppDatabase.getDBInstance()?.companyMasterDao()?.deleteAll()
                            AppDatabase.getDBInstance()?.companyMasterDao()?.insertAll(resp.company_list)
                            callCRMTypeMasterAPI()
                        }else{
                            callCRMTypeMasterAPI()
                        }
                    }, { error ->
                        error.printStackTrace()
                        callCRMTypeMasterAPI()
                    })
            )
    }

    fun callCRMTypeMasterAPI(){
            val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                repository.callTypeMaster(Pref.session_token.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val resp = result as TypeMasterRes
                        if(resp.status == NetworkConstant.SUCCESS){
                            AppDatabase.getDBInstance()?.typeMasterDao()?.deleteAll()
                            AppDatabase.getDBInstance()?.typeMasterDao()?.insertAll(resp.type_list)
                            callCRMStatusMasterAPI()

                        }else{
                            callCRMStatusMasterAPI()
                        }
                    }, { error ->
                        error.printStackTrace()
                        callCRMStatusMasterAPI()
                    })
            )
    }

    fun callCRMStatusMasterAPI(){
            val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                repository.callStatusMaster(Pref.session_token.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val resp = result as StatusMasterRes
                        if(resp.status == NetworkConstant.SUCCESS){
                            AppDatabase.getDBInstance()?.statusMasterDao()?.deleteAll()
                            AppDatabase.getDBInstance()?.statusMasterDao()?.insertAll(resp.status_list)
                            callCRMSourceMasterAPI()

                        }else{
                            callCRMSourceMasterAPI()
                        }
                    }, { error ->
                        error.printStackTrace()
                        callCRMSourceMasterAPI()
                    })
            )
    }

    fun callCRMSourceMasterAPI(){
            val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                repository.callSourceMaster(Pref.session_token.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val resp = result as SourceMasterRes
                        if(resp.status == NetworkConstant.SUCCESS){
                            AppDatabase.getDBInstance()?.sourceMasterDao()?.deleteAll()
                            AppDatabase.getDBInstance()?.sourceMasterDao()?.insertAll(resp.source_list)
                            callCRMStageMasterAPI()
                        }else{
                            callCRMStageMasterAPI()
                        }
                    }, { error ->
                        error.printStackTrace()
                        callCRMStageMasterAPI()
                    })
            )
    }

    fun callCRMStageMasterAPI(){
            val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                repository.callStageMaster(Pref.session_token.toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val resp = result as StageMasterRes
                        if(resp.status == NetworkConstant.SUCCESS){
                            AppDatabase.getDBInstance()?.stageMasterDao()?.deleteAll()
                            AppDatabase.getDBInstance()?.stageMasterDao()?.insertAll(resp.stage_list)
                            getShopListApiSync()
                        }else{
                            getShopListApiSync()
                        }
                    }, { error ->
                        error.printStackTrace()
                        getShopListApiSync()
                    })
            )

    }

    fun getShopListApiSync() {
        val repository = ShopListRepositoryProvider.provideShopListRepository()
      //  progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
            repository.getShopList(Pref.session_token!!, Pref.user_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val shopList = result as ShopListResponse
                    if (shopList.status == NetworkConstant.SUCCESS) {
                        progress_wheel.stopSpinning()
                        if (shopList.data!!.shop_list == null || shopList.data!!.shop_list!!.isEmpty()) {

                        } else {
                            convertToShopListSetAdapter(shopList.data!!.shop_list!!)
                        }
                    }else {
                        progress_wheel.stopSpinning()
                    }
                }, { error ->
                    error.printStackTrace()
                    progress_wheel.stopSpinning()

                })
        )
    }


    fun convertToShopListSetAdapter(shop_list: List<ShopData>) {
        progress_wheel.spin()
        AppDatabase.getDBInstance()!!.addShopEntryDao().deleteAll()
        val list: MutableList<AddShopDBModelEntity> = ArrayList()
        val shopObj = AddShopDBModelEntity()
        doAsync {
            for (i in 0 until shop_list.size) {
                shopObj.shop_id = shop_list[i].shop_id
                shopObj.shopName = shop_list[i].shop_name
                shopObj.shopImageLocalPath = shop_list[i].Shop_Image
                try {
                    shopObj.shopLat = shop_list[i].shop_lat!!.toDouble()
                    shopObj.shopLong = shop_list[i].shop_long!!.toDouble()
                }catch (ex:Exception){
                    ex.printStackTrace()
                    shopObj.shopLat = 0.0
                    shopObj.shopLong = 0.0
                }
                shopObj.duration = "0"
                shopObj.endTimeStamp = "0"
                shopObj.timeStamp = "0"
                shopObj.dateOfBirth = shop_list[i].dob
                shopObj.dateOfAniversary = shop_list[i].date_aniversary
                //shopObj.visited = true
                shopObj.visitDate = AppUtils.getCurrentDate()
                //shopObj.totalVisitCount = "1"
                if (shop_list[i].total_visit_count == "0")
                    shopObj.totalVisitCount = "1"
                else
                    shopObj.totalVisitCount = shop_list[i].total_visit_count
                shopObj.address = shop_list[i].address
                shopObj.ownerEmailId = shop_list[i].owner_email
                shopObj.ownerContactNumber = shop_list[i].owner_contact_no
                shopObj.pinCode = shop_list[i].pin_code
                shopObj.isUploaded = true

                if(shop_list[i].owner_name==null){
                    shopObj.ownerName = shop_list[i].shop_name
                }else{
                    shopObj.ownerName = shop_list[i].owner_name
                }
                shopObj.user_id = Pref.user_id
                shopObj.orderValue = 0
                shopObj.type = shop_list[i].type
                shopObj.assigned_to_dd_id = shop_list[i].assigned_to_dd_id
                shopObj.assigned_to_pp_id = shop_list[i].assigned_to_pp_id
                //shopObj.lastVisitedDate = AppUtils.getCurrentDate()

                if (shop_list[i].last_visit_date!!.contains(".")) {
                    shopObj.lastVisitedDate =
                        AppUtils.changeAttendanceDateFormat(shop_list[i].last_visit_date!!.split(".")[0])
                }
                else {
                    shopObj.lastVisitedDate = AppUtils.changeAttendanceDateFormat(shop_list[i].last_visit_date!!)
                }
                if (shopObj.lastVisitedDate == AppUtils.getCurrentDateChanged()) {
                    shopObj.visited = true
                }
                else {
                    shopObj.visited = false
                }

                shopObj.is_otp_verified = shop_list[i].is_otp_verified
                shopObj.added_date = shop_list[i].added_date

                if (shop_list[i].amount == null || shop_list[i].amount == "0.00") {
                    shopObj.amount = ""
                }
                else {
                    shopObj.amount = shop_list[i].amount
                }

                if (shop_list[i].entity_code == null) {
                    shopObj.entity_code = ""
                }
                else {
                    shopObj.entity_code = shop_list[i].entity_code
                }

                if (shop_list[i].area_id == null) {
                    shopObj.area_id = ""
                }
                else {
                    shopObj.area_id = shop_list[i].area_id
                }

                if (TextUtils.isEmpty(shop_list[i].model_id)) {
                    shopObj.model_id = ""
                }
                else {
                    shopObj.model_id = shop_list[i].model_id
                }

                if (TextUtils.isEmpty(shop_list[i].primary_app_id)) {
                    shopObj.primary_app_id = ""
                }
                else {
                    shopObj.primary_app_id = shop_list[i].primary_app_id
                }

                if (TextUtils.isEmpty(shop_list[i].secondary_app_id)) {
                    shopObj.secondary_app_id = ""
                }
                else {
                    shopObj.secondary_app_id = shop_list[i].secondary_app_id
                }

                if (TextUtils.isEmpty(shop_list[i].lead_id)) {
                    shopObj.lead_id = ""
                }
                else {
                    shopObj.lead_id = shop_list[i].lead_id
                }

                if (TextUtils.isEmpty(shop_list[i].stage_id)) {
                    shopObj.stage_id = ""
                }
                else {
                    shopObj.stage_id = shop_list[i].stage_id
                }

                if (TextUtils.isEmpty(shop_list[i].funnel_stage_id)) {
                    shopObj.funnel_stage_id = ""
                }
                else {
                    shopObj.funnel_stage_id = shop_list[i].funnel_stage_id
                }

                if (TextUtils.isEmpty(shop_list[i].booking_amount)) {
                    shopObj.booking_amount = ""
                }
                else {
                    shopObj.booking_amount = shop_list[i].booking_amount
                }

                if (TextUtils.isEmpty(shop_list[i].type_id)) {
                    shopObj.type_id = ""
                }
                else {
                    shopObj.type_id = shop_list[i].type_id
                }

                shopObj.family_member_dob = shop_list[i].family_member_dob
                shopObj.director_name = shop_list[i].director_name
                shopObj.person_name = shop_list[i].key_person_name
                shopObj.person_no = shop_list[i].phone_no
                shopObj.add_dob = shop_list[i].addtional_dob
                shopObj.add_doa = shop_list[i].addtional_doa

                shopObj.doc_degree = shop_list[i].degree
                shopObj.doc_family_dob = shop_list[i].doc_family_member_dob
                shopObj.specialization = shop_list[i].specialization
                shopObj.patient_count = shop_list[i].average_patient_per_day
                shopObj.category = shop_list[i].category
                shopObj.doc_address = shop_list[i].doc_address
                shopObj.doc_pincode = shop_list[i].doc_pincode
                shopObj.chamber_status = shop_list[i].is_chamber_same_headquarter.toInt()
                shopObj.remarks = shop_list[i].is_chamber_same_headquarter_remarks
                shopObj.chemist_name = shop_list[i].chemist_name
                shopObj.chemist_address = shop_list[i].chemist_address
                shopObj.chemist_pincode = shop_list[i].chemist_pincode
                shopObj.assistant_name = shop_list[i].assistant_name
                shopObj.assistant_no = shop_list[i].assistant_contact_no
                shopObj.assistant_dob = shop_list[i].assistant_dob
                shopObj.assistant_doa = shop_list[i].assistant_doa
                shopObj.assistant_family_dob = shop_list[i].assistant_family_dob

                if (TextUtils.isEmpty(shop_list[i].entity_id)) {
                    shopObj.entity_id = ""
                }
                else {
                    shopObj.entity_id = shop_list[i].entity_id
                }

                if (TextUtils.isEmpty(shop_list[i].party_status_id)) {
                    shopObj.party_status_id = ""
                }
                else {
                    shopObj.party_status_id = shop_list[i].party_status_id
                }

                if (TextUtils.isEmpty(shop_list[i].retailer_id)) {
                    shopObj.retailer_id = ""
                }
                else {
                    shopObj.retailer_id = shop_list[i].retailer_id
                }

                if (TextUtils.isEmpty(shop_list[i].dealer_id)) {
                    shopObj.dealer_id = ""
                }
                else {
                    shopObj.dealer_id = shop_list[i].dealer_id
                }
                if (TextUtils.isEmpty(shop_list[i].beat_id)) {
                    shopObj.beat_id = ""
                }
                else {
                    shopObj.beat_id = shop_list[i].beat_id
                }

                if (TextUtils.isEmpty(shop_list[i].account_holder)) {
                    shopObj.account_holder = ""
                }
                else {
                    shopObj.account_holder = shop_list[i].account_holder
                }

                if (TextUtils.isEmpty(shop_list[i].account_no)) {
                    shopObj.account_no = ""
                }
                else {
                    shopObj.account_no = shop_list[i].account_no
                }

                if (TextUtils.isEmpty(shop_list[i].bank_name)) {
                    shopObj.bank_name = ""
                }
                else {
                    shopObj.bank_name = shop_list[i].bank_name
                }

                if (TextUtils.isEmpty(shop_list[i].ifsc_code)) {
                    shopObj.ifsc_code = ""
                }
                else {
                    shopObj.ifsc_code = shop_list[i].ifsc_code
                }

                if (TextUtils.isEmpty(shop_list[i].upi)) {
                    shopObj.upi_id = ""
                }
                else {
                    shopObj.upi_id = shop_list[i].upi
                }
                if (TextUtils.isEmpty(shop_list[i].assigned_to_shop_id)) {
                    shopObj.assigned_to_shop_id = ""
                }
                else {
                    shopObj.assigned_to_shop_id = shop_list[i].assigned_to_shop_id
                }

                shopObj.project_name=shop_list[i].project_name
                shopObj.landline_number=shop_list[i].landline_number
                shopObj.agency_name=shop_list[i].agency_name
                /*10-2-2022*/
                shopObj.alternateNoForCustomer=shop_list[i].alternateNoForCustomer
                shopObj.whatsappNoForCustomer=shop_list[i].whatsappNoForCustomer
                shopObj.isShopDuplicate=shop_list[i].isShopDuplicate

                shopObj.purpose=shop_list[i].purpose

                //start AppV 4.2.2 tufan    20/09/2023 FSSAI Lic No Implementation 26813
                try {
                    shopObj.FSSAILicNo = shop_list[i].FSSAILicNo
                }catch (ex:Exception){
                    ex.printStackTrace()
                    shopObj.FSSAILicNo = ""
                }
//end AppV 4.2.2 tufan    20/09/2023 FSSAI Lic No Implementation 26813


                /*GSTIN & PAN NUMBER*/
                shopObj.gstN_Number=shop_list[i].GSTN_Number
                shopObj.shopOwner_PAN=shop_list[i].ShopOwner_PAN

                //crm details
                shopObj.companyName_id = if(shop_list[i].crm_companyID.isNullOrEmpty()) "" else shop_list[i].crm_companyID
                shopObj.companyName = if(shop_list[i].crm_companyName.isNullOrEmpty()) "" else shop_list[i].crm_companyName
                shopObj.jobTitle = if(shop_list[i].crm_jobTitle.isNullOrEmpty()) "" else shop_list[i].crm_jobTitle
                shopObj.crm_type_ID = if(shop_list[i].crm_typeID.isNullOrEmpty()) "" else shop_list[i].crm_typeID
                shopObj.crm_type = if(shop_list[i].crm_type.isNullOrEmpty()) "" else shop_list[i].crm_type
                shopObj.crm_status_ID = if(shop_list[i].crm_statusID.isNullOrEmpty()) "" else shop_list[i].crm_statusID
                shopObj.crm_status = if(shop_list[i].crm_status.isNullOrEmpty()) "" else shop_list[i].crm_status
                shopObj.crm_source_ID = if(shop_list[i].crm_sourceID.isNullOrEmpty()) "" else shop_list[i].crm_sourceID
                shopObj.crm_source = if(shop_list[i].crm_source.isNullOrEmpty()) "" else shop_list[i].crm_source
                shopObj.crm_reference = if(shop_list[i].crm_reference.isNullOrEmpty()) "" else shop_list[i].crm_reference
                shopObj.crm_reference_ID = if(shop_list[i].crm_referenceID.isNullOrEmpty()) "" else shop_list[i].crm_referenceID
                shopObj.crm_reference_ID_type = if(shop_list[i].crm_referenceID_type.isNullOrEmpty()) "" else shop_list[i].crm_referenceID_type
                shopObj.crm_stage_ID = if(shop_list[i].crm_stage_ID.isNullOrEmpty()) "" else shop_list[i].crm_stage_ID
                shopObj.crm_stage = if(shop_list[i].crm_stage.isNullOrEmpty()) "" else shop_list[i].crm_stage
                shopObj.crm_assignTo = if(shop_list[i].assign_to.isNullOrEmpty()) "" else shop_list[i].assign_to
                shopObj.crm_saved_from = if(shop_list[i].saved_from_status.isNullOrEmpty()) "" else shop_list[i].saved_from_status
                shopObj.crm_firstName = if(shop_list[i].shop_firstName.isNullOrEmpty()) "" else shop_list[i].shop_firstName
                shopObj.crm_lastName = if(shop_list[i].shop_lastName.isNullOrEmpty()) "" else shop_list[i].shop_lastName

                list.add(shopObj)
                AppDatabase.getDBInstance()!!.addShopEntryDao().insert(shopObj)
            }
            uiThread {
                progress_wheel.stopSpinning()
            }
        }
    }

    private fun convertToReqAndApiCallForShopStatus(addShopData: AddShopDBModelEntity) {
        val addShopReqData = AddShopRequestData()
        addShopReqData.session_token = Pref.session_token
        addShopReqData.address = addShopData.address
        addShopReqData.actual_address = addShopData.address
        addShopReqData.owner_contact_no = addShopData.ownerContactNumber
        addShopReqData.owner_email = addShopData.ownerEmailId
        addShopReqData.owner_name = addShopData.ownerName
        addShopReqData.pin_code = addShopData.pinCode
        addShopReqData.shop_lat = addShopData.shopLat.toString()
        addShopReqData.shop_long = addShopData.shopLong.toString()
        addShopReqData.shop_name = addShopData.shopName.toString()
        addShopReqData.shop_id = addShopData.shop_id
        addShopReqData.added_date = ""
        addShopReqData.user_id = Pref.user_id
        addShopReqData.type = addShopData.type
        addShopReqData.assigned_to_pp_id = addShopData.assigned_to_pp_id
        addShopReqData.assigned_to_dd_id = addShopData.assigned_to_dd_id
        /*addShopReqData.dob = addShopData.dateOfBirth
        addShopReqData.date_aniversary = addShopData.dateOfAniversary*/
        addShopReqData.amount = addShopData.amount
        addShopReqData.area_id = addShopData.area_id
        /*val addShop = AddShopRequest()
        addShop.data = addShopReqData*/

        addShopReqData.model_id = addShopData.model_id
        addShopReqData.primary_app_id = addShopData.primary_app_id
        addShopReqData.secondary_app_id = addShopData.secondary_app_id
        addShopReqData.lead_id = addShopData.lead_id
        addShopReqData.stage_id = addShopData.stage_id
        addShopReqData.funnel_stage_id = addShopData.funnel_stage_id
        addShopReqData.booking_amount = addShopData.booking_amount
        addShopReqData.type_id = addShopData.type_id

        if (!TextUtils.isEmpty(addShopData.dateOfBirth))
            addShopReqData.dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.dateOfBirth)

        if (!TextUtils.isEmpty(addShopData.dateOfAniversary))
            addShopReqData.date_aniversary =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.dateOfAniversary)

        addShopReqData.director_name = addShopData.director_name
        addShopReqData.key_person_name = addShopData.person_name
        addShopReqData.phone_no = addShopData.person_no

        if (!TextUtils.isEmpty(addShopData.family_member_dob))
            addShopReqData.family_member_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.family_member_dob)

        if (!TextUtils.isEmpty(addShopData.add_dob))
            addShopReqData.addtional_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.add_dob)

        if (!TextUtils.isEmpty(addShopData.add_doa))
            addShopReqData.addtional_doa =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.add_doa)

        addShopReqData.specialization = addShopData.specialization
        addShopReqData.category = addShopData.category
        addShopReqData.doc_address = addShopData.doc_address
        addShopReqData.doc_pincode = addShopData.doc_pincode
        addShopReqData.is_chamber_same_headquarter = addShopData.chamber_status.toString()
        addShopReqData.is_chamber_same_headquarter_remarks = addShopData.remarks
        addShopReqData.chemist_name = addShopData.chemist_name
        addShopReqData.chemist_address = addShopData.chemist_address
        addShopReqData.chemist_pincode = addShopData.chemist_pincode
        addShopReqData.assistant_contact_no = addShopData.assistant_no
        addShopReqData.average_patient_per_day = addShopData.patient_count
        addShopReqData.assistant_name = addShopData.assistant_name

        if (!TextUtils.isEmpty(addShopData.doc_family_dob))
            addShopReqData.doc_family_member_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.doc_family_dob)

        if (!TextUtils.isEmpty(addShopData.assistant_dob))
            addShopReqData.assistant_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.assistant_dob)

        if (!TextUtils.isEmpty(addShopData.assistant_doa))
            addShopReqData.assistant_doa =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.assistant_doa)

        if (!TextUtils.isEmpty(addShopData.assistant_family_dob))
            addShopReqData.assistant_family_dob =
                AppUtils.changeAttendanceDateFormatToCurrent(addShopData.assistant_family_dob)

        addShopReqData.entity_id = addShopData.entity_id
        addShopReqData.party_status_id = addShopData.party_status_id
        addShopReqData.retailer_id = addShopData.retailer_id
        addShopReqData.dealer_id = addShopData.dealer_id
        addShopReqData.beat_id = addShopData.beat_id
        addShopReqData.assigned_to_shop_id = addShopData.assigned_to_shop_id
        //addShopReqData.actual_address = addShopData.actual_address

        if(addShopData.shopStatusUpdate.equals("0"))
            addShopReqData.shopStatusUpdate = addShopData.shopStatusUpdate
        else
            addShopReqData.shopStatusUpdate = "1"

        // contact module
        try{
            addShopReqData.address = addShopData.address
            addShopReqData.actual_address = addShopData.address
            addShopReqData.shop_firstName= addShopData.crm_firstName
            addShopReqData.shop_lastName=  addShopData.crm_lastName
            addShopReqData.crm_companyID=  if(addShopData.companyName_id.equals("")) "0" else addShopData.companyName_id
            addShopReqData.crm_jobTitle=  addShopData.jobTitle
            addShopReqData.crm_typeID=  if(addShopData.crm_type_ID.equals("")) "0" else addShopData.crm_type_ID
            addShopReqData.crm_statusID=  if(addShopData.crm_status_ID.equals("")) "0" else addShopData.crm_status_ID
            addShopReqData.crm_sourceID= if(addShopData.crm_source_ID.equals("")) "0" else addShopData.crm_source_ID
            addShopReqData.crm_reference=  addShopData.crm_reference
            addShopReqData.crm_referenceID=  if(addShopData.crm_reference_ID.equals("")) "0" else addShopData.crm_reference_ID
            addShopReqData.crm_referenceID_type=  addShopData.crm_reference_ID_type
            addShopReqData.crm_stage_ID=  if(addShopData.crm_stage_ID.equals("")) "0" else addShopData.crm_stage_ID
            addShopReqData.assign_to=  addShopData.crm_assignTo_ID
            addShopReqData.saved_from_status=  addShopData.crm_saved_from
        }catch (ex:Exception){
            ex.printStackTrace()
            Timber.d("Logout edit sync err ${ex.message}")
        }

        callEditShopApiForShopStatus(addShopReqData)
    }

    private fun callEditShopApiForShopStatus(addShopReqData: AddShopRequestData) {
        val index = addShopReqData.shop_id!!.indexOf("_")
        progress_wheel.spin()
            val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
            BaseActivity.compositeDisposable.add(
                repository.editShop(addShopReqData)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val addShopResult = result as AddShopResponse
                        Timber.d("Edit Shop : " + ", SHOP: " + addShopReqData.shop_name + ", RESPONSE:" + result.message)
                        if (addShopResult.status == NetworkConstant.SUCCESS) {
                            AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsEditUploaded(1, addShopReqData.shop_id)
                            shopContactList("")
                            progress_wheel.stopSpinning()
                            (mContext as DashboardActivity).showSnackMessage("Status updated successfully")
                        }
                    }, { error ->
                        error.printStackTrace()
                        progress_wheel.stopSpinning()
                    })
            )
    }

    fun generateContactDtlsPdf(shopObj:AddShopDBModelEntity){
        var document: Document = Document(PageSize.A4, 36f, 36f, 36f, 80f)
        val time = System.currentTimeMillis()
        var fileName = "Contact" +  "_" + time
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/FSMApp/CallHis/"

        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        try{
            var pdfWriter : PdfWriter = PdfWriter.getInstance(document, FileOutputStream(path + fileName + ".pdf"))
            document.open()

            var font: Font = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD)
            var font1: Font = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL)
            var fontBoldU: Font = Font(Font.FontFamily.HELVETICA, 9f, Font.UNDERLINE or Font.BOLD)
            var fontBoldUHeader: Font = Font(Font.FontFamily.HELVETICA, 12f, Font.UNDERLINE or Font.BOLD)

            //image add
            val bm: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val bitmap = Bitmap.createScaledBitmap(bm, 50, 50, true);
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            var img: Image? = null
            val byteArray: ByteArray = stream.toByteArray()
            try {
                img = Image.getInstance(byteArray)
                img.scaleToFit(70f, 70f)
                img.scalePercent(50f)
                img.alignment = Image.ALIGN_LEFT
            } catch (e: BadElementException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            document.add(img)

            val para = Paragraph()
            para.alignment = Element.ALIGN_CENTER
            para.indentationLeft = 220f
            val glue = Chunk(VerticalPositionMark())
            val ph = Phrase()
            ph.add(Chunk("Contact Details", fontBoldUHeader))
            ph.add(glue)
            ph.add(Chunk("DATE: " + AppUtils.getCurrentDate_DD_MM_YYYY() + " ", font1))
            para.add(ph)
            document.add(para)

            val spac = Paragraph("", font)
            spac.spacingAfter = 15f
            document.add(spac)

            val name = Paragraph("Name                         :      " + shopObj?.shopName, font1)
            name.alignment = Element.ALIGN_LEFT
            name.spacingAfter = 2f
            document.add(name)

            val addr = Paragraph("Address                     :      " + shopObj?.address+" "+shopObj?.pinCode, font1)
            addr.alignment = Element.ALIGN_LEFT
            addr.spacingAfter = 2f
            document.add(addr)

            val contNo = Paragraph("Contact No.               :      " + shopObj?.ownerContactNumber, font1)
            contNo.alignment = Element.ALIGN_LEFT
            contNo.spacingAfter = 2f
            document.add(contNo)

            val email = Paragraph("Email                         :      " + if(shopObj?.ownerEmailId.isNullOrEmpty()) "None" else shopObj?.ownerEmailId, font1)
            email.alignment = Element.ALIGN_LEFT
            email.spacingAfter = 2f
            document.add(email)

            val crm_company = Paragraph("Company                  :      " + if(shopObj?.companyName.isNullOrEmpty()) "None" else shopObj?.companyName, font1)
            crm_company.alignment = Element.ALIGN_LEFT
            crm_company.spacingAfter = 2f
            document.add(crm_company)

            val jobTitle = Paragraph("Job Title                    :      " + if(shopObj?.jobTitle.isNullOrEmpty()) "None" else shopObj?.jobTitle, font1)
            jobTitle.alignment = Element.ALIGN_LEFT
            jobTitle.spacingAfter = 2f
            document.add(jobTitle)

            val type = Paragraph("Type                         :      " + if(shopObj?.crm_type.isNullOrEmpty()) "None" else shopObj?.crm_type, font1)
            type.alignment = Element.ALIGN_LEFT
            type.spacingAfter = 2f
            document.add(type)

            val source = Paragraph("Source                      :      " + if(shopObj?.crm_source.isNullOrEmpty()) "None" else shopObj?.crm_source, font1)
            source.alignment = Element.ALIGN_LEFT
            source.spacingAfter = 2f
            document.add(source)

            val ref = Paragraph("Reference                 :      " + if(shopObj?.crm_reference.isNullOrEmpty()) "None" else shopObj?.crm_reference, font1)
            ref.alignment = Element.ALIGN_LEFT
            ref.spacingAfter = 2f
            document.add(ref)

            val stage = Paragraph("Stage                        :      " + if(shopObj?.crm_stage.isNullOrEmpty()) "None" else shopObj?.crm_stage, font1)
            stage.alignment = Element.ALIGN_LEFT
            stage.spacingAfter = 2f
            document.add(stage)

            val status = Paragraph("Status                       :      " + if(shopObj?.crm_status.isNullOrEmpty()) "None" else shopObj?.crm_status, font1)
            status.alignment = Element.ALIGN_LEFT
            status.spacingAfter = 2f
            document.add(status)

            var added_date = shopObj.added_date.replace("T"," ").split(" ").get(0).toString()
            var added_time = shopObj.added_date.replace("T"," ").split(" ").get(1).toString()
            val addedDtTi = Paragraph("Added Date-Time     :      " + if(added_date.isNullOrEmpty()) "None" else AppUtils.getFormatedDateNew(added_date,"yyyy-mm-dd","dd-mm-yyyy")+" - "+added_time.substring(0,5).toString(), font1)
            addedDtTi.alignment = Element.ALIGN_LEFT
            addedDtTi.spacingAfter = 2f
            document.add(addedDtTi)

            val crmFrom = Paragraph("Contact From           :      " + if(shopObj?.crm_saved_from.isNullOrEmpty()) "None" else shopObj?.crm_saved_from, font1)
            crmFrom.alignment = Element.ALIGN_LEFT
            crmFrom.spacingAfter = 2f
            document.add(crmFrom)

            val expSaleV = Paragraph("Expected Sale Value  :      " + if(shopObj?.amount.isNullOrEmpty()) "0.00" else shopObj?.amount, font1)
            expSaleV.alignment = Element.ALIGN_LEFT
            expSaleV.spacingAfter = 2f
            document.add(expSaleV)

            document.add(spac)

            document.close()

            var sendingPath = path + fileName + ".pdf"
            if (!TextUtils.isEmpty(sendingPath)) {
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    val fileUrl = Uri.parse(sendingPath)
                    val file = File(fileUrl.path)
                    val uri: Uri = FileProvider.getUriForFile(mContext, mContext.applicationContext.packageName.toString() + ".provider", file)
                    shareIntent.type = "image/png"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    startActivity(Intent.createChooser(shareIntent, "Share pdf using"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong1))
                }
            }

        }catch (ex:Exception){
            ex.printStackTrace()
        }
    }

    private fun updateLabel() {
        selectedDate = AppUtils.getFormattedDateForApi(myCalendar.time)
        var obj: ContactActivityEntity = ContactActivityEntity()
        obj.apply {
            shop_id  = selectedShopIdForActivity
            activity_date = selectedDate
            create_date_time = AppUtils.getCurrentDateTime()
            isActivityDone = false
        }
        AppDatabase.getDBInstance()?.contactActivityDao()?.insert(obj)

        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_ok)
        val dialogHeader = simpleDialog.findViewById(R.id.dialog_yes_header_TV) as AppCustomTextView
        dialogHeader.text = "Contact auto activity set for ${AppUtils.getFormatedDateNew(selectedDate,"yyyy-mm-dd","dd-mm-yyyy")} is successful."
        val dialogYes = simpleDialog.findViewById(R.id.tv_dialog_yes) as AppCustomTextView
        dialogYes.setOnClickListener({ view ->
            simpleDialog.cancel()
            BaseActivity.isApiInitiated = false
        })
        simpleDialog.show()
    }

}