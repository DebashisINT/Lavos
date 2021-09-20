package com.lavos.features.location.shopRevisitStatus

import com.lavos.features.location.shopdurationapi.ShopDurationApi
import com.lavos.features.location.shopdurationapi.ShopDurationRepository

object ShopRevisitStatusRepositoryProvider {
    fun provideShopRevisitStatusRepository(): ShopRevisitStatusRepository {
        return ShopRevisitStatusRepository(ShopRevisitStatusApi.create())
    }
}