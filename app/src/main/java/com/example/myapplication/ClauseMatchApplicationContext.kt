package com.example.myapplication

import com.example.myapplication.document.Document
import com.example.myapplication.document.User

var currentUser: User? = null

var sessionId = ""

var currentDocument: Document? = null

var xsrfToken = ""

val cookiesKey = "app_cookies"

var hostname = "https://qa2.clausematch.com/"

var wsHostname = "wss://qa2.clausematch.com/api/v1/ws"
