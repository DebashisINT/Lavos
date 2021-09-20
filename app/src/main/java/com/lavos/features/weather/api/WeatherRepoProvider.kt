package com.lavos.features.weather.api

import com.lavos.features.task.api.TaskApi
import com.lavos.features.task.api.TaskRepo

object WeatherRepoProvider {
    fun weatherRepoProvider(): WeatherRepo {
        return WeatherRepo(WeatherApi.create())
    }
}