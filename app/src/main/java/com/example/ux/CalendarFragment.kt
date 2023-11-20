package com.example.ux

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ux.databinding.FragmentCalendarBinding
import com.google.android.material.tabs.TabLayout

class CalendarFragment : Fragment() {

    lateinit var binding: FragmentCalendarBinding
    lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toolbar()
    }

    private fun Toolbar() {
        val tabLayout = binding.friendTabs

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabPosition = tab?.position

                tab?.icon?.setColorFilter(Color.parseColor("#002AFF"), PorterDuff.Mode.SRC_IN)

                when (tabPosition) {
                    0 -> {
                        val scheduleFragment = ScheduleFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.friend_container, scheduleFragment).commit()
                    }
                    1 -> {
                        val deadlineFragment = DeadlineFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.friend_container, deadlineFragment).commit()
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
        val scheduleFragment = ScheduleFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.friend_container, scheduleFragment).commit()

        tabLayout.getTabAt(0)?.icon?.setColorFilter(Color.parseColor("#002AFF"), PorterDuff.Mode.SRC_IN)
    }
}