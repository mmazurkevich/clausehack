package com.example.myapplication.document

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

class DocumentFragment : Fragment() {

    private val PAGE_SIZE = 20

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: DocumentListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var swipeContainer: SwipeRefreshLayout
    private val LOADED_CONTENT = "DOCUMENT_LIST"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.document_fragment, container, false)

        val loadedDocuments = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LOADED_CONTENT, "")

        viewManager = LinearLayoutManager(context)
        viewAdapter = DocumentListItemAdapter(context!!)
        recyclerView = view.findViewById<RecyclerView>(R.id.document_list).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        recyclerView.addOnScrollListener(DocumentScrollListener(viewManager as LinearLayoutManager))

        if (loadedDocuments != null && loadedDocuments.isNotBlank()) {
            viewAdapter.mValues = gson.fromJson(loadedDocuments, Array<Document>::class.java).toMutableList()
            viewAdapter.notifyDataSetChanged()
        } else {
            loadDocuments(0)
        }

        swipeContainer = view.findViewById(R.id.documents_refresh)
        swipeContainer.setOnRefreshListener {
            viewAdapter.mValues.clear()
            loadDocuments(0)
        }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)

        return view
    }

    private fun loadDocuments(page: Int) {
        val documentService = (this.activity!!.application as ClauseMatchApplication).documentService
        documentService.getCategoriesList().enqueue(categoryLoadCallback)
        documentService.getDocuments(page = page, pageSize = PAGE_SIZE).enqueue(documentLoadCallback)
    }

    private val documentLoadCallback: Callback<Pageable> = object : Callback<Pageable> {
        override fun onResponse(call: Call<Pageable>, response: Response<Pageable>) {
            val documents = response.body()?.content ?: emptyList()

            viewAdapter.mValues.addAll(documents)
            viewAdapter.notifyDataSetChanged()

            swipeContainer.isRefreshing = false
            PreferenceManager
                    .getDefaultSharedPreferences(context).edit()
                    .putString(LOADED_CONTENT, gson.toJson(viewAdapter.mValues))
                    .apply()

        }

        override fun onFailure(call: Call<Pageable>, t: Throwable) {
            Log.d("MAIN_ACTIVITY", t.stackTrace.toString())
        }
    }

    private val categoryLoadCallback: Callback<MutableList<Category>> = object : Callback<MutableList<Category>> {
        override fun onResponse(call: Call<MutableList<Category>>, response: Response<MutableList<Category>>) {
            print(response.message())
        }

        override fun onFailure(call: Call<MutableList<Category>>, t: Throwable) {
            Log.d("MAIN_ACTIVITY", t.stackTrace.toString())
        }
    }

    inner class DocumentScrollListener(linearLayoutManager: LinearLayoutManager) : EndlessRecyclerViewScrollListener(linearLayoutManager) {

        override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
            loadDocuments(page)
        }

    }

}