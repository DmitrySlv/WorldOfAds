package com.ds_create.worldofads.act

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.ds_create.worldofads.MainActivity
import com.ds_create.worldofads.R
import com.ds_create.worldofads.adapters.ImageAdapter
import com.ds_create.worldofads.models.Ad
import com.ds_create.worldofads.models.DbManager
import com.ds_create.worldofads.databinding.ActivityEditAdsBinding
import com.ds_create.worldofads.dialogs.DialogSpinnerHelper
import com.ds_create.worldofads.fragments.FragmentCloseInterface
import com.ds_create.worldofads.fragments.ImageListFrag
import com.ds_create.worldofads.utils.CityHelper
import com.ds_create.worldofads.utils.ImageManager
import com.ds_create.worldofads.utils.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import java.io.ByteArrayOutputStream

class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {

    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var chooseImageFrag: ImageListFrag? = null
    var editImagePos = 0
    private var imageIndex = 0
    private var isEditState = false
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        checkEditState()
        imageChangeCounter()

        onClickSelectCountry()
        onClickSelectCity()
        onClickSelectCat()
        onClickGetImages()
        onClickPublish()
    }

    private fun checkEditState() {
        isEditState = isEditState()
        if (isEditState) {
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            ad?.let {
                fillViews(ad!!)
            }
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(binding) {
        tvCountry.text = ad.country
        tvCity.text = ad.city
        edTel.setText(ad.tel)
        edIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSent.toBoolean()
        tvCat.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
        updateImageCounter(0)
        ImageManager.fillImageArray(ad, imageAdapter)
    }

    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
    }

    //OnClicks
    fun onClickSelectCountry() = with(binding) {
        tvCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCountries(this@EditAdsAct)
            dialog.showSpinnerDialog(this@EditAdsAct, listCountry, tvCountry)
            if (tvCity.text.toString() != getString(R.string.select_city)) {
                tvCity.text = getString(R.string.select_city)
            }
        }
    }

    fun onClickSelectCity() = with(binding) {
        tvCity.setOnClickListener {
            val selectedCountry = tvCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {
                val listCity = CityHelper.getAllCities(this@EditAdsAct, selectedCountry)
                dialog.showSpinnerDialog(this@EditAdsAct, listCity, tvCity)
            } else {
                Toast.makeText(
                    this@EditAdsAct,
                    this@EditAdsAct.getString(R.string.no_country_selected),
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    fun onClickSelectCat() {
        binding.tvCat.setOnClickListener {
        val listCity = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this@EditAdsAct, listCity, binding.tvCat)
    }
}

    fun onClickGetImages() {
        binding.ibEdit.setOnClickListener {
            if (imageAdapter.mainArray.size == 0) {
                ImagePicker.getMultiImages(this, 3)
            } else {
                openChooseImageFrag(null)
                chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
            }
        }
    }

    fun onClickPublish() {
        binding.btPublish.setOnClickListener {
            if (isFieldsEmpty()) {
                showToast(getString(R.string.attention_fields_empty))
                return@setOnClickListener
            }
            binding.progressLayout.visibility = View.VISIBLE
            ad = fillAd()
            uploadImages()
            }
        }

    private fun isFieldsEmpty(): Boolean = with(binding) {
        return tvCountry.text.toString() == getString(R.string.select_country) ||
                tvCity.text.toString() == getString(R.string.select_city) ||
                edTel.text.isEmpty() ||
                edIndex.text.isEmpty() ||
                tvCat.text.toString() == getString(R.string.select_category) ||
                edTitle.text.isEmpty() ||
                edPrice.text.isEmpty() ||
                edDescription.text.isEmpty()
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object: DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                binding.progressLayout.visibility = View.GONE
               if (isDone) finish()
            }
        }
    }

    private fun fillAd(): Ad {
        val adTemp: Ad
        binding.apply {
            adTemp = Ad(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                edTel.text.toString(),
                edIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCat.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                edEmail.text.toString(),
                ad?.mainImage ?: "empty",
                ad?.image2 ?: "empty",
                ad?.image3 ?: "empty",
                ad?.key ?: dbManager.db.push().key,
                "0",
                dbManager.auth.uid,
                ad?.time ?: System.currentTimeMillis().toString()
            )
        }
        return adTemp
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
        updateImageCounter(binding.vpImages.currentItem)
    }

    fun openChooseImageFrag(newList: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFrag(this)
        if (newList != null) chooseImageFrag?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.placeHolder, chooseImageFrag!!)
        fm.commit()
    }

    private fun uploadImages() {
        if (imageIndex == 3) {
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
        val oldUrl = getUrlFromAd()
        if (imageAdapter.mainArray.size > imageIndex) {
            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
            if (oldUrl.startsWith("http")) {
                updateImage(byteArray, oldUrl) {
                    nextImage(it.result.toString())
                }
            } else {
                uploadImage(byteArray) {
//            dbManager.publishAd(ad!!, onPublishFinish())
                    nextImage(it.result.toString())
                }
            }
        } else {
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }

    private fun nextImage(uri: String) {
        setImageUriToAd(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAd(uri: String) {
        when (imageIndex) {
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(image2 = uri)
            2 -> ad = ad?.copy(image3 = uri)
        }
    }

    private fun getUrlFromAd(): String {
        return listOf(ad?.mainImage!!, ad?.image2!!, ad?.image3!!)[imageIndex]
    }

    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream)
        return outputStream.toByteArray()
    }

    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>) {
        val imStorageRef = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val uploadTask = imStorageRef.putBytes(byteArray)
        uploadTask.continueWithTask {
            task -> imStorageRef.downloadUrl.addOnCompleteListener(listener)
        }
    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>) {
       dbManager.dbStorage.storage.getReferenceFromUrl(oldUrl)
           .delete()
           .addOnCompleteListener(listener)
    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>) {
        val imStorageRef = dbManager.dbStorage.storage.getReferenceFromUrl(url)
        val uploadTask = imStorageRef.putBytes(byteArray)
        uploadTask.continueWithTask {
                task -> imStorageRef.downloadUrl.addOnCompleteListener(listener)
        }
    }

    private fun imageChangeCounter() {
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    private fun updateImageCounter(counter: Int) {
        var index = 1
        val itemCount = binding.vpImages.adapter?.itemCount
        if (itemCount == 0) index = 0
        val imageCounter = "${counter + index}/$itemCount"
        binding.tvImageCounter.text = imageCounter
    }
}