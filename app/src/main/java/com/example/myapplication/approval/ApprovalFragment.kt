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
import android.widget.Button
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

    private var scope: Scope = Scope.paragraph

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

        loadApprovals()

        val paragraphApprovalButton = view.findViewById<Button>(R.id.paragraph_approval_button)
        paragraphApprovalButton.setOnClickListener {
            scope = Scope.paragraph
            loadApprovals()
        }

        val documentApprovalButton = view.findViewById<Button>(R.id.document_approval_button)
        documentApprovalButton.setOnClickListener {
            scope = Scope.document
            loadApprovals()
        }

        swipeContainer = view.findViewById(R.id.approval_refresh)
        swipeContainer.setOnRefreshListener {
            loadApprovalsFromRemote()
        }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)

        return view
    }

    private fun loadApprovals() {
        val loadedApprovals = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LOADED_CONTENT, "")
        if (loadedApprovals != null && loadedApprovals.isNotBlank()) {
            val approvals = gson.fromJson(loadedApprovals, Array<ApprovalPendingDto>::class.java).toList()
            viewAdapter.mValues = filterApprovalsByScope(scope, approvals)
            viewAdapter.notifyDataSetChanged()
        } else {
            loadApprovalsFromRemote()
        }
    }

    private fun loadApprovalsFromRemote() {
        val approvalService = (this.activity!!.application as ClauseMatchApplication).approvalService
        val approvalCallback = approvalService.getApprovals()
        approvalCallback.enqueue(approvalsLoadCallback)
    }

    private val approvalsLoadCallback: Callback<List<ApprovalPendingDto>> = object : Callback<List<ApprovalPendingDto>> {

        override fun onResponse(call: Call<List<ApprovalPendingDto>>, response: Response<List<ApprovalPendingDto>>) {
            val approvals = response.body() ?: emptyList()

            viewAdapter.mValues = filterApprovalsByScope(scope, approvals)
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

    private fun filterApprovalsByScope(scope: Scope, approvals: List<ApprovalPendingDto>): List<ApprovalPendingDto> {
        return approvals.filter { it.approval.scope == scope }
    }

}