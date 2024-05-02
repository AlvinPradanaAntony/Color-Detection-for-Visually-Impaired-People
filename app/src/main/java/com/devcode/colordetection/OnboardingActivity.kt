package com.devcode.colordetection

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.viewpager2.widget.ViewPager2
import com.devcode.colordetection.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding : ActivityOnboardingBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var sectionsPagerAdapter: OnBoardingPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewPager()
        setupAction()
    }

    private fun setupViewPager() {
        val dotsIndicator = binding.dotsIndicator
        sectionsPagerAdapter = OnBoardingPagerAdapter(this)
        viewPager = binding.slideViewPager
        viewPager.adapter = sectionsPagerAdapter
        dotsIndicator.attachTo(viewPager)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == sectionsPagerAdapter.itemCount - 1) {
                    binding.buttonNavigation.text = getString(R.string.end_fragment)
                } else {
                    binding.buttonNavigation.text = getString(R.string.start_fragment)
                }
            }
        })
    }

    private fun setupAction() {
        binding.buttonNavigation.setOnClickListener {
            if (viewPager.currentItem + 1 < sectionsPagerAdapter.itemCount) {
                viewPager.currentItem += 1
            } else {
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                onBoardingFinished()
                finish()
            }
        }
        binding.skipButton.setOnClickListener {
            startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
            onBoardingFinished()
            finish()
        }
    }

    private fun onBoardingFinished(){
        val sharedPref = this@OnboardingActivity.getSharedPreferences("onBoarding", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }
}