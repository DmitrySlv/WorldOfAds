package com.ds_create.worldofads.fragments

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ds_create.worldofads.R
import com.ds_create.worldofads.act.EditAdsAct
import com.ds_create.worldofads.databinding.SelectImageFragItemBinding
import com.ds_create.worldofads.utils.AdapterCallback
import com.ds_create.worldofads.utils.ImageManager
import com.ds_create.worldofads.utils.ImagePicker
import com.ds_create.worldofads.utils.ItemTouchMoveCallback

class SelectImageRvAdapter(
    val adapterCallback: AdapterCallback
    ): RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(),
    ItemTouchMoveCallback.ItemTouchAdapter {

    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageHolder {
        val viewBinding = SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(viewBinding, parent.context, this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
       holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
       return mainArray.size
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(
       val viewBinding: SelectImageFragItemBinding,
       val context: Context,
       val adapter: SelectImageRvAdapter
        ) : RecyclerView.ViewHolder(viewBinding.root) {

       fun setData(bitmap: Bitmap) {
           viewBinding.imEditImage.setOnClickListener {
               ImagePicker.getSingleImage(context as EditAdsAct)
               context.editImagePos = adapterPosition
           }
           viewBinding.imDelete.setOnClickListener {
               adapter.mainArray.removeAt(adapterPosition)
               adapter.notifyItemRemoved(adapterPosition)
               for (n in 0 until adapter.mainArray.size) {
                   adapter.notifyItemChanged(n)
               }
               adapter.adapterCallback.onItemDelete()
           }
           viewBinding.tvTitle.text = context.resources.getStringArray(R.array.title_image_array)[adapterPosition]
           ImageManager.chooseScaleType(viewBinding.imageContent, bitmap)
           viewBinding.imageContent.setImageBitmap(bitmap)
       }
    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean) {
        if (needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }

}