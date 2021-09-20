package com.lavos.features.stockAddCurrentStock.api

import com.lavos.features.location.shopRevisitStatus.ShopRevisitStatusApi
import com.lavos.features.location.shopRevisitStatus.ShopRevisitStatusRepository

object ShopAddStockProvider {
    fun provideShopAddStockRepository(): ShopAddStockRepository {
        return ShopAddStockRepository(ShopAddStockApi.create())
    }
}