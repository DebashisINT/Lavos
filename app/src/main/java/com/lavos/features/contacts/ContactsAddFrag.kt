package com.lavos.features.contacts

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.NetworkConstant
import com.lavos.app.Pref
import com.lavos.app.domain.AddShopDBModelEntity
import com.lavos.app.domain.CompanyMasterEntity
import com.lavos.app.domain.ShopActivityEntity
import com.lavos.app.domain.ShopExtraContactEntity
import com.lavos.app.domain.SourceMasterEntity
import com.lavos.app.domain.StageMasterEntity
import com.lavos.app.domain.StatusMasterEntity
import com.lavos.app.domain.TeamListEntity
import com.lavos.app.domain.TypeMasterEntity
import com.lavos.app.utils.AppUtils
import com.lavos.app.utils.InputFilterDecimal
import com.lavos.app.utils.Toaster
import com.lavos.base.BaseResponse
import com.lavos.base.presentation.BaseActivity
import com.lavos.base.presentation.BaseFragment
import com.lavos.features.addshop.api.AddShopRepositoryProvider
import com.lavos.features.addshop.model.AddShopRequestData
import com.lavos.features.addshop.model.AddShopResponse
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.location.LocationWizard
import com.lavos.features.location.SingleShotLocationProvider
import com.lavos.features.member.api.TeamRepoProvider
import com.lavos.features.shopdetail.presentation.api.EditShopRepoProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.pnikosis.materialishprogress.ProgressWheel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import lecho.lib.hellocharts.model.Line
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.util.Calendar
import java.util.Locale
import java.util.Random

class ContactsAddFrag : BaseFragment(), View.OnClickListener {

    private lateinit var mContext: Context
    private lateinit var progress_wheel: ProgressWheel
    private lateinit var et_fName: TextInputEditText
    private lateinit var et_lName: TextInputEditText
    private lateinit var et_companyName: TextInputEditText
    private lateinit var rv_companyNameHint: RecyclerView
    private lateinit var et_jobTitle: TextInputEditText
    private lateinit var et_email: TextInputEditText
    private lateinit var et_phone: TextInputEditText
    private lateinit var et_addr: TextInputEditText
    private lateinit var et_pin: TextInputEditText
    private lateinit var tv_assignTo: TextInputEditText
    private lateinit var iv_assignTo: LinearLayout
    private lateinit var tv_type: TextInputEditText
    private lateinit var iv_type: LinearLayout
    private lateinit var tv_status: TextInputEditText
    private lateinit var iv_status: LinearLayout
    private lateinit var tv_source: TextInputEditText
    private lateinit var iv_source: LinearLayout
    private lateinit var et_remarks: TextInputEditText
    private lateinit var et_saleValue: TextInputEditText
    private lateinit var tv_stage: TextInputEditText
    private lateinit var iv_stage: LinearLayout
    private lateinit var tv_reference: TextInputEditText
    private lateinit var iv_reference: LinearLayout

    private lateinit var ll_addExtraContact: LinearLayout
    private lateinit var extraContRoot: HorizontalScrollView
    private lateinit var tv_extraContact1: TextView
    private lateinit var tv_extraContact2: TextView
    private lateinit var tv_extraContact3: TextView
    private lateinit var tv_extraContact4: TextView
    private lateinit var tv_extraContact5: TextView
    private lateinit var tv_extraContact6: TextView

    private lateinit var cvSubmit: CardView
    private lateinit var cvReferenceRoot: CardView

    private lateinit var adapterCompanyNameHint: AdapterContactCompany

    private var locationStr_lat:String = ""
    private var locationStr_long:String = ""
    private var locationStr:String = ""
    private var location_pinStr:String = ""
    private var str_companyId:String = ""
    private var str_assignToID:String = Pref.user_id!!
    private var str_typeID:String = ""
    private var str_statusID:String = ""
    private var str_sourceID:String = ""
    private var str_stageID:String = ""
    private var str_referenceID:String = ""
    private var str_referenceID_type:String = ""

    private lateinit var iv_mic: ImageView

    var shopExtraContactList:ArrayList<ShopExtraContactEntity> = ArrayList()

