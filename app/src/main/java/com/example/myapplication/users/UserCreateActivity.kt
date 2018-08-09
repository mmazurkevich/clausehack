package com.example.myapplication.users

import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.R
import com.example.myapplication.document.User
import kotlinx.android.synthetic.main.user_create_activity.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserCreateActivity : AppCompatActivity() {

    private lateinit var userService: UserApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_create_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userService = (application as ClauseMatchApplication).userService
        user_create_btn.setOnClickListener {
            val userDto = UserDto(input_first_name.text.toString(),
                    input_last_name.text.toString(),
                    input_email.text.toString(),
                    input_username.text.toString(),
                    input_password.text.toString(),
                    input_confirm_password.text.toString())
            userService.createUser(userDto).enqueue(userCreateCallback)
        }
    }

    private fun grantAuthorities(userId: String) {
        val authorities = mutableListOf<AuthorityDto>()
        if (system_administration.isChecked)
            authorities.add(AuthorityDto("superadmin"))
        if (create_templates.isChecked)
            authorities.add(AuthorityDto("template_creator"))
        if (create_documents.isChecked)
            authorities.add(AuthorityDto("document_creator"))
        if (create_metadata_fields.isChecked)
            authorities.add(AuthorityDto("metadata_form_creator"))
        if (edit_documents.isChecked)
            authorities.add(AuthorityDto("user"))
        if (manage_users.isChecked)
            authorities.add(AuthorityDto("user_manager"))
        if (x_man.isChecked)
            authorities.add(AuthorityDto("advanced_user"))
        userService.updateAuthorities(userId, authorities).enqueue(grantAuthoritiesCallback)
    }

    private val grantAuthoritiesCallback: Callback<ResponseBody> = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            Log.d("USER_CREATE_ACTIVITY", "Saving status code ${response.code()}")
            onBackPressed()
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.d("USER_CREATE_ACTIVITY", t.stackTrace.toString())
            onBackPressed()
        }
    }

    private val userCreateCallback: Callback<User> = object : Callback<User> {
        override fun onResponse(call: Call<User>, response: Response<User>) {
            val user = response.body()
            if (user != null)
                grantAuthorities(user.id!!)
        }

        override fun onFailure(call: Call<User>, t: Throwable) {
            Log.d("USER_CREATE_ACTIVITY", t.stackTrace.toString())
            onBackPressed()
        }
    }
}
