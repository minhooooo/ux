package com.example.ux

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import java.util.concurrent.atomic.AtomicInteger


class VoteAdapter(private val dataList: List<VoteData>) : RecyclerView.Adapter<VoteAdapter.RankViewHolder>() {
    class RankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val agreeRecyclerView: RecyclerView = view.findViewById(R.id.agree_recyclerView)
        val disagreeRecyclerView: RecyclerView = view.findViewById(R.id.disagree_recyclerView)
        val yetRecyclerView: RecyclerView = view.findViewById(R.id.yet_recyclerView)
        val voteChart: PieChart = view.findViewById(R.id.chart)
        val day : TextView = view.findViewById(R.id.day)
        val time : TextView = view.findViewById(R.id.time)
        val radioGroup :RadioGroup = view.findViewById(R.id.radio_group)
        val agreebtn : RadioButton = view.findViewById(R.id.radio_agree)
        val disagreebtn : RadioButton = view.findViewById(R.id.radio_disagree)
        val dropbtn : ImageButton = view.findViewById(R.id.drop_button)
        val droplayout : LinearLayout = view.findViewById(R.id.drop_text)
        val checkbox : CheckBox = view.findViewById(R.id.checkbox)
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

        var agreeVoteresult : MutableList<UserProfile> = mutableListOf()
        var disagreeVoteresult : MutableList<UserProfile> = mutableListOf()
        var yetVoteresult : MutableList<UserProfile> = mutableListOf()

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
                    isDrawHoleEnabled = false
                    legend.isEnabled = false

                    var yValues = ArrayList<PieEntry>()
                    //데이터 예시  PieEntry(40f, "Korea")

                    yValues.add(PieEntry(agreecount.toFloat(), ""))
                    yValues.add(PieEntry(disagreecount.toFloat(), ""))
                    yValues.add(PieEntry(yetcount.toFloat(), ""))


                    val dataSet = PieDataSet(yValues, "vote").apply {
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

        val fixedRef = db.child(voteData.chatId).child("meeting")
            .child(currentweek[0]).child(currentweek[1]).child(currentweek[2])

        holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                fixedRef.child("fix").child(voteData.item).setValue(true)
                fixedRef.child("rank").child(voteData.item).child("isChecked").setValue(true)
            }
            else {
                fixedRef.child("fix").child(voteData.item).removeValue()
                fixedRef.child("rank").child(voteData.item).child("isChecked").setValue(false)
            }
        }

        holder.checkbox.isChecked = voteData.isfixed
        Log.d("checkbox",voteData.isfixed.toString() )

        holder.dropbtn.setOnClickListener {
            if (voteData.isopend) {
                holder.dropbtn.setImageResource(R.drawable.icon_droped)
                holder.droplayout.visibility=View.VISIBLE
                // 각 RecyclerView의 초기 어댑터 설정은 여기서 수행합니다.
                agreeVoteresult.clear()
                disagreeVoteresult.clear()
                yetVoteresult.clear()

                holder.agreeRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
                holder.agreeRecyclerView.adapter = UserProfileAdapter(agreeVoteresult)

                holder.disagreeRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
                holder.disagreeRecyclerView.adapter = UserProfileAdapter(disagreeVoteresult)

                holder.yetRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
                holder.yetRecyclerView.adapter = UserProfileAdapter(yetVoteresult)

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

                        val totalVotesCount = agreeVotes.size + disagreeVotes.size + yetVotes.size
                        val loadedVotesCount = AtomicInteger(0)


                        // 'agree', 'disagree', 'yet'
                        fun loadUserProfile(uids: List<String>, result: MutableList<UserProfile>, onComplete: () -> Unit) {
                            for (uid in uids) {
                                val userRef = Firebase.database.getReference("moi").child(uid)
                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val profileColor = snapshot.child("profileColor").getValue(String::class.java) ?: "bg1"
                                        val username = snapshot.child("username").getValue(String::class.java) ?: "알 수 없는 사용자"
                                        result.add(UserProfile(uid, profileColor, username))

                                        // 모든 데이터 로드가 완료되었는지 확인합니다.
                                        if (loadedVotesCount.incrementAndGet() == totalVotesCount) {
                                            onComplete()
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // 에러 처리
                                    }
                                })
                            }
                        }

                        // 각 그룹의 사용자 프로필을 로드합니다.
                        loadUserProfile(agreeVotes, agreeVoteresult) {
                            // 모든 데이터 로드가 완료되면 RecyclerView를 업데이트합니다.
                            holder.agreeRecyclerView.adapter?.notifyDataSetChanged()
                            Log.d("notify","agreeVote "+agreeVoteresult.size.toString())
                        }
                        loadUserProfile(disagreeVotes, disagreeVoteresult) {
                            holder.disagreeRecyclerView.adapter?.notifyDataSetChanged()
                            Log.d("notify","disagreeVote "+disagreeVoteresult.size.toString())

                        }
                        loadUserProfile(yetVotes, yetVoteresult) {
                            holder.yetRecyclerView.adapter?.notifyDataSetChanged()
                            Log.d("notify","yetVote "+yetVoteresult.size.toString())

                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
            else {
                holder.dropbtn.setImageResource(R.drawable.icon_dropyet)
                holder.droplayout.visibility=View.GONE
            }
            voteData.isopend = !voteData.isopend
        }
        var currentUserVote = voteData.status

        fun updateRadioButtonState(vote: String) {
            when (vote) {
                "agree" -> {
                    holder.agreebtn.isChecked = true
                    holder.agreebtn.buttonTintList =
                        ContextCompat.getColorStateList(holder.agreebtn.context, R.color.bg6)
                    holder.disagreebtn.buttonTintList =
                        ContextCompat.getColorStateList(holder.disagreebtn.context, R.color.bg11)
                }

                "disagree" -> {
                    holder.disagreebtn.isChecked = true
                    holder.agreebtn.buttonTintList =
                        ContextCompat.getColorStateList(holder.agreebtn.context, R.color.bg6)
                    holder.disagreebtn.buttonTintList =
                        ContextCompat.getColorStateList(holder.disagreebtn.context, R.color.bg11)
                }

                "yet" -> {
                    holder.agreebtn.isChecked = false
                    holder.agreebtn.buttonTintList =
                        ContextCompat.getColorStateList(holder.agreebtn.context, R.color.bg6)
                    holder.disagreebtn.isChecked = false
                    holder.disagreebtn.buttonTintList =
                        ContextCompat.getColorStateList(holder.disagreebtn.context, R.color.bg11)
                }
            }
        }


        // 사용자의 투표 상태를 확인하는 함수
        fun updateUserVote(newVote: String) {
            // 이전 투표 항목 삭제 (이미 선택된 투표가 있다면)
            currentUserVote?.let {
                if (it != newVote) {
                    meetingRef.child(it).child(uid).removeValue()
                }
            }

            // 새 투표 상태 저장
            meetingRef.child(newVote).child(uid).setValue(true)
            currentUserVote = newVote
        }

        updateRadioButtonState(currentUserVote)

        holder.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_agree -> { // '동의' 버튼 ID
                    updateUserVote("agree")
                    updateRadioButtonState("agree")
                }
                R.id.radio_disagree -> { // '비동의' 버튼 ID
                    updateUserVote("disagree")
                    updateRadioButtonState("disagree")
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}
