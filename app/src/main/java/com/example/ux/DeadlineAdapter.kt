package com.example.ux

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import kotlin.collections.ArrayList

class DeadlineAdapter(private val dayList: ArrayList<LocalDate?>):
    RecyclerView.Adapter<DeadlineAdapter.ItemViewHolder>() {

        private lateinit var recyclerView: RecyclerView

        lateinit var listSize: String
        lateinit var myUid: String

        class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val dayText: TextView = itemView.findViewById(R.id.dayText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.calendar_cell, parent, false)

            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            myUid = FirebaseAuth.getInstance().currentUser?.uid!!
            val db = Firebase.database.getReference("moi")
            listSize = "0"

            var day = dayList[holder.adapterPosition] // 날짜
            var selectedDate: LocalDate = LocalDate.now() // 현재 날짜
            var iYear = day?.year
            var iMonth = day?.monthValue
            var iDay = day?.dayOfMonth

            if (day != null) {
                holder.dayText.text = day.dayOfMonth.toString()
                // 오늘 날짜 색상 지정
                if (day.isEqual(selectedDate)) {
                    holder.dayText.setTextColor(Color.parseColor("#48D366"))
                }

                val valueEventListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // 데이터베이스의 값이 변경되었을 때 실행되는 로직을 여기에 작성
                        db.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (users in dataSnapshot.children) {
                                    val uid = users.child("uid").value.toString()

                                    if (myUid == uid) {
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Error handling
                            }
                        })
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // 에러 처리
                    }
                }

                // ValueEventListener를 addValueEventListener로 등록하여 데이터의 변화를 감지합니다.
                db.addValueEventListener(valueEventListener)

                holder.itemView.setOnClickListener {
                    var yearMonth = "$iYear 년   $iMonth 월   $iDay 일"
                }
            } else {
                holder.dayText.text = ""
                holder.itemView.setOnClickListener(null)
            }
        }

        override fun getItemCount(): Int {
            return dayList.size
        }
}