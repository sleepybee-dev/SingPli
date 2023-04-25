package com.sleepybee.singpli.ui.mypage

/**
 * Created by leeseulbee on 2023/04/25.
 */

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.sleepybee.singpli.R
import com.sleepybee.singpli.databinding.FragmentMypageBinding
import com.sleepybee.singpli.ui.adapter.HeartedListAdapter
import com.sleepybee.singpli.ui.adapter.MyPageTabAdapter
import com.sleepybee.singpli.ui.common.BaseDataBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPageFragment : BaseDataBindingFragment<FragmentMypageBinding>(R.layout.fragment_mypage) {

    private val heartedViewModel: HeartedViewModel by viewModels()
    private var heartedListAdapter = HeartedListAdapter()

    override fun initBinding() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewpagerMypage.adapter = MyPageTabAdapter(requireActivity())
        TabLayoutMediator(binding.tabMypage, binding.viewpagerMypage) { tab, position ->
            when (position) {
                0 -> "내 선곡"
                1 -> "찜한 선곡"
                2 -> "찜한 플리"
            }
        }.attach()
    }

}