package com.lavos.features.contacts

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lavos.R
import com.lavos.app.Pref
import com.lavos.app.domain.AddShopDBModelEntity
import com.lavos.app.domain.SchedulerMasterEntity
import com.lavos.app.utils.AppUtils
import kotlinx.android.synthetic.main.row_call_log_list.view.tv_row_call_log_his_sync_status
import kotlinx.android.synthetic.main.row_churn_shop_l.view.iv_tag1
import kotlinx.android.synthetic.main.row_contact_list.view.iv_row_cont_list_cont_number
import kotlinx.android.synthetic.main.row_contact_list.view.iv_row_cont_list_cont_number_email
import kotlinx.android.synthetic.main.row_contact_list.view.iv_row_cont_list_cont_number_info
import kotlinx.android.synthetic.main.row_contact_list.view.iv_row_cont_list_cont_number_whatsapp
import kotlinx.android.synthetic.main.row_contact_list.view.iv_row_contact_edit
import kotlinx.android.synthetic.main.row_contact_list.view.iv_row_contact_share
import kotlinx.android.synthetic.main.row_contact_list.view.iv_row_contact_sync_unsync
import kotlinx.android.synthetic.main.row_contact_list.view.ll_row_contact_addr_root
import kotlinx.android.synthetic.main.row_contact_list.view.ll_row_contact_list_auto_activity
import kotlinx.android.synthetic.main.row_contact_list.view.ll_row_contact_list_update_status
import kotlinx.android.synthetic.main.row_contact_list.view.row_contact_list_update_status_view
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_added_addition_source
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_added_dt_ti
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_addr
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_cont_assign_to
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_cont_number
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_cont_number_email
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_cont_number_whatsapp
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_cont_source
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_cont_stage
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_cont_status
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_cont_type
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_expctd_sales_value
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_name
import kotlinx.android.synthetic.main.row_contact_list.view.tv_row_cont_list_name_initial
import kotlinx.android.synthetic.main.row_schedule_list.view.tv_row_schedul_list_name
import kotlinx.android.synthetic.main.row_schedule_list.view.tv_row_schedul_selected_template
import kotlinx.android.synthetic.main.row_schedule_list.view.tv_row_schedul_selected_template_contact
import kotlinx.android.synthetic.main.row_schedule_list.view.tv_row_schedul_selected_template_date
import kotlinx.android.synthetic.main.row_schedule_list.view.tv_row_schedul_selected_template_hourminute
import kotlinx.android.synthetic.main.row_schedule_list.view.tv_row_schedul_selected_template_mode
import kotlinx.android.synthetic.main.row_schedule_list.view.tv_row_schedul_selected_template_rule
import kotlinx.android.synthetic.main.row_schedule_list.view.tv_row_schedul_template_content


class AdapterSchedulerList(var mContext:Context, var shopL:ArrayList<SchedulerMasterEntity>, var searchText:String, var listner:onClick):
    RecyclerView.Adapter<AdapterSchedulerList.ContactListViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactListViewHolder {
        var v = LayoutInflater.from(mContext).inflate(R.layout.row_schedule_list,parent,false)
        return ContactListViewHolder(v)
    }

    override fun onBindViewHolder(holder: ContactListViewHolder, position: Int) {
        holder.bindItems()
    }

    override fun getItemCount(): Int {
        return shopL.size
    }

    inner class ContactListViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bindItems(){
            itemView.apply {
                tv_row_schedul_list_name.text= shopL.get(adapterPosition).scheduler_name!!.toString().trim()
                tv_row_schedul_template_content.setText(shopL.get(adapterPosition).template_content.toString().trim())
                tv_row_schedul_selected_template.text = shopL.get(adapterPosition).select_template.toString().trim()
                tv_row_schedul_selected_template_mode.text = shopL.get(adapterPosition).select_mode.toString().trim()
                tv_row_schedul_selected_template_rule.text = shopL.get(adapterPosition).select_rule.toString().trim()
                tv_row_schedul_selected_template_hourminute.text = shopL.get(adapterPosition).select_hour+" "+shopL.get(adapterPosition).select_minute.toString().trim()
                tv_row_schedul_selected_template_date.text = shopL.get(adapterPosition).select_date.toString().trim()
                tv_row_schedul_selected_template_date.text =
                    AppUtils.getDateFormat(shopL.get(adapterPosition).select_date.toString().trim())
                        .toString()
                tv_row_schedul_selected_template_contact.text = shopL.get(adapterPosition).select_contact.toString().trim()

                if(shopL.get(adapterPosition).isActivityDone){
                    itemView.iv_row_contact_sync_unsync.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff888888")))
                }else{
                    itemView.iv_row_contact_sync_unsync.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#009cc4")))
                }

                if (shopL.get(adapterPosition).select_mode.toString().trim().equals("WhatsApp")){
                    iv_row_cont_list_cont_number_whatsapp.setOnClickListener {
                        listner.onWhatsClick(shopL.get(adapterPosition))
                    }
                }
                else if (shopL.get(adapterPosition).select_mode.toString().trim().equals("Email")){
                    iv_row_cont_list_cont_number_email.setOnClickListener {
                            listner.onEmailClick(shopL.get(adapterPosition))
                    }
                }
            }
        }
    }

    interface onClick{
        fun onWhatsClick(obj:SchedulerMasterEntity)
        fun onEmailClick(obj:SchedulerMasterEntity)
    }

}