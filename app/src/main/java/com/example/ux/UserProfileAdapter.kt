package com.example.ux

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.model.UserProfile

class UserProfileAdapter(private val userList: List<UserProfile>) : RecyclerView.Adapter<UserProfileAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileBG: ImageView = view.findViewById(R.id.profileBG)
        val profileName: TextView = view.findViewById(R.id.profileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rank_profile_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userProfile = userList[position]

        val resourceId = holder.itemView.context.resources.getIdentifier(
            userProfile.profileColor, "drawable", holder.itemView.context.packageName
        )

        val defaultId = R.drawable.bg6

        if (resourceId != 0) {
            holder.profileBG.setImageResource(resourceId)
        } else {
            // 리소스 ID가 없을 경우 기본 이미지를 설정합니다.
            holder.profileBG.setImageResource(defaultId)
        }
        Log.d("name",userProfile.username)
        holder.profileName.text = userProfile.username
    }

    override fun getItemCount() = userList.size
}