    var myCalendar = Calendar.getInstance(Locale.ENGLISH)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object{
        var editShopID: String = ""
        fun getInstance(objects: Any): ContactsAddFrag {
            val objFragment = ContactsAddFrag()

            var obj = objects as String
            editShopID=obj

            return objFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.frag_contacts_add, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        // set views
        progress_wheel = view.findViewById(R.id.progress_wheel_frag_add_cont)
        et_fName = view.findViewById(R.id.et_frag_cont_add_fname)
        et_lName = view.findViewById(R.id.et_frag_cont_add_lname)
        et_companyName = view.findViewById(R.id.et_frag_cont_add_conpany_name)
        rv_companyNameHint = view.findViewById(R.id.rv_frag_contact_add_company_hint)
        et_jobTitle = view.findViewById(R.id.et_frag_cont_add_job_title)
        et_email = view.findViewById(R.id.et_frag_cont_add_email)
        et_phone = view.findViewById(R.id.et_frag_cont_add_phone)
        et_addr = view.findViewById(R.id.et_frag_cont_add_addr)
        et_pin = view.findViewById(R.id.et_frag_cont_add_pin)
        tv_assignTo = view.findViewById(R.id.tv_frag_cont_add_assign_to)
        iv_assignTo = view.findViewById(R.id.iv_frag_cont_add_assign_to)
        tv_type = view.findViewById(R.id.tv_frag_cont_add_type)
        iv_type = view.findViewById(R.id.iv_frag_cont_add_type)
        tv_status = view.findViewById(R.id.tv_frag_cont_add_status)
        iv_status = view.findViewById(R.id.iv_frag_cont_add_status)
        tv_source = view.findViewById(R.id.tv_frag_cont_add_source)
        iv_source = view.findViewById(R.id.iv_frag_cont_add_source)
        et_remarks = view.findViewById(R.id.et_frag_cont_add_remarks)
        et_saleValue = view.findViewById(R.id.et_frag_cont_add_sale_value)
        tv_stage = view.findViewById(R.id.tv_frag_cont_add_stage)
        iv_stage = view.findViewById(R.id.iv_frag_cont_add_stage)
        tv_reference = view.findViewById(R.id.tv_frag_cont_add_reference)
        iv_reference = view.findViewById(R.id.iv_frag_cont_add_reference)
        cvReferenceRoot = view.findViewById(R.id.cv_frag_add_cont_reference_root)
        extraContRoot = view.findViewById(R.id.sv_frag_cot_add_extra_cont_root)
        ll_addExtraContact = view.findViewById(R.id.ll_frag_cont_add_extra_contact)
        tv_extraContact1 = view.findViewById(R.id.tv_frag_cont_add_extra_contact_1)
        tv_extraContact2 = view.findViewById(R.id.tv_frag_cont_add_extra_contact_2)
        tv_extraContact3 = view.findViewById(R.id.tv_frag_cont_add_extra_contact_3)
        tv_extraContact4 = view.findViewById(R.id.tv_frag_cont_add_extra_contact_4)
        tv_extraContact5 = view.findViewById(R.id.tv_frag_cont_add_extra_contact_5)
        tv_extraContact6 = view.findViewById(R.id.tv_frag_cont_add_extra_contact_6)

        iv_mic = view.findViewById(R.id.iv_frag_contacts_add_remarks_voice)

        cvSubmit = view.findViewById(R.id.cv_frag_cont_add_submit)

        // set onclick listners
        //tv_assignTo.setOnClickListener(this)
        iv_assignTo.setOnClickListener(this)
        cvSubmit.setOnClickListener(this)
        //tv_type.setOnClickListener(this)
        //tv_status.setOnClickListener(this)
        //tv_source.setOnClickListener(this)
        //tv_stage.setOnClickListener(this)
        //tv_reference.setOnClickListener(this)
        ll_addExtraContact.setOnClickListener(this)
        tv_extraContact1.setOnClickListener(this)
        tv_extraContact2.setOnClickListener(this)
        tv_extraContact3.setOnClickListener(this)
        tv_extraContact4.setOnClickListener(this)
        tv_extraContact5.setOnClickListener(this)
        tv_extraContact6.setOnClickListener(this)
        iv_mic.setOnClickListener(this)
        iv_stage.setOnClickListener(this)
        iv_reference.setOnClickListener(this)
        iv_source.setOnClickListener(this)
        iv_status.setOnClickListener(this)
        iv_type.setOnClickListener(this)

        tv_extraContact2.isEnabled=false
        tv_extraContact3.isEnabled=false
        tv_extraContact4.isEnabled=false
        tv_extraContact5.isEnabled=false
        tv_extraContact6.isEnabled=false

        rv_companyNameHint.visibility = View.GONE
        cvReferenceRoot.visibility = View.GONE
        extraContRoot.visibility = View.GONE

        progress_wheel.spin()
  /*      if(AppUtils.isOnline(mContext)){
            singleLocation()
        }else{
            locationStr = LocationWizard.getNewLocationName(mContext, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
            location_pinStr = LocationWizard.getPostalCode(mContext, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
            locationStr_lat =Pref.current_latitude.toDouble().toString()
            locationStr_long =Pref.current_longitude.toDouble().toString()

            et_addr.setText(locationStr)
            et_pin.setText(location_pinStr)
            progress_wheel.stopSpinning()
        }*/
        locationStr = LocationWizard.getNewLocationName(mContext, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
        location_pinStr = LocationWizard.getPostalCode(mContext, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())
        locationStr_lat =Pref.current_latitude.toDouble().toString()
        locationStr_long =Pref.current_longitude.toDouble().toString()

        et_addr.setText(locationStr)
        et_pin.setText(location_pinStr)
        progress_wheel.stopSpinning()


        Handler().postDelayed(Runnable {
            setData()
        }, 1500)

        et_saleValue.filters = arrayOf<InputFilter>(InputFilterDecimal(15, 2))
        /*et_saleValue.addTextChangedListener(
            CustomSpecialTextWatcher1(et_saleValue, 15, 2, object : CustomSpecialTextWatcher1.GetCustomTextChangeListener {
                override fun beforeTextChange(text: String) {
                }
                override fun customTextChange(text: String) {
                }
            })
        )*/

        tv_stage.isEnabled = false
        tv_type.isEnabled = false
        tv_status.isEnabled = false
        tv_source.isEnabled = false
        tv_reference.isEnabled = false
        tv_assignTo.isEnabled = false

        if(!editShopID.equals("")){
            (mContext as DashboardActivity).setTopBarTitle("Edit Contact")
            setEditData()
        }else{
            (mContext as DashboardActivity).setTopBarTitle("Add Contact")
        }
    }

    fun setEditData(){
        var shopObj =  AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(editShopID)
        et_fName.setText(shopObj.crm_firstName)
        et_lName.setText(shopObj.crm_lastName)
        et_companyName.setText(shopObj.companyName)
        et_jobTitle.setText(shopObj.jobTitle)
        et_email.setText(shopObj.ownerEmailId)
        et_phone.setText(shopObj.ownerContactNumber)
        et_addr.setText(shopObj.address)
        et_pin.setText(shopObj.pinCode)

        tv_type.setText(shopObj.crm_type)
        tv_status.setText(shopObj.crm_status)
        tv_source.setText(shopObj.crm_source)
        if(shopObj.crm_source.equals("Reference",ignoreCase = true)){
            cvReferenceRoot.visibility = View.VISIBLE
            try{
                if(shopObj.crm_reference_ID_type.equals("User",ignoreCase = true)) {
                    var obj = AppDatabase.getDBInstance()?.teamListDao()?.getByID(shopObj.crm_reference_ID)
                    tv_reference.setText(obj!!.user_name)
                }else{
                    var obj = AppDatabase.getDBInstance()?.addShopEntryDao()?.getShopByIdN(shopObj.crm_reference_ID)!!
                    tv_reference.setText(obj!!.shopName)
                }
            }catch (ex:Exception){
                ex.printStackTrace()
            }
        }else{
            cvReferenceRoot.visibility = View.GONE
        }
        et_remarks.setText(shopObj.remarks)
        et_saleValue.setText(shopObj.amount.toString())
        tv_stage.setText(shopObj.crm_stage)

        str_companyId = shopObj.companyName_id
        str_typeID = shopObj.crm_type_ID
        str_statusID = shopObj.crm_status_ID
        str_sourceID = shopObj.crm_source_ID
        str_referenceID = shopObj.crm_reference_ID
        str_referenceID_type = shopObj.crm_reference_ID_type
        str_stageID = shopObj.crm_stage_ID

        et_fName.isEnabled = false
        et_lName.isEnabled = false
        et_companyName.isEnabled = false

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.iv_frag_contacts_add_remarks_voice->{
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
            R.id.cv_frag_cont_add_submit ->{
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                if(editShopID.equals("")){
                    submitValidationCheck()
                }else{
                    editValidationCheck(editShopID)
                }
            }
            R.id.tv_frag_cont_add_assign_to,R.id.iv_frag_cont_add_assign_to->{
                if((AppDatabase.getDBInstance()?.teamListDao()?.getAll() as ArrayList<TeamListEntity>).size>0){
                    loadTeamMember(AppDatabase.getDBInstance()?.teamListDao()?.getAll() as ArrayList<TeamListEntity>)
                }else{
                    getTeamList()
                }
            }
            R.id.tv_frag_cont_add_type,R.id.iv_frag_cont_add_type->{
                getCRMTypeList()
            }
            R.id.tv_frag_cont_add_status,R.id.iv_frag_cont_add_status ->{
                getCRMStatusList()
            }
            R.id.tv_frag_cont_add_source,R.id.iv_frag_cont_add_source ->{
                getCRMSourceList()
            }
            R.id.tv_frag_cont_add_stage,R.id.iv_frag_cont_add_stage ->{
                getCRMStageList()
            }
            R.id.tv_frag_cont_add_reference,R.id.iv_frag_cont_add_reference ->{
                if((AppDatabase.getDBInstance()?.teamListDao()?.getAll() as ArrayList<TeamListEntity>).size>0){
                    loadTeamMemberReference(AppDatabase.getDBInstance()?.teamListDao()?.getAll() as ArrayList<TeamListEntity>)
                }else{
                    getReferenceList()
                }
            }
            R.id.ll_frag_cont_add_extra_contact ->{
                extraContRoot.visibility = View.VISIBLE
            }
            R.id.tv_frag_cont_add_extra_contact_1 ->{
                if((shopExtraContactList.map { it.contact_serial } as ArrayList<String>).contains("1")){
                    showExtraContDialog("",true,shopExtraContactList.get(0))
                }else{
                    showExtraContDialog("1",false)
                }
            }
            R.id.tv_frag_cont_add_extra_contact_2 ->{
                if((shopExtraContactList.map { it.contact_serial } as ArrayList<String>).contains("2")){
                    showExtraContDialog("",true,shopExtraContactList.get(1))
                }else{
                showExtraContDialog("2",false)
                }
            }
            R.id.tv_frag_cont_add_extra_contact_3 ->{
                if((shopExtraContactList.map { it.contact_serial } as ArrayList<String>).contains("3")){
                    showExtraContDialog("",true,shopExtraContactList.get(2))
                }else{
                showExtraContDialog("3",false)
                }
            }
            R.id.tv_frag_cont_add_extra_contact_4 ->{
                if((shopExtraContactList.map { it.contact_serial } as ArrayList<String>).contains("4")){
                    showExtraContDialog("",true,shopExtraContactList.get(3))
                }else{
                showExtraContDialog("4",false)
                }
            }
            R.id.tv_frag_cont_add_extra_contact_5 ->{
                if((shopExtraContactList.map { it.contact_serial } as ArrayList<String>).contains("5")){
                    showExtraContDialog("",true,shopExtraContactList.get(4))
                }else{
                showExtraContDialog("5",false)
                }
            }
            R.id.tv_frag_cont_add_extra_contact_6 ->{
                if((shopExtraContactList.map { it.contact_serial } as ArrayList<String>).contains("6")){
                    showExtraContDialog("",true,shopExtraContactList.get(5))
                }else{
                showExtraContDialog("6",false)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 7009){
            try {
                val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                et_remarks.setText(result!![0].toString())
            }catch (ex:Exception){
                ex.printStackTrace()
            }
        }
    }



    fun showExtraContDialog(serial:String,isOnlyView:Boolean,savedObj:ShopExtraContactEntity=ShopExtraContactEntity()){
        val simpleDialog = Dialog(mContext)
        simpleDialog.setCancelable(true)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_multiple_contact)

        val tvHead = simpleDialog.findViewById(R.id.tv_header_dialog_multi_contact) as TextView
        val ic_cross = simpleDialog.findViewById(R.id.iv_dialog_multi_cont_cross) as ImageView
        val et_contactName = simpleDialog.findViewById(R.id.et_dialog_multi_contact_name) as EditText
        val et_contactPhno = simpleDialog.findViewById(R.id.et_dialog_multi_contact_phno) as EditText
        val et_contact_email = simpleDialog.findViewById(R.id.et_dialog_multi_contact_email) as EditText
        val et_dob = simpleDialog.findViewById(R.id.tv_dialog_multi_contact_dob) as TextView
        val et_anniv = simpleDialog.findViewById(R.id.tv_dialog_multi_contact_anniv) as TextView
        val fab_add = simpleDialog.findViewById(R.id.fab_dialog_multi_contact_plus) as FloatingActionButton

        tvHead.text = "Add Phone No."

        if(isOnlyView){
            et_contactName.setText(savedObj.contact_name)
            et_contactPhno.setText(savedObj.contact_number)
            et_contact_email.setText(savedObj.contact_email)
            var dob = if(savedObj.contact_dob.equals("")) "" else AppUtils.getFormatedDateNew(savedObj.contact_dob,"yyyy-mm-dd","dd-mm-yyyy")
            var doa = if(savedObj.contact_doa.equals("")) "" else AppUtils.getFormatedDateNew(savedObj.contact_doa,"yyyy-mm-dd","dd-mm-yyyy")
            et_dob.setText(dob)
            et_anniv.setText(doa)

            et_contactName.isEnabled = false
            et_contactPhno.isEnabled = false
            et_contact_email.isEnabled = false
            et_dob.isEnabled = false
            et_anniv.isEnabled = false

            fab_add.visibility = View.GONE
        }

        val dateOtherAnniv = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            et_anniv.setText(AppUtils.getFormattedDateForApi1(myCalendar.time))
        }
        val dateOtherDOB = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            et_dob.setText(AppUtils.getFormattedDateForApi1(myCalendar.time))
        }

        fab_add.setOnClickListener({ view ->
            if(et_contactName.text.toString().length == 0){
                Toaster.msgShort(mContext,"Please enter Contact Name")
                return@setOnClickListener
            }
            if(et_contactPhno.text.toString().length == 0 || et_contactPhno.text.toString().length !=10){
                Toaster.msgShort(mContext,"Please enter valid Contact Phone Number")
                return@setOnClickListener
            }
            saveExtraContact(serial,et_contactName.text.toString(),et_contactPhno.text.toString(),et_contact_email.text.toString(),if(et_dob.text.toString().length>0) AppUtils.getFormatedDateNew(et_dob.text.toString(),"dd-mm-yyyy","yyyy-mm-dd") else "",
                if(et_anniv.text.toString().length>0) AppUtils.getFormatedDateNew(et_anniv.text.toString(),"dd-mm-yyyy","yyyy-mm-dd") else "" )

            simpleDialog.dismiss()
        })
        et_anniv.setOnClickListener({ view ->
            AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
            var aniDatePicker = DatePickerDialog(mContext, R.style.DatePickerTheme, dateOtherAnniv, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH))
            aniDatePicker.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
            aniDatePicker.show()
        })
        et_dob.setOnClickListener({ view ->
            AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
            var dobDatePicker = DatePickerDialog(mContext, R.style.DatePickerTheme, dateOtherDOB, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH))
            dobDatePicker.datePicker.maxDate = Calendar.getInstance(Locale.ENGLISH).timeInMillis
            dobDatePicker.show()
        })

        ic_cross.setOnClickListener {
            simpleDialog.dismiss()
        }
        simpleDialog.show()
    }

    fun saveExtraContact(serial:String,name:String="",phNo:String="",email:String?="",dob:String?="",doa:String?=""){
        var obj : ShopExtraContactEntity = ShopExtraContactEntity()
        obj.apply {
            shop_id = ""
            contact_serial = serial
            contact_name = name
            contact_number = phNo
            contact_email = email
            contact_dob = dob
            contact_doa = doa
            isUploaded = false
        }
        shopExtraContactList.add(obj)
        if(serial.equals("1")){
            tv_extraContact1.setTextColor(ContextCompat.getColor(mContext, R.color.color_custom_white_1))
            //tv_extraContact1.isEnabled = false
            tv_extraContact2.isEnabled = true
        }else if(serial.equals("2")){
            tv_extraContact2.setTextColor(ContextCompat.getColor(mContext, R.color.color_custom_white_1))
            //tv_extraContact2.isEnabled = false
            tv_extraContact3.isEnabled = true
        }else if(serial.equals("3")){
            tv_extraContact3.setTextColor(ContextCompat.getColor(mContext, R.color.color_custom_white_1))
            //tv_extraContact3.isEnabled = false
            tv_extraContact4.isEnabled = true
        }else if(serial.equals("4")){
            tv_extraContact4.setTextColor(ContextCompat.getColor(mContext, R.color.color_custom_white_1))
            //tv_extraContact4.isEnabled = false
            tv_extraContact5.isEnabled = true
        }else if(serial.equals("5")){
            tv_extraContact5.setTextColor(ContextCompat.getColor(mContext, R.color.color_custom_white_1))
            //tv_extraContact5.isEnabled = false
            tv_extraContact6.isEnabled = true
        }else if(serial.equals("6")){
            tv_extraContact6.setTextColor(ContextCompat.getColor(mContext, R.color.color_custom_white_1))
            //tv_extraContact6.isEnabled = false
        }

    }

    fun editValidationCheck(editShopID: String) {
        progress_wheel.spin()

        if(et_fName.text.toString().length==0 || et_fName.text.toString().trim().equals("")){
            et_fName.requestFocus()
            et_fName.setError("Enter First Name")
            progress_wheel.stopSpinning()
            return
        }
        if(et_phone.text.toString().length!=10 || et_phone.text.toString().equals("0000000000") || et_phone.text.toString().get(0).toString().equals("0")){
            et_phone.requestFocus()
            et_phone.setError("Enter valid Phone No.")
            progress_wheel.stopSpinning()
            return
        }
        if(AppDatabase.getDBInstance()!!.addShopEntryDao().getDuplicateShopData(et_phone.text.toString(),editShopID).size > 0){
            et_phone.requestFocus()
            et_phone.setError("Duplicate Phone No.")
            progress_wheel.stopSpinning()
            return
        }

        if(!et_email.text.toString().equals("")){
            if(!et_email.text.toString().contains("@")){
                et_email.requestFocus()
                et_email.setError("Enter Valid Email.")
                progress_wheel.stopSpinning()
                return
            }
        }

        doAsync {

            if(str_companyId.equals("")){
                var comObj = AppDatabase.getDBInstance()!!.companyMasterDao().getInfoByName(et_companyName.text.toString())
                if(comObj==null){
                    if(!et_companyName.text.toString().equals("")){
                        var companyObj = CompanyMasterEntity()
                        companyObj.company_id = 0
                        companyObj.company_name = et_companyName.text.toString()
                        companyObj.isUploaded = false
                        AppDatabase.getDBInstance()!!.companyMasterDao().insert(companyObj)
                        if (AppUtils.isOnline(mContext)){
                            println("comp_master_tag sync call start")
                            syncCompanyMaster(et_companyName.text.toString())
                            println("comp_master_tag sync call end")
                        }
                    }else{
                        str_companyId = "0"
                    }
                }else{
                    str_companyId =  comObj.company_id.toString()
                }
            }

            uiThread {
                Handler().postDelayed(Runnable {
                    println("comp_master_tag sync call proceed")
                    var shopObj = AddShopDBModelEntity()
                    shopObj = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(editShopID)
                    if (AppUtils.isOnline(mContext) && !et_companyName.text.toString().equals("")){
                        str_companyId= AppDatabase.getDBInstance()!!.companyMasterDao().getInfoByName(et_companyName.text.toString()).company_id.toString()
                        println("comp_master_tag sync call proceed comid ${str_companyId}")
                    }
                    val random = Random()
                    //shopObj.shop_id = editShopID
                    //shopObj.shopName = et_fName.text.toString()+" "+et_lName.text.toString()
                    //shopObj.ownerName = et_fName.text.toString()+" "+et_lName.text.toString()
                    //shopObj.crm_firstName = et_fName.text.toString()
                    //shopObj.crm_lastName = et_lName.text.toString()
                    shopObj.companyName_id = str_companyId
                    shopObj.companyName = et_companyName.text.toString()

                    shopObj.jobTitle = et_jobTitle.text.toString()
                    shopObj.ownerEmailId = et_email.text.toString()
                    shopObj.ownerContactNumber = et_phone.text.toString()
                    shopObj.address = et_addr.text.toString()
                    shopObj.pinCode = et_pin.text.toString()
                    shopObj.shopLat = locationStr_lat.toDouble()
                    shopObj.shopLong = locationStr_long.toDouble()
                    shopObj.crm_assignTo = if(tv_assignTo.text.toString().length == 0) Pref.user_name else tv_assignTo.text.toString()
                    shopObj.crm_assignTo_ID = str_assignToID
                    shopObj.crm_type = tv_type.text.toString()
                    shopObj.crm_type_ID = str_typeID
                    shopObj.crm_status=tv_status.text.toString()
                    shopObj.crm_status_ID=str_statusID
                    shopObj.crm_source=tv_source.text.toString()
                    shopObj.crm_source_ID=str_sourceID
                  //  shopObj.crm_reference=""
                   // shopObj.crm_reference_ID=""
                  //  shopObj.crm_reference_ID_type=""
                    shopObj.remarks = et_remarks.text.toString()
                    shopObj.amount = et_saleValue.text.toString()
                    shopObj.crm_stage=tv_stage.text.toString()
                    shopObj.crm_stage_ID=str_stageID
                    shopObj.crm_reference = tv_reference.text.toString()
                    shopObj.crm_reference_ID = str_referenceID
                    shopObj.crm_reference_ID_type = str_referenceID_type
                    //shopObj.type = "99"
                    //shopObj.added_date = AppUtils.getCurrentDateTime()
                    shopObj.crm_saved_from = "Manually"
                    //shopObj.isUploaded = false

                    //AppDatabase.getDBInstance()!!.addShopEntryDao().insert(shopObj)
                    AppDatabase.getDBInstance()!!.addShopEntryDao().updateContactDtls(shopObj.shop_id,shopObj.jobTitle,shopObj.ownerEmailId,shopObj.ownerContactNumber,
                        shopObj.address,shopObj.pinCode,shopObj.shopLat,shopObj.shopLong,shopObj.crm_assignTo,shopObj.crm_assignTo_ID,shopObj.crm_type,shopObj.crm_type_ID,
                        shopObj.crm_status,shopObj.crm_source,shopObj.crm_source_ID,/*shopObj.crm_reference,*shopObj.crm_reference_ID,shopObj.crm_reference_ID_type,*/
                        shopObj.remarks,shopObj.amount,shopObj.crm_stage,shopObj.crm_stage_ID,shopObj.crm_reference,shopObj.crm_reference_ID,shopObj.crm_reference_ID_type,
                                shopObj.crm_saved_from, 0)

                    progress_wheel.stopSpinning()
                    if (AppUtils.isOnline(mContext)){
                        editSyncContact(shopObj)
                    }
                    else{
                        voiceMsg("Contact added successfully")
                        Toaster.msgShort(mContext,"Contact added successfully.")
                        (mContext as DashboardActivity).onBackPressed()
                    }
                }, 1900)

            }
        }
    }

     fun editSyncContact(shopObj: AddShopDBModelEntity) {
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

        addShopRequestData.shop_firstName= shopObj.crm_firstName
        addShopRequestData.shop_lastName=  shopObj.crm_lastName
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
        val repository = EditShopRepoProvider.provideEditShopWithoutImageRepository()
        BaseActivity.compositeDisposable.add(
            repository.editShop(addShopRequestData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val addShopResult = result as AddShopResponse
                    Timber.d("EditShop : " + ", SHOP: " + addShopRequestData.shop_name + ", RESPONSE:" + result.message)
                    progress_wheel.stopSpinning()
                    if (addShopResult.status == NetworkConstant.SUCCESS) {

                        AppDatabase.getDBInstance()!!.addShopEntryDao().updateIsEditUploaded(1, addShopRequestData.shop_id)
                        voiceMsg("Contact edited successfully")
                        Toaster.msgShort(mContext,"Contact edited successfully.")
                        (mContext as DashboardActivity).onBackPressed()
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

    fun submitValidationCheck(){

        progress_wheel.spin()

        if(et_fName.text.toString().length==0 || et_fName.text.toString().trim().equals("")){
            et_fName.requestFocus()
            et_fName.setError("Enter First Name")
            progress_wheel.stopSpinning()
            return
        }
        if(et_phone.text.toString().length!=10 || et_phone.text.toString().equals("0000000000") || et_phone.text.toString().get(0).toString().equals("0")){
            et_phone.requestFocus()
            et_phone.setError("Enter valid Phone No.")
            progress_wheel.stopSpinning()
            return
        }
        if(AppDatabase.getDBInstance()!!.addShopEntryDao().getDuplicateShopData(et_phone.text.toString()).size > 0){
            et_phone.requestFocus()
            et_phone.setError("Duplicate Phone No.")
            progress_wheel.stopSpinning()
            return
        }

        if(!et_email.text.toString().equals("")){
            if(!et_email.text.toString().contains("@")){
                et_email.requestFocus()
                et_email.setError("Enter Valid Email.")
                progress_wheel.stopSpinning()
                return
            }
        }

        doAsync {

            if(str_companyId.equals("")){
                var comObj = AppDatabase.getDBInstance()!!.companyMasterDao().getInfoByName(et_companyName.text.toString())
                if(comObj==null){
                    if(!et_companyName.text.toString().equals("")){
                        var companyObj = CompanyMasterEntity()
                        companyObj.company_id = 0
                        companyObj.company_name = et_companyName.text.toString()
                        companyObj.isUploaded = false
                        AppDatabase.getDBInstance()!!.companyMasterDao().insert(companyObj)
                        if (AppUtils.isOnline(mContext)){
                            println("comp_master_tag sync call start")
                            syncCompanyMaster(et_companyName.text.toString())
                            println("comp_master_tag sync call end")
                        }
                    }else{
                        str_companyId = "0"
                    }
                }else{
                    str_companyId =  comObj.company_id.toString()
                }
            }

            uiThread {
                Handler().postDelayed(Runnable {
                    println("comp_master_tag sync call proceed")
                    var shopObj = AddShopDBModelEntity()
                    if (AppUtils.isOnline(mContext) && !et_companyName.text.toString().equals("")){
                        str_companyId= AppDatabase.getDBInstance()!!.companyMasterDao().getInfoByName(et_companyName.text.toString()).company_id.toString()
                        println("comp_master_tag sync call proceed comid ${str_companyId}")
                    }
                    val random = Random()
                    shopObj.shop_id = Pref.user_id + "_" + System.currentTimeMillis().toString() +  (random.nextInt(999 - 100) + 100).toString()
                    shopObj.shopName = et_fName.text.toString()+" "+et_lName.text.toString()
                    shopObj.ownerName = et_fName.text.toString()+" "+et_lName.text.toString()
                    shopObj.crm_firstName = et_fName.text.toString()
                    shopObj.crm_lastName = et_lName.text.toString()
                    shopObj.companyName_id = str_companyId
                    shopObj.companyName = et_companyName.text.toString()
                    shopObj.jobTitle = et_jobTitle.text.toString()
                    shopObj.ownerEmailId = et_email.text.toString()
                    shopObj.ownerContactNumber = et_phone.text.toString()
                    shopObj.address = et_addr.text.toString()
                    shopObj.pinCode = et_pin.text.toString()
                    shopObj.shopLat = locationStr_lat.toDouble()
                    shopObj.shopLong = locationStr_long.toDouble()
                    shopObj.crm_assignTo = if(tv_assignTo.text.toString().length == 0) Pref.user_name else tv_assignTo.text.toString()
                    shopObj.crm_assignTo_ID = str_assignToID
                    shopObj.crm_type = tv_type.text.toString()
                    shopObj.crm_type_ID = str_typeID
                    shopObj.crm_status=tv_status.text.toString()
                    shopObj.crm_status_ID=str_statusID
                    shopObj.crm_source=tv_source.text.toString()
                    shopObj.crm_source_ID=str_sourceID
                    shopObj.crm_reference=""
                    shopObj.crm_reference_ID=""
                    shopObj.crm_reference_ID_type=""
                    shopObj.remarks = et_remarks.text.toString()
                    shopObj.amount = et_saleValue.text.toString()
                    shopObj.crm_stage=tv_stage.text.toString()
                    shopObj.crm_stage_ID=str_stageID
                    shopObj.crm_reference = tv_reference.text.toString()
                    shopObj.crm_reference_ID = str_referenceID
                    shopObj.crm_reference_ID_type = str_referenceID_type
                    shopObj.type = "99"
                    shopObj.added_date = AppUtils.getCurrentDateTime()
                    shopObj.crm_saved_from = "Manually"
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
                    shopActivityEntity.isDurationCalculated = false
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

                    progress_wheel.stopSpinning()
                    if (AppUtils.isOnline(mContext)){
                        syncContact(shopObj)
                    }
                    else{
                        voiceMsg("Contact added successfully")
                        Toaster.msgShort(mContext,"Contact added successfully.")
                        (mContext as DashboardActivity).onBackPressed()
                    }
                }, 1900)

            }
        }

    }



    private fun singleLocation() {
        try{
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
                        et_addr.setText(locationStr)
                        et_pin.setText(location_pinStr)
                        progress_wheel.stopSpinning()
                    }

                })
        }catch (ex:Exception){
            ex.printStackTrace()
            progress_wheel.stopSpinning()
        }
    }

    private fun setData(){
        var compL:ArrayList<CompanyMasterEntity> = AppDatabase.getDBInstance()?.companyMasterDao()?.getAll() as ArrayList<CompanyMasterEntity>
        adapterCompanyNameHint = AdapterContactCompany(mContext,compL,object :AdapterContactCompany.onClick{
            override fun onNameClick(obj: CompanyMasterEntity) {
                et_companyName.setText(obj.company_name)
                et_companyName.setSelection(obj.company_name.length)
                str_companyId = obj.company_id.toString()
                rv_companyNameHint.visibility = View.GONE
            }

            override fun onNoData(nodata: Boolean) {
                if(nodata){
                    rv_companyNameHint.visibility = View.GONE
                }else{
                    rv_companyNameHint.visibility = View.VISIBLE
                }
            }
        })
        rv_companyNameHint.adapter = adapterCompanyNameHint

        et_companyName.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                var str = p0.toString()
                if(str.length>1){
                    adapterCompanyNameHint?.filter.filter(str)
                    rv_companyNameHint.visibility = View.VISIBLE
                }else{
                    rv_companyNameHint.visibility = View.GONE
                }

                var comObj = AppDatabase.getDBInstance()!!.companyMasterDao().getInfoByName(et_companyName.text.toString())
                if(comObj == null){
                    str_companyId=""
                }
            }
        })
    }

    private fun getTeamList() {
        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage("Internet connectivity is required for data sync purposes.")
            return
        }
        progress_wheel.spin()
        val repository = TeamRepoProvider.teamRepoProvider()
        BaseActivity.compositeDisposable.add(
            repository.teamListNew(Pref.user_id!!, false, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val response = result as TeamListRes
                    if (response.status == NetworkConstant.SUCCESS) {
                        progress_wheel.stopSpinning()
                        if (response.member_list != null && response.member_list!!.size > 0) {
                            AppDatabase.getDBInstance()?.teamListDao()?.insertAll(response.member_list!!)
                            loadTeamMember(response.member_list!!)
                        } else {
                            (mContext as DashboardActivity).showSnackMessage(response.message!!)
                        }
                    } else {
                        progress_wheel.stopSpinning()
                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                    }
                }, { error ->
                    error.printStackTrace()
                    progress_wheel.stopSpinning()
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                })
        )
    }

    private fun getReferenceList() {
        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage("Internet connectivity is required for data sync purposes.")
            return
        }
        progress_wheel.spin()
        val repository = TeamRepoProvider.teamRepoProvider()
        BaseActivity.compositeDisposable.add(
            repository.teamListNew(Pref.user_id!!, false, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val response = result as TeamListRes
                    if (response.status == NetworkConstant.SUCCESS) {
                        progress_wheel.stopSpinning()
                        if (response.member_list != null && response.member_list!!.size > 0) {
                            AppDatabase.getDBInstance()?.teamListDao()?.insertAll(response.member_list!!)
                            loadTeamMemberReference(response.member_list!!)
                        } else {
                            (mContext as DashboardActivity).showSnackMessage(response.message!!)
                        }
                    } else {
                        progress_wheel.stopSpinning()
                        (mContext as DashboardActivity).showSnackMessage(response.message!!)
                    }
                }, { error ->
                    error.printStackTrace()
                    progress_wheel.stopSpinning()
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                })
        )
    }

    fun getCRMTypeList(){
        var crmTypeList =  AppDatabase.getDBInstance()?.typeMasterDao()?.getAll() as ArrayList<TypeMasterEntity>
        if(crmTypeList.size>0){
            var genericL : ArrayList<CustomData> = ArrayList()
            for(i in 0..crmTypeList.size-1){
                genericL.add(CustomData(crmTypeList.get(i).type_id.toString(),crmTypeList.get(i).type_name.toString()))
            }
            GenericDialog.newInstance("Type",genericL as ArrayList<CustomData>){
                str_typeID = it.id
                tv_type.setText(it.name)
            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        }else{
            Toaster.msgShort(mContext, "No Type Found")
        }
    }

    fun getCRMStatusList(){
        var crmStatusList = AppDatabase.getDBInstance()?.statusMasterDao()?.getAll() as ArrayList<StatusMasterEntity>
        if(crmStatusList.size>0){
            var genericL : ArrayList<CustomData> = ArrayList()
            for(i in 0..crmStatusList.size-1){
                genericL.add(CustomData(crmStatusList.get(i).status_id.toString(),crmStatusList.get(i).status_name.toString()))
            }
            GenericDialog.newInstance("Status",genericL as ArrayList<CustomData>){
                str_statusID = it.id
                tv_status.setText(it.name)
            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        }else{
            Toaster.msgShort(mContext, "No Status Found")
        }
    }

    fun getCRMSourceList(){
        var crmSourceList = AppDatabase.getDBInstance()?.sourceMasterDao()?.getAll() as ArrayList<SourceMasterEntity>
        if(crmSourceList.size>0){
            var genericL : ArrayList<CustomData> = ArrayList()
            for(i in 0..crmSourceList.size-1){
                genericL.add(CustomData(crmSourceList.get(i).source_id.toString(),crmSourceList.get(i).source_name.toString()))
            }
            GenericDialog.newInstance("Source",genericL as ArrayList<CustomData>){
                str_sourceID = it.id
                tv_source.setText(it.name)
                if(it.name.equals("Reference",ignoreCase = true)){
                    cvReferenceRoot.visibility = View.VISIBLE
                }else{
                    cvReferenceRoot.visibility = View.GONE
                }
            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        }else{
            Toaster.msgShort(mContext, "No Source Found")
        }
    }

    fun getCRMStageList(){
        var crmStageList = AppDatabase.getDBInstance()?.stageMasterDao()?.getAll() as ArrayList<StageMasterEntity>
        if(crmStageList.size>0){
            var genericL : ArrayList<CustomData> = ArrayList()
            for(i in 0..crmStageList.size-1){
                genericL.add(CustomData(crmStageList.get(i).stage_id.toString(),crmStageList.get(i).stage_name.toString()))
            }
            GenericDialog.newInstance("Stage",genericL as ArrayList<CustomData>){
                str_stageID = it.id
                tv_stage.setText(it.name)
            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        }else{
            Toaster.msgShort(mContext, "No Stage Found")
        }
    }

    private fun loadTeamMember(member_list:ArrayList<TeamListEntity>) {
        if (member_list.size>0) {
            var genericL : ArrayList<CustomData> = ArrayList()
            for(i in 0..member_list.size-1){
                genericL.add(CustomData(member_list.get(i).user_id.toString(),member_list.get(i).user_name.toString()))
            }
            GenericDialog.newInstance("Assign To",genericL as ArrayList<CustomData>){
                tv_assignTo.setText(it.name)
                str_assignToID = it.id
            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        } else {
            Toaster.msgShort(mContext, "No Member Found")
        }
    }

    private fun loadTeamMemberReference(member_list:ArrayList<TeamListEntity>) {
        if (member_list != null && member_list.isNotEmpty()) {
            var genericL : ArrayList<CustomData> = ArrayList()
            for(i in 0..member_list.size-1){
                genericL.add(CustomData(member_list.get(i).user_id.toString(),member_list.get(i).user_name.toString()))
            }
            var contL = AppDatabase.getDBInstance()!!.addShopEntryDao().getContatcShops() as ArrayList<AddShopDBModelEntity>
            if(contL.size>0){
                for(i in 0..contL.size-1){
                    genericL.add(CustomData(contL.get(i).shop_id.toString(),contL.get(i).shopName.toString()))
                }
            }
            GenericDialog.newInstance("Reference",genericL as ArrayList<CustomData>){
                tv_reference.setText(it.name)
                str_referenceID = it.id
                if(it.id.contains("_")){
                    str_referenceID_type = "Contact"
                }else{
                    str_referenceID_type = "User"
                }
            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        } else {
            Toaster.msgShort(mContext, "No Reference Found")
        }
    }

    private fun voiceMsg(msg: String) {
        if (Pref.isVoiceEnabledForAttendanceSubmit) {
            val speechStatus = (mContext as DashboardActivity).textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null)
            if (speechStatus == TextToSpeech.ERROR)
                Log.e("Add Day Start", "TTS error in converting Text to Speech!");
        }
    }

    private fun syncContact(shopObj: AddShopDBModelEntity){
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

        addShopRequestData.shop_firstName=  shopObj.crm_firstName
        addShopRequestData.shop_lastName=  shopObj.crm_lastName
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
                        voiceMsg("Contact added successfully")
                        Toaster.msgShort(mContext,"Contact added successfully.")
                        (mContext as DashboardActivity).onBackPressed()
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

}