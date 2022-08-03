package com.ds_create.worldofads.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ds_create.worldofads.R
import com.ds_create.worldofads.act.EditAdsAct
import com.ds_create.worldofads.databinding.ListImageFragBinding
import com.ds_create.worldofads.dialoghelper.ProgressDialog
import com.ds_create.worldofads.utils.AdapterCallback
import com.ds_create.worldofads.utils.ImageManager
import com.ds_create.worldofads.utils.ImagePicker
import com.ds_create.worldofads.utils.ItemTouchMoveCallback
import kotlinx.coroutines.*

class ImageListFrag(
    private val fragmentCloseInterface: FragmentCloseInterface
): BaseAdsFrag(), AdapterCallback {

    val adapter = SelectImageRvAdapter(this)
    val dragCallback = ItemTouchMoveCallback(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var job: Job?= null
    private var addImageItem: MenuItem? = null
    lateinit var binding: ListImageFragBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ListImageFragBinding.inflate(layoutInflater, container, false)
        adView = binding.adView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        binding.apply {
            touchHelper.attachToRecyclerView(rcViewSelectImage)
            rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
            rcViewSelectImage.adapter = adapter
        }
    }

    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
        adapter.updateAdapter(bitmapList, true)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this@ImageListFrag)?.commit()
        fragmentCloseInterface.onFragClose(adapter.mainArray)
        job?.cancel()
    }

    fun resizeSelectedImages(newList: ArrayList<Uri>, needClear: Boolean, activity: Activity) {
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity)
            val bitmapList = ImageManager.imageResize(newList, activity)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if (adapter.mainArray.size > 2) {
                addImageItem?.isVisible = false
            }
        }
    }

    private fun setUpToolbar() {
        binding.apply {
            tb.inflateMenu(R.menu.menu_choose_image)
            val deleteItem = tb.menu.findItem(R.id.id_delete_image)
            addImageItem = tb.menu.findItem(R.id.id_add_image)
            if (adapter.mainArray.size > 2) {
                addImageItem?.isVisible = false
            }

            tb.setNavigationOnClickListener {
                showInterAd()
            }

            deleteItem.setOnMenuItemClickListener {
                adapter.updateAdapter(ArrayList(), true)
                addImageItem?.isVisible = true
                true
            }
            addImageItem?.setOnMenuItemClickListener {
                val imageCounter = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
                ImagePicker.addImages(
                    activity as EditAdsAct,
                    imageCounter
                )
                true
            }
        }
    }

    fun updateAdapter(newList: ArrayList<Uri>, activity: Activity) {
        resizeSelectedImages(newList, false, activity)
    }

    fun setSingleImage(uri: Uri, pos: Int) {
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), activity as Activity)
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }
    }
}