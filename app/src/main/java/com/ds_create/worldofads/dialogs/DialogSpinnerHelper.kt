package com.ds_create.worldofads.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ds_create.worldofads.R
import com.ds_create.worldofads.utils.CityHelper

class DialogSpinnerHelper() {

    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)
        val adapter = RcViewDialogSpinnerAdapter(tvSelection, dialog)
        val rcView = view.findViewById<RecyclerView>(R.id.rcSpView)
        val searchView = view.findViewById<SearchView>(R.id.svSpinner)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
        dialog.setView(view)
        adapter.updateAdapter(list)
        setSearchView(adapter, list, searchView, context)
        dialog.show()
    }

    private fun setSearchView(
        adapter: RcViewDialogSpinnerAdapter, list: ArrayList<String>, searchView: SearchView?, context: Context
    ) {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = CityHelper.filterListData(list, newText, context)
                adapter.updateAdapter(tempList)
                return true
            }
        })
    }

}