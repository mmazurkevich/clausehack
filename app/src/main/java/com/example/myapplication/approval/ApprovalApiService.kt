package com.example.myapplication.approval

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApprovalApiService {

    @GET("api/v1/approvals/my-pending-approvals")
    fun getApprovals(): Call<List<ApprovalPendingDto>>

    @PUT("api/v1/documents/{documentId}/approvals/{approvalId}")
    fun acceptOrRejectApproval(@Path("documentId") documentId: String,
                               @Path("approvalId") approvalId: String,
                               @Body dto: ApprovalDto): Call<ResponseBody>

}