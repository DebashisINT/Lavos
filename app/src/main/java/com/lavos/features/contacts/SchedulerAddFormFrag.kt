package com.lavos.features.contacts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.lavos.MultiFun
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.Pref
import com.lavos.app.domain.AddShopDBModelEntity
import com.lavos.app.domain.ModeTemplateEntity
import com.lavos.app.domain.RuleTemplateEntity
import com.lavos.app.domain.ScheduleTemplateEntity
import com.lavos.app.domain.SchedulerMasterEntity
import com.lavos.app.utils.AppUtils
import com.lavos.app.utils.PermissionUtils
import com.lavos.app.utils.ProcessImageUtils_v1
import com.lavos.app.utils.Toaster
import com.lavos.base.presentation.BaseFragment
import com.lavos.features.NewQuotation.Mail
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.reimbursement.presentation.FullImageDialog
import com.lavos.widgets.AppCustomEditText
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pnikosis.materialishprogress.ProgressWheel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.themechangeapp.pickimage.PermissionHelper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


class SchedulerAddFormFrag : BaseFragment(), View.OnClickListener {

    private var hr: String=""
    private var min: String=""
    private var particular_date_select: String=""
    private var contactTickL: ArrayList<ScheduleContactDtls> = ArrayList()
    private var selectedHr: String=""
    private var selectedMin: String=""
    private var str_templateID:String = Pref.user_id!!
    private var str_modeoftemplateID:String = Pref.user_id!!
    private var str_ruleoftemplateID:String = Pref.user_id!!
    private var str_contactoftemplate:String = Pref.user_id!!
    private var sortBy:String = ""
    private lateinit var progress_wheel: ProgressWheel
    private lateinit var mContext: Context
    private var filePath = ""
    private var permissionUtils: PermissionUtils? = null
    private lateinit var hour_numPicker:NumberPicker
    private lateinit var min_numPicker:NumberPicker
    private lateinit var tv_selectedTime:TextView
    private lateinit var iv_add_schedular_form_attachment1:ImageView
    private lateinit var iv_add_schedular_form_attachment2:ImageView
    private lateinit var iv_add_schedular_form_attachment3:ImageView
    private lateinit var schedulername: TextInputEditText
    private lateinit var selectTemplate: TextInputEditText
    private lateinit var et_templateContent: TextInputEditText
    private lateinit var tv_rule_Of_scheduler: TextInputEditText
    private lateinit var selectContactSchedule: TextInputEditText
    private lateinit var selectMode: TextInputEditText
    private lateinit var calendarView : MaterialCalendarView
    private lateinit var cv_calendar : CardView
    private lateinit var sw_repeatmonth : Switch
    private lateinit var adapterScheduleContactName:AdapterScheduleContactName
    private lateinit var iv_template : LinearLayout
    private lateinit var textInLaySelectMode : TextInputLayout
    private lateinit var textInLaySelectContact : TextInputLayout
    private lateinit var textInLaySelectRule : TextInputLayout
    private lateinit var iv_mode_template : LinearLayout
    private lateinit var iv_rule_Of_scheduler_dropdown : LinearLayout
    private lateinit var iv_frag_cont_add_schedule_to : LinearLayout
    private lateinit var ll_frag_sch_add_template_root : LinearLayout
    private lateinit var cv_frag_Shceduler_add_submit : CardView
    private lateinit var cv_scheduler_timer : CardView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    companion object{
        fun getInstance(objects: Any): SchedulerAddFormFrag {
            val objFragment = SchedulerAddFormFrag()
            var obj = objects as String
            return objFragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_scheduler_add_form, container, false)
        initView(view)
        return view
    }
    private fun initView(view: View) {
        progress_wheel = view.findViewById(R.id.progress_wheel_frag_add_cont)
        cv_calendar = view.findViewById(R.id.cv_calendar)
        hour_numPicker = view.findViewById(R.id.np_frag_schedule_add_form_hr)
        iv_add_schedular_form_attachment1 = view.findViewById(R.id.iv_add_schedular_form_attachment1)
        iv_add_schedular_form_attachment2 = view.findViewById(R.id.iv_add_schedular_form_attachment2)
        iv_add_schedular_form_attachment3 = view.findViewById(R.id.iv_add_schedular_form_attachment3)
        sw_repeatmonth = view.findViewById(R.id.sw_repeatmonth)
        schedulername = view.findViewById(R.id.et_add_form_schedule_name)
        selectTemplate = view.findViewById(R.id.tv_select_template)
        et_templateContent = view.findViewById(R.id.et_templateContent)
        min_numPicker = view.findViewById(R.id.np_frag_schedule_add_form_min)
        tv_selectedTime = view.findViewById(R.id.tv_frag_schedule_add_form_selected_time)
        calendarView = view.findViewById(R.id.calendarView_frag_schedule_calendar)
        selectMode = view.findViewById(R.id.tv_select_mode_template)
        selectContactSchedule = view.findViewById(R.id.tv_frag_cont_add_contact_to_schedule)
        iv_mode_template = view.findViewById(R.id.iv_select_mode_template)
        tv_rule_Of_scheduler = view.findViewById(R.id.tv_rule_Of_scheduler)
        iv_rule_Of_scheduler_dropdown = view.findViewById(R.id.iv_rule_Of_scheduler_dropdown)
        ll_frag_sch_add_template_root = view.findViewById(R.id.ll_frag_sch_add_template_root)
        iv_frag_cont_add_schedule_to = view.findViewById(R.id.iv_frag_cont_add_schedule_to)
        cv_frag_Shceduler_add_submit = view.findViewById(R.id.cv_frag_Shceduler_add_submit)
        iv_template = view.findViewById(R.id.iv_frag_sched_add_form_template_dropDown)
        textInLaySelectContact = view.findViewById(R.id.textInLaySelectContact)
        textInLaySelectRule = view.findViewById(R.id.textInLaySelectRule)
        textInLaySelectMode = view.findViewById(R.id.textInLaySelectMode)
        cv_scheduler_timer = view.findViewById(R.id.cv_scheduler_timer)
        cv_scheduler_timer.visibility = View.GONE
       // selectTemplate.isEnabled = false
      /*  selectTemplate.setFocusable(false)
        selectTemplate.setClickable(true)
        selectTemplate.inputType=InputType.TYPE_NULL*/
       // selectMode.isEnabled = false
       // tv_rule_Of_scheduler.isEnabled = false
       // selectContactSchedule.isEnabled = false
        iv_template.setOnClickListener(this)
        selectContactSchedule.setOnClickListener(this)
        textInLaySelectMode.setOnClickListener(this)
        textInLaySelectRule.setOnClickListener(this)
        textInLaySelectContact.setOnClickListener(this)
        selectTemplate.setOnClickListener(this)
        tv_rule_Of_scheduler.setOnClickListener(this)
        iv_mode_template.setOnClickListener(this)
        selectMode.setOnClickListener(this)
        selectContactSchedule.setOnClickListener(this)
        ll_frag_sch_add_template_root.setOnClickListener(this)
        iv_rule_Of_scheduler_dropdown.setOnClickListener(this)
        iv_frag_cont_add_schedule_to.setOnClickListener(this)
        cv_frag_Shceduler_add_submit.setOnClickListener(this)

        setCal()
        setTestData()
        setModeData()
        setRuleData()

        iv_add_schedular_form_attachment1.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                initPermissionCheck()
            else {
                showPictureDialog()
            }
        }


    }

    private fun showPictureDialog() {
         val pictureDialog = AlertDialog.Builder(mContext)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems = arrayOf("Select photo from gallery", "Capture Image", "Select file from file manager")
            pictureDialog.setItems(pictureDialogItems,
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        0 -> selectImageInAlbum()
                        1 -> {
                            //(mContext as DashboardActivity).openFileManager()
                            launchCamera()
                        }
                        2 -> {
                            (mContext as DashboardActivity).openFileManager()
                        }
                    }
                })
            pictureDialog.show()
        }
    private fun launchCamera() {
        (mContext as DashboardActivity).captureImage()
    }
    private fun selectImageInAlbum() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        (mContext as DashboardActivity).startActivityForResult(galleryIntent, PermissionHelper.REQUEST_CODE_STORAGE)
    }

    private fun initPermissionCheck() {

        //begin mantis id 26741 Storage permission updation Suman 22-08-2023
        var permissionList = arrayOf<String>( Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissionList += Manifest.permission.READ_MEDIA_IMAGES
            permissionList += Manifest.permission.READ_MEDIA_AUDIO
            permissionList += Manifest.permission.READ_MEDIA_VIDEO
        }else{
            permissionList += Manifest.permission.WRITE_EXTERNAL_STORAGE
            permissionList += Manifest.permission.READ_EXTERNAL_STORAGE
        }
//end mantis id 26741 Storage permission updation Suman 22-08-2023

        permissionUtils = PermissionUtils(mContext as Activity, object : PermissionUtils.OnPermissionListener {
            override fun onPermissionGranted() {
                showPictureDialog()
            }

            override fun onPermissionNotGranted() {
                (mContext as DashboardActivity).showSnackMessage(getString(R.string.accept_permission))
            }

        },permissionList)// arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun setCal(){
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE)

        calendarView.state().edit()
            .setMinimumDate(CalendarDay.today())
            //.setMaximumDate(CalendarDay.today())
            .commit()
        hour_numPicker.minValue = 0
        hour_numPicker.maxValue = 23
        min_numPicker.minValue = 0
        min_numPicker.maxValue = 59
        var hrL = Array<String>(24) { "" }
        for(i in 0..23){
            hrL[i] = "${i} h"
        }
        var minL = Array<String>(60) { "" }
        for(i in 0..59){
            minL[i] = "${i} min"
        }
        hour_numPicker.displayedValues =hrL
        min_numPicker.displayedValues =minL
        selectedHr = "0 h"
        selectedMin = "0 min"
        cv_calendar.visibility = View.GONE
        sw_repeatmonth.visibility = View.GONE

          /*  hr = String.format("%02d", selectedHr.replace(" h","").toInt())
            min = String.format("%02d", selectedMin.replace(" min","").toInt())

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = simpleDateFormat.parse("2024-01-11"+" $hr:$min:00")
            val timestamp = date?.time*/


        hour_numPicker.setOnValueChangedListener(object : NumberPicker.OnValueChangeListener {
            override fun onValueChange(numberPicker: NumberPicker, i: Int, i2: Int) {
                try{
                    selectedHr = hrL[i2].toString()
                    tv_selectedTime.text = selectedHr+" "+ selectedMin
                   /* if (tv_selectedTime.text.toString().equals("0 h 0 min")){
                        cv_calendar.visibility = View.GONE
                        sw_repeatmonth.visibility = View.GONE
                    }else{
                        cv_calendar.visibility = View.VISIBLE
                        sw_repeatmonth.visibility = View.VISIBLE
                    }*/
                }catch (ex:Exception){
                    ex.printStackTrace()
                    println("tag_picker_err ${ex.message}")
                }
            }
        })
        min_numPicker.setOnValueChangedListener(object : NumberPicker.OnValueChangeListener {
            override fun onValueChange(numberPicker: NumberPicker, i: Int, i2: Int) {
                try{
                    selectedMin = minL[i2].toString()
                    tv_selectedTime.text =selectedHr+" "+ selectedMin
                   /* if (tv_selectedTime.text.toString().equals("0 h 0 min")){
                        cv_calendar.visibility = View.GONE
                        sw_repeatmonth.visibility = View.GONE
                    }else{
                        cv_calendar.visibility = View.VISIBLE
                        sw_repeatmonth.visibility = View.VISIBLE
                    }*/
                }catch (ex:Exception){
                    ex.printStackTrace()
                    println("tag_picker_err ${ex.message}")
                }
            }
        })
    }
    private fun setTestData(){
        if((AppDatabase.getDBInstance()?.scheduleTemplateDao()?.getAll() as ArrayList<ScheduleTemplateEntity>).size == 0){

            var obj = ScheduleTemplateEntity()
            obj.template_id = 1
            obj.template_name = "Auto"

            var obj1 = ScheduleTemplateEntity()
            obj1.template_id = 2
            obj1.template_name = "Manual"

            AppDatabase.getDBInstance()?.scheduleTemplateDao()?.insert(obj)
            AppDatabase.getDBInstance()?.scheduleTemplateDao()?.insert(obj1)
        }
    }
    private fun setModeData(){

        if((AppDatabase.getDBInstance()?.modeTemplateDao()?.getAll() as ArrayList<ModeTemplateEntity>).size == 0){
            var objMode1 = ModeTemplateEntity()
            objMode1.mode_template_id = 1
            objMode1.mode_template_name = "WhatsApp"

            var objMode2 = ModeTemplateEntity()
            objMode2.mode_template_id = 2
            objMode2.mode_template_name = "Email"

            AppDatabase.getDBInstance()?.modeTemplateDao()?.insert(objMode1)
            AppDatabase.getDBInstance()?.modeTemplateDao()?.insert(objMode2)
        }


    }
    private fun setRuleData(){
        if((AppDatabase.getDBInstance()?.ruleTemplateDao()?.getAll() as ArrayList<RuleTemplateEntity>).size == 0){
            var objRule1 = RuleTemplateEntity()
            objRule1.rule_template_id = 1
            objRule1.rule_template_name = "Auto"

            var objRule2 = RuleTemplateEntity()
            objRule2.rule_template_id = 2
            objRule2.rule_template_name = "Manual"

            AppDatabase.getDBInstance()?.ruleTemplateDao()?.insert(objRule1)
            AppDatabase.getDBInstance()?.ruleTemplateDao()?.insert(objRule2)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.iv_frag_sched_add_form_template_dropDown , R.id.tv_select_template->{
                if((AppDatabase.getDBInstance()?.scheduleTemplateDao()?.getAll() as ArrayList<ScheduleTemplateEntity>).size>0){
                    loadTemplateList(AppDatabase.getDBInstance()?.scheduleTemplateDao()?.getAll() as ArrayList<ScheduleTemplateEntity>)
                }else{
                    Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.iv_select_mode_template, R.id.tv_select_mode_template->{
                if((AppDatabase.getDBInstance()?.modeTemplateDao()?.getAll() as ArrayList<ModeTemplateEntity>).size>0){
                    loadModeOfTemplateList(AppDatabase.getDBInstance()?.modeTemplateDao()?.getAll() as ArrayList<ModeTemplateEntity>)
                }else{
                    Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.iv_rule_Of_scheduler_dropdown , R.id.tv_rule_Of_scheduler->{
                if((AppDatabase.getDBInstance()?.modeTemplateDao()?.getAll() as ArrayList<ModeTemplateEntity>).size>0){
                    loadRuleOfTemplateList(AppDatabase.getDBInstance()?.ruleTemplateDao()?.getAll() as ArrayList<RuleTemplateEntity>)
                }else{
                    Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.iv_frag_cont_add_schedule_to , R.id.tv_frag_cont_add_contact_to_schedule->{
                var contL = AppDatabase.getDBInstance()!!.addShopEntryDao().getContatcShopsByAddedDate() as ArrayList<AddShopDBModelEntity>
                if(contL.size >0){
                    loadContactOfTemplateList(contL)
                } else{
                    Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.cv_frag_Shceduler_add_submit ->{
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                    submitValidationCheck()

               /* try {
                    val time = System.currentTimeMillis()
                    var fileName = "Contact" +  "_" + time
                     val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/FSMApp/CallHis/"
                    var sendingPath = "/storage/emulated/0/"+Pref.scheduler_file

                    var m = Mail()
                    var toArr = arrayOf("")
                    m = Mail("suman.bachar@indusnet.co.in", "jfekuhstltfkarrv") // generate under 2-step verification -> app password
                    toArr = arrayOf("suman.bachar@indusnet.co.in","puja.basak@indusnet.co.in")
                    m.setTo(toArr)
                    m.setFrom("TEAM");
                    m.setSubject("Contact Activity.")
                    m.setBody("Hello ,  \n Please find attachment below. \n\n\n Regards \n${Pref.user_name}.")

                    doAsync {
                        try{
                            val fileUrl = Uri.parse(sendingPath)
                            m.send(fileUrl.path)

                        }catch (ex:Exception){
                            ex.printStackTrace()
                            println("tag_file_automain error ${ex.message}")
                        }
                        uiThread {
                            println("tag_file_automain success")
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }*/

            }
        }
    }
    private fun submitValidationCheck() {
        hr = String.format("%02d", selectedHr.replace(" h","").toInt())
        min = String.format("%02d", selectedMin.replace(" min","").toInt())
        Pref.scheduler_template=
            et_templateContent.text.toString().trim()+"\n" +
                    "\n" +
                    "\n" +
                    " Regards\n" +
                    "${Pref.user_name}"
        progress_wheel.spin()

        if(schedulername.text.toString().length==0 || schedulername.text.toString().trim().equals("")){
            schedulername.requestFocus()
            schedulername.setError("Enter Scheduler Name")
            progress_wheel.stopSpinning()
            return
        }
        if(selectTemplate.text.toString().length==0 || selectTemplate.text.toString().trim().equals("")){
            (mContext as DashboardActivity).showSnackMessage("Select a template.")
            progress_wheel.stopSpinning()
            return
        }
        if(et_templateContent.text.toString().length==0 || et_templateContent.text.toString().trim().equals("") && et_templateContent.isEnabled==true){
            (mContext as DashboardActivity).showSnackMessage("Write template content")
            progress_wheel.stopSpinning()
            return
        }
        if(selectMode.text.toString().length==0 || selectMode.text.toString().trim().equals("")){
            (mContext as DashboardActivity).showSnackMessage("Select a mode")
            progress_wheel.stopSpinning()
            return
        }
        if(tv_rule_Of_scheduler.text.toString().length==0 || tv_rule_Of_scheduler.text.toString().trim().equals("")){
            Toast.makeText(mContext, "Select a rule", Toast.LENGTH_SHORT).show()
            (mContext as DashboardActivity).showSnackMessage("Select a rule")
            progress_wheel.stopSpinning()
            return
        }
       /* if(selectedHr.equals("0 h") && selectedMin.equals("0 min") && calendarView.visibility==View.VISIBLE && cv_scheduler_timer.visibility==View.VISIBLE ){
            Toast.makeText(mContext, "Select an hour", Toast.LENGTH_SHORT).show()
            progress_wheel.stopSpinning()
            return
        }*/
       if (calendarView.selectedDates.size==0 &&  tv_rule_Of_scheduler.text.toString().trim().equals("Auto")){
            Toast.makeText(mContext, "Select a date", Toast.LENGTH_SHORT).show()
           (mContext as DashboardActivity).showSnackMessage("Select a date")
           progress_wheel.stopSpinning()
            return
        }
        if(selectContactSchedule.text.toString().length==0 || selectContactSchedule.text.toString().trim().equals("")){
            (mContext as DashboardActivity).showSnackMessage("Select a contact")
            progress_wheel.stopSpinning()
            return
        }
        try {
            doAsync {
                var schedulerObj = SchedulerMasterEntity()
                schedulerObj.scheduler_name = schedulername.text.toString().trim()
                schedulerObj.select_template = selectTemplate.text.toString().trim()
                schedulerObj.template_content = et_templateContent.text.toString().trim()
                schedulerObj.select_mode_id = str_modeoftemplateID
                schedulerObj.select_mode = selectMode.text.toString().trim()
                schedulerObj.select_rule_id = str_ruleoftemplateID
                schedulerObj.select_rule = tv_rule_Of_scheduler.text.toString().trim()
                schedulerObj.select_hour = selectedHr
                schedulerObj.select_minute = selectedMin
                schedulerObj.select_time = this@SchedulerAddFormFrag.hr +":"+ this@SchedulerAddFormFrag.min +":00"
                schedulerObj.repeat_every_month = false
                //  schedulerObj.select_contact = selectContactSchedule.text.toString().trim()
                schedulerObj.isUploaded = true
                schedulerObj.save_date_time = AppUtils.getCurrentDateTimeNew()
                if(schedulerObj.select_rule.equals("Manual",ignoreCase = true)){
                    schedulerObj.isActivityDone = true
                }else{
                    schedulerObj.isActivityDone = false
                }
                var addedContact_idL : ArrayList<String> = ArrayList()

                for(i in 0..contactTickL.size-1){
                    schedulerObj.select_contact_id = contactTickL.get(i).contact_id
                    schedulerObj.select_contact = contactTickL.get(i).contact_name

                    if(calendarView.selectedDates.size == 0){
                        schedulerObj.select_timestamp = ""
                        schedulerObj.select_date = ""
                        AppDatabase.getDBInstance()!!.schedulerMasterDao().insert(schedulerObj)
                    }else{
                        for(j in 0..calendarView.selectedDates.size-1){
                            particular_date_select = calendarView.selectedDates.get(j).year.toString() + "-" + String.format("%02d", calendarView.selectedDates.get(j).month.toInt()
                            ) + "-" + String.format("%02d", calendarView.selectedDates.get(j).day.toInt())
                            schedulerObj.select_date = particular_date_select

                            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val date = simpleDateFormat.parse(particular_date_select+" $hr:$min:00")
                            val timestamp = date?.time
                            schedulerObj.select_timestamp = timestamp.toString()
                            AppDatabase.getDBInstance()!!.schedulerMasterDao().insert(schedulerObj)
                        }
                    }
                    addedContact_idL.add(schedulerObj.select_contact_id)
                }
                uiThread {
                    Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show()
                    if(schedulerObj.select_rule.equals("Manual",ignoreCase = true)){
                        for(l in 0..addedContact_idL.size-1){
                            var shopObj = AppDatabase.getDBInstance()!!.addShopEntryDao().getShopByIdN(addedContact_idL.get(l))
                            MultiFun.generateContactDtlsPdf(shopObj,mContext)
                        }
                        progress_wheel.stopSpinning()
                        (mContext as DashboardActivity).onBackPressed()
                    }else{
                        progress_wheel.stopSpinning()
                        (mContext as DashboardActivity).onBackPressed()
                    }
                }
            }

        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private fun loadTemplateList(schedule_list:ArrayList<ScheduleTemplateEntity>) {
        if (schedule_list.size>0) {
            var genericL : ArrayList<CustomData> = ArrayList()
            for(i in 0..schedule_list.size-1){
                genericL.add(CustomData(schedule_list.get(i).template_id.toString(),schedule_list.get(i).template_name.toString()))
            }
            GenericDialog.newInstance("Template",genericL as ArrayList<CustomData>){
                selectTemplate.setText(it.name)
                str_templateID = it.id
                if (!selectTemplate.text.toString().equals("Manual")){
                    et_templateContent.isEnabled = false
                    et_templateContent.setText(Pref.scheduler_template)
                    et_templateContent.setTextColor(context?.getResources()!!.getColor(R.color.gray))
                }else{
                    et_templateContent.isEnabled =true
                }

            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        } else {
            Toaster.msgShort(mContext, "No Template Found")
        }
    }
    private fun loadModeOfTemplateList(mode_list:ArrayList<ModeTemplateEntity>) {
        if (mode_list.size>0) {
            var genericL : ArrayList<CustomData> = ArrayList()
            for(i in 0..mode_list.size-1){
                genericL.add(CustomData(mode_list.get(i).mode_template_id.toString(),mode_list.get(i).mode_template_name.toString()))
            }
            GenericDialog.newInstance("Mode of Template",genericL as ArrayList<CustomData>){
                selectMode.setText(it.name)
                str_modeoftemplateID = it.id
            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        } else {
            Toaster.msgShort(mContext, "No Mode Found")
        }

    }
    private fun loadRuleOfTemplateList(rule_list:ArrayList<RuleTemplateEntity>) {
        if (rule_list.size>0) {
            var genericL : ArrayList<CustomData> = ArrayList()
            for(i in 0..rule_list.size-1){
                genericL.add(CustomData(rule_list.get(i).rule_template_id.toString(),rule_list.get(i).rule_template_name.toString()))
            }
            GenericDialog.newInstance("Rule of Template",genericL as ArrayList<CustomData>){
                tv_rule_Of_scheduler.setText(it.name)
                str_ruleoftemplateID = it.id
                if (tv_rule_Of_scheduler.text.toString().equals("Manual")){
                    cv_scheduler_timer.visibility = View.GONE
                    cv_calendar.visibility = View.GONE
                    sw_repeatmonth.visibility = View.GONE
                }else{
                    cv_scheduler_timer.visibility = View.VISIBLE
                    cv_calendar.visibility = View.VISIBLE
                    sw_repeatmonth.visibility = View.VISIBLE
                }
            }.show((mContext as DashboardActivity).supportFragmentManager, "")
        } else {
            Toaster.msgShort(mContext, "No Rule Found")
        }
    }
@SuppressLint("SuspiciousIndentation")
private fun loadContactOfTemplateList(contact_list:ArrayList<AddShopDBModelEntity>) {
    if (contact_list.size > 0) {
            val contactDialog = Dialog(mContext)
            contactDialog.setCancelable(true)
            contactDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            contactDialog.setContentView(R.layout.dialog_cont_select)
            val rvContactL = contactDialog.findViewById(R.id.rv_dialog_cont_list) as RecyclerView
            val tvHeader = contactDialog.findViewById(R.id.tv_dialog_cont_sel_header) as TextView
            val submit = contactDialog.findViewById(R.id.tv_dialog_cont_list_submit) as TextView
            val et_contactNameSearch =
                contactDialog.findViewById(R.id.et_dialog_contact_search) as AppCustomEditText
            val cb_selectAll =
                contactDialog.findViewById(R.id.cb_dialog_cont_select_all) as CheckBox
            val iv_close =
                contactDialog.findViewById(R.id.iv_dialog_generic_list_close_icon) as ImageView
            tvHeader.text = "Select Contact(s)"
            iv_close.setOnClickListener {
                contactDialog.dismiss()
            }
        var finalL: ArrayList<ScheduleContactDtls> = ArrayList()
        contactTickL = ArrayList()
        for(i in 0..contact_list.size-1){
            finalL.add(ScheduleContactDtls(contact_list.get(i).shopName,contact_list.get(i).ownerContactNumber,contact_list.get(i).shop_id,false))
        }
        adapterScheduleContactName = AdapterScheduleContactName(mContext, finalL, object : AdapterScheduleContactName.onClick {
            override fun onTickUntick(obj: ScheduleContactDtls, isTick: Boolean) {
                if (isTick) {
                    contactTickL.add(obj)
                    finalL.filter { it.contact_name.equals(obj.contact_name) }.first().isTick = true
                    finalL.filter { it.contact_id.equals(obj.contact_id) }.first().isTick = true

                    tvHeader.text = "Selected Contact(s) : (${contactTickL.size})"


                } else {
                    contactTickL.remove(obj)
                    finalL.filter { it.contact_name.equals(obj.contact_name) }.first().isTick = false
                    tvHeader.text = "Selected Contact(s) : (${contactTickL.size})"
                }
            }
        }, {
            it
                    })
                cb_selectAll.setOnCheckedChangeListener { compoundButton, b ->
                    if (compoundButton.isChecked) {
                        adapterScheduleContactName.selectAll()
                        cb_selectAll.setText("Deselect All")
                    } else {
                        adapterScheduleContactName.deselectAll()
                        cb_selectAll.setText("Select All")
                    }
                }

                et_contactNameSearch.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        adapterScheduleContactName!!.getFilter()
                            .filter(et_contactNameSearch.text.toString().trim())
                    }
                })
                submit.setOnClickListener {
                    if (contactTickL.size > 0) {
                        contactDialog.dismiss()
                        var nameText = ""
                        if(contactTickL.size==1){
                            nameText = contactTickL.get(0).contact_name
                        }else{
                            for(i in 0..contactTickL.size-1) {
                                nameText = nameText+contactTickL.get(i).contact_name+","
                            }
                        }
                        if (nameText.endsWith(",")) {
                            nameText = nameText.substring(0, nameText.length - 1);
                        }
                        selectContactSchedule.setText(nameText)
                    } else {
                        contactDialog.dismiss()
                        Toaster.msgShort(mContext, "Select Contact(s)")
                    }
                }
                rvContactL.adapter = adapterScheduleContactName
                contactDialog.show()
        }
    }

    fun setImage(filePath: String) {
        val file = File(filePath)
        FullImageDialog.getInstance(file.path).show((mContext as DashboardActivity).supportFragmentManager, "")

        var newFile: File? = null

        progress_wheel.spin()
        doAsync {

            val processImage = ProcessImageUtils_v1(mContext, file, 50, true)
            newFile = processImage.ProcessImage()

            uiThread {
                if (newFile != null) {
                    Timber.e("=========Image from new technique==========")
                    documentPic(newFile!!.length(), newFile?.absolutePath!!)
                } else {
                    // Image compression
                    val fileSize = AppUtils.getCompressImage(filePath)
                    documentPic(fileSize, filePath)
                }
            }
        }
    }

    private fun documentPic(fileSize: Long, file_path: String) {
        val fileSizeInKB = fileSize / 1024
        Log.e("Add Document", "image file size after compression=====> $fileSizeInKB KB")

        progress_wheel.stopSpinning()

        if (!TextUtils.isEmpty(Pref.maxFileSize)) {
            if (fileSizeInKB > Pref.maxFileSize.toInt()) {
                (mContext as DashboardActivity).showSnackMessage("More than " + Pref.maxFileSize + " KB file is not allowed")
                return
            }
        }

        filePath = file_path
    }
}