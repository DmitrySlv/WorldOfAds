package com.ds_create.worldofads.models

import android.util.Log
import com.ds_create.worldofads.utils.FilterManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DbManager {
    val db = Firebase.database.getReference(MAIN_NODE)
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAd(ad: Ad, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(auth.uid!!)
            .child(AD_NODE)
            .setValue(ad)
            .addOnCompleteListener {
                val adFilter = FilterManager.createFilter(ad)
                db.child(ad.key ?: "empty")
                    .child(FILTER_NODE)
                    .setValue(adFilter)
                    .addOnCompleteListener {
                        finishWorkListener.onFinish(it.isSuccessful)
                    }
            }
    }

    fun adViewed(ad: Ad) {
        var counter = ad.viewsCounter.toInt()
        counter++
        if (auth.uid != null) {
            db.child(ad.key ?: "empty")
                .child(INFO_NODE)
                .setValue(InfoItem(counter.toString(), ad.emailsCounter, ad.callsCounter))
        }
    }

    fun onFavClick(ad: Ad, listener: FinishWorkListener) {
        if (ad.isFav) {
            removeFromFavs(ad, listener)
        } else {
            addToFavs(ad, listener)
        }
    }

   private fun addToFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let {
                    uid -> db.child(it).child(FAVS_NODE).child(uid).setValue(uid)
                .addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish(true)
                }
            }
        }
    }

    private fun removeFromFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let {
                    uid -> db.child(it).child(FAVS_NODE).child(uid).removeValue()
                .addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish(true)
                }
            }
        }
    }

    fun getMyAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getMyFavs(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild("/favs/${auth.uid}").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAdsFirstPage(filter: String, readDataCallback: ReadDataCallback?) {
        val query = if (filter.isEmpty()) {
            db.orderByChild( "/adFilter/time").limitToLast(ADS_LIMIT)
        } else {
            getAllAdsByFilterFirstPage(filter)
        }
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAdsByFilterFirstPage(tempFilter: String): Query {
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
        return db.orderByChild( "/adFilter/$orderBy")
            .startAt(filter).endAt(filter + "\uf8ff").limitToLast(ADS_LIMIT)
    // "_\uf8ff" - это время обявления, которое может находиться в любом месте списка.
    }

    fun getAllAdsNextPage(time: String, filter: String, readDataCallback: ReadDataCallback?) {
        if (filter.isEmpty()) {
         val query = db.orderByChild( "/adFilter/time").endBefore(time).limitToLast(ADS_LIMIT)
            readDataFromDb(query, readDataCallback)
        } else {
            getAllAdsByFilterNextPage(filter, time, readDataCallback)
        }

    }

    private fun getAllAdsByFilterNextPage(
        tempFilter: String, time: String, readDataCallback: ReadDataCallback?
    ) {
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
        val query = db.orderByChild( "/adFilter/$orderBy")
            .endBefore(filter + "_$time").limitToLast(ADS_LIMIT)
        // "_\uf8ff" - это время обявления, которое может находиться в любом месте списка.
        readNextPageFromDb(query, filter, orderBy, readDataCallback)
    }

    fun getAllAdsFromCatFirstPage(cat: String, filter: String, readDataCallback: ReadDataCallback?) {
        val query = if(filter.isEmpty()) {
            db.orderByChild( "/adFilter/cat_time")
                .startAt(cat).endAt(cat + "_\uf8ff").limitToLast(ADS_LIMIT)
        // "_\uf8ff" - это время обявления, которое может находиться в любом месте списка.
        } else {
            getAllAdsFromCatByFilterFirstPage(cat, filter)
        }
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAdsFromCatByFilterFirstPage(cat: String, tempFilter: String): Query {
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        return db.orderByChild( "/adFilter/$orderBy")
            .startAt(filter).endAt(filter + "\uf8ff").limitToLast(ADS_LIMIT)
    // "_\uf8ff" - это время обявления, которое может находиться в любом месте списка.
    }

    fun getAllAdsFromCatNextPage(
        cat: String, time: String, filter: String, readDataCallback: ReadDataCallback?
    ) {
        if (filter.isEmpty()) {
            val query = db.orderByChild( "/adFilter/cat_time")
                .endBefore(cat + "_" + time).limitToLast(ADS_LIMIT)
            // "_\uf8ff" - это время обявления, которое может находиться в любом месте списка.
            readDataFromDb(query, readDataCallback)
        } else {
            getAllAdsFromCatByFilterNextPage(cat, time, filter, readDataCallback)
        }
    }

    fun getAllAdsFromCatByFilterNextPage(
        cat: String, time: String, tempFilter: String, readDataCallback: ReadDataCallback?
    ) {
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        val query = db.orderByChild( "/adFilter/$orderBy")
            .endBefore(filter + "_" + time).limitToLast(ADS_LIMIT)
    // "_\uf8ff" - это время обявления, которое может находиться в любом месте списка.
        readNextPageFromDb(query, filter, orderBy, readDataCallback)
    }

    fun deleteAd(ad: Ad, listener: FinishWorkListener) {
        if (ad.key == null || ad.uid == null) {
            return
        } else {
            val deleteMap = mapOf(
                "/adFilter" to null,
                "/info" to null,
                "/favs" to null,
                "/${ad.uid}" to null
            )
            db.child(ad.key).updateChildren(deleteMap).addOnCompleteListener {
                if (it.isSuccessful) deleteImagesFromStorage(ad, 0, listener)
            }
        }
    }

    private fun deleteImagesFromStorage(ad: Ad, index: Int, listener: FinishWorkListener) {
        val imageList = listOf(ad.mainImage, ad.image2, ad.image3)
        if (ad.mainImage == "empty") {
            listener.onFinish(true)
            return
        }
        dbStorage.storage.getReferenceFromUrl(imageList[index]).delete().addOnCompleteListener {
            if (it.isSuccessful) {
                if (imageList.size > index + 1) {
                    if (imageList[index + 1] != "empty") {
                        deleteImagesFromStorage(ad, index + 1, listener)
                } else {
                    listener.onFinish(true)
                }
            } else {
                    listener.onFinish(true)
                }
            }
        }
    }

    private fun readDataFromDb(query: Query, readDataCallback: ReadDataCallback?) {
       query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach {
                        if (ad == null) {
                            ad = it.child(AD_NODE).getValue(Ad::class.java)
                        }
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val favCounter = item.child(FAVS_NODE).childrenCount
                    val isFav = auth.uid?.let {
                        item.child(FAVS_NODE).child(it).getValue(String::class.java)
                    }
                    ad?.isFav = isFav != null

                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailsCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null) adArray.add(ad!!)
            }
                Log.d("MyLog", "Size db: ${adArray.size}")
                readDataCallback?.readData(adArray)
        }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun readNextPageFromDb(
        query: Query, filter: String, orderBy: String, readDataCallback: ReadDataCallback?
    ) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach {
                        if (ad == null) {
                            ad = it.child(AD_NODE).getValue(Ad::class.java)
                        }
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val filterNodeValue = item.child(FILTER_NODE).child(orderBy).value.toString()
                    Log.d("MyLog", "Filter value: $filterNodeValue")

                    val favCounter = item.child(FAVS_NODE).childrenCount
                    val isFav = auth.uid?.let {
                        item.child(FAVS_NODE).child(it).getValue(String::class.java)
                    }
                    ad?.isFav = isFav != null

                    ad?.favCounter = favCounter.toString()
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailsCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    if (ad != null && filterNodeValue.startsWith(filter)) adArray.add(ad!!)
                }
//                Log.d("MyLog", "Size db: ${adArray.size}")
                readDataCallback?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Ad>)
    }

    interface FinishWorkListener {
        fun onFinish(isDone: Boolean)
    }

    companion object {
        const val AD_NODE = "ad"
        const val FILTER_NODE = "adFilter"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
        const val ADS_LIMIT = 2
    }
}