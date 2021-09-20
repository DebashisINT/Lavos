package com.lavos.features.dashboard.presentation.api.dayStartEnd

import com.lavos.features.stockCompetetorStock.api.AddCompStockApi
import com.lavos.features.stockCompetetorStock.api.AddCompStockRepository

object DayStartEndRepoProvider {
    fun dayStartRepositiry(): DayStartEndRepository {
        return DayStartEndRepository(DayStartEndApi.create())
    }

}