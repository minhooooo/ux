package com.example.ux

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ux.databinding.ActivitySignUpBinding
import com.example.ux.databinding.FragmentScheduleBinding
import com.google.firebase.auth.FirebaseAuth


class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var uid: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = arguments?.getString(ARG_UID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_UID = "uid"

        fun newInstance(uid: String): ScheduleFragment {
            val fragment = ScheduleFragment()
            val bundle = Bundle().apply {
                putString(ARG_UID, uid)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun addtime(){
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
                        val params = child.layoutParams as GridLayout.LayoutParams

                        val column = child.tag.toString().toIntOrNull()

                        if (child is TextView && column != 0) {
                            child.setOnClickListener {
                                // 클릭된 TextView의 배경색 변경
                                child.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg6))
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
                }
                isCreate = !isCreate // 상태 토글
            }
        }
    }
}