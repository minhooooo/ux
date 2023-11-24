package com.example.ux

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ux.databinding.FragmentScheduleBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private var uid: String? = null
    private var clickedTextViewIds: MutableList<String> = mutableListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        uid = arguments?.getString("uid")
        Log.d("schedule", uid.toString())

        var currentTime = LocalDateTime.now();
        Log.d("dateTest", "1) 현재시간::  " + currentTime)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentTime.format(formatter)
        week(formattedDate)
        // Firebase에서 데이터 로드
        loadDataFromFirebase(uid!!)
        onUserInteractionChangedData()
        return binding.root

    }


    private fun loadDataFromFirebase(uid: String) {
        val db = Firebase.database.getReference("moi")
        val possibleRef = db.child(uid).child("timeTable").child("possible")

        possibleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터 로드 및 전역 변수에 저장
                snapshot.children.forEach { childSnapshot ->
                    childSnapshot.getValue(String::class.java)?.let {stringValue ->
                        clickedTextViewIds.add(stringValue)
                    }
                }
                // UI 업데이트
                updateUIWithLoadedData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read value.", error.toException())
            }
        })
    }

    private fun updateUIWithLoadedData() {
        clickedTextViewIds.forEach { textViewIdName ->
            val resId = resources.getIdentifier(textViewIdName, "id", requireContext().packageName)
            val textView = binding.root.findViewById<TextView>(resId)
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_selected)
            textView.setTag("cell_selected") // 태그 설정
        }
    }

    // 사용자 인터랙션에 의한 데이터 변경
    fun onUserInteractionChangedData() {
        binding.apply {
            var isCreate = true

            create.setOnClickListener {
                if (isCreate) {
                    // 현재 'create' 상태인 경우, 'apply' 상태로 변경
                    create.text = "적용"
                    create.setBackgroundResource(R.drawable.btn_circle_blue) // 'apply' 상태의 배경
                    create.setTextColor(Color.WHITE) // 'apply' 상태의 텍스트 색상

                    //gridlayout에 반응 추가
                    for (i in 0 until timeTableView.childCount) {
                        val child = timeTableView.getChildAt(i)
                        val column = child.tag.toString().toIntOrNull()

                        if (child is TextView && column != 0) {
                            child.setOnClickListener {
                                if (!isCreate){
                                    val resourceName = resources.getResourceEntryName(child.id)

                                    if (child.tag?.toString() == "cell_selected") {
                                        child.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_normal)
                                        child.setTag("cell_normal")
                                        clickedTextViewIds.remove(resourceName)
                                    }

                                    else{
                                        child.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_selected)
                                        child.setTag("cell_selected")
                                        clickedTextViewIds.add(resourceName)
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    // 현재 'apply' 상태인 경우, 'create' 상태로 변경
                    create.text = "등록"
                    create.setBackgroundResource(R.drawable.btn_circle_gray) // 'create' 상태의 배경
                    create.setTextColor(Color.parseColor("#787878")) // 'create' 상태의 텍스트 색상

                    //gridlayout에 색이 바뀐부분 db저장
                    saveDataToFirebase()
                }
                isCreate = !isCreate // 상태 토글
            }
        }
    }
    private fun saveDataToFirebase() {
        if (isNetworkAvailable()) {
            val db = Firebase.database.getReference("moi")
            val possibleRef = db.child(uid!!).child("timeTable").child("possible")
            possibleRef.setValue(clickedTextViewIds)
        } else {
            // TODO: 네트워크가 사용 불가능할 때
        }
    }

    // 네트워크 상태 체크
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
    }
    private fun week(eventDate: String) {
        val dateArray = eventDate.split("-").toTypedArray()

        val year = dateArray[0].toInt()
        val month = dateArray[1].toInt()
        val day = dateArray[2].toInt()

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1) // Calendar.MONTH는 0부터 시작합니다. 예: 0은 1월, 1은 2월, ...
        cal.set(Calendar.DAY_OF_MONTH, day)
        val weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH)
        Log.d("scheduls", "EventDate: $year 년 $month 월 $weekOfMonth 번째주")

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}