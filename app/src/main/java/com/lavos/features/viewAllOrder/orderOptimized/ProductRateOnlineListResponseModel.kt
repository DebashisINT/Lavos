package com.lavos.features.viewAllOrder.orderOptimized

import com.lavos.app.domain.ProductOnlineRateTempEntity
import com.lavos.base.BaseResponse
import com.lavos.features.login.model.productlistmodel.ProductRateDataModel
import java.io.Serializable

class ProductRateOnlineListResponseModel: BaseResponse(), Serializable {
    var product_rate_list: ArrayList<ProductOnlineRateTempEntity>? = null
}