package com.example.myapplication.document

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DocumentApiService {

    @GET("api/v1/documents")
    fun getDocuments(@Query("order") order: String = "desc",
                     @Query("orderBy") orderBy: String = "updatedAt",
                     @Query("page") page: Int = 0,
                     @Query("pageSize") pageSize: Int = 100,
                     @Query("query") query: String = ""): Call<Pageable>

    @GET("api/v1/documents/{documentId}/events")
    fun getDocumentComments(@Path("documentId") documentId: String,
                            @Query("subtypes") types: String = "document_comment",
                            @Query("versionFrom") versionFrom: Long = 0,
                            @Query("versionTo") versionTo: Long = 0): Call<MutableList<DocumentComment>>

    @POST("api/v1/documents/{documentId}/comments")
    fun createDocumentComment(@Path("documentId") documentId: String,
                           @Body documentComment: DocumentCommentDto): Call<DocumentCommentDto>


    @POST("api/v1/documents/{documentId}/export/pdf")
    fun getDocumentTemporaryUrl(@Path("documentId") documentId: String,
                     @Query("isIncludeDeletePending") isIncludeDeletePending: Boolean = false,
                     @Query("isIncludeToc") isIncludeToc: Boolean = false,
                     @Query("version") version: Long): Call<DocumentAmazonUri>

    @GET("api/v1/files/{amazonTemporaryUrl}")
    fun loadDocumentFromAmazon(@Path("amazonTemporaryUrl") temporaryUrl: String): Call<ResponseBody>

    @GET("api/v1/categories")
    fun getCategoriesList(): Call<MutableList<Category>>
}