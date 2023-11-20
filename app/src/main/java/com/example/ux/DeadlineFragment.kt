package com.example.ux

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.FragmentDeadlineBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class DeadlineFragment : Fragment() {

    lateinit var selectedDate: LocalDate
    lateinit var binding: FragmentDeadlineBinding

    lateinit var myUid: String
    lateinit var setMonth: String
    lateinit var getMonth: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeadlineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedDate = LocalDate.now() // 현재 날짜
        setMonth = selectedDate.month.toString()
        setMonthView() // 월 이동

        binding.lastMonth.setOnClickListener {
            // 이전 달 버튼 이벤트
            selectedDate = selectedDate.minusMonths(1)
            setMonth = selectedDate.month.toString()
            setMonthView()
        }

        binding.nextMonth.setOnClickListener {
            // 다음 달 버튼 이벤트
            selectedDate = selectedDate.plusMonths(1)
            setMonth = selectedDate.month.toString()
            setMonthView()
        }
    }

    private fun setMonthView() {
        binding.nowMonth.text = monthYearFromDate(selectedDate)

        // 날짜 생성해서 리스트 담기
        val dayList = dayInMonthArray(selectedDate)
        val adapter = DeadlineAdapter(dayList)
        var manager: RecyclerView.LayoutManager = GridLayoutManager(requireContext(), 7)

        calendarFromFirebase() // 월별 정보 가져오기

        binding.calendar.layoutManager = manager
        binding.calendar.adapter = adapter
    }

    private fun calendarFromFirebase() {
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!

        val db = Firebase.database.getReference("moi")

        // ValueEventListener 생성
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 데이터베이스의 값이 변경되었을 때 실행되는 로직을 여기에 작성
                db.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (users in dataSnapshot.children) {
                            val uid = users.child("uid").value.toString()

                            if (myUid == uid) {
                                when (setMonth) {
                                    "JANUARY" -> getMonth = "1m"
                                    "FEBRUARY" -> getMonth = "2m"
                                    "MARCH" -> getMonth = "3m"
                                    "APRIL" -> getMonth = "4m"
                                    "MAY" -> getMonth = "5m"
                                    "JUNE" -> getMonth = "6m"
                                    "JULY" -> getMonth = "7m"
                                    "AUGUST" -> getMonth = "8m"
                                    "SEPTEMBER" -> getMonth = "9m"
                                    "OCTOBER" -> getMonth = "10m"
                                    "NOVEMBER" -> getMonth = "11m"
                                    "DECEMBER" -> getMonth = "12m"
                                }
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
    }

    private fun monthYearFromDate(date: LocalDate): String {
        var formatter = DateTimeFormatter.ofPattern("MM월 yyyy")
        return date.format(formatter)
    }

    private fun dayInMonthArray(date: LocalDate): ArrayList<LocalDate?> {
        // 날짜 생성
        var dayList = ArrayList<LocalDate?>()
        var yearMonth = YearMonth.from(date)

        // 해당 월 마지막 날짜 가져오기(28, 30, 31일)
        var lastDay = yearMonth.lengthOfMonth()

        // 해당 월 첫 번째 날짜 가져오기(예: 5월 1일)
        var firstDay = selectedDate.withDayOfMonth(1)

        // 첫 번째 날 요일 가져오기(월:1, 일:7)
        var dayOfWeek = firstDay.dayOfWeek.value

        for(i in 1..41) {
            if(i <= dayOfWeek || i > (lastDay + dayOfWeek)) {
                dayList.add(null)
            }else {
                dayList.add(LocalDate.of(selectedDate.year, selectedDate.monthValue, i - dayOfWeek))
            }
        }

        return dayList
    }
}