package com.example.myapplication.document.pdf

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.R
import com.example.myapplication.currentDocument
import com.example.myapplication.document.DocumentAmazonUri
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException


class DocumentPreviewActivity : AppCompatActivity(), OnPageChangeListener {

    val documentId = currentDocument?.uri ?: ""
    val documentVersion = currentDocument?.versionOrder ?: 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.document_preview_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val documentService = (application as ClauseMatchApplication).documentService
        documentService.getDocumentTemporaryUrl(documentId, version = documentVersion)
                .enqueue(documentTemporaryUrlCallback)
    }

    fun loadDocument(temproatyUri: String) {
        val documentService = (application as ClauseMatchApplication).documentService
        documentService.loadDocumentFromAmazon(temproatyUri).enqueue(documentLoadCallback)
    }

    fun displayLoadedDocument() {
        val pdfFile = File(this.filesDir, "$documentId.pdf")
        val pdfView = findViewById<PDFView>(R.id.pdf_view)
        pdfView.fromFile(pdfFile).onPageChange(this)
                .spacing(5)
                .load()
    }

    private fun writeResponseBodyToDisk(body: ResponseBody): Boolean {
        return try {
            openFileOutput("$documentId.pdf", Context.MODE_PRIVATE).use {
                it.write(body.bytes())
            }
            Log.d("DOC_PREVIEW_ACTIVITY", "Document $documentId.pdf with size ${body.contentLength()} bytes was loaded")
            true
        } catch (e: IOException) {
            false
        }
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        title = String.format("%s %s/%s", currentDocument?.title, page + 1, pageCount)
    }

    private val documentTemporaryUrlCallback: Callback<DocumentAmazonUri> = object : Callback<DocumentAmazonUri> {
        override fun onResponse(call: Call<DocumentAmazonUri>, response: Response<DocumentAmazonUri>) {
            val uriSegments = response.body()!!.amazonTemporaryUrl.split('/')
            loadDocument(uriSegments[uriSegments.size - 1])
        }

        override fun onFailure(call: Call<DocumentAmazonUri>, t: Throwable) {
            Log.d("DOC_PREVIEW_ACTIVITY", t.stackTrace.toString())
        }
    }

    private val documentLoadCallback: Callback<ResponseBody> = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (writeResponseBodyToDisk(response.body()!!)) {
                displayLoadedDocument()
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.d("DOC_PREVIEW_ACTIVITY", t.stackTrace.toString())
        }
    }

}
