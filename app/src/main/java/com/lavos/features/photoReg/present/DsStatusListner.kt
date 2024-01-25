package com.lavos.features.photoReg.present

import com.lavos.app.domain.ProspectEntity
import com.lavos.features.photoReg.model.UserListResponseModel

interface DsStatusListner {
    fun getDSInfoOnLick(obj: ProspectEntity)
}