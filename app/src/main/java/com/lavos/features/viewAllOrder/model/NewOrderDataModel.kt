package com.lavos.features.viewAllOrder.model

import com.lavos.app.domain.NewOrderColorEntity
import com.lavos.app.domain.NewOrderGenderEntity
import com.lavos.app.domain.NewOrderProductEntity
import com.lavos.app.domain.NewOrderSizeEntity
import com.lavos.features.stockCompetetorStock.model.CompetetorStockGetDataDtls

class NewOrderDataModel {
    var status:String ? = null
    var message:String ? = null
    var Gender_list :ArrayList<NewOrderGenderEntity>? = null
    var Product_list :ArrayList<NewOrderProductEntity>? = null
    var Color_list :ArrayList<NewOrderColorEntity>? = null
    var size_list :ArrayList<NewOrderSizeEntity>? = null
}

