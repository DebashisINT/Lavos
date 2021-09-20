package com.lavos.features.member.presentation

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.elvishew.xlog.XLog
import com.lavos.R
import com.lavos.app.NetworkConstant
import com.lavos.app.Pref
import com.lavos.app.SearchListener
import com.lavos.app.types.FragType
import com.lavos.app.uiaction.IntentActionable
import com.lavos.app.utils.AppUtils
import com.lavos.base.presentation.BaseActivity
import com.lavos.base.presentation.BaseFragment
import com.lavos.features.commondialog.presentation.CommonDialog
import com.lavos.features.commondialog.presentation.CommonDialogClickListener
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.member.api.TeamRepoProvider
import com.lavos.features.member.model.TeamListDataModel
import com.lavos.features.member.model.TeamListResponseModel
import com.lavos.widgets.AppCustomTextView
import com.pnikosis.materialishprogress.ProgressWheel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Saikat on 29-01-2020.
 */
class MemberListFragment : BaseFragment() {

    private lateinit var mContext: Context

    private lateinit var rv_member_list: RecyclerView
    private lateinit var tv_no_data_available: AppCustomTextView
    private lateinit var progress_wheel: ProgressWheel
    private lateinit var rl_member_list_main: RelativeLayout
    private lateinit var tv_team_struct: AppCustomTextView
    private lateinit var tv_member_no: AppCustomTextView

    private var userId = ""
    private var isListUpdated = false
    private var isFirstScreen = false
    private var adapter: MemberListAdapter? = null
    private var member_list: ArrayList<TeamListDataModel>? = null

    companion object {

        fun newInstance(userId: Any): MemberListFragment {
            val fragment = MemberListFragment()

            if (userId is String) {
                val bundle = Bundle()
                bundle.putString("user_id", userId)
                fragment.arguments = bundle
            }

            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

        userId = arguments?.getString("user_id")?.toString()!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_member_list, container, false)

        initView(view)
        initSearchListener()
        getTeamList()

        return view
    }

    private fun initView(view: View) {
        rv_member_list = view.findViewById(R.id.rv_member_list)
        rv_member_list.layoutManager = LinearLayoutManager(mContext)

        tv_no_data_available = view.findViewById(R.id.tv_no_data_available)
        tv_team_struct = view.findViewById(R.id.tv_team_struct)

        progress_wheel = view.findViewById(R.id.progress_wheel)
        progress_wheel.stopSpinning()

        rl_member_list_main = view.findViewById(R.id.rl_member_list_main)
        rl_member_list_main.setOnClickListener(null)

        tv_member_no = view.findViewById(R.id.tv_member_no)

        if ((mContext as DashboardActivity).isAllTeam)
            tv_member_no.visibility = View.VISIBLE
        else
            tv_member_no.visibility = View.GONE

        setHierarchyData()
    }

    private fun setHierarchyData() {
        tv_team_struct.apply {
            (mContext as DashboardActivity).teamHierarchy.takeIf { it.isNotEmpty() }?.let {
                visibility = View.VISIBLE
                isFirstScreen = false

                it.forEachIndexed { i, name ->
                    text = if (i == 0)
                        name
                    else
                        text.toString().trim() + "-> " + name

                }

            } ?: let {
                visibility = View.GONE
                isFirstScreen = true
            }
        }
    }

    private fun initSearchListener() {
        (mContext as DashboardActivity).setSearchListener(object : SearchListener {
            override fun onSearchQueryListener(query: String) {
                if (query.isBlank()) {
                    member_list?.let {
                        adapter?.refreshList(it)
                        tv_member_no.text = "Total member(s): " + it.size
                    }
                    //initAdapter(shop_list!!)
                } else {
                    adapter?.filter?.filter(query)
                }
            }
        })
    }


