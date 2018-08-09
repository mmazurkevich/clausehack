package com.example.myapplication.users

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.R
import com.example.myapplication.document.User
import com.example.myapplication.gson
import kotlinx.android.synthetic.main.user_edit_activity.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserEditActivity : AppCompatActivity() {

    private lateinit var userService: UserApiService
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val userGson = intent.getStringExtra("USER")
        user = gson.fromJson(userGson, User::class.java)
        fillUserEditForm(user)

        userService = (application as ClauseMatchApplication).userService
        user_update_btn.setOnClickListener {
            val userUpdateDto = UserUpdateDto(input_update_first_name.text.toString(),
                    input_update_last_name.text.toString(),
                    input_update_email.text.toString(),
                    user.username!!)
            userService.updateUser(user.id!!, userUpdateDto).enqueue(userUpdateCallback)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                onBackPressed()
                true
            }
        }
    }

    private fun fillUserEditForm(user: User) {
        input_update_first_name.setText(user.firstName)
        input_update_last_name.setText(user.lastName)
        input_update_email.setText(user.email)
        user.authorities?.forEach {
            if (it.authority == "superadmin")
                system_administration.isChecked = true
            if (it.authority == "template_creator")
                create_templates.isChecked = true
            if (it.authority == "document_creator")
                create_documents.isChecked = true
            if (it.authority == "metadata_form_creator")
                create_metadata_fields.isChecked = true
            if (it.authority == "user")
                edit_documents.isChecked = true
            if (it.authority == "user_manager")
                manage_users.isChecked = true
            if (it.authority == "advanced_user")
                x_man.isChecked = true
        }
    }

    private fun grantAuthorities() {
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
        userService.updateAuthorities(user.id!!, authorities).enqueue(grantAuthoritiesCallback)
    }

    private fun reloadUserContent() {
        PreferenceManager
                .getDefaultSharedPreferences(this).edit()
                .putBoolean(shouldReloadUsers, true)
                .apply()
    }

    private val grantAuthoritiesCallback: Callback<ResponseBody> = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            Log.d("USER_CREATE_ACTIVITY", "Saving status code ${response.code()}")
            reloadUserContent()
            onBackPressed()
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.d("USER_CREATE_ACTIVITY", t.stackTrace.toString())
            onBackPressed()
        }
    }

    private val userUpdateCallback: Callback<User> = object : Callback<User> {
        override fun onResponse(call: Call<User>, response: Response<User>) {
            grantAuthorities()
        }

        override fun onFailure(call: Call<User>, t: Throwable) {
            Log.d("USER_CREATE_ACTIVITY", t.stackTrace.toString())
            onBackPressed()
        }
    }
}
