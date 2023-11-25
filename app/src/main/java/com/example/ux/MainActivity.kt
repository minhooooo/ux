package com.example.ux

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.ux.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startFragment()
    }

    fun startFragment() {
        // 하단 탭이 눌렸을 때 화면을 전환하기 위해선 이벤트 처리하기 위해 BottomNavigationView 객체 생성
        var mainBnv = findViewById<BottomNavigationView>(R.id.main_bnv)
        val uid = intent.getStringExtra("uid")
        val bundle = Bundle()
        bundle.putString("uid", uid)
        Log.d("mainactivity", uid.toString())
        // OnNavigationItemSelectedListener를 통해 탭 아이템 선택 시 이벤트를 처리
        // navi_menu.xml 에서 설정했던 각 아이템들의 id를 통해 알맞은 프래그먼트로 변경하게 한다.

        mainBnv.run()
        {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.first -> {
                        // 다른 프래그먼트 화면으로 이동하는 기능
                        val chatlistFragment = ChatlistFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fl_container, chatlistFragment).commit()
                    }
                    R.id.second -> {
                        val calendarFragment = CalendarFragment()
                        calendarFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fl_container, calendarFragment).commit()
                    }
                    R.id.third -> {
                        val profileFragment = ProfileFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fl_container, profileFragment).commit()
                    }
                }
                true
            }
            selectedItemId = R.id.first
        }
    }
}