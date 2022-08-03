package com.ds_create.worldofads.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.ds_create.worldofads.adapters.ImageAdapter
import com.ds_create.worldofads.models.Ad
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream


object ImageManager {

    fun getImageSize(uri: Uri, act: Activity): List<Int> {
        val inStream = act.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inStream, null, options)
        return listOf(options.outWidth, options.outHeight)
    }

   suspend fun imageResize(uris: ArrayList<Uri>, act: Activity): List<Bitmap> = withContext(Dispatchers.IO) {
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()
        for (n in uris.indices) {
            val size = getImageSize(uris[n], act)
            val imageRatio = size[IMAGE_WIDTH].toFloat() / size[IMAGE_HEIGHT].toFloat()

            if (imageRatio > 1) {
                if (size[IMAGE_WIDTH] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                } else {
                    tempList.add(listOf(size[IMAGE_WIDTH], size[IMAGE_HEIGHT]))
                }
            } else {
                if (size[IMAGE_HEIGHT] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                } else {
                    tempList.add(listOf(size[IMAGE_WIDTH], size[IMAGE_HEIGHT]))
                }
            }
        }
       for (i in uris.indices) {
           kotlin.runCatching {
               bitmapList.add(
                   Picasso.get().load(uris[i])
                       .resize(tempList[i][IMAGE_WIDTH], tempList[i][IMAGE_HEIGHT])
                       .get()
               )
           }
       }
       return@withContext bitmapList
    }

   private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO) {

        val bitmapList = ArrayList<Bitmap>()

        for (i in uris.indices) {
            kotlin.runCatching {
                bitmapList.add(Picasso.get().load(uris[i]).get())
            }
        }
        return@withContext bitmapList
    }

    fun fillImageArray(ad: Ad, imageAdapter: ImageAdapter) {
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        CoroutineScope(Dispatchers.Main).launch {
            val bitmapList = getBitmapFromUris(listUris)
            imageAdapter.update(bitmapList as ArrayList<Bitmap>)
        }
    }

    fun chooseScaleType(im: ImageView, bitmap: Bitmap) {
        if (bitmap.width > bitmap.height) {
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    private const val MAX_IMAGE_SIZE = 1000
    private const val IMAGE_WIDTH = 0
    private const val IMAGE_HEIGHT = 1
}