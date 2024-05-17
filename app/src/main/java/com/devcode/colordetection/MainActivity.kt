package com.devcode.colordetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.devcode.colordetection.databinding.ActivityMainBinding
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.Collections
import java.util.Locale

class MainActivity : CameraActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private lateinit var camera: JavaCameraView
    private lateinit var btnFlash: ImageButton
    private var isFlashOn = false
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupCamera()
        setupTextToSpeech()
        setupAction()
    }


    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale("in", "ID"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Bahasa tidak didukung", Toast.LENGTH_SHORT).show()
                } else {
                    textToSpeech.setSpeechRate(1.15f)
                    val voiceMale = Voice("id-id-x-ide-network", Locale("in", "ID"),  Voice.QUALITY_HIGH, Voice.LATENCY_LOW, true, null)
                    val voiceFemale = Voice("id-id-x-idc-network", Locale("in", "ID"), Voice.QUALITY_HIGH, Voice.LATENCY_LOW, true, null)
                    textToSpeech.voice = voiceMale
//                    textToSpeech.setPitch(1.16f) // Female
                    textToSpeech.setPitch(0.9f) // Male
                }
            } else {
                Toast.makeText(this, "Inisialisasi TTS Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCamera() {
        camera = binding.cameraView
        cameraBridgeViewBase = camera
        cameraBridgeViewBase.setCvCameraViewListener(object :
            CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {}

            override fun onCameraViewStopped() {}

            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
                return inputFrame.rgba()
            }
        })
        val message = if (OpenCVLoader.initLocal()) {
            cameraBridgeViewBase.enableView(); "OpenCV loaded"
        } else "OpenCV not loaded"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupAction() {
        val btnSpeak = binding.playSpeak
        val textfield = binding.textField
        btnFlash = binding.btnFlash
        btnFlash.setOnClickListener {
            isFlashOn = if (!isFlashOn) {
                try {
                    camera.setFlashMode(this, 1)
                    btnFlash.setImageResource(R.drawable.ic_flash)
                    true
                } catch (e: Exception) {
                    Toast.makeText(this, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                    false
                }
            } else {
                try {
                    camera.setFlashMode(this, 0)
                    btnFlash.setImageResource(R.drawable.ic_flash_off)
                    false
                } catch (e: Exception) {
                    Toast.makeText(this, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
        btnSpeak.setOnClickListener {
            textToSpeech.speak(textfield.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    public override fun onResume() {
        super.onResume()
        cameraBridgeViewBase.enableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraBridgeViewBase.disableView()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    override fun onPause() {
        super.onPause()
        cameraBridgeViewBase.disableView()
    }

    override fun getCameraViewList(): MutableList<out CameraBridgeViewBase> {
        return Collections.singletonList(cameraBridgeViewBase)
    }

    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            getPermission()
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}