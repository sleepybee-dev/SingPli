package com.sleepybee.singpli.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sleepybee.singpli.ui.mypage.HeartedFragment

/**
 * Created by leeseulbee on 2023/04/25.
 */
class MyPageTabAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    val tabList : List<Fragment> = listOf(HeartedFragment())

    override fun createFragment(position: Int): Fragment = tabList[position]

    override fun getItemCount(): Int = tabList.size


}