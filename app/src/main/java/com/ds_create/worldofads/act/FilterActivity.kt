package com.ds_create.worldofads.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.ds_create.worldofads.R
import com.ds_create.worldofads.databinding.ActivityFilterBinding
import com.ds_create.worldofads.dialogs.DialogSpinnerHelper
import com.ds_create.worldofads.utils.CityHelper

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBarSettings()
        getFilter()

        onClickSelectCountry()
        onClickSelectCity()
        onClickDone()
        onClickClear()
    }

    fun actionBarSettings() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    //OnClicks
   private fun onClickSelectCountry() = with(binding) {
        tvCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountry, tvCountry)
            if (tvCity.text.toString() != getString(R.string.select_city)) {
                tvCity.text = getString(R.string.select_city)
            }
        }
    }

   private fun onClickSelectCity() = with(binding) {
       tvCity.setOnClickListener {
           val selectedCountry = tvCountry.text.toString()
           if (selectedCountry != getString(R.string.select_country)) {
               val listCity = CityHelper.getAllCities(this@FilterActivity, selectedCountry)
               dialog.showSpinnerDialog(this@FilterActivity, listCity, tvCity)
           } else {
               Toast.makeText(
                   this@FilterActivity,
                   this@FilterActivity.getString(R.string.no_country_selected),
                   Toast.LENGTH_LONG
               ).show()
           }
       }
    }

    private fun onClickDone() = with(binding) {
        btDone.setOnClickListener {
            val intent = Intent().apply {
                putExtra(FILTER_KEY, createFilter())
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun onClickClear() = with(binding) {
        btClear.setOnClickListener {
            tvCountry.text = getString(R.string.select_country)
            tvCity.text = getString(R.string.select_city)
            edIndex.setText("")
            checkBoxWithSend.isChecked = false
            setResult(RESULT_CANCELED)
        }
    }

    private fun createFilter(): String = with(binding) {
        val stringBuilder = StringBuilder()
        val arrayTempFilter = listOf(
            tvCountry.text,
            tvCity.text,
            edIndex.text,
            checkBoxWithSend.isChecked.toString()
        )
        for ((index, string) in arrayTempFilter.withIndex()) {
            if (string != getString(R.string.select_country) &&
                string != getString(R.string.select_city) &&
                    string.isNotEmpty()) {
                stringBuilder.append(string)
                if (index != arrayTempFilter.size - 1)stringBuilder.append("_")
            } else {
                stringBuilder.append("empty")
                if (index != arrayTempFilter.size - 1)stringBuilder.append("_")
            }
        }
        return stringBuilder.toString()
    }

    private fun getFilter() = with(binding) {
        val filter = intent.getStringExtra(FILTER_KEY)
        if (filter != null && filter != "empty") {
            val filterArray = filter.split("_")
            if (filterArray[0] != "empty") tvCountry.text = filterArray[0]
            if (filterArray[1] != "empty") tvCity.text = filterArray[1]
            if (filterArray[2] != "empty") edIndex.setText(filterArray[2])
            checkBoxWithSend.isChecked = filterArray[3].toBoolean()
        }
    }

    companion object {
        const val FILTER_KEY = "filter_key"
    }

}