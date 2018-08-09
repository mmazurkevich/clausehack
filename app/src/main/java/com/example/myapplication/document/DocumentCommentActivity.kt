package com.example.myapplication.document

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.example.myapplication.*
import com.example.myapplication.stomped.client.StompedClient
import com.example.myapplication.stomped.component.StompedFrame
import com.example.myapplication.stomped.listener.OkWebSocketListener
import com.example.myapplication.stomped.listener.StompedListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.document_comment_activity.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit


class DocumentCommentActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: DocumentCommentListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    val documentId = currentDocument?.uri ?: ""
    val documentVersion = currentDocument?.versionOrder ?: 0

    private var stompedClient: StompedClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.document_comment_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val loggingInterceptor = HttpLoggingInterceptor { Log.d("OkHttp", it) }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        Log.d("CONNECTING STOMP", "CONNECTING")

        val okClient = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .pingInterval(30, TimeUnit.SECONDS)
                .addInterceptor(SendSavedCookiesInterceptor(this))
                .addInterceptor(SaveReceivedCookiesInterceptor(this))
                .addInterceptor(loggingInterceptor)
                .build()

        //Build The Request
        val request = Request.Builder()
                .url(wsHostname)
                .build()

        val webSocket = okClient.newWebSocket(request, OkWebSocketListener(documentId))

        stompedClient = StompedClient(webSocket)

        stompedClient?.subscribe("/user/queue/events/$documentId", object : StompedListener() {

            override fun onNotify(frame: StompedFrame) {
                runOnUiThread {
                    Log.d("Received event", frame.stompedBody)
                    val wsEvent = Gson().fromJson(frame.stompedBody.dropLast(1), WSEventDto::class.java)
                    if (wsEvent.type == "eventMsg" && wsEvent.eventDto?.type == "comment" &&
                            wsEvent.eventDto.action == "created") {
                        viewAdapter.addWSItem(wsEvent)
                    }
                }
            }
        })


        val documentService = (application as ClauseMatchApplication).documentService

        documentService.getDocumentComments(documentId, versionTo = documentVersion)
                .enqueue(commentLoadCallback)
        viewManager = LinearLayoutManager(this).apply {
            reverseLayout = true
        }
        viewAdapter = DocumentCommentListItemAdapter(this)
        recyclerView = findViewById<RecyclerView>(R.id.document_comment_list).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        comment_send_btn.setOnClickListener {
            val commentDto = DocumentCommentDto(UUID.randomUUID().toString(),
                                documentId = currentDocument?.uri!!,
                                version = currentDocument?.versionOrder,
                                createdBy = currentUser!!,
                                content = "<p>${comment_message.text}</p>")
            documentService.createDocumentComment(currentDocument?.uri!!, commentDto)
                            .enqueue(commentCreatedCallback)
            comment_message.text.clear()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("DISCONNECTING STOMP", "DISCONNECTING")
        stompedClient?.disconnect()
    }

    private val commentLoadCallback: Callback<MutableList<DocumentComment>> = object : Callback<MutableList<DocumentComment>> {
        override fun onResponse(call: Call<MutableList<DocumentComment>>, response: Response<MutableList<DocumentComment>>) {
            viewAdapter.mValues = response.body()
            viewAdapter.notifyDataSetChanged()
        }

        override fun onFailure(call: Call<MutableList<DocumentComment>>, t: Throwable) {
            Log.d("DOC_COMMENT_ACTIVITY", t.stackTrace.toString())
        }
    }

    private val commentCreatedCallback: Callback<DocumentCommentDto> = object : Callback<DocumentCommentDto> {
        override fun onResponse(call: Call<DocumentCommentDto>, response: Response<DocumentCommentDto>) {
            Log.d("DOC_COMMENT_ACTIVITY", "Saving status code ${response.code()}")
        }

        override fun onFailure(call: Call<DocumentCommentDto>, t: Throwable) {
            Log.d("DOC_COMMENT_ACTIVITY", t.stackTrace.toString())
        }
    }
}
