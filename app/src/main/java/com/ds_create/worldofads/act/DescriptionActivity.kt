package com.ds_create.worldofads.act

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.ds_create.worldofads.MainActivity
import com.ds_create.worldofads.R
import com.ds_create.worldofads.adapters.ImageAdapter
import com.ds_create.worldofads.databinding.ActivityDescriptionBinding
import com.ds_create.worldofads.models.Ad
import com.ds_create.worldofads.utils.ImageManager

class DescriptionActivity : AppCompatActivity() {

    lateinit var binding: ActivityDescriptionBinding
    lateinit var imageAdapter: ImageAdapter
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.fbTel.setOnClickListener { call() }
        binding.fbEmail.setOnClickListener { sendEmail() }
    }

    private fun init() {
        imageAdapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = imageAdapter
        }
        getIntentFromMainAct()
        imageChangeCounter()
    }

    private fun getIntentFromMainAct() {
        ad = intent.getSerializableExtra(MainActivity.AD) as Ad
        if (ad != null) updateUi(ad!!)
    }

    private fun updateUi(ad: Ad) {
        ImageManager.fillImageArray(ad, imageAdapter)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: Ad) = with(binding) {
        tvCountry.text = ad.country
        tvCity.text = ad.city
        tvTel.text = ad.tel
        tvIndex.text = ad.index
        tvWithSent.text = isWithSent(ad.withSent.toBoolean())
        tvTitle.text = ad.title
        tvPrice.text = ad.price
        tvDescription.text = ad.description
        tvEmail.text = ad.email
    }

    private fun isWithSent(withSent: Boolean): String {
        return if (withSent) getString(R.string.with_sent_yes)
        else getString(R.string.with_sent_no)
    }

    private fun call() {
        val callUri = "tel:${ad?.tel}"
        val intentCall = Intent(Intent.ACTION_DIAL)
        intentCall.data = callUri.toUri()
        startActivity(intentCall)
    }

    private fun sendEmail() {
        val intentSendEmail = Intent(Intent.ACTION_SEND)
        intentSendEmail.type = "message/rfc822" //данный стринг нужно запомнить для отправки этой функции
        intentSendEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email)) //Нужно чтобы был именно arrayOf. Для вставки в стр email в письме
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.intent_ad_subject)) //для вставки в стр "Название" в письме
            putExtra(Intent.EXTRA_TEXT, "Здравствуйте, меня заинтересовало ваше объявление!") //Сообщение в письме
        }
        try {
            startActivity(Intent.createChooser(intentSendEmail, getString(R.string.to_open_with))) //сообщение спрашивающее с чем открыть email. Если есть app для открытия
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.toast_not_found_email_send_app), Toast.LENGTH_LONG).show() //Сообщение в случае если нет app для открытия email
        }
    }

    private fun imageChangeCounter() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
                binding.tvImageCounter.text = imageCounter
            }
        })
    }
}