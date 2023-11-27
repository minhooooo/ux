package com.example.ux

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.ChatlistItemViewBinding
import com.example.ux.databinding.FriendItemViewBinding

class FriendlistAdapter(private val items: Array<FriendData>) :
    RecyclerView.Adapter<FriendlistAdapter.FriendlistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendlistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FriendItemViewBinding.inflate(inflater, parent, false)
        return FriendlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendlistViewHolder, position: Int) {
        val item = items[position]
        val listener = View.OnClickListener { it ->
            Toast.makeText(
                it.context,
                "Clicked -> name : ${item.name}, status msg : ${item.statusMsg}",
                Toast.LENGTH_SHORT
            ).show()
        }
        holder.bind(listener, item)
    }

    override fun getItemCount(): Int = items.size

    class FriendlistViewHolder(private val binding: FriendItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, item: FriendData) {
            binding.apply {
                //friendItemPictureIv.setBackgroundResource(item.imgResId)
                val circleBackgroundDrawable = friendItemPictureIv.background as GradientDrawable
                circleBackgroundDrawable.setColor(Color.RED)
                friendItemNameTv.text = item.name
                friendItemStatusTv.text = item.statusMsg
                root.setOnClickListener(listener)
            }
        }
    }

}