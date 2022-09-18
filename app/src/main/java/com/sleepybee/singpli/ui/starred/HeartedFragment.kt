package com.sleepybee.singpli.ui.starred

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sleepybee.singpli.databinding.FragmentHeartedBinding
import com.sleepybee.singpli.ui.adapter.HeartedListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HeartedFragment : Fragment() {

    private var _binding: FragmentHeartedBinding? = null

    private val binding get() = _binding!!

    private var heartedViewModel: HeartedViewModel? = null
    private var heartedListAdapter = HeartedListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        heartedViewModel =
            ViewModelProvider(this)[HeartedViewModel::class.java]

        _binding = FragmentHeartedBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.rvRecentHearted.adapter = heartedListAdapter

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        heartedViewModel?.getHeartedSnippets()?.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                binding.rvRecentHearted.visibility = View.GONE
                binding.tvNoDataHearted.visibility = View.VISIBLE
            } else {
                binding.rvRecentHearted.visibility = View.VISIBLE
                binding.tvNoDataHearted.visibility = View.GONE
                heartedListAdapter.setSnippetList(ArrayList(it))

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}