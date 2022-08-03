package com.ds_create.worldofads.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ds_create.worldofads.models.Ad
import com.ds_create.worldofads.models.DbManager

class FirebaseViewModel: ViewModel() {

    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Ad>>()

    fun loadAllAdsFirstPage(filter: String) {
        dbManager.getAllAdsFirstPage(filter, object : DbManager.ReadDataCallback {

            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsNextPage(time: String, filter: String) {
        dbManager.getAllAdsNextPage(time, filter, object : DbManager.ReadDataCallback {

            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCat(cat: String, filter: String) {
        dbManager.getAllAdsFromCatFirstPage(cat, filter, object : DbManager.ReadDataCallback {

            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCatNextPage(cat: String, time: String, filter: String) {
        dbManager.getAllAdsFromCatNextPage(cat, time, filter, object : DbManager.ReadDataCallback {

            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun onFavClick(ad: Ad) {
        dbManager.onFavClick(ad, object: DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value
                val position = updatedList?.indexOf(ad)
                if (position != -1) {
                    position?.let {
                        val favCounter = if (ad.isFav){
                            ad.favCounter.toInt() -1
                        } else {
                            ad.favCounter.toInt() +1
                        }
                        updatedList[position] = updatedList[position].copy(
                            isFav = !ad.isFav,
                            favCounter = favCounter.toString()
                            )
                    }
                }
                liveAdsData.postValue(updatedList)
            }
        })
    }

    fun adViewed(ad: Ad) {
        dbManager.adViewed(ad)
    }

    fun loadMyAds() {
        dbManager.getMyAds(object : DbManager.ReadDataCallback {

            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadMyFavs() {
        dbManager.getMyFavs(object : DbManager.ReadDataCallback {

            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun deleteItem(ad: Ad) {
        dbManager.deleteAd(ad, object : DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList)
            }
        })
    }
}