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


class UserFragment : Fragment(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: UserListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var swipeContainer: SwipeRefreshLayout
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

        if (loadedUsers != null && loadedUsers.isNotBlank()) {
            viewAdapter.mValues = gson.fromJson(loadedUsers, Array<User>::class.java).toList()
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
        floatingActionButton.setOnClickListener { it ->
            val intent = Intent(context, UserCreateActivity::class.java)
            context!!.startActivity(intent)
        }
        return view
    }

    private fun loadUsers() {
        val userService = (this.activity!!.application as ClauseMatchApplication).userService
        userService.getUsers(includeDisabled = true).enqueue(usersLoadCallback)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (viewHolder is UserListItemAdapter.ViewHolder) {
//            // get the removed item name to display it in snack bar
//            val name = cartList.get(viewHolder.adapterPosition).getName()
//
//            // backup of removed item for undo purpose
//            val deletedItem = cartList.get(viewHolder.adapterPosition)
//            val deletedIndex = viewHolder.adapterPosition
//
//            // remove the item from recycler view
//            mAdapter.removeItem(viewHolder.adapterPosition)

            // showing snack bar with Undo option
            val snackbar = Snackbar
                    .make(swipeContainer, "User removed from cart!", Snackbar.LENGTH_LONG)
            snackbar.setAction("UNDO", View.OnClickListener {
//                // undo is selected, restore the deleted item
//                mAdapter.restoreItem(deletedItem, deletedIndex)
            })
            snackbar.setActionTextColor(Color.YELLOW)
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
            Log.d("MAIN_ACTIVITY", t.stackTrace.toString())
        }
    }
}
