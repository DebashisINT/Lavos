package com.lavos.features.newcollectionreport

import com.lavos.features.photoReg.model.UserListResponseModel

interface PendingCollListner {
    fun getUserInfoOnLick(obj: PendingCollData)
}