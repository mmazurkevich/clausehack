package com.example.myapplication

import android.app.Application
import android.preference.PreferenceManager
import android.util.Log
import com.example.myapplication.approval.ApprovalApiService
import com.example.myapplication.authorization.AuthorizationApiService
import com.example.myapplication.document.Category
import com.example.myapplication.document.DocumentApiService
import com.example.myapplication.document.users.UserPermissionsApiService
import com.example.myapplication.users.UserApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

var gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Category::class.java, CategoryDeserializer())
        .create()

class ClauseMatchApplication : Application() {

    val loggingInterceptor = HttpLoggingInterceptor { Log.d("OkHttp", it) }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
            .addInterceptor(SendSavedCookiesInterceptor(this))
            .addInterceptor(SaveReceivedCookiesInterceptor(this))
            .addInterceptor(loggingInterceptor)
            .build()


    val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(hostname)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    val authorizationService: AuthorizationApiService = retrofit.create(AuthorizationApiService::class.java)
    val documentService: DocumentApiService = retrofit.create(DocumentApiService::class.java)
    val userService: UserApiService = retrofit.create(UserApiService::class.java)
    val approvalService: ApprovalApiService = retrofit.create(ApprovalApiService::class.java)
    val userPermissionsService: UserPermissionsApiService = retrofit.create(UserPermissionsApiService::class.java)

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
        PreferenceManager
                .getDefaultSharedPreferences(this).edit().remove("appCookies").apply()
    }
}