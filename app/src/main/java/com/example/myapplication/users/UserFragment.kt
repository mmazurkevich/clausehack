package com.example.myapplication.users

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.R
import com.example.myapplication.document.User
import com.example.myapplication.gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v4.view.accessibility.AccessibilityEventCompat.setAction
import android.support.design.widget.Snackbar
import android.content.ClipData.Item
import android.content.Intent
import android.graphics.Color
import android.support.design.widget.FloatingActionButton
import okhttp3.ResponseBody


class UserFragment : Fragment(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: UserListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var userService: UserApiService
    private val LOADED_CONTENT = "USERS_LIST"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.user_fragment, container, false)

        val loadedUsers = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LOADED_CONTENT, "")

        viewManager = LinearLayoutManager(context)
        viewAdapter = UserListItemAdapter(context!!)
        recyclerView = view.findViewById<RecyclerView>(R.id.user_list).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        userService = (this.activity!!.application as ClauseMatchApplication).userService

        if (loadedUsers != null && loadedUsers.isNotBlank()) {
            viewAdapter.mValues = gson.fromJson(loadedUsers, Array<User>::class.java).toMutableList()
            viewAdapter.notifyDataSetChanged()
        } else {
            loadUsers()
        }

        val itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        swipeContainer = view.findViewById(R.id.users_refresh)
        swipeContainer.setOnRefreshListener { loadUsers() }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)

        val floatingActionButton = view.findViewById(R.id.add_user_btn) as FloatingActionButton
        floatingActionButton.setOnClickListener {
            val intent = Intent(context, UserCreateActivity::class.java)
            context!!.startActivity(intent)
        }
        return view
    }

    private fun loadUsers() {
        swipeContainer.isRefreshing = true
        userService.getUsers(includeDisabled = false).enqueue(usersLoadCallback)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (viewHolder is UserListItemAdapter.ViewHolder) {
            val user = viewAdapter.mValues!![position]

            // backup of removed item for undo purpose
            val deletedItem = user
            val deletedIndex = viewHolder.adapterPosition

            // disable user request
            userService.disableUser(user.id!!).enqueue(disableUserCallback)

            // remove the item from recycler view
            viewAdapter.mValues!!.removeAt(position)
            viewAdapter.notifyItemRemoved(position)

            // showing snack bar with Undo option
            val snackbar = Snackbar.make(swipeContainer, "User ${user.fullName} was disabled",
                                    Snackbar.LENGTH_SHORT)
            snackbar.show()
        }
    }

    private val usersLoadCallback: Callback<MutableList<User>> = object : Callback<MutableList<User>> {
        override fun onResponse(call: Call<MutableList<User>>, response: Response<MutableList<User>>) {
            val users = response.body()
            viewAdapter.mValues = users
            viewAdapter.notifyDataSetChanged()
            swipeContainer.isRefreshing = false
            PreferenceManager
                    .getDefaultSharedPreferences(context).edit()
                    .putString(LOADED_CONTENT, gson.toJson(users))
                    .apply()
        }

        override fun onFailure(call: Call<MutableList<User>>, t: Throwable) {
            Log.d("USER_LOAD", t.stackTrace.toString())
        }
    }

    private val disableUserCallback: Callback<ResponseBody> = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            loadUsers()
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.d("USER_DISABLE", t.stackTrace.toString())
        }
    }
}
