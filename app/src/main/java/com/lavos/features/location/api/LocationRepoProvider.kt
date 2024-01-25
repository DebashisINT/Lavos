package com.lavos.features.location.api

import com.lavos.features.location.shopdurationapi.ShopDurationApi
import com.lavos.features.location.shopdurationapi.ShopDurationRepository


object LocationRepoProvider {
    fun provideLocationRepository(): LocationRepo {
        return LocationRepo(LocationApi.create())
    }
}