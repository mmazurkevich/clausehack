package com.example.myapplication.document


import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.currentDocument
import kotlinx.android.synthetic.main.document_list_item.view.*
import java.text.SimpleDateFormat


class DocumentListItemAdapter(
        private val context: Context,
        var mValues: MutableList<Document> = mutableListOf())
    : RecyclerView.Adapter<DocumentListItemAdapter.ViewHolder>() {

    val dateFormat = SimpleDateFormat("d MMM YYYY")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.document_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val document = mValues[position]
            holder.documentTitle.text = document.title
            val stage = if (document.isPublished) "Final" else "Draft"
            holder.docInfo.text = "$stage • ${dateFormat.format(document.firstRevisionCreatedDate)}"
            holder.docIcon.setImageResource(
                    if (document.template)
                        R.drawable.ic_template
                    else
                        R.drawable.ic_document)
            if (document.hasPermission) {
                holder.itemView.setOnClickListener {
                    currentDocument = document
                    val bottomSheetFragment = DocumentBottomSheetFragment()
                    bottomSheetFragment.show((context as AppCompatActivity).supportFragmentManager, bottomSheetFragment.tag)
                }
                holder.docAccess.visibility = View.GONE
            }else
                holder.docAccess.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val documentTitle: TextView = view.document_title
        val docInfo: TextView = view.doc_info
        val docIcon: ImageView = view.doc_icon
        val docAccess: TextView = view.doc_access
    }
}