    private fun getTeamList() {

        if (!AppUtils.isOnline(mContext)) {
            tv_no_data_available.visibility = View.VISIBLE
            (mContext as DashboardActivity).showSnackMessage(getString(R.string.no_internet))
            return
        }

        progress_wheel.spin()
        val repository = TeamRepoProvider.teamRepoProvider()
        BaseActivity.compositeDisposable.add(
                repository.teamList(userId, isFirstScreen, (mContext as DashboardActivity).isAllTeam)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as TeamListResponseModel
                            XLog.d("GET TEAM DATA : " + "RESPONSE : " + response.status + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + response.message)
                            progress_wheel.stopSpinning()
                            if (response.status == NetworkConstant.SUCCESS) {

                                response.team_struct?.let {
                                    tv_team_struct.text = it
                                }

                                if (response.member_list != null && response.member_list!!.size > 0) {
                                    member_list = response.member_list!!
                                    initAdapter(response.member_list!!)
                                } else {
                                    tv_no_data_available.visibility = View.VISIBLE
                                    (mContext as DashboardActivity).showSnackMessage(response.message!!)
                                }
                            } else {
                                tv_no_data_available.visibility = View.VISIBLE
                                (mContext as DashboardActivity).showSnackMessage(response.message!!)
                            }

                        }, { error ->
                            progress_wheel.stopSpinning()
                            XLog.d("GET TEAM DATA : " + "ERROR : " + "\n" + "Time : " + AppUtils.getCurrentDateTime() + ", USER :" + Pref.user_name + ",MESSAGE : " + error.localizedMessage)
                            error.printStackTrace()
                            (mContext as DashboardActivity).showSnackMessage(getString(R.string.something_went_wrong))
                            tv_no_data_available.visibility = View.VISIBLE
                        })
        )
    }

    private fun initAdapter(member_list: ArrayList<TeamListDataModel>) {
        tv_no_data_available.visibility = View.GONE
        tv_member_no.text = "Total member(s): " + member_list.size
        adapter = MemberListAdapter(mContext, member_list, object : MemberListAdapter.OnClickListener {

            override fun getSize(size: Int) {
                tv_member_no.text = "Total member(s): " + size
            }

            override fun onLocClick(team: TeamListDataModel) {
                (mContext as DashboardActivity).loadFragment(FragType.MemberActivityFragment, true, team.user_id)
            }

            override fun onPjpClick(team: TeamListDataModel) {
                (mContext as DashboardActivity).loadFragment(FragType.MemberPJPListFragment, true, team/*.user_id*/)
            }

            override fun onCallClick(team: TeamListDataModel) {
                if (TextUtils.isEmpty(team.contact_no) || team.contact_no.equals("null", ignoreCase = true)
                        || !AppUtils.isValidateMobile(team.contact_no)) {
                    (mContext as DashboardActivity).showSnackMessage(getString(R.string.error_phn_no_unavailable))
                } else {
                    IntentActionable.initiatePhoneCall(mContext, team.contact_no)
                }
            }

            override fun onShopClick(team: TeamListDataModel) {
                //(mContext as DashboardActivity).loadFragment(FragType.MemberShopListFragment, true, member_list[adapterPosition].user_id)
                CommonDialog.getInstance(AppUtils.hiFirstNameText(), "What you like to do?", getString(R.string.total_shops), getString(R.string.new_visit_shop), false, false, true, object : CommonDialogClickListener {
                    override fun onLeftClick() {
                        checkTeamHierarchyList(team.user_name)
                        if (Pref.isShowPartyInAreaWiseTeam) {
                            (mContext as DashboardActivity).loadFragment(FragType.AreaListFragment, true, team.user_id)
                            (mContext as DashboardActivity).isAllMemberShop = true
                        } else
                            (mContext as DashboardActivity).loadFragment(FragType.MemberAllShopListFragment, true, team.user_id)
                    }

                    override fun onRightClick(editableData: String) {
                        checkTeamHierarchyList(team.user_name)

                        if (Pref.isShowPartyInAreaWiseTeam) {
                            (mContext as DashboardActivity).loadFragment(FragType.AreaListFragment, true, team.user_id)
                            (mContext as DashboardActivity).isAllMemberShop = false
                        } else
                            (mContext as DashboardActivity).loadFragment(FragType.MemberShopListFragment, true, team.user_id)
                    }

                }).show((mContext as DashboardActivity).supportFragmentManager, "")
            }

            override fun onTeamClick(team: TeamListDataModel) {
                //(mContext as DashboardActivity).isAddBackStack = true

                (mContext as DashboardActivity).apply {

                    /*teamHierarchy = if (teamHierarchy.isNotEmpty())
                        teamHierarchy + "-> " + member_list[adapterPosition].user_name
                    else
                        member_list[adapterPosition].user_name*/

                    teamHierarchy.add(team.user_name)

                    loadFragment(FragType.MemberListFragment, true, team.user_id)
                }
            }
        })
        rv_member_list.adapter = adapter
    }

    private fun checkTeamHierarchyList(user_name: String) {
        (mContext as DashboardActivity).teamHierarchy.apply {
            //if .(size == 0) {
            add(user_name)
            /*isListUpdated = true
        }*/
        }
    }

    fun updateMemberTeamHierarchy() {
        try {
            (mContext as DashboardActivity).teamHierarchy.apply {
                //if (isListUpdated) {
                removeAt(size - 1)
                /*  isListUpdated = false
            }*/
            }
            initSearchListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateTeamHierarchy() {
        /*if (tv_team_struct.visibility == View.GONE || !(mContext as DashboardActivity).teamHierarchy.contains("->")) {
            (mContext as DashboardActivity).teamHierarchy = ""
        }*/

        (mContext as DashboardActivity).teamHierarchy.apply {
            removeAt(size - 1)
            setHierarchyData()
        }
    }
}