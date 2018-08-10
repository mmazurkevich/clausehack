package com.example.myapplication.approval

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.R
import kotlinx.android.synthetic.main.approval_list_item.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ApprovalListItemAdapter(private val context: Context,
                              private val approvalFragment: ApprovalFragment,
                              var mValues: List<ApprovalPendingDto>? = null) : RecyclerView.Adapter<ApprovalListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalListItemAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.approval_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApprovalListItemAdapter.ViewHolder, position: Int) {
        if (mValues != null) {
            val approvalDto = mValues!![position]
            holder.documentTitle.text = approvalDto.documentTitle

            holder.content.text = if (approvalDto.paragraph != null) {
                approvalDto.paragraph.content.replace("<[^>]*>".toRegex(), "")
            } else {
               ""
            }

            holder.docIcon.setImageResource(R.drawable.ic_document)

            holder.acceptButton.setOnClickListener {
                approvalDto.approval.status = Status.ACCEPTED
                acceptOrRejectApproval(approvalDto.documentUri, approvalDto.approval.id, approvalDto.approval, position)
            }

            holder.rejectApproval.setOnClickListener {
                // Open comment dialog
                holder.rejectDialog.visibility = View.VISIBLE
            }

            // Cancel rejection
            holder.cancelReject.setOnClickListener {
                approvalDto.approval.status = Status.PENDING
                holder.rejectDialog.visibility = View.GONE

                closeKeyboard()
            }

            // Send rejection
            holder.sendReject.setOnClickListener {
                val comment = holder.rejectComment.text.toString()

                if (comment.isNotEmpty()) {
                    approvalDto.approval.status = Status.REJECTED
                    approvalDto.approval.comment = comment
                    acceptOrRejectApproval(approvalDto.documentUri, approvalDto.approval.id, approvalDto.approval, position)
                }

                closeKeyboard()
            }
        }
    }

    override fun getItemCount(): Int = mValues?.size ?: 0

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val documentTitle: TextView = view.document_title
        val content: TextView = view.paragraph_content
        val docIcon: ImageView = view.doc_icon
        val acceptButton: Button = view.accept_approval
        val rejectApproval: Button = view.reject_approval
        val rejectDialog: LinearLayout = view.reject_dialog
        val rejectComment: EditText = view.reject_comment
        val cancelReject: Button = view.cancel_reject_comment
        val sendReject: Button = view.send_reject_comment
    }

    private fun acceptOrRejectApproval(documentId: String, approvalId: String, approvalDto: ApprovalDto, position: Int) {
        val activity = context as Activity
        val approvalService = (activity.application as ClauseMatchApplication).approvalService
        val approvalCallback = approvalService.acceptOrRejectApproval(documentId, approvalId, approvalDto)
        approvalCallback.enqueue(AcceptOrRejectApprovalCallback(position))
    }

    private fun closeKeyboard() {
        val activity = context as Activity
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (null != activity.currentFocus)
            imm.hideSoftInputFromWindow(activity.currentFocus.applicationWindowToken, 0)
    }

    inner class AcceptOrRejectApprovalCallback(private val position: Int): Callback<ResponseBody> {

        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                notifyItemRemoved(position)
                approvalFragment.loadApprovalsFromRemote()
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.d("APPROVAL_ACTIVITY", t.stackTrace.toString())
        }
    }

}