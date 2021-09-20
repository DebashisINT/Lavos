package com.lavos.features.stockCompetetorStock.api

import com.lavos.base.BaseResponse
import com.lavos.features.orderList.model.NewOrderListResponseModel
import com.lavos.features.stockCompetetorStock.ShopAddCompetetorStockRequest
import com.lavos.features.stockCompetetorStock.model.CompetetorStockGetData
import io.reactivex.Observable

class AddCompStockRepository(val apiService:AddCompStockApi){

    fun addCompStock(shopAddCompetetorStockRequest: ShopAddCompetetorStockRequest): Observable<BaseResponse> {
        return apiService.submShopCompStock(shopAddCompetetorStockRequest)
    }

    fun getCompStockList(sessiontoken: String, user_id: String, date: String): Observable<CompetetorStockGetData> {
        return apiService.getCompStockList(sessiontoken, user_id, date)
    }
}