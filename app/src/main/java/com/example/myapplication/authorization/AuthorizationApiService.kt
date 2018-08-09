package com.example.myapplication.authorization

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthorizationApiService {

    @POST("api/v1/authorization")
    fun authorize(@Body userAuth: UserAuth): Call<ResponseBody>
}