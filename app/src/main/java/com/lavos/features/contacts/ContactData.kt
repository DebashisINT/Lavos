package com.lavos.features.contacts

import com.lavos.app.domain.CallHisEntity
import com.lavos.app.domain.CompanyMasterEntity
import com.lavos.app.domain.SourceMasterEntity
import com.lavos.app.domain.StageMasterEntity
import com.lavos.app.domain.StatusMasterEntity
import com.lavos.app.domain.TeamListEntity
import com.lavos.app.domain.TypeMasterEntity
import com.lavos.base.BaseResponse

data class ContactGr(var gr_id:String="",var gr_name:String="")
data class ContactDtls(val gr_name:String="", val name:String="", val number:String="", var isTick:Boolean = false)
data class ScheduleContactDtls(val contact_name:String="", val contact_number:String="", val contact_id:String="", var isTick:Boolean = false)

data class ContactMasterRes(var company_list:ArrayList<CompanyMasterEntity>):BaseResponse()
data class TypeMasterRes(var type_list:ArrayList<TypeMasterEntity>):BaseResponse()
data class StatusMasterRes(var status_list:ArrayList<StatusMasterEntity>):BaseResponse()
data class TeamListRes(var member_list:ArrayList<TeamListEntity>):BaseResponse()


data class CustomData(var id:String="",var name:String="")

data class SourceMasterRes(var source_list:ArrayList<SourceMasterEntity>):BaseResponse()
data class StageMasterRes(var stage_list:ArrayList<StageMasterEntity>):BaseResponse()

data class CallHisDtls(var user_id:String="",var call_his_list:ArrayList<CallHisEntity> = ArrayList()) :BaseResponse()

data class CompanyReqData(var created_by:String = "",var session_token:String="",var company_name_list:ArrayList<CompanyName> = ArrayList())

data class CompanyName(var company_name:String="")


