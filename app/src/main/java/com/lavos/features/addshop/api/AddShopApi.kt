package com.lavos.features.addshop.api

import com.lavos.app.NetworkConstant
import com.lavos.base.BaseResponse
import com.lavos.features.addshop.model.AddShopRequestData
import com.lavos.features.addshop.model.AddShopResponse
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Created by Pratishruti on 22-11-2017.
 */
interface AddShopApi {

    @POST("Shoplist/AddShop")
    fun getAddShop(@Body addShop: AddShopRequestData?): Observable<AddShopResponse>

    @Multipart
    @POST("ShopRegistration/NewShopRegister")
    fun getAddShopWithDocImage(@Query("data") addShop: String, @Part logo_img_data: MultipartBody.Part?): Observable<AddShopResponse>

    @Multipart
    @POST("ShopRegistration/AddCompetitorImage")
    fun getAddShopCompetetorImage(@Query("data") addShop: String, @Part competitor_img: MultipartBody.Part?): Observable<BaseResponse>

    @Multipart
    @POST("ShopRegistration/RegisterShop")
    fun getAddShopWithImage(@Query("data") addShop: String, @Part logo_img_data: MultipartBody.Part?): Observable<AddShopResponse>

    @Multipart
    @POST("ShopRegistration/RegisterShop")
    fun getAddShopWithoutImage(@Query("data") addShop: String): Observable<AddShopResponse>


    @Multipart
    @POST("MultipartFile/upload")
    fun uploadImage(@Part logo_img_data: MultipartBody.Part?): Observable<BaseResponse>

    /**
     * Companion object to create the GithubApiService
     */
    companion object Factory {
        fun create(): AddShopApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.ADD_SHOP_BASE_URL)
                    .build()

            return retrofit.create(AddShopApi::class.java)
        }

        fun createWithoutMultipart(): AddShopApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(AddShopApi::class.java)
        }
    }
}