package com.devcode.colordetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (OpenCVLoader.initDebug()) {
            Toast.makeText(this, "OpenCV is loaded", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "OpenCV is not loaded", Toast.LENGTH_SHORT).show()
        }
    }
}