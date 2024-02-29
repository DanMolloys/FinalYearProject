package com.example.finalyearprojectdm

import android.content.Context
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.finalyearprojectdm.databinding.ImageItemBinding

class ImageAdapter(private val context: Context, private val imgs: TypedArray) : BaseAdapter() {

    override fun getCount(): Int {
        return imgs.length()
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    // create a new ImageView for each item referenced by the Adapter
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ImageItemBinding = if (convertView == null) {
            // if it's not recycled, inflate the layout with view binding
            ImageItemBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            // if it's recycled, reuse the existing binding
            convertView.tag as ImageItemBinding
        }

        binding.imageView.setImageResource(imgs.getResourceId(position, -1))
        // Set the tag to the binding for reuse
        binding.root.tag = binding
        return binding.root
    }
}