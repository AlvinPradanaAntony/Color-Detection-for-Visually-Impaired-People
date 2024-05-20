package com.devcode.colordetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.devcode.colordetection.databinding.ActivityMainBinding
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

class MainActivity : CameraActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private lateinit var camera: JavaCameraView
    private lateinit var btnFlash: ImageButton
    private var isFlashOn = false
    private lateinit var mRgba: Mat
    private lateinit var mGray: Mat
    private lateinit var cascadeClassifier: CascadeClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupCamera()
        setupAction()
//        loadModel()
    }

    private fun loadModel() {
        try {
            val cascadeFile = resources.openRawResource(R.raw.haarcascade_frontalface_alt)
            val cascadeDir = getDir("cascade", MODE_PRIVATE)
            val cascadeFileOut = File(cascadeDir, "haarcascade_frontalface_alt.xml")
            val os = FileOutputStream(cascadeFileOut)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (cascadeFile.read(buffer).also { bytesRead = it } != -1) {
                os.write(buffer, 0, bytesRead)
            }

            cascadeFile.close()
            os.close()

            cascadeClassifier = CascadeClassifier(cascadeFileOut.absolutePath)
            Toast.makeText(this, "Model loaded", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Model not loaded", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setupCamera() {
        camera = binding.cameraView
        cameraBridgeViewBase = camera
        cameraBridgeViewBase.setCvCameraViewListener(object :
            CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {}

            override fun onCameraViewStopped() {}

            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
                mRgba = inputFrame.rgba()
                mGray = inputFrame.gray()

                // Convert Rgba to Gray
//                Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGBA2GRAY)

                // Edge Detection
//                val edges = Mat()
//                Imgproc.Canny(mRgba, edges, 80.0, 200.0)

                // Face Detection
                // Processing pass mRgba to cascadeClassifier
//                mRgba = CascadeRec(mRgba)

                return mRgba
            }
        })
        val message = if (OpenCVLoader.initLocal()) {
            cameraBridgeViewBase.enableView(); "OpenCV loaded"
        } else "OpenCV not loaded"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun CascadeRec(mRgba: Mat): Mat {
//        val faces = MatOfRect()
//        cascadeClassifier.detectMultiScale(mRgba, faces)
//        for (rect in faces.toArray()) {
//            Imgproc.rectangle(
//                mRgba,
//                rect.tl(),
//                rect.br(),
//                org.opencv.core.Scalar(255.0, 0.0, 0.0, 255.0),
//                3
//            )
//        }

        Core.flip(mRgba.t(), mRgba, 1)
        val mRgb = Mat()
        Imgproc.cvtColor(mRgba, mRgb, Imgproc.COLOR_RGBA2RGB)

        val height: Int = mRgb.height();
        val absoluteFaceSize = (height * 0.1).toInt()
        val faces = MatOfRect()
        if (cascadeClassifier != null){
            cascadeClassifier.detectMultiScale(mRgb, faces, 1.1, 2, 2, Size(absoluteFaceSize.toDouble(), absoluteFaceSize.toDouble()), Size())
        }
        val facesArray: Array<Rect> = faces.toArray()
        for (face in facesArray) {
            Imgproc.rectangle(mRgba, face.tl(), face.br(), Scalar(0.0, 255.0, 0.0, 255.0), 2
            )
        }
        Core.flip(mRgba.t(), mRgba, 0)

        return mRgba
    }

    private fun setupAction() {
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