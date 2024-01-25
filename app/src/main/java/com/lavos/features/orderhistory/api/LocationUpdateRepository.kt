package com.lavos.features.orderhistory.api

import com.lavos.base.BaseResponse
import com.lavos.features.orderhistory.model.LocationUpdateRequest
import io.reactivex.Observable

/**
 * Created by Pratishruti on 23-11-2017.
 */
class LocationUpdateRepository(val apiService:LocationUpdateApi) {
    fun sendLocationUpdate(location: LocationUpdateRequest): Observable<BaseResponse> {
        for(i in 0..location.location_details!!.size-1){
            println("distance_loc_tag LocationUpdateRepository ${location.location_details!!.get(i).latitude} ${location.location_details!!.get(i).longitude} ${location.location_details!!.get(i).distance_covered}")
        }
        return apiService.sendLocationUpdates(location)
    }
}