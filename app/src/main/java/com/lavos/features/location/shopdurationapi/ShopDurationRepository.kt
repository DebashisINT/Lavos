package com.lavos.features.location.shopdurationapi

import com.lavos.app.Pref
import com.lavos.base.BaseResponse
import com.lavos.features.location.model.MeetingDurationInputParams
import com.lavos.features.location.model.ShopDurationRequest
import com.lavos.features.location.model.VisitRemarksResponseModel
import io.reactivex.Observable

/**
 * Created by Pratishruti on 29-11-2017.
 */
class ShopDurationRepository(val apiService: ShopDurationApi) {
    fun shopDuration(shopDuration: ShopDurationRequest?): Observable<ShopDurationRequest> {
        return apiService.submitShopDuration(shopDuration)
    }

    fun meetingDuration(meetingDuration: MeetingDurationInputParams?): Observable<BaseResponse> {
        return apiService.submitMeetingDuration(meetingDuration)
    }

    fun getRemarksList(): Observable<VisitRemarksResponseModel> {
        return apiService.getRemarksList(Pref.session_token!!, Pref.user_id!!)
    }
}