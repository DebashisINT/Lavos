package com.lavos.features.viewAllOrder.interf

import com.lavos.app.domain.NewOrderGenderEntity
import com.lavos.features.viewAllOrder.model.ProductOrder

interface ColorListOnCLick {
    fun colorListOnCLick(size_qty_list: ArrayList<ProductOrder>, adpPosition:Int)
}