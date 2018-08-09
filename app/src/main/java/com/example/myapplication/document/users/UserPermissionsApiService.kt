package com.example.myapplication.document.users

import com.example.myapplication.document.DocumentCommentDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UserPermissionsApiService {

    @GET("api/v1/permissions")
    fun getUsersPerDocument(@Query("documentId") documentId: String,
                              @Query("scope") scope: String = "document"): Call<Permission>
}