package com.example.myapplication.users

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.document.User
import com.example.myapplication.gson
import kotlinx.android.synthetic.main.user_edit_activity.*


class UserEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_edit_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val userGson = intent.getStringExtra("USER")
        val user = gson.fromJson(userGson, User::class.java)
        fillUserEditForm(user)

        user_update_btn.setOnClickListener {  }
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
}
