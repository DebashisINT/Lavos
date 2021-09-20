package com.lavos.features.viewAllOrder.interf

import com.lavos.app.domain.NewOrderGenderEntity
import com.lavos.app.domain.NewOrderProductEntity

interface ProductListNewOrderOnClick {
    fun productListOnClick(product: NewOrderProductEntity)
}