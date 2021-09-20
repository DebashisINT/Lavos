package com.lavos.features.dashboard.presentation.api.dayStartEnd

import com.lavos.app.Pref
import com.lavos.base.BaseResponse
import com.lavos.features.dashboard.presentation.model.DaystartDayendRequest
import com.lavos.features.dashboard.presentation.model.StatusDayStartEnd
import com.lavos.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.lavos.features.stockCompetetorStock.api.AddCompStockApi
import io.reactivex.Observable

class DayStartEndRepository (val apiService: DayStartEndApi){
    fun dayStart(daystartDayendRequest: DaystartDayendRequest): Observable<BaseResponse> {
        return apiService.submitDayStartEnd(daystartDayendRequest)
    }

    fun dayStartEndStatus(date:String): Observable<StatusDayStartEnd> {
        return apiService.statusDayStartEnd(Pref.session_token!!, Pref.user_id!!,date)
    }


}