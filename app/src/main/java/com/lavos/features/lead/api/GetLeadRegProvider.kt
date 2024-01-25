package com.lavos.features.lead.api

import com.lavos.features.NewQuotation.api.GetQuotListRegRepository
import com.lavos.features.NewQuotation.api.GetQutoListApi


object GetLeadRegProvider {
    fun provideList(): GetLeadListRegRepository {
        return GetLeadListRegRepository(GetLeadListApi.create())
    }
}