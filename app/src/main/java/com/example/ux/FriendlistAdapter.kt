package com.example.ux

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.FriendItemViewBinding
import kotlin.coroutines.coroutineContext

class FriendlistAdapter(val context: Context, private val items: Array<FriendData>) :
    RecyclerView.Adapter<FriendlistAdapter.FriendlistViewHolder>() {

    /*private lateinit var friendUnit: (FriendData)->Unit*//*constructor(items : Array<FriendData>, listener: (FriendData) -> Unit) : this(items) {
        friendUnit = listener
    }*/

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
                "Clicked -> name : ${item.name}, univ : ${item.university}",
                Toast.LENGTH_SHORT
            ).show()
        }
        holder.bind(listener, item)
    }

    override fun getItemCount(): Int = items.size

    inner class FriendlistViewHolder(private val binding: FriendItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, item: FriendData) {
            binding.apply {
                //friendItemPictureIv.setBackgroundResource(item.imgResId)
//                val circleBackgroundDrawable = friendItemPictureIv.background as GradientDrawable

                val bgColor = item.imgName

                // bgColor를 기반으로 리소스 ID 동적으로 생성
                val resourceId = context.resources.getIdentifier(bgColor, "drawable", context.packageName)
                friendItemPictureIv.setBackgroundResource(resourceId)

//                circleBackgroundDrawable.setColor(Color.LTGRAY)
                friendItemNameTv.text = item.name
                friendItemUnivTv.text = item.university

                /*friendUnit?.let {
                        root.setOnClickListener {
                            friendUnit?.invoke(item)
                        }
                    }*/
            }
        }
    }

}

