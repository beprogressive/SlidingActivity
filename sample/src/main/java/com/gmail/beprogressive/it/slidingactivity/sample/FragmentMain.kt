package com.gmail.beprogressive.it.slidingactivity.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.gmail.beprogressive.it.slidingactivity.SlidingActivity
import com.gmail.beprogressive.it.slidingactivity.SlidingFragment
import com.gmail.beprogressive.it.slidingactivity.sample.databinding.FragmentBinding

class FragmentMain : SlidingFragment() {

    private lateinit var binding: FragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment, container, false)
        binding.viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.activity = activity as MainActivity

        return binding.root
    }

    override fun getSlidingContainer(): View {
        return binding.slidingContainer
    }

    override fun canSlideRight(): Boolean {
        return (activity as? SlidingActivity)?.canSlideRight()?: false
    }
}