package com.example.myapplication

import android.content.Context
import android.preference.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class SendSavedCookiesInterceptor(private val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
//        val preferences = PreferenceManager
//                .getDefaultSharedPreferences(context)
//                .getStringSet(cookiesKey, mutableSetOf()) as HashSet<String>
//
//        preferences.forEach {
//            builder.addHeader("Cookie", it)
//        }

        builder.addHeader("cookie", "XSRF-TOKEN=${xsrfToken}; JSESSIONID=${sessionId}")
        builder.addHeader("x-xsrf-token", xsrfToken)

        return chain.proceed(builder.build())
    }
}

class SaveReceivedCookiesInterceptor(private val context: Context) : Interceptor {

    @JvmField
    val setCookieHeader = "Set-Cookie"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        if (!originalResponse.headers(setCookieHeader).isEmpty()) {
//            val cookies = PreferenceManager
//                    .getDefaultSharedPreferences(context)
//                    .getStringSet(cookiesKey, HashSet()) as HashSet<String>

            originalResponse.headers(setCookieHeader).forEach {
//                cookies.add(it)
                if (it.contains("JSESSIONID"))
                    sessionId = it.substring(11, 43)
                if (it.contains("XSRF-TOKEN"))
                    xsrfToken = it.substring(11, 47)
            }

//            PreferenceManager
//                    .getDefaultSharedPreferences(context)
//                    .edit()
//                    .putStringSet(cookiesKey, cookies)
//                    .apply()
        }

        return originalResponse
    }

}


