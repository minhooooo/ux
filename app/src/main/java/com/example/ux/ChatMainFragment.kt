package com.example.ux

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ux.databinding.FragmentChatMainBinding
import com.google.android.material.tabs.TabLayout

class ChatMainFragment : Fragment() {

    lateinit var binding: FragmentChatMainBinding
    lateinit var mContext: Context
    private var uid: String? = null
    private var chatId : String? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatMainBinding.inflate(inflater, container, false)
        uid = arguments?.getString("uid")
        chatId = arguments?.getString("chatId")
        Log.d("ChatMain", chatId.toString())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toolbar()
    }

    private fun Toolbar() {
        val tabLayout = binding.chatTabs
        val bundle = Bundle()
        bundle.putString("uid", uid)
        bundle.putString("chatId",chatId)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabPosition = tab?.position

                tab?.icon?.setColorFilter(Color.parseColor("#002AFF"), PorterDuff.Mode.SRC_IN)

                when (tabPosition) {
                    0 -> {
                        val chatscheduleFragment = ChatScheduleFragment()
                        chatscheduleFragment.arguments = bundle

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.chat_container, chatscheduleFragment).commit()
                    }
                    1 -> {
                        val chatrankFragment = ChatRankFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.chat_container, chatrankFragment).commit()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // icon color black으로
                tab?.icon?.setColorFilter(Color.parseColor("#E0E0E0"), PorterDuff.Mode.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //구현하지 않음
            }

        })

        // Set default fragment
//        val chatscheduleFragment = ChatScheduleFragment()
//        chatscheduleFragment.arguments = bundle
        val chatRankFragment = ChatRankFragment()
        chatRankFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.chat_container, chatRankFragment).commit()

        tabLayout.getTabAt(0)?.icon?.setColorFilter(Color.parseColor("#002AFF"), PorterDuff.Mode.SRC_IN)
    }
}