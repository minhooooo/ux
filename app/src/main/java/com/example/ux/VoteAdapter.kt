package com.example.ux

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.model.UserProfile
import com.example.ux.model.VoteData
import com.example.ux.model.VoteResult
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class VoteAdapter(private val dataList: List<VoteData>) : RecyclerView.Adapter<VoteAdapter.RankViewHolder>() {

    class RankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val agreeRecyclerView: RecyclerView = view.findViewById(R.id.agree_recyclerView)
        val disagreeRecyclerView: RecyclerView = view.findViewById(R.id.disagree_recyclerView)
        val yetRecyclerView: RecyclerView = view.findViewById(R.id.yet_recyclerView)
        val voteChart: PieChart = view.findViewById(R.id.chart)
        val day : TextView = view.findViewById(R.id.day)
        val time : TextView = view.findViewById(R.id.time)
        val agreebtn : RadioButton = view.findViewById(R.id.radio_agree)
        val disagreebtn : RadioButton = view.findViewById(R.id.radio_disagree)
        val dropbtn : ImageButton = view.findViewById(R.id.drop_button)
        val droplayout : LinearLayout = view.findViewById(R.id.drop_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rank_cell, parent, false)
        return RankViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
        val voteData = dataList[position]
        val dayOfWeekMap = mapOf(
            "monday" to "월요일",
            "tuesday" to "화요일",
            "wednesday" to "수요일",
            "thursday" to "목요일",
            "friday" to "금요일",
            "saturday" to "토요일",
            "sunday" to "일요일"
        )
        fun getKoreanDayOfWeek(dayOfWeek: String): String {
            return dayOfWeekMap[dayOfWeek.toLowerCase()] ?: "알 수 없는 요일"
        }
        val (dayOfWeek, time) = voteData.item.partition { it.isLetter() }
        val koreanDayOfWeek = getKoreanDayOfWeek(dayOfWeek)
        val starttime = time.substring(0, 2).toInt()
        val currentweek = voteData.currentweek
        val uid = voteData.userId

        val db = Firebase.database.getReference("chat")
        val meetingRef = db.child(voteData.chatId).child("meeting")
            .child(currentweek[0]).child(currentweek[1]).child(currentweek[2]).child("rank").child(voteData.item)
        meetingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 스냅샷을 사용하여 각 투표 범주의 자식 노드를 가져옵니다.
                val agreeVotesSnapshot = snapshot.child("agree")
                val disagreeVotesSnapshot = snapshot.child("disagree")
                val yetVotesSnapshot = snapshot.child("yet")

                // 각 범주의 투표자 ID를 리스트로 변환합니다.
                val agreeVotes = agreeVotesSnapshot.children.mapNotNull { it.key }
                val disagreeVotes = disagreeVotesSnapshot.children.mapNotNull { it.key }
                val yetVotes = yetVotesSnapshot.children.mapNotNull { it.key }

                // 'agree', 'disagree', 'yet' 데이터를 VoteResult 객체에 추가
                val voteResult = VoteResult(agreeVotes, disagreeVotes, yetVotes)
                // VoteResult 객체를 사용하여 UI 업데이트 등의 작업 수행
                val agreecount = voteResult.agreeVotes.size
                val disagreecount = voteResult.disagreeVotes.size
                val yetcount = voteResult.yetVotes.size

                //chart
                with(holder.voteChart) {
                    setUsePercentValues(true)
                    description.isEnabled = false
                    setExtraOffsets(5f, 10f, 5f, 5f)
                    dragDecelerationFrictionCoef = 0.95f
                    isDrawHoleEnabled = false

                    var yValues = ArrayList<PieEntry>()
                    //데이터 예시  PieEntry(40f, "Korea")

                    yValues.add(PieEntry(agreecount.toFloat(), "찬성"))
                    yValues.add(PieEntry(disagreecount.toFloat(), "반대"))
                    yValues.add(PieEntry(yetcount.toFloat(), "미참여"))


                    val dataSet = PieDataSet(yValues, "vote").apply {
                        sliceSpace = 3f
                        selectionShift = 5f
                        colors =  listOf(
                            Color.rgb(111, 195, 255), //찬성
                            Color.rgb(232, 136, 136), //반대
                            Color.rgb(211, 200, 207)  //미참여
                        )

                        setValueFormatter(object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "" // 모든 값에 대해 빈 문자열 반환
                            }
                        })
                    }

                    val data = PieData(dataSet)



                    setData(data)
                    animateY(1000, Easing.EaseInOutCubic)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리
            }
        })

        holder.day.setText(koreanDayOfWeek)
        holder.time.setText("${starttime}시 ~ ${starttime+1}시")


        var isCreate = true
        holder.dropbtn.setOnClickListener {
            if (isCreate) {
                holder.dropbtn.setImageResource(R.drawable.icon_droped)
                holder.droplayout.visibility=View.VISIBLE
                meetingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // 스냅샷을 사용하여 각 투표 범주의 자식 노드를 가져옵니다.
                        val agreeVotesSnapshot = snapshot.child("agree")
                        val disagreeVotesSnapshot = snapshot.child("disagree")
                        val yetVotesSnapshot = snapshot.child("yet")

                        // 각 범주의 투표자 ID를 리스트로 변환합니다.
                        val agreeVotes = agreeVotesSnapshot.children.mapNotNull { it.key }
                        val disagreeVotes = disagreeVotesSnapshot.children.mapNotNull { it.key }
                        val yetVotes = yetVotesSnapshot.children.mapNotNull { it.key }

                        var agreeVoteresult : MutableList<UserProfile> = mutableListOf()
                        var disagreeVoteresult : MutableList<UserProfile> = mutableListOf()
                        var yetVoteresult : MutableList<UserProfile> = mutableListOf()

                        // 'agree', 'disagree', 'yet'
                        for (uid in agreeVotes) {
                            val userRef = Firebase.database.getReference("moi").child(uid)

                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val profileColor = snapshot.child("profileColor").getValue(String::class.java) ?: "bg1"
                                    val username = snapshot.child("username").getValue(String::class.java) ?: "알 수 없는 사용자"

                                    agreeVoteresult.add(UserProfile(uid,profileColor,username))
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // 에러 처리
                                }
                            })
                        }
                        for (uid in disagreeVotes) {
                            val userRef = Firebase.database.getReference("moi").child(uid)

                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val profileColor = snapshot.child("profileColor").getValue(String::class.java) ?: "bg1"
                                    val username = snapshot.child("username").getValue(String::class.java) ?: "알 수 없는 사용자"

                                    disagreeVoteresult.add(UserProfile(uid,profileColor,username))
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // 에러 처리
                                }
                            })
                        }

                        for (uid in yetVotes) {
                            val userRef = Firebase.database.getReference("moi").child(uid)

                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val profileColor = snapshot.child("profileColor").getValue(String::class.java) ?: "bg1"
                                    val username = snapshot.child("username").getValue(String::class.java) ?: "알 수 없는 사용자"

                                    yetVoteresult.add(UserProfile(uid,profileColor,username))
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // 에러 처리
                                }
                            })
                        }

                        // agree
                        holder.agreeRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
                        holder.agreeRecyclerView.adapter = UserProfileAdapter(agreeVoteresult)

                        // disagree
                        holder.disagreeRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
                        holder.disagreeRecyclerView.adapter = UserProfileAdapter(disagreeVoteresult)

                        // yet
                        holder.yetRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
                        holder.yetRecyclerView.adapter = UserProfileAdapter(yetVoteresult)
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
            else {
                holder.dropbtn.setImageResource(R.drawable.icon_dropyet)
                holder.droplayout.visibility=View.GONE
            }
            isCreate = !isCreate
        }
        var currentUserVote: String? = null

        // 사용자의 투표 상태를 확인하는 함수
        fun updateUserVote(newVote: String) {

            // "agree", "disagree", "yet" 하위 항목에서 사용자의 uid 검색 및 업데이트
            val voteOptions = listOf("agree", "disagree", "yet")
            for (option in voteOptions) {
                meetingRef.child(option).child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && option != newVote) {
                            // 이전 투표 항목 삭제
                            meetingRef.child(option).child(uid).removeValue()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // 에러 처리
                    }
                })
                meetingRef.child(newVote).child(uid).setValue(true)
                currentUserVote = newVote
            }

            // 새 투표 상태 저장
            meetingRef.child(newVote).setValue(uid)
            currentUserVote = newVote
        }

        holder.agreebtn.setOnClickListener {
            if (currentUserVote != "agree") {
                updateUserVote("agree")
            }
        }
        holder.disagreebtn.setOnClickListener {
            if (currentUserVote != "disagree") {
                updateUserVote("disagree")
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}
