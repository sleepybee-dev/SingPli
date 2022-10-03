package com.sleepybee.singpli.ui.starred

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.sleepybee.singpli.R
import com.sleepybee.singpli.databinding.FragmentHeartedBinding
import com.sleepybee.singpli.ui.adapter.HeartedListAdapter
import com.sleepybee.singpli.ui.common.BaseDataBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HeartedFragment : BaseDataBindingFragment<FragmentHeartedBinding>(R.layout.fragment_hearted) {

    private val heartedViewModel: HeartedViewModel by viewModels()
    private var heartedListAdapter = HeartedListAdapter()

    override fun initBinding() {
        binding.rvRecentHearted.adapter = heartedListAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        heartedViewModel.heartedSnippets.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.rvRecentHearted.visibility = View.GONE
                binding.tvNoDataHearted.visibility = View.VISIBLE
            } else {
                binding.rvRecentHearted.visibility = View.VISIBLE
                binding.tvNoDataHearted.visibility = View.GONE
                heartedListAdapter.setSnippetList(ArrayList(it))
            }
        }
    }

}