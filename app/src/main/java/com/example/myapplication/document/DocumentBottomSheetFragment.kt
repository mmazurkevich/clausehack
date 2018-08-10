package com.example.myapplication.document


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.document.comments.DocumentCommentActivity
import com.example.myapplication.document.pdf.DocumentPreviewActivity
import com.example.myapplication.document.users.DocumentUserActivity

class DocumentBottomSheetFragment : BottomSheetDialogFragment() {

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.document_actions_dialog, null)
        dialog.setContentView(contentView)

        val docChatBtn = dialog.findViewById(R.id.doc_chat) as TextView
        val docPreviewBtn = dialog.findViewById(R.id.doc_preview) as TextView
        val docUsersBtn = dialog.findViewById(R.id.doc_users) as TextView

        docChatBtn.setOnClickListener({
            val intent = Intent(context, DocumentCommentActivity::class.java)
            context!!.startActivity(intent)
        })

        docUsersBtn.setOnClickListener({
            val intent = Intent(context, DocumentUserActivity::class.java)
            context!!.startActivity(intent)
        })

        docPreviewBtn.setOnClickListener({
            val intent = Intent(context, DocumentPreviewActivity::class.java)
            context!!.startActivity(intent)
        })
    }
}