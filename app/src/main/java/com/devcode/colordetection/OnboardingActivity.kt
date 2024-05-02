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
    private lateinit var indicators: Array<TextView?>
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewPager()
        setupAction()
    }

    private fun setupViewPager() {
        sectionsPagerAdapter = OnBoardingPagerAdapter(this)
        viewPager = binding.slideViewPager
        viewPager.adapter = sectionsPagerAdapter
        initializeIndicators(sectionsPagerAdapter.itemCount)
        setUpIndicator(0)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setUpIndicator(position)
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
                finish()
            }
        }
        binding.skipButton.setOnClickListener {
            startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun initializeIndicators(count: Int) {
        indicators = arrayOfNulls(count)
        binding.indicatorLayout.removeAllViews()
        for (i in 0 until count) {
            indicators[i] = TextView(applicationContext)
            indicators[i]?.text = Html.fromHtml("&#8226;")
            indicators[i]?.textSize = 35f
            indicators[i]?.setTextColor(resources.getColor(R.color.tail_blue_100))
            binding.indicatorLayout.addView(indicators[i])
        }
    }

    private fun setUpIndicator(position: Int) {
        if (indicators.isNotEmpty()) {
            for (i in indicators.indices) {
                indicators[i]?.setTextColor(resources.getColor(R.color.tail_blue_100))
            }
            indicators[position]?.setTextColor(resources.getColor(R.color.tail_blue_500))
        }
    }
}