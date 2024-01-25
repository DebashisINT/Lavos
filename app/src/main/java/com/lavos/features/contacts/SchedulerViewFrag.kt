package com.lavos.features.contacts

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lavos.R
import com.lavos.app.AppDatabase
import com.lavos.app.domain.AddShopDBModelEntity
import com.lavos.app.domain.SchedulerMasterEntity
import com.lavos.app.types.FragType
import com.lavos.app.uiaction.IntentActionable
import com.lavos.app.utils.AppUtils
import com.lavos.app.widgets.MovableFloatingActionButton
import com.lavos.base.presentation.BaseFragment
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.nearbyshops.presentation.UpdateShopStatusDialog
import com.pnikosis.materialishprogress.ProgressWheel
import java.util.Calendar

class SchedulerViewFrag : BaseFragment(), View.OnClickListener {
    private lateinit var mContext: Context
    private lateinit var progress_wheel: ProgressWheel
    private lateinit var rvSchedulL: RecyclerView
    private lateinit var tvNodata: TextView
    private lateinit var mFab: MovableFloatingActionButton
    private lateinit var adapterSchedulerList: AdapterSchedulerList

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object{
        fun getInstance(objects: Any): SchedulerViewFrag {
            val objFragment = SchedulerViewFrag()
            var obj = objects as String

            return objFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_scheduler_add, container, false)
        initView(view)
        return view
    }
    private fun initView(view: View) {
        progress_wheel = view.findViewById(R.id.progress_wheel_frag_schedule_his_list)
        rvSchedulL = view.findViewById(R.id.rv_frag_scheduler_list)
        tvNodata = view.findViewById(R.id.tv_frag_schedul_his_noData)
        mFab = view.findViewById(R.id.fab_frag_add_schedule_in_contact)

        mFab.setOnClickListener(this)
        mFab.setCustomClickListener {
            (mContext as DashboardActivity).loadFragment(FragType.SchedulerAddFormFrag, true, "")
        }

        callSchedulerList()
    }

    fun callSchedulerList() {
        var contL : ArrayList<SchedulerMasterEntity> = ArrayList()
        contL = AppDatabase.getDBInstance()!!.schedulerMasterDao().getAll() as ArrayList<SchedulerMasterEntity>

        if(contL.size>0){
            tvNodata.visibility = View.GONE
            (mContext as DashboardActivity).setTopBarTitle("Scheduler(s) : ${contL.size}")
            adapterSchedulerList = AdapterSchedulerList(mContext,contL,"",object :AdapterSchedulerList.onClick{
                override fun onWhatsClick(obj: SchedulerMasterEntity) {
                }

                override fun onEmailClick(obj: SchedulerMasterEntity) {
                   // IntentActionable.sendMail(mContext, obj.ownerEmailId, "")
                }

            })
            rvSchedulL.adapter = adapterSchedulerList
            rvSchedulL.visibility = View.VISIBLE
        }else{
            (mContext as DashboardActivity).setTopBarTitle("Contact(s)")
            tvNodata.visibility = View.VISIBLE
            rvSchedulL.visibility = View.GONE
        }

    }

    override fun onClick(p0: View?) {

    }


}