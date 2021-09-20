package com.lavos.features.weather.api

import com.lavos.base.BaseResponse
import com.lavos.features.task.api.TaskApi
import com.lavos.features.task.model.AddTaskInputModel
import com.lavos.features.weather.model.ForeCastAPIResponse
import com.lavos.features.weather.model.WeatherAPIResponse
import io.reactivex.Observable

class WeatherRepo(val apiService: WeatherApi) {
    fun getCurrentWeather(zipCode: String): Observable<WeatherAPIResponse> {
        return apiService.getTodayWeather(zipCode)
    }

    fun getWeatherForecast(zipCode: String): Observable<ForeCastAPIResponse> {
        return apiService.getForecast(zipCode)
    }
}