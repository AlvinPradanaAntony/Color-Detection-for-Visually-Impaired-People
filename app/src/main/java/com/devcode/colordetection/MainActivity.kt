package com.devcode.colordetection

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devcode.colordetection.databinding.ActivityMainBinding
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import weka.classifiers.Classifier
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.SerializationHelper
import java.util.Collections
import java.util.Locale
import kotlin.math.max
import kotlin.math.min


class MainActivity : CameraActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraBridgeViewBase: CameraBridgeViewBase
    private lateinit var camera: JavaCameraView
    private lateinit var rvList: RecyclerView
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var mCamera: Camera
    private lateinit var mRgba: Mat
    private lateinit var mGray: Mat
    private lateinit var mHsv: Mat
    private lateinit var mRgbaF: Mat
    private lateinit var mRgbaT: Mat
    private lateinit var mBlobColorHsv: Scalar
    private lateinit var mBlobColorRgba: Scalar
    private lateinit var pointer: View
    private val list = ArrayList<ListRes>()
    private var isFlashOn = false
    private var fw = 0
    private var fh = 0
    private var liveC: String? = null
    private var lastVoiceOverTime: Long = 0
    private var previousLiveC: String? = null

    // Handler dan Runnable untuk eksekusi periodik
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateColorDetection()
            handler.postDelayed(this, 1000) // Eksekusi ulang setiap 1 detik
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)
        setupTextToSpeech()
        pointer = binding.pointer
        setupCamera()
        setupAction()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            list.addAll(resolutionList())
        }
    }

    private fun setupCamera() {
        camera = binding.cameraView
        cameraBridgeViewBase = camera
        cameraBridgeViewBase.setCvCameraViewListener(object :
            CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {
                mRgba = Mat(height, width, CvType.CV_8UC4)
                mGray = Mat(height, width, CvType.CV_8UC1)
                mHsv = Mat(width, height, CvType.CV_8UC3)
                mRgbaT = Mat(height, width, CvType.CV_8UC4)
                mRgbaF = Mat(fh, fw, CvType.CV_8UC4)
                mBlobColorRgba = Scalar(255.0);
                mBlobColorHsv = Scalar(255.0);
                Log.w("Camera View Size", "Width: $width, Height: $height")
            }

            override fun onCameraViewStopped() {
                mRgba.release()
            }

            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
                mRgba = inputFrame.rgba()
                mGray = inputFrame.gray()

                // Koordinat layar View pointer
                // Koordinat pointer dalam tampilan kamera
                val pointerX = (pointer.x + pointer.width / 2).toInt()
                val pointerY = (pointer.y + pointer.height / 2).toInt()

                // Hitung skala antara tampilan kamera dan matriks
                val scaleX = mRgba.cols().toDouble() / camera.width
                val scaleY = mRgba.rows().toDouble() / camera.height

                // Koordinat dalam matriks kamera
                val centerX = (pointerX * scaleX).toInt().coerceIn(0, mRgba.cols() - 1)
                val centerY = (pointerY * scaleY).toInt().coerceIn(0, mRgba.rows() - 1)
                Log.w("Pointer", "Center X: $centerX, Center Y: $centerY")

                val sizePointerWidth = 8
                val sizePointerHeight = 8
                val rectX = centerX - sizePointerWidth / 2
                val rectY = centerY - sizePointerHeight / 2

                val touchedRect = Rect(rectX, rectY, sizePointerWidth, sizePointerHeight)
                val touchedRegionRgba = mRgba.submat(touchedRect)

                val touchedRegionHsv = Mat()
                Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL)

//                Imgproc.rectangle(mRgba, touchedRect, Scalar(255.0, 0.0, 0.0), 1)

                mBlobColorHsv = Core.sumElems(touchedRegionHsv)
                val pointCount = touchedRect.width * touchedRect.height
                for (i in mBlobColorHsv.`val`.indices) mBlobColorHsv.`val`[i] /= pointCount.toDouble()
                mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv)

                return mRgba
            }
        })
        val message = if (OpenCVLoader.initLocal()) {
            cameraBridgeViewBase.enableView(); "OpenCV loaded"
        } else "OpenCV not loaded"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupAction() {
        binding.btnFlash.setOnClickListener {
            isFlashOn = if (!isFlashOn) {
                try {
                    camera.setFlashMode(this, 1)
                    binding.btnFlash.setImageResource(R.drawable.ic_flash)
                    true
                } catch (e: Exception) {
                    Toast.makeText(this, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                    false
                }
            } else {
                try {
                    camera.setFlashMode(this, 0)
                    binding.btnFlash.setImageResource(R.drawable.ic_flash_off)
                    false
                } catch (e: Exception) {
                    Toast.makeText(this, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
        binding.btnResolutions.setOnClickListener {
            alertDialog = AlertDialog.Builder(this@MainActivity)
            dialog = alertDialog.create()
            val dialogView = layoutInflater.inflate(R.layout.item_layout_info_dialog, null, false)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setView(dialogView)
            val rvRes = dialogView.findViewById<View>(R.id.list_resolutions)
            rvList = rvRes as RecyclerView
            rvList.setHasFixedSize(true)
            showRecyclerList()
            dialogView.findViewById<View>(R.id.btn_close).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
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
                }
            } else {
                Toast.makeText(this, "Inisialisasi TTS Gagal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun playVoiceOver(message: String) {
        Log.d("TTS", "Speaking: $message")
        try {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("TTS", "Success")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TTS", "Error: ${e.message}")
        }

    }

    private fun resolutionList(): ArrayList<ListRes> {
        try {
            mCamera = Camera.open()
            val parameters = mCamera.parameters
            val sizes: List<Camera.Size> = parameters.supportedPreviewSizes
            val resolution_array_list = ArrayList<ListRes>()

            for (i in sizes.indices) {
                try {
                    val frameWidth = sizes[i].width
                    val frameHeight = sizes[i].height
                    val res = "$frameWidth x $frameHeight"
                    val listRes = ListRes(res, frameWidth, frameHeight)
                    resolution_array_list.add(listRes)
                    Log.w("Resolution List", "Resolution: $res")
                } catch (e: Exception) {
                    Log.e("Error Resolution List", "Error: ${e.message}")
                }
            }
            return resolution_array_list
        } catch (e: Exception) {
            Log.e("Error Camera", "Error: ${e.message}")
        }
        return ArrayList()
    }

    private fun showRecyclerList() {
        rvList.layoutManager = LinearLayoutManager(this@MainActivity)
        val listAdapter = ViewAdapter(list)
        rvList.adapter = listAdapter
        listAdapter.setOnItemClickCallback(object : ViewAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ListRes) {
                val resolution = data.resolution
                val frameWidth = data.width
                val frameHeight = data.height
                fw = frameWidth
                fh = frameHeight
                camera.disableView()
                camera.setMaxFrameSize(frameWidth, frameHeight)
                camera.enableView()
                Toast.makeText(
                    this@MainActivity,
                    "$resolution, Width: $frameWidth, Height: $frameHeight",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        })
    }

    private fun convertScalarHsv2Rgba(hsvColor: Scalar): Scalar {
        val pointMatRgba = Mat()
        val pointMatHsv = Mat(1, 1, CvType.CV_8UC3, hsvColor)
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4)
        return Scalar(pointMatRgba[0, 0])
    }

    private fun knnProcess1(r: Int, g: Int, b: Int, modelAndHeader: Array<Array<Any>?>): String? {
        var prediction: String? = null
        try {
            // Create list of attributes.
            val numericAtt = ArrayList<Attribute>()
            val nominalAtt = ArrayList<String>()

            // Add numeric attributes/columns
            numericAtt.add(Attribute("R"))
            numericAtt.add(Attribute("G"))
            numericAtt.add(Attribute("B"))

            // Add nominal/letters attributes/column
            nominalAtt.add("val3") // label for att2-----------
            numericAtt.add(Attribute("Color Names", nominalAtt))

            // Create Instances object
            val data = Instances("Testing Data", numericAtt, 1000)

            // Fill with data
            val vals = DoubleArray(data.numAttributes())
            vals[0] = r.toDouble()
            vals[1] = g.toDouble()
            vals[2] = b.toDouble()
            vals[3] = nominalAtt.indexOf("val3").toDouble()

            // Add to instance
            data.add(DenseInstance(1.0, vals))
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1)
            }

            // Set class index for comparison testing
            data.setClassIndex(data.numAttributes() - 1)

            // KNN Weka part
            if (modelAndHeader[0]?.size != 2) {
                throw Exception("[InputMappedClassifier] serialized model file does not seem to contain both a model and the instances header used in training it!")
            } else {
                val knnmodel = modelAndHeader[0]?.get(0) as Classifier
                val m_modelHeader = modelAndHeader[0]?.get(1) as Instances

                val value = knnmodel.classifyInstance(data.instance(0))

                // Get the name of the class value
                prediction = m_modelHeader.classAttribute().value(value.toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return prediction
    }

    private fun setViewBackgroundColor(view: View, color: Int, cornerRadius: Float) {
        val gradientDrawable = GradientDrawable().apply {
            setColor(color)
            this.cornerRadius = cornerRadius
        }
        view.background = gradientDrawable
    }

    override fun onResume() {
        super.onResume()
        cameraBridgeViewBase.enableView()
        handler.post(updateRunnable)
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(applicationContext, "There is a problem in opencv", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraBridgeViewBase.disableView()
        textToSpeech.stop()
        textToSpeech.shutdown()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        cameraBridgeViewBase.disableView()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateColorDetection() {
        val colorResult = Color.rgb(
            mBlobColorRgba.`val`[0].toInt(),
            mBlobColorRgba.`val`[1].toInt(),
            mBlobColorRgba.`val`[2].toInt()
        )
        var r = 0
        var g = 0
        var b = 0
        //Get RGB Values
        r = mBlobColorRgba.`val`[0].toInt()
        g = mBlobColorRgba.`val`[1].toInt()
        b = mBlobColorRgba.`val`[2].toInt()
        val modelAndHeaderEN = arrayOf<Array<Any>?>(null)
        try {
            modelAndHeaderEN[0] = SerializationHelper.readAll(assets.open("u2_rgb_en_k-5.model"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        liveC = knnProcess1(r, g, b, modelAndHeaderEN)
// Cek waktu pemanggilan terakhir dari playVoiceOver dan perubahan pada liveC
        val currentTime = System.currentTimeMillis()
        val shouldSpeak = currentTime - lastVoiceOverTime >= 3000 || liveC != previousLiveC

        if (shouldSpeak) {
            playVoiceOver(liveC.toString())
            lastVoiceOverTime = currentTime // Update waktu pemanggilan terakhir
            previousLiveC = liveC // Update nilai liveC sebelumnya
        }
        binding.colorName.text = liveC
        binding.colorHex.text = "Color: #" + String.format("%02X", mBlobColorRgba.`val`[0].toInt()) + String.format("%02X", mBlobColorRgba.`val`[1].toInt()) + String.format("%02X", mBlobColorRgba.`val`[2].toInt())
        binding.viewColor.setBackgroundColor(colorResult)
        binding.colorHex.setTextColor(colorResult)
        binding.cvSnapcolor.setCardBackgroundColor(colorResult)
        setViewBackgroundColor(binding.viewColor, colorResult, 100f)
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
        } else {
            list.addAll(resolutionList())
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
        } else {
            list.addAll(resolutionList())
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}