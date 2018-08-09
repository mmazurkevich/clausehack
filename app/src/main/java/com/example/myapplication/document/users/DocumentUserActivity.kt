package com.example.myapplication.document.users

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.MenuItem
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.R
import com.example.myapplication.currentDocument
import com.example.myapplication.users.UserListItemAdapter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentUserActivity : AppCompatActivity(), PermissionRecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private lateinit var userPermissionsService: UserPermissionsApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: PermissionUserListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    val documentId = currentDocument?.uri ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.document_user_activity)

        title = currentDocument?.title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewManager = LinearLayoutManager(this)
        viewAdapter = PermissionUserListItemAdapter(this)
        recyclerView = this.findViewById<RecyclerView>(R.id.doc_user_list).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val itemTouchHelperCallback = PermissionRecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        userPermissionsService = (application as ClauseMatchApplication).userPermissionsService
        userPermissionsService.getUsersPerDocument(documentId).enqueue(userPermissionsLoadCallback)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (viewHolder is PermissionUserListItemAdapter.ViewHolder) {
            val permission = viewAdapter.mValues!![position]

            // disable user request
            userPermissionsService.deletePermission(permission.id!!).enqueue(deleteUserFromDocumentCallback)

            // remove the item from recycler view
            viewAdapter.mValues!!.removeAt(position)
            viewAdapter.notifyItemRemoved(position)

            // showing snack bar with Undo option
            Snackbar.make(this.findViewById(R.id.doc_user_layout), "User ${permission.user!!.fullName} was removed from document",
                    Snackbar.LENGTH_SHORT).show()
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

    private val deleteUserFromDocumentCallback: Callback<ResponseBody> = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.d("DOCUMENT_USER_ACTIVITY", t.stackTrace.toString())
        }
    }

    private val userPermissionsLoadCallback: Callback<List<Permission>> = object : Callback<List<Permission>> {
        override fun onResponse(call: Call<List<Permission>>, response: Response<List<Permission>>) {
            viewAdapter.mValues = response.body()?.filter { it.type == "userPermission" }?.toMutableList()
            viewAdapter.notifyDataSetChanged()
        }

        override fun onFailure(call: Call<List<Permission>>, t: Throwable) {
            Log.d("DOCUMENT_USER_ACTIVITY", t.stackTrace.toString())
            onBackPressed()
        }
    }
}
