package com.example.myapplication.stomped.listener

import android.util.Log
import com.example.myapplication.sessionId
import com.example.myapplication.stomped.client.StompedListenerRouter
import com.example.myapplication.stomped.component.StompedCommand
import com.example.myapplication.stomped.component.StompedFrame
import com.example.myapplication.stomped.component.StompedHeaders.*
import com.example.myapplication.stomped.component.StompedMessageParser
import com.example.myapplication.xsrfToken
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class OkWebSocketListener constructor(val documentId: String) : WebSocketListener() {

    private val TAG = OkWebSocketListener::class.java.toString()

    override fun onOpen(webSocket: WebSocket?, response: okhttp3.Response?) {

        val frame = StompedFrame.construct(StompedCommand.STOMP_COMMAND_CONNECT)
        frame.addHeader(STOMP_HEADER_SESSION_ID, sessionId)
        frame.addHeader(STOMP_HEADER_XSRF_TOCKEN, xsrfToken)
        frame.addHeader(STOMP_HEADER_DOCUMENT_ID, documentId)
        frame.addHeader(STOMP_HEADER_ACCEPT_VERSION, "1.1,1.0")
        frame.addHeader(STOMP_HEADER_HEARTBEAT, "10000,10000")
        webSocket?.send(frame.build())

        Log.d(TAG, "OkHttp WebSocket connection created.")
    }

    override fun onMessage(webSocket: WebSocket?, message: String?) {

        val frame = StompedMessageParser.constructFrame(message!!)

        if (frame.command == StompedCommand.STOMP_COMMAND_CONNECTED) {
            Log.d(TAG, "Message connected received from server")
        }

        if (frame.stompedHeaders.hasHeader(STOMP_HEADER_DESTINATION)) {
            StompedListenerRouter.getInstance().sendMessage(frame)
        }

        Log.d(TAG, "Message received from server")
    }

    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
        Log.d(TAG, "ByteString message received")
    }

    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
        Log.d(TAG, "WebSocket is closing: " + reason!!)
    }

    override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: okhttp3.Response?) {
        Log.d(TAG, "WebSocket failure\n", t)
    }
}