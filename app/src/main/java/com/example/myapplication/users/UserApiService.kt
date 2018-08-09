package com.example.myapplication.users

import com.example.myapplication.document.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UserApiService {

    @GET("api/v1/users/me")
    fun getCurrentUser(): Call<User>

    @GET("api/v1/users")
    fun getUsers(@Query("includeDisabled") includeDisabled: Boolean = false): Call<MutableList<User>>

    @POST("api/v1/users")
    fun createUser(@Body user: UserDto): Call<User>

    @POST("api/v1/users/{userId}/actions/disable")
    fun disableUser(@Path("userId") userId: String): Call<ResponseBody>

    @PATCH("api/v1/users/{userId}")
    fun updateUser(@Path("userId") userId: String,
                   @Body userUpdate: UserUpdateDto): Call<User>

    @POST("api/v1/authorities/actions/user/{userId}/update-authorities")
    fun updateAuthorities(@Path("userId") userId: String, @Body authorities: List<AuthorityDto>): Call<ResponseBody>
}