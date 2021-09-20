package com.lavos.features.viewAllOrder.interf

import com.lavos.app.domain.NewOrderColorEntity
import com.lavos.app.domain.NewOrderProductEntity

interface ColorListNewOrderOnClick {
    fun productListOnClick(color: NewOrderColorEntity)
}