package com.lavos.features.stockAddCurrentStock.api

import com.lavos.base.BaseResponse
import com.lavos.features.location.model.ShopRevisitStatusRequest
import com.lavos.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.lavos.features.stockAddCurrentStock.ShopAddCurrentStockRequest
import com.lavos.features.stockAddCurrentStock.model.CurrentStockGetData
import com.lavos.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class ShopAddStockRepository (val apiService : ShopAddStockApi){
    fun shopAddStock(shopAddCurrentStockRequest: ShopAddCurrentStockRequest?): Observable<BaseResponse> {
        return apiService.submShopAddStock(shopAddCurrentStockRequest)
    }

    fun getCurrStockList(sessiontoken: String, user_id: String, date: String): Observable<CurrentStockGetData> {
        return apiService.getCurrStockListApi(sessiontoken, user_id, date)
    }

}