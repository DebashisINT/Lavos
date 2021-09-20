package com.lavos.features.location.shopRevisitStatus

import com.lavos.base.BaseResponse
import com.lavos.features.location.model.ShopDurationRequest
import com.lavos.features.location.model.ShopRevisitStatusRequest
import io.reactivex.Observable

class ShopRevisitStatusRepository(val apiService : ShopRevisitStatusApi) {
    fun shopRevisitStatus(shopRevisitStatus: ShopRevisitStatusRequest?): Observable<BaseResponse> {
        return apiService.submShopRevisitStatus(shopRevisitStatus)
    }
}