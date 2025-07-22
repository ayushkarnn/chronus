package com.app.chronus.network


import retrofit2.http.GET
import retrofit2.http.Path

interface PollinationsApi {
    @GET("prompt/{prompt}")
    suspend fun getWish(@Path("prompt") prompt: String): String
}
