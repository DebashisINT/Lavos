package com.lavos.features.login.model.productlistmodel

import com.lavos.app.domain.ModelEntity
import com.lavos.app.domain.ProductListEntity
import com.lavos.base.BaseResponse

class ModelListResponse: BaseResponse() {
    var model_list: ArrayList<ModelEntity>? = null
}