package com.lavos.features.NewQuotation.api



object GetQuotRegProvider {

    fun provideSaveButton(): GetQuotListRegRepository {
        return GetQuotListRegRepository(GetQutoListApi.create())
    }


}