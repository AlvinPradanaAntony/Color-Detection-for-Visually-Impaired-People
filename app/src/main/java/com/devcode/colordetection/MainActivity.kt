package com.devcode.colordetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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

class MainActivity : CameraActivity()  {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private lateinit var camera: JavaCameraView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupCamera()
        val toolbar = binding.toolbarId
        toolbar.title = "Deteksi Warna"

    }

    private fun setupCamera(){
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
        val message = if (OpenCVLoader.initLocal()) { cameraBridgeViewBase.enableView(); "OpenCV loaded" } else "OpenCV not loaded"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    public override fun onResume() {
        super.onResume()
        cameraBridgeViewBase.enableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraBridgeViewBase.disableView()
    }

    override fun onPause() {
        super.onPause()
        cameraBridgeViewBase.disableView()
    }

    override fun getCameraViewList(): MutableList<out CameraBridgeViewBase> {
        return Collections.singletonList(cameraBridgeViewBase)
    }

    private fun getPermission() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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