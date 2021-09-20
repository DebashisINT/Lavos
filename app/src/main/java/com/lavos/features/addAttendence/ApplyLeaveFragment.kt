package com.lavos.features.addAttendence

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.NetworkConstant
import com.lavos.app.Pref
import com.lavos.app.domain.LeaveTypeEntity
import com.lavos.app.domain.RouteEntity
import com.lavos.app.utils.AppUtils
import com.lavos.app.utils.FTStorageUtils
import com.lavos.base.BaseResponse
import com.lavos.base.presentation.BaseActivity
import com.lavos.base.presentation.BaseFragment
import com.lavos.features.addAttendence.api.addattendenceapi.AddAttendenceRepoProvider
import com.lavos.features.addAttendence.api.leavetytpeapi.LeaveTypeRepoProvider
import com.lavos.features.addAttendence.model.LeaveTypeResponseModel
import com.lavos.features.addAttendence.model.SendLeaveApprovalInputParams
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.location.LocationWizard
import com.lavos.widgets.AppCustomEditText
import com.lavos.widgets.AppCustomTextView
import com.elvishew.xlog.XLog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

/**
 * Created by Saikat on 05-Aug-20.
 */
class ApplyLeaveFragment : BaseFragment(), View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private lateinit var mContext: Context

    private lateinit var tv_show_date_range: AppCustomTextView
    private lateinit var rl_leave_type_header: RelativeLayout
    private lateinit var tv_leave_type: AppCustomTextView
    private lateinit var iv_leave_type_dropdown: ImageView
    private lateinit var ll_leave_type_list: LinearLayout
    private lateinit var rv_leave_type_list: RecyclerView
    private lateinit var et_leave_reason_text: AppCustomEditText
    private lateinit var tv_submit: AppCustomTextView
    private lateinit var rl_leave_main: RelativeLayout
    private lateinit var progress_wheel: com.pnikosis.materialishprogress.ProgressWheel

    private var startDate = ""
    private var endDate = ""
    private var leaveId = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_apply_leave, container, false)

        initView(view)
        initClickListener()

        return view
    }

    private fun initView(view: View) {
        tv_show_date_range = view.findViewById(R.id.tv_show_date_range)
        rl_leave_type_header = view.findViewById(R.id.rl_leave_type_header)
        tv_leave_type = view.findViewById(R.id.tv_leave_type)
        iv_leave_type_dropdown = view.findViewById(R.id.iv_leave_type_dropdown)
        ll_leave_type_list = view.findViewById(R.id.ll_leave_type_list)
        rv_leave_type_list = view.findViewById(R.id.rv_leave_type_list)
        rv_leave_type_list.layoutManager = LinearLayoutManager(mContext)
        et_leave_reason_text = view.findViewById(R.id.et_leave_reason_text)
        tv_submit = view.findViewById(R.id.tv_submit)
        rl_leave_main = view.findViewById(R.id.rl_leave_main)
        progress_wheel = view.findViewById(R.id.progress_wheel)
        progress_wheel.stopSpinning()

        checkForLeaveTypeData()

        et_leave_reason_text.setOnTouchListener(View.OnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)

            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
            }

            false
        })

        openDateRangeCalendar()
    }

    private fun openDateRangeCalendar() {
        val now = Calendar.getInstance(Locale.ENGLISH)
        now.add(Calendar.DAY_OF_MONTH, +1)
        val dpd = com.borax12.materialdaterangepicker.date.DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        )
        dpd.isAutoHighlight = true
        val tomorrowsDateLong = Calendar.getInstance(Locale.ENGLISH).timeInMillis + (1000 * 60 * 60 * 24)
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = tomorrowsDateLong
        dpd.minDate = cal
        dpd.show((context as Activity).fragmentManager, "Datepickerdialog")
    }

    private fun checkForLeaveTypeData() {
        if (AppDatabase.getDBInstance()?.leaveTypeDao()?.getAll()!!.isEmpty())
            getLeaveTypeList()
        else {
            setLeaveTypeAdapter(AppDatabase.getDBInstance()?.leaveTypeDao()?.getAll() as ArrayList<LeaveTypeEntity>)
        }
    }

    private fun getLeaveTypeList() {
        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        val repository = LeaveTypeRepoProvider.leaveTypeListRepoProvider()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.getLeaveTypeList()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as LeaveTypeResponseModel
                            if (response.status == NetworkConstant.SUCCESS) {
                                val list = response.leave_type_list

                                if (list != null && list.size > 0) {
                                    doAsync {

                                        for (i in list.indices) {

                                            val leave = LeaveTypeEntity()
                                            leave.id = list[i].id?.toInt()!!
                                            leave.leave_type = list[i].type_name
                                            AppDatabase.getDBInstance()?.leaveTypeDao()?.insert(leave)
                                        }

                                        uiThread {
                                            setLeaveTypeAdapter(AppDatabase.getDBInstance()?.leaveTypeDao()?.getAll() as ArrayList<LeaveTypeEntity>)
                                            progress_wheel.stopSpinning()
                                        }
                                    }
                                } else
                                    progress_wheel.stopSpinning()
                            } else
                                progress_wheel.stopSpinning()
                        }, { error ->
                            progress_wheel.stopSpinning()
                            (mContext as DashboardActivity).showSnackMessage("ERROR")
                        })
        )
    }

    private fun setLeaveTypeAdapter(leaveTypeList: ArrayList<LeaveTypeEntity>?) {
        rv_leave_type_list.adapter = LeaveTypeListAdapter(mContext, leaveTypeList!!, object : LeaveTypeListAdapter.OnLeaveTypeClickListener {
            override fun onLeaveTypeClick(leaveType: LeaveTypeEntity?, adapterPosition: Int) {
                tv_leave_type.text = leaveType?.leave_type
                (mContext as DashboardActivity).leaveType = leaveType?.leave_type!!
                leaveId = leaveType.id.toString()
                ll_leave_type_list.visibility = View.GONE
                iv_leave_type_dropdown.isSelected = false
            }
        })
    }

    private fun initClickListener() {
        tv_submit.setOnClickListener(this)
        rl_leave_type_header.setOnClickListener(this)
        rl_leave_main.setOnClickListener(null)
        tv_show_date_range.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {

            R.id.tv_submit -> {
                AppUtils.hideSoftKeyboard(mContext as DashboardActivity)
                visibilityCheck()
            }

            R.id.rl_leave_type_header -> {
                if (iv_leave_type_dropdown.isSelected) {
                    iv_leave_type_dropdown.isSelected = false
                    ll_leave_type_list.visibility = View.GONE
                } else {
                    iv_leave_type_dropdown.isSelected = true
                    ll_leave_type_list.visibility = View.VISIBLE
                }
            }

            R.id.tv_show_date_range -> {
                openDateRangeCalendar()
            }
        }
    }

    private fun visibilityCheck() {
        if (TextUtils.isEmpty(leaveId))
            (mContext as DashboardActivity).showSnackMessage("Please select leave type")
        else if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate))
            (mContext as DashboardActivity).showSnackMessage("Please select date range")
        else if (Pref.willLeaveApprovalEnable && TextUtils.isEmpty(et_leave_reason_text.text.toString().trim()))
            (mContext as DashboardActivity).showSnackMessage("Please enter reason")
        else
            callLeaveApprovalApi()
    }

    private fun callLeaveApprovalApi() {

        if (!AppUtils.isOnline(mContext)) {
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        val leaveApproval = SendLeaveApprovalInputParams()
        leaveApproval.session_token = Pref.session_token!!
        leaveApproval.user_id = Pref.user_id!!
        leaveApproval.leave_from_date = startDate
        leaveApproval.leave_to_date = endDate
        leaveApproval.leave_type = leaveId

        if (TextUtils.isEmpty(Pref.current_latitude))
            leaveApproval.leave_lat = "0.0"
        else
            leaveApproval.leave_lat = Pref.current_latitude

        if (TextUtils.isEmpty(Pref.current_longitude))
            leaveApproval.leave_long = "0.0"
        else
            leaveApproval.leave_long = Pref.current_longitude

        if (TextUtils.isEmpty(Pref.current_latitude))
            leaveApproval.leave_add = ""
        else
            leaveApproval.leave_add = LocationWizard.getLocationName(mContext, Pref.current_latitude.toDouble(), Pref.current_longitude.toDouble())


        if (!TextUtils.isEmpty(et_leave_reason_text.text.toString().trim()))
            leaveApproval.leave_reason = et_leave_reason_text.text.toString().trim()

        XLog.d("=========Apply Leave Input Params==========")
        XLog.d("session_token======> " + leaveApproval.session_token)
        XLog.d("user_id========> " + leaveApproval.user_id)
        XLog.d("leave_from_date=======> " + leaveApproval.leave_from_date)
        XLog.d("leave_to_date=======> " + leaveApproval.leave_to_date)
        XLog.d("leave_type========> " + leaveApproval.leave_type)
        XLog.d("leave_lat========> " + leaveApproval.leave_lat)
        XLog.d("leave_long========> " + leaveApproval.leave_long)
        XLog.d("leave_add========> " + leaveApproval.leave_add)
        XLog.d("leave_reason========> " + leaveApproval.leave_reason)
        XLog.d("===============================================")

        val repository = AddAttendenceRepoProvider.leaveApprovalRepo()
        progress_wheel.spin()
        BaseActivity.compositeDisposable.add(
                repository.sendLeaveApproval(leaveApproval)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            progress_wheel.stopSpinning()
                            val response = result as BaseResponse
                            XLog.d("Apply Leave Response Code========> " + response.status)
                            XLog.d("Apply Leave Response Msg=========> " + response.message)
                            BaseActivity.isApiInitiated = false

                            if (response.status == NetworkConstant.SUCCESS) {
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)

                                Handler().postDelayed(Runnable {
                                    (mContext as DashboardActivity).onBackPressed()
                                }, 500)

                            } else {
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            XLog.d("Apply Leave Response ERROR=========> " + error.message)
                            BaseActivity.isApiInitiated = false
                            progress_wheel.stopSpinning()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                        })
        )
    }

    override fun onDateSet(datePickerDialog: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int, yearEnd: Int, monthOfYearEnd: Int,
                           dayOfMonthEnd: Int) {

        datePickerDialog?.minDate = Calendar.getInstance(Locale.ENGLISH)
        var monthOfYear = monthOfYear
        var monthOfYearEnd = monthOfYearEnd
        var day = "" + dayOfMonth
        var dayEnd = "" + dayOfMonthEnd
        if (dayOfMonth < 10)
            day = "0$dayOfMonth"
        if (dayOfMonthEnd < 10)
            dayEnd = "0$dayOfMonthEnd"
        var fronString: String = day + "-" + FTStorageUtils.formatMonth((monthOfYear + 1).toString() + "") + "-" + year
        var endString: String = dayEnd + "-" + FTStorageUtils.formatMonth((monthOfYearEnd + 1).toString() + "") + "-" + yearEnd
        if (AppUtils.getStrinTODate(endString).before(AppUtils.getStrinTODate(fronString))) {
            (mContext as DashboardActivity).showSnackMessage("Your end date is before start date.")
            return
        }
        val date = "Leave: From " + day + AppUtils.getDayNumberSuffix(day.toInt()) + FTStorageUtils.formatMonth((++monthOfYear).toString() + "") + " " + year + " To " + dayEnd + AppUtils.getDayNumberSuffix(dayEnd.toInt()) + FTStorageUtils.formatMonth((++monthOfYearEnd).toString() + "") + " " + yearEnd
        tv_show_date_range.visibility = View.VISIBLE
        tv_show_date_range.text = date

        startDate = AppUtils.convertFromRightToReverseFormat(fronString)
        endDate = AppUtils.convertFromRightToReverseFormat(endString)
    }
}