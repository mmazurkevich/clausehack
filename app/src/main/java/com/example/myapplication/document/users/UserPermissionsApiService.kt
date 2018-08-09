package com.example.myapplication.document.users

import com.example.myapplication.document.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UserPermissionsApiService {

    @GET("api/v1/permissions")
    fun getUsersPerDocument(@Query("documentId") documentId: String,
                              @Query("scope") scope: String = "document"): Call<List<Permission>>

    @GET("api/v1/users")
    fun getUsersByParameters(@Query("groupId") groupId: String? = null,
                             @Query("query") query: String?,
                             @Query("includeDisabled") includeDisabled: Boolean): Call<List<User>>

    @POST("api/v1/permissions")
    fun createOrUpdateUserAndGroupPermissionForDocument(@Path("documentId") documentId: String,
                                                        @Body dto: Permission): Call<Permission>

    @DELETE("api/v1/permissions/{permissionId}")
    fun deletePermission(@Path("permissionId") permissionId: String): Call<ResponseBody>
}