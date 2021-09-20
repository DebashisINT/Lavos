package com.lavos.features.nearbyshops.model

import com.lavos.app.domain.ModelEntity
import com.lavos.app.domain.ProductListEntity
import com.lavos.base.BaseResponse
import java.io.Serializable

/**
 * Created by Saikat on 01-Jun-20.
 */
data class ShopTypeResponseModel(var Shoptype_list: ArrayList<ShopTypeDataModel>? = null) : BaseResponse(), Serializable

data class ShopTypeDataModel(var shoptype_id: String = "",
                             var shoptype_name: String = "") : Serializable

data class ShopTypeStockViewResponseModel(var Shoptype_list: ArrayList<ShopTypeStockViewDataModel>? = null) : BaseResponse(), Serializable

data class ShopTypeStockViewDataModel(var shoptype_id: String = "",
                                      var shoptype_name: String = "",
                                      var CurrentStockEnable:Int,
                                      var CompetitorStockEnable:Int) : Serializable

data class ModelListResponseModel(var model_list: ArrayList<DataModel>? = null) : Serializable, BaseResponse()
data class ModelListResponseModelNew(var model_list: ArrayList<ModelEntity>? = null) : Serializable, BaseResponse()

data class DataModel(var id: String = "",
                     var name: String = "") : Serializable

data class PrimaryAppListResponseModel(var primary_application_list: ArrayList<DataModel>? = null) : Serializable, BaseResponse()

data class SecondaryAppListResponseModel(var secondary_application_list: ArrayList<DataModel>? = null) : Serializable, BaseResponse()

data class LeadListResponseModel(var lead_type_list: ArrayList<DataModel>? = null) : Serializable, BaseResponse()

data class StageListResponseModel(var stage_list: ArrayList<DataModel>? = null) : Serializable, BaseResponse()

data class FunnelStageListResponseModel(var funnel_stage_list: ArrayList<DataModel>? = null) : Serializable, BaseResponse()

data class StateCityResponseModel(var city_id: String = "",
                                  var city: String = "",
                                  var state_id: String = "",
                                  var state: String = "",
                                  var country: String = "") : BaseResponse(), Serializable

data class NewOrderModel(var amount: Double = 0.0,
                         var month: String = "",
                         var year: String = "") : Serializable

