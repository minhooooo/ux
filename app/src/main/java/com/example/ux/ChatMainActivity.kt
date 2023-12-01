package com.example.ux

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.ux.databinding.ActivityChatMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth

class ChatMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatMainBinding
    private var uid: String? = null
    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        uid = FirebaseAuth.getInstance().currentUser?.uid!!
        chatId = intent.getStringExtra("chatId")
        Log.d("ChatMain", chatId.toString())

        setupToolbar()
    }

    private fun setupToolbar() {
        val tabLayout = binding.chatTabs
        val bundle = Bundle().apply {
            putString("chatId", chatId)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.icon?.setColorFilter(Color.parseColor("#002AFF"), PorterDuff.Mode.SRC_IN)

                when (tab.position) {
                    0 -> {
                        val chatScheduleFragment = ChatScheduleFragment().apply { arguments = bundle }
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.chat_container, chatScheduleFragment).commit()
                    }
                    1 -> {
                        val chatRankFragment = ChatRankFragment().apply { arguments = bundle }
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.chat_container, chatRankFragment).commit()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.icon?.setColorFilter(Color.parseColor("#E0E0E0"), PorterDuff.Mode.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // 구현하지 않음
            }
        })

        // 기본 프래그먼트 설정
        val chatScheduleFragment = ChatScheduleFragment().apply { arguments = bundle }
        supportFragmentManager.beginTransaction()
            .replace(R.id.chat_container, chatScheduleFragment).commit()

        tabLayout.getTabAt(0)?.icon?.setColorFilter(Color.parseColor("#002AFF"), PorterDuff.Mode.SRC_IN)
    }
}
