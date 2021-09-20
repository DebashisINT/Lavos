package com.lavos.features.nearbyuserlist.api

import com.lavos.app.Pref
import com.lavos.features.nearbyuserlist.model.NearbyUserResponseModel
import com.lavos.features.newcollection.model.NewCollectionListResponseModel
import com.lavos.features.newcollection.newcollectionlistapi.NewCollectionListApi
import io.reactivex.Observable

class NearbyUserRepo(val apiService: NearbyUserApi) {
    fun nearbyUserList(): Observable<NearbyUserResponseModel> {
        return apiService.getNearbyUserList(Pref.session_token!!, Pref.user_id!!)
    }
}