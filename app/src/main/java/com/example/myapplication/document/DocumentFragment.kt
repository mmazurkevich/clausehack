package com.example.myapplication.document

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import com.example.myapplication.ClauseMatchApplication
import com.example.myapplication.R
import com.example.myapplication.gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DocumentFragment : Fragment() {

    private val PAGE_SIZE = 80

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: DocumentListItemAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var documentService: DocumentApiService

    private var searchText = ""

    private val LOADED_CONTENT = "DOCUMENT_LIST"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.document_fragment, container, false)

        val loadedDocuments = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LOADED_CONTENT, "")

        viewManager = LinearLayoutManager(context)
        viewAdapter = DocumentListItemAdapter(context!!)
        documentService = (this.activity!!.application as ClauseMatchApplication).documentService

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

        setHasOptionsMenu(true)

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
                    searchText = newText
                    search(0, searchText)
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

            })

            searchView.setOnCloseListener {
                restoreCachedData()
                return@setOnCloseListener false
            }

        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun restoreCachedData() {
        viewAdapter.mValues.clear()
        val loadedDocuments = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(LOADED_CONTENT, "")
        viewAdapter.mValues.addAll(gson.fromJson(loadedDocuments, Array<Document>::class.java).toList())
        viewAdapter.notifyDataSetChanged()
    }

    private fun search(page: Int, query: String) {
        // Clear data before searching
        viewAdapter.mValues.clear()

        if (query.isNotEmpty()) {
            documentService.getDocuments(page = page, pageSize = PAGE_SIZE, query = query).enqueue(searchDocumentCallback)
        } else {
            // Restore from cache
            restoreCachedData()
        }
    }

    private fun loadDocuments(page: Int, query: String? = null) {
        //documentService.getCategoriesList().enqueue(categoryLoadCallback)

        if (query != null) {
            documentService.getDocuments(page = page, pageSize = PAGE_SIZE, query = query).enqueue(searchDocumentCallback)
        } else {
            documentService.getDocuments(page = page, pageSize = PAGE_SIZE).enqueue(documentLoadCallback)
        }
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

    private val searchDocumentCallback: Callback<Pageable> = object : Callback<Pageable> {
        override fun onResponse(call: Call<Pageable>, response: Response<Pageable>) {
            val documents = response.body()?.content ?: emptyList()

            documents.forEach { it.title = it.title?.replace("<[^>]*>".toRegex(), "") }

            viewAdapter.mValues.addAll(documents)
            viewAdapter.notifyDataSetChanged()

            swipeContainer.isRefreshing = false
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
            if (searchText.isNotEmpty()) {
                loadDocuments(page, query = searchText)
            } else {
                loadDocuments(page)
            }
        }

    }

}