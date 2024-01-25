package com.lavos.features.damageProduct.api

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.lavos.app.FileUtils
import com.lavos.base.BaseResponse
import com.lavos.features.NewQuotation.model.*
import com.lavos.features.addshop.model.AddShopRequestData
import com.lavos.features.addshop.model.AddShopResponse
import com.lavos.features.damageProduct.model.DamageProductResponseModel
import com.lavos.features.damageProduct.model.delBreakageReq
import com.lavos.features.damageProduct.model.viewAllBreakageReq
import com.lavos.features.login.model.userconfig.UserConfigResponseModel
import com.lavos.features.myjobs.model.WIPImageSubmit
import com.lavos.features.photoReg.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class GetDamageProductListRegRepository(val apiService : GetDamageProductListApi) {

    fun viewBreakage(req: viewAllBreakageReq): Observable<DamageProductResponseModel> {
        return apiService.viewBreakage(req)
    }

    fun delBreakage(req: delBreakageReq): Observable<BaseResponse>{
        return apiService.BreakageDel(req.user_id!!,req.breakage_number!!,req.session_token!!)
    }

}