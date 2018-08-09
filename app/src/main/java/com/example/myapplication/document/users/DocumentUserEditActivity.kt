package com.example.myapplication.document.users

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.example.myapplication.*
import android.R.attr.button
import android.util.Log
import android.widget.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DocumentUserEditActivity : AppCompatActivity() {

    private lateinit var userPermissionsService: UserPermissionsApiService
    private lateinit var permission: Permission
    private lateinit var userFullName: TextView
    private lateinit var email: TextView
    private lateinit var userIcon: ImageView
    private lateinit var primaryRole: Spinner
    private lateinit var commentRole: Spinner
    private lateinit var approvalRole: Spinner
    val documentId = currentDocument?.uri ?: ""

    val primaryRoles = listOf("Owner", "Editor", "Reviewer", "Read Publish")
    val commentRoles = listOf("Create & Read", "Read-only", "hidden")
    val approvalRoles = listOf("Add & Remove", "View", "hidden")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.document_user_edit_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userPermissionsService = (application as ClauseMatchApplication).userPermissionsService

        val permissionGson = intent.getStringExtra("PERMISSION")
        permission = gson.fromJson(permissionGson, Permission::class.java)

        userFullName = this.findViewById(R.id.permission_user_full_name)
        email = this.findViewById(R.id.permission_user_email)
        userIcon = this.findViewById(R.id.permission_user_icon)
        primaryRole = this.findViewById(R.id.primary_role_spinner)
        commentRole = this.findViewById(R.id.comments_spinner)
        approvalRole = this.findViewById(R.id.approvals_spinner)

        this.findViewById<Button>(R.id.doc_user_update_btn).setOnClickListener {
            permission.primaryRole = when (primaryRole.selectedItem.toString()) {
                "Owner" -> "ADMIN"
                "Editor" -> "WRITE"
                "Reviewer" -> "READ"
                "Read Publish" -> "READ_PUBLISHED"
                else -> ""
            }
            permission.commentRole = when (commentRole.selectedItem.toString()) {
                "Create & Read" -> "WRITE"
                "Read-only" -> "READ"
                "hidden" -> "NONE"
                else -> ""
            }
            permission.approvalRole = when (approvalRole.selectedItem.toString()) {
                "Add & Remove" -> "WRITE"
                "View" -> "READ"
                "hidden" -> "NONE"
                else -> ""
            }
            userPermissionsService.createOrUpdateUserAndGroupPermissionForDocument(documentId, permission)
                    .enqueue(userPermissionsLoadCallback)
        }

        fillPermissionEditForm(permission)
    }

    private fun fillPermissionEditForm(permission: Permission) {
        val user = permission.user!!

        userFullName.setText(user.fullName)
        email.setText(user.email)
        val letters = user.firstName?.first().toString() + user.lastName?.first()
        val drawable = TextDrawable.builder()
                .beginConfig().textColor(ContextCompat.getColor(this, R.color.colorPrimary)).endConfig()
                .buildRound(letters.toUpperCase(), ContextCompat.getColor(this, R.color.controlUserIconBack))
        userIcon.setImageDrawable(drawable)
        fillPermissionSpinner()

        when (permission.commentRole) {
            "WRITE" -> commentRole.setSelection(0)
            "READ" -> commentRole.setSelection(1)
            "NONE" -> commentRole.setSelection(2)
        }

        when (permission.approvalRole) {
            "WRITE" -> approvalRole.setSelection(0)
            "READ" -> approvalRole.setSelection(1)
            "NONE" -> approvalRole.setSelection(2)
        }

        when (permission.primaryRole) {
            "ADMIN" -> primaryRole.setSelection(0)
            "WRITE" -> primaryRole.setSelection(1)
            "READ" -> primaryRole.setSelection(2)
            "READ_PUBLISHED" -> primaryRole.setSelection(3)
        }
    }

    private fun fillPermissionSpinner() {
        var dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, primaryRoles)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        primaryRole.adapter = dataAdapter
        dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, commentRoles)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        commentRole.adapter = dataAdapter
        dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, approvalRoles)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        approvalRole.adapter = dataAdapter
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

    private val userPermissionsLoadCallback: Callback<Permission> = object : Callback<Permission> {
        override fun onResponse(call: Call<Permission>, response: Response<Permission>) {
            onBackPressed()
        }

        override fun onFailure(call: Call<Permission>, t: Throwable) {
            Log.d("DOC_USER_EDIT_ACTIVITY", t.stackTrace.toString())
            onBackPressed()
        }
    }
}
