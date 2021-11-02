package com.gmail.beprogressive.it.slidingactivity.sample

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.beprogressive.it.slidingactivity.SlidingActivity
import com.gmail.beprogressive.it.slidingactivity.sample.databinding.ActivityMainBinding


class MainActivity : SlidingActivity() {

    private lateinit var binding: ActivityMainBinding

    private var alertTouch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this, MainViewModelFactory()).get(
            MainViewModel::class.java
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        binding.activity = this
    }

    override fun getSlidingContainer(): View {
        return binding.slidingRoot
    }

    override fun onSlidingFinished() {
    }

    override fun onSlidingStarted() {
    }

    override fun canSlideRight(): Boolean {
        return !alertTouch
    }

    fun onStartFragmentClick() {
        startFragment()
    }

    private fun startFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.animator.slide_in_from_right, 0, 0,
                R.animator.slide_out_to_right
            ).add(R.id.container, FragmentMain(), "Fragment")
            .addToBackStack("Fragment").commit()
    }
}

class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor().newInstance()
    }
}