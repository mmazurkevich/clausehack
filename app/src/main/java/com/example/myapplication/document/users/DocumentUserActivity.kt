package com.example.myapplication.document.users

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.R
import com.example.myapplication.currentDocument
import com.example.myapplication.document.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentUserActivity : AppCompatActivity(), PermissionRecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private lateinit var userPermissionsService: UserPermissionsApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: PermissionUserListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var searchView: SearchView
    val documentId = currentDocument?.uri ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.document_user_activity)

        title = currentDocument?.title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewManager = LinearLayoutManager(this)
        viewAdapter = PermissionUserListItemAdapter(this, this)
        recyclerView = this.findViewById<RecyclerView>(R.id.doc_user_list).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val itemTouchHelperCallback = PermissionRecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        userPermissionsService = (application as ClauseMatchApplication).userPermissionsService
        loadUsers()
    }

    public override fun onResume() {
        super.onResume()
        userPermissionsService.getUsersPerDocument(documentId).enqueue(userPermissionsLoadCallback)
    }

    fun grantUserPermission(permission: Permission) {
        userPermissionsService.createOrUpdateUserAndGroupPermissionForDocument(documentId, permission)
                .enqueue(grantUserDocumentPermissionCallback)
    }

    fun loadUsers() {
        viewAdapter.isAddMode = false
        userPermissionsService.getUsersPerDocument(documentId).enqueue(userPermissionsLoadCallback)
    }

    fun searchUsers(query: String) {
        viewAdapter.isAddMode = true
        userPermissionsService.getUsersByParameters(query = query).enqueue(possibleUserLoadCallback)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)
        val searchItem = menu.findItem(R.id.action_search)
        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
            val searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as EditText
            searchEditText.setHintTextColor(Color.WHITE)

            val searchTextView = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as EditText
            val mCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            mCursorDrawableRes.isAccessible = true
            mCursorDrawableRes.set(searchTextView, 0)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isBlank()) {
                        loadUsers()
                    } else {
                        searchUsers(newText)
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }
            })
            searchView.setOnCloseListener {
                loadUsers()
                return@setOnCloseListener false
            }
        }
        return true
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

    private val possibleUserLoadCallback: Callback<List<User>> = object : Callback<List<User>> {
        override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
            viewAdapter.mValues = response.body()?.map {
                Permission().apply {
                    primaryRole = "WRITE"
                    commentRole = "WRITE"
                    approvalRole = "WRITE"
                    type = "userPermission"
                    user = it
                    userId = it.id
                    documentUri = documentId
                    scope = "document"
                }
            }?.toMutableList()
            viewAdapter.notifyDataSetChanged()
        }

        override fun onFailure(call: Call<List<User>>, t: Throwable) {
            Log.d("DOCUMENT_USER_ACTIVITY", t.stackTrace.toString())
        }
    }

    private val grantUserDocumentPermissionCallback: Callback<Permission> = object : Callback<Permission> {
        override fun onResponse(call: Call<Permission>, response: Response<Permission>) {
            if (!searchView.isIconified) {
                searchView.onActionViewCollapsed()
            }
        }

        override fun onFailure(call: Call<Permission>, t: Throwable) {
            Log.d("DOCUMENT_USER_ACTIVITY", t.stackTrace.toString())
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
