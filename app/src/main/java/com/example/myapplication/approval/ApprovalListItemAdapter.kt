package com.example.myapplication.approval

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.R
import kotlinx.android.synthetic.main.approval_list_item.view.*

class ApprovalListItemAdapter(private val context: Context,
                              var mValues: List<ApprovalPendingDto>? = null) : RecyclerView.Adapter<ApprovalListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalListItemAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.approval_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApprovalListItemAdapter.ViewHolder, position: Int) {
        if (mValues != null) {
            val approval = mValues!![position]
            holder.documentTitle.text = approval.documentTitle

            holder.content.text = if (approval.paragraph != null) {
                approval.paragraph.content
            } else {
               ""
            }

            holder.docIcon.setImageResource(R.drawable.ic_document)
        }
    }

    override fun getItemCount(): Int = mValues?.size ?: 0

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val documentTitle: TextView = view.document_title
        val content: TextView = view.paragraph_content
        val docIcon: ImageView = view.doc_icon
    }

}