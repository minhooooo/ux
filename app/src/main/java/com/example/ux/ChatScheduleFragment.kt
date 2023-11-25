package com.example.ux

import android.content.Context
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
import com.example.ux.databinding.FragmentChatScheduleBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class ChatScheduleFragment : Fragment() {
    private var _binding: FragmentChatScheduleBinding? = null
    private val binding get() = _binding!!
    private var chatId: String? = null
    private var uid: String? = null
    private var FixededTextViewIds: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatScheduleBinding.inflate(inflater, container, false)
        chatId = arguments?.getString("chatId")
        uid = arguments?.getString("uid")
        Log.d("schedule", chatId.toString())

        var currentTime = LocalDateTime.now();

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentTime.format(formatter)

        Log.d("dateTest", "현재 날짜: " + formattedDate)

        var currentweek = week(formattedDate)


        onUserInteractionChangedData(currentweek)
        // Firebase에서 데이터 로드
        loadDataFromFirebase(chatId!!,currentweek) //db가져오기
        return binding.root

    }


    private fun loadDataFromFirebase(chatId: String, currentweek:Array<String>) {

    }

    private fun updateUIWithLoadedData() {

    }

    // 사용자 인터랙션에 의한 데이터 변경
    fun onUserInteractionChangedData(currentweek: Array<String>) {
        binding.apply {
            var tempweek=currentweek
            nowYear.setText("${tempweek[0]}년")
            nowWeek.setText("${tempweek[1]}월${tempweek[2]}일 ~ ${tempweek[4]}월${tempweek[5]}일")
            lastWeek.setOnClickListener {
                FixededTextViewIds.forEach { textViewIdName ->
                    val resId = resources.getIdentifier(textViewIdName, "id", requireContext().packageName)
                    val textView = binding.root.findViewById<TextView>(resId)
                    textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_normal)
                    textView.setTag("cell_normal") // 태그 설정
                }
                FixededTextViewIds.clear()

                tempweek=changeweek(tempweek,-7)

                // UI 업데이트
                nowYear.setText("${tempweek[0]}년")
                nowWeek.setText("${tempweek[1]}월${tempweek[2]}일 ~ ${tempweek[4]}월${tempweek[5]}일")

                loadDataFromFirebase(uid!!,tempweek)
            }
            nextWeek.setOnClickListener {
                FixededTextViewIds.forEach { textViewIdName ->
                    val resId = resources.getIdentifier(textViewIdName, "id", requireContext().packageName)
                    val textView = binding.root.findViewById<TextView>(resId)
                    textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_normal)
                    textView.setTag("cell_normal") // 태그 설정
                }
                FixededTextViewIds.clear()

                tempweek=changeweek(tempweek,7)
                // UI 업데이트
                nowYear.setText("${tempweek[0]}년")
                nowWeek.setText("${tempweek[1]}월${tempweek[2]}일 ~ ${tempweek[4]}월${tempweek[5]}일")

                loadDataFromFirebase(uid!!,tempweek)
            }

        }
    }
    private fun saveDataToFirebase(currentweek: Array<String>) {

        if (isNetworkAvailable()) {
            val db = Firebase.database.getReference("moi")
            val possibleRef = db.child(uid!!).child("timeTable").child("possible")
                .child(currentweek[0]).child(currentweek[1]).child(currentweek[2])
            possibleRef.setValue(FixededTextViewIds)
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
    private fun week(eventDate: String): Array<String> {
        val dateArray = eventDate.split("-").toTypedArray()

        val cal = Calendar.getInstance()
        cal[dateArray[0].toInt(), dateArray[1].toInt() - 1] = dateArray[2].toInt()

        // 일주일의 첫날을 일요일
        cal.firstDayOfWeek = Calendar.SUNDAY

        // 시작일과 특정날짜의 차이
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK] - cal.firstDayOfWeek

        // 해당 주차의 첫째날을 지정
        cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek)

        val sf = SimpleDateFormat("yyyy-MM-dd")

        // 해당 주차의 첫째 날짜
        val startDt = sf.format(cal.time)

        cal.add(Calendar.DAY_OF_MONTH, 6)

        val endDt = sf.format(cal.time)

        val startDtArray = startDt.split("-").toTypedArray()
        val endDtArray = endDt.split("-").toTypedArray()

        Log.d("schedule", startDtArray.joinToString(",")+"~"+endDtArray.joinToString(","))

        return startDtArray + endDtArray
    }

    private fun changeweek(tempweek:Array<String>,num:Int):Array<String>{
        // 현재 주의 시작 날짜를 기반으로 계산
        var cal = Calendar.getInstance()
        cal.set(tempweek[0].toInt(), tempweek[1].toInt() - 1, tempweek[2].toInt()) // 현재 주의 시작 날짜 설정
        cal.add(Calendar.DAY_OF_MONTH, num)

        // 새로운 주의 날짜로 tempweek 업데이트
        var newYear = cal.get(Calendar.YEAR).toString()
        var newMonth = (cal.get(Calendar.MONTH) + 1).toString() // 월은 0부터 시작하므로 1 더함
        var newDay = cal.get(Calendar.DAY_OF_MONTH).toString()

        // tempweek 업데이트
        tempweek[0] = newYear
        tempweek[1] = newMonth
        tempweek[2] = newDay

        cal.set(tempweek[3].toInt(), tempweek[4].toInt() - 1, tempweek[5].toInt()) // 현재 주의 시작 날짜 설정
        cal.add(Calendar.DAY_OF_MONTH, num)

        newYear = cal.get(Calendar.YEAR).toString()
        newMonth = (cal.get(Calendar.MONTH) + 1).toString() // 월은 0부터 시작하므로 1 더함
        newDay = cal.get(Calendar.DAY_OF_MONTH).toString()

        tempweek[3] = newYear
        tempweek[4] = newMonth
        tempweek[5] = newDay

        Log.d("schedule", tempweek.joinToString(","))

        return tempweek
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

