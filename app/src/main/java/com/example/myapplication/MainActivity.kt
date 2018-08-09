package com.example.myapplication

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.myapplication.approval.ApprovalFragment
import com.example.myapplication.document.DocumentFragment
import com.example.myapplication.document.User
import com.example.myapplication.users.UserFragment
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val documentFragment = DocumentFragment()
        fragmentTransaction.replace(R.id.fragment_container, documentFragment, documentFragment.tag).commit()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val userService = (this.application as ClauseMatchApplication).userService
        userService.getCurrentUser().enqueue(userLoadCallback)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (item.itemId) {
            R.id.navigation_documents -> {
                val documentFragment = DocumentFragment()
                fragmentTransaction.replace(R.id.fragment_container, documentFragment, documentFragment.tag).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_approvals -> {
                val approvalFragment = ApprovalFragment()
                fragmentTransaction.replace(R.id.fragment_container, approvalFragment, approvalFragment.tag).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_users -> {
                val userFragment = UserFragment()
                fragmentTransaction.replace(R.id.fragment_container, userFragment, userFragment.tag).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val userLoadCallback: Callback<User> = object : Callback<User> {
        override fun onResponse(call: Call<User>, response: Response<User>) {
            currentUser = response.body()
        }

        override fun onFailure(call: Call<User>, t: Throwable) {
            Log.d("MAIN_ACTIVITY", t.stackTrace.toString())
        }
    }
}
