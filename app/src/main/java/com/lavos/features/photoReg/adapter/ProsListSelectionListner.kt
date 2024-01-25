package com.lavos.features.photoReg.adapter

import com.lavos.features.photoReg.model.ProsCustom
import com.lavos.features.photoReg.model.UserListResponseModel

interface ProsListSelectionListner {
    fun getInfo(obj: ProsCustom)
}