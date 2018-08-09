package com.example.myapplication.approval

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
import com.example.myapplication.gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApprovalFragment: Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ApprovalListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var swipeContainer: SwipeRefreshLayout

    private val LOADED_CONTENT = "APPROVAL_LIST"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // List of approvals
        val view = inflater.inflate(R.layout.approval_fragment, container, false)

        viewManager = LinearLayoutManager(context)
        viewAdapter = ApprovalListItemAdapter(context!!)

        recyclerView = view.findViewById<RecyclerView>(R.id.approval_list).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val loadedApprovals = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LOADED_CONTENT, "")
        if (loadedApprovals != null && loadedApprovals.isNotBlank()) {
            viewAdapter.mValues = gson.fromJson(loadedApprovals, Array<ApprovalPendingDto>::class.java).toList()
            viewAdapter.notifyDataSetChanged()
        } else {
            loadApprovals()
        }

        swipeContainer = view.findViewById(R.id.approval_refresh)
        swipeContainer.setOnRefreshListener { loadApprovals() }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)

        return view
    }

    private fun loadApprovals() {
        val approvalService = (this.activity!!.application as ClauseMatchApplication).approvalService
        val approvals = approvalService.getApprovals()
        approvals.enqueue(approvalsLoadCallback)
    }

    private val approvalsLoadCallback: Callback<List<ApprovalPendingDto>> = object : Callback<List<ApprovalPendingDto>> {

        override fun onResponse(call: Call<List<ApprovalPendingDto>>, response: Response<List<ApprovalPendingDto>>) {
            val approvals = response.body()

            viewAdapter.mValues = approvals
            viewAdapter.notifyDataSetChanged()
            swipeContainer.isRefreshing = false

            PreferenceManager
                    .getDefaultSharedPreferences(context).edit()
                    .putString(LOADED_CONTENT, gson.toJson(approvals))
                    .apply()
        }

        override fun onFailure(call: Call<List<ApprovalPendingDto>>, t: Throwable) {
            Log.d("APPROVAL_ACTIVITY", t.stackTrace.toString())
        }
    }

}