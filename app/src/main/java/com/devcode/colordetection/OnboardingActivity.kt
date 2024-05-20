package com.devcode.colordetection

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.devcode.colordetection.databinding.ActivityOnboardingBinding
import java.util.Locale

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var sectionsPagerAdapter: OnBoardingPagerAdapter
    private lateinit var textToSpeech: TextToSpeech
    private var isVoiceOverEnabled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupTextToSpeech()
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
                if (isVoiceOverEnabled) {
                    playVoiceOver(position)
                }
                binding.buttonNavigation.text =
                    if (position == sectionsPagerAdapter.itemCount - 1) {
                        getString(R.string.end_fragment)
                    } else {
                        getString(R.string.start_fragment)
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
        binding.apply {
            soundButton.setOnClickListener {
                if (textToSpeech.isSpeaking) {
                    textToSpeech.stop()
                    soundButton.setImageResource(R.drawable.ic_sound_off)
                    isVoiceOverEnabled = false
                } else {
                    playVoiceOver(viewPager.currentItem)
                    soundButton.setImageResource(R.drawable.ic_sound)
                    isVoiceOverEnabled = true
                }
            }
        }
    }

    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale("in", "ID"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Bahasa tidak didukung", Toast.LENGTH_SHORT).show()
                } else {
                    textToSpeech.setSpeechRate(1.15f)
                    val voiceMale = Voice("id-id-x-ide-network", Locale("in", "ID"), Voice.QUALITY_HIGH, Voice.LATENCY_LOW, true, null)
                    val voiceFemale = Voice("id-id-x-idc-network", Locale("in", "ID"), Voice.QUALITY_HIGH, Voice.LATENCY_LOW, true, null)
                    textToSpeech.voice = voiceMale
//                    textToSpeech.setPitch(1.16f) // Female
                    textToSpeech.setPitch(0.9f) // Male
                    playVoiceOver(viewPager.currentItem)
                    isVoiceOverEnabled = true
                }
            } else {
                Toast.makeText(this, "Inisialisasi TTS Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun playVoiceOver(position: Int) {
        val message = when (position) {
            0 -> "Selamat datang di Vision, aplikasi deteksi dan pengenalan warna yang dapat membantu Anda mengenali warna di sekitar."
            1 -> "Untuk menggunakan Vision, arahkan ponsel Anda ke benda atau lingkungan yang ingin Anda kenali warnanya. Vision akan memberikan feedback berupa audio yang memberitahukan warna yang dikenali. Mudah dan cepat. Selamat menggunakan Vision. Mari kita mulai !"
            else -> ""
        }
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun onBoardingFinished() {
        val sharedPref = this@OnboardingActivity.getSharedPreferences("onBoarding", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}