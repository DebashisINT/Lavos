package com.lavos.features.viewAllOrder.interf

import com.lavos.app.domain.NewOrderProductEntity
import com.lavos.app.domain.NewOrderSizeEntity

interface SizeListNewOrderOnClick {
    fun sizeListOnClick(size: NewOrderSizeEntity)
}