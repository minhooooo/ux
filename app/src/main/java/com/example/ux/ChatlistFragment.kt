package com.example.ux

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.ChatlistData


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatlistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class ChatlistFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatlistAdapter: ChatlistAdapter
    private val chatList: MutableList<ChatlistData> = mutableListOf(
        ChatlistData(R.drawable.bg12,"UX Design","이따 봬요!"),
        ChatlistData(R.drawable.bg11,"전공기초프로젝트1","저희 보고서 어떻게 됐나요"),
        ChatlistData(R.drawable.bg8,"소프트웨어 아키텍처","오늘 수업 없죠?"),
        ChatlistData(R.drawable.bg1,"동아리 팀플","저 이번주 스터디 못갈것같은데요.."),
        ChatlistData(R.drawable.bg12,"UX Design","이따 봬요!"),
        ChatlistData(R.drawable.bg11,"전공기초프로젝트1","저희 보고서 어떻게 됐나요"),
        ChatlistData(R.drawable.bg8,"소프트웨어 아키텍처","오늘 수업 없죠?"),
        ChatlistData(R.drawable.bg1,"동아리 팀플","저 이번주 스터디 못갈것같은데요.."),
        ChatlistData(R.drawable.bg12,"UX Design","이따 봬요!"),
        ChatlistData(R.drawable.bg11,"전공기초프로젝트1","저희 보고서 어떻게 됐나요"),
        ChatlistData(R.drawable.bg8,"소프트웨어 아키텍처","오늘 수업 없죠?"),
        ChatlistData(R.drawable.bg1,"동아리 팀플","저 이번주 스터디 못갈것같은데요..")
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chatlist, container,false)

        //RecyclerView 초기화
        recyclerView = view.findViewById(R.id.chat_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatlistAdapter = ChatlistAdapter(chatList.toTypedArray()) // Adapter 초기화
        recyclerView.adapter = chatlistAdapter // RecyclerView에 Adapter 설정


        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatlistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}