package com.lavos.features.newcollection.model

import com.lavos.app.domain.CollectionDetailsEntity
import com.lavos.base.BaseResponse
import com.lavos.features.shopdetail.presentation.model.collectionlist.CollectionListDataModel

/**
 * Created by Saikat on 15-02-2019.
 */
class NewCollectionListResponseModel : BaseResponse() {
    //var collection_list: ArrayList<CollectionListDataModel>? = null
    var collection_list: ArrayList<CollectionDetailsEntity>? = null
}