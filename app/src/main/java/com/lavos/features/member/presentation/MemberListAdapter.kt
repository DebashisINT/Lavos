package com.lavos.features.member.presentation

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.lavos.R
import com.lavos.app.Pref
import com.lavos.features.dashboard.presentation.DashboardActivity
import com.lavos.features.member.model.TeamListDataModel
import kotlinx.android.synthetic.main.inflate_member_list_item.view.*


/**
 * Created by Saikat on 29-01-2020.
 */
class MemberListAdapter(context: Context, val teamList: ArrayList<TeamListDataModel>, val listener: OnClickListener) :
        RecyclerView.Adapter<MemberListAdapter.MyViewHolder>(), Filterable {

    private val layoutInflater: LayoutInflater
    private var context: Context
    private var mTeamList: ArrayList<TeamListDataModel>? = null
    private var tempTeamList: ArrayList<TeamListDataModel>? = null
    private var filterTeamList: ArrayList<TeamListDataModel>? = null

    init {
        layoutInflater = LayoutInflater.from(context)
        this.context = context
        mTeamList = ArrayList()
        tempTeamList = ArrayList()
        filterTeamList = ArrayList()

        mTeamList?.addAll(teamList)
        tempTeamList?.addAll(teamList)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindItems(context, mTeamList!!, listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = layoutInflater.inflate(R.layout.inflate_member_list_item, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mTeamList!!.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(context: Context, teamList: ArrayList<TeamListDataModel>, listener: OnClickListener) {

            itemView.tv_name.text = teamList[adapterPosition].user_name

            itemView.tv_shop_details.text = "Click for " + Pref.shopText + " Details"

            if (Pref.isActivatePJPFeature) {
                /*if (Pref.isAllowPJPUpdateForTeam)
                    itemView.iv_pjp_icon.visibility = View.VISIBLE
                else {
                    if (teamList[adapterPosition].user_id == Pref.user_id)
                        itemView.iv_pjp_icon.visibility = View.VISIBLE
                    else
                        itemView.iv_pjp_icon.visibility = View.GONE
                }*/
                itemView.iv_pjp_icon.visibility = View.VISIBLE
            } else
                itemView.iv_pjp_icon.visibility = View.GONE

            if((context as DashboardActivity).isAllTeam)
                itemView.tv_team_details.visibility = View.GONE
            else
                itemView.tv_team_details.visibility = View.VISIBLE


            itemView.tv_team_details.setOnClickListener({
                listener.onTeamClick(teamList[adapterPosition])
            })

            itemView.tv_shop_details.setOnClickListener({
                listener.onShopClick(teamList[adapterPosition])
            })

            itemView.iv_call_icon.setOnClickListener {
                listener.onCallClick(teamList[adapterPosition])
            }

            itemView.iv_pjp_icon.setOnClickListener {
                listener.onPjpClick(teamList[adapterPosition])
            }

            itemView.iv_map_icon.setOnClickListener {
                listener.onLocClick(teamList[adapterPosition])
            }

            itemView.iv_job.visibility=View.GONE
        }
    }

    interface OnClickListener {
        fun onTeamClick(team: TeamListDataModel)

        fun onShopClick(team: TeamListDataModel)

        fun onCallClick(team: TeamListDataModel)

        fun onPjpClick(team: TeamListDataModel)

        fun onLocClick(team: TeamListDataModel)

        fun getSize(size: Int)
    }

    override fun getFilter(): Filter {
        return SearchFilter()
    }

    inner class SearchFilter : Filter() {
        override fun performFiltering(p0: CharSequence?): FilterResults {
            val results = FilterResults()

            filterTeamList?.clear()

            tempTeamList?.indices!!
                    .filter { tempTeamList?.get(it)?.user_name?.toLowerCase()?.contains(p0?.toString()?.toLowerCase()!!)!! }
                    .forEach { filterTeamList?.add(tempTeamList?.get(it)!!) }

            results.values = filterTeamList
            results.count = filterTeamList?.size!!

            return results
        }

        override fun publishResults(p0: CharSequence?, results: FilterResults?) {

            try {
                filterTeamList = results?.values as ArrayList<TeamListDataModel>?
                mTeamList?.clear()
                val hashSet = HashSet<String>()
                if (filterTeamList != null) {

                    filterTeamList?.indices!!
                            .filter { hashSet.add(filterTeamList?.get(it)?.user_id!!) }
                            .forEach { mTeamList?.add(filterTeamList?.get(it)!!) }

                    listener.getSize(mTeamList?.size!!)

                    notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshList(teamList: ArrayList<TeamListDataModel>) {
        mTeamList?.clear()
        mTeamList?.addAll(teamList)

        tempTeamList?.clear()
        tempTeamList?.addAll(teamList)

        if (filterTeamList == null)
            filterTeamList = ArrayList()
        filterTeamList?.clear()

        notifyDataSetChanged()
    }

}