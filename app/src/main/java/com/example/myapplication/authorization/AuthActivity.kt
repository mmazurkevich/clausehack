package com.example.myapplication.authorization

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AuthActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java)
        setContentView(R.layout.activity_login)

        val authorizationService = (this.application as ClauseMatchApplication).authorizationService

        sign_in_button.setOnClickListener {
            val userAuth = UserAuth(auth_username.text.toString(), password.text.toString())
            authorizationService.authorize(userAuth).enqueue(
                    object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            startActivity(intent)
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            stack_trace.text = t.stackTrace.toString()
                        }
                    }
            )
        }
    }

}
