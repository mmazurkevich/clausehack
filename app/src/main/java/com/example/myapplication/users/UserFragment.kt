package com.example.myapplication.users

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.R
import com.example.myapplication.document.User
import com.example.myapplication.gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserFragment : Fragment(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: UserListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var userService: UserApiService
    private var originUsersList: MutableList<User>? = null
    private val LOADED_CONTENT = "USERS_LIST"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.user_fragment, container, false)
        setHasOptionsMenu(true)

        viewManager = LinearLayoutManager(context)
        viewAdapter = UserListItemAdapter(context!!)
        recyclerView = view.findViewById<RecyclerView>(R.id.user_list).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        swipeContainer = view.findViewById(R.id.users_refresh)
        swipeContainer.setOnRefreshListener { loadUsers() }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)

        userService = (this.activity!!.application as ClauseMatchApplication).userService

        val shouldReload = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(shouldReloadUsers, false)

        if (shouldReload) {
            loadUsers()
        } else {
            restoreCachedOrLoadUsers()
        }

        val itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        val floatingActionButton = view.findViewById(R.id.add_user_btn) as FloatingActionButton
        floatingActionButton.setOnClickListener {
            val intent = Intent(context, UserCreateActivity::class.java)
            context!!.startActivity(intent)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val shouldReload = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(shouldReloadUsers, false)
        if (shouldReload) {
            loadUsers()
        }
    }

    private fun restoreCachedOrLoadUsers() {
        val loadedUsers = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LOADED_CONTENT, "")
        if (loadedUsers != null && loadedUsers.isNotBlank()) {
            originUsersList = gson.fromJson(loadedUsers, Array<User>::class.java).toMutableList()
            viewAdapter.mValues = originUsersList
            viewAdapter.notifyDataSetChanged()
        } else {
            loadUsers()
        }
    }

    private fun loadUsers() {
        swipeContainer.isRefreshing = true
        userService.getUsers(includeDisabled = true).enqueue(usersLoadCallback)
        PreferenceManager
                .getDefaultSharedPreferences(context).edit()
                .putBoolean(shouldReloadUsers, false)
                .apply()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (viewHolder is UserListItemAdapter.ViewHolder) {
            val user = viewAdapter.mValues!![position]

            if (user.accountEnabled) {
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_bar, menu)
        val searchItem = menu.findItem(R.id.action_search)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            val searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as EditText
            searchEditText.setHintTextColor(Color.WHITE)

            val searchTextView = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text) as EditText
            val mCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            mCursorDrawableRes.isAccessible = true
            mCursorDrawableRes.set(searchTextView, 0)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    search(newText)
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }
            })
            searchView.setOnCloseListener {
                restoreCachedOrLoadUsers()
                return@setOnCloseListener false
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun search(query: String) {
        val filteredUserList = mutableListOf<User>()
        originUsersList?.forEach {
            if (it.firstName!!.contains(query, true)
                    || it.lastName!!.contains(query, true)
                    || it.email!!.contains(query, true)) {
                filteredUserList.add(it)
            }
        }
        viewAdapter.mValues = filteredUserList
        viewAdapter.notifyDataSetChanged()
    }

    private val usersLoadCallback: Callback<MutableList<User>> = object : Callback<MutableList<User>> {
        override fun onResponse(call: Call<MutableList<User>>, response: Response<MutableList<User>>) {
            val users = response.body()
            originUsersList = users
            viewAdapter.mValues = originUsersList
            viewAdapter.notifyDataSetChanged()
            swipeContainer.isRefreshing = false
            PreferenceManager
                    .getDefaultSharedPreferences(context).edit()
                    .putString(LOADED_CONTENT, gson.toJson(originUsersList))
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
