package com.example.tire_dataset_build_app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Mode : AppCompatActivity() {
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var image_id:Int = 0
    lateinit var mviewFinder:androidx.camera.view.PreviewView
    private var imageCapture: ImageCapture? = null
    private var camera : Camera? = null
    private var cameraController : CameraControl? = null
    private var cameraInfo: CameraInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode)
        mviewFinder = findViewById<androidx.camera.view.PreviewView>(R.id.viewFinder)
        findViewById<TextView>(R.id.num).setText(StoreVariable.num_of_pic.toString())
        val mfinish = findViewById<Button>(R.id.finish)
        val mtakebt = findViewById<ImageButton>(R.id.camera_capture_button)
        val mode_select = findViewById<Button>(R.id.select_mode)
        val flash = findViewById<ImageButton>(R.id.flash)

        var mIntent = getIntent()
        image_id = mIntent.getIntExtra("image_id", -999)
        findViewById<ImageView>(R.id.tire_image).setImageResource(image_id)

        showPopup()

        mode_select.setOnClickListener {
            val intent = Intent(this, SelectModeActivity::class.java)
            startActivity(intent)
        }

        mfinish.setOnClickListener {
            val intent = Intent(this, InputResultActivity::class.java)
            startActivity(intent)
        }

        mtakebt.setOnTouchListener(object:View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when(event?.action){
                    MotionEvent.ACTION_DOWN ->{
                        mtakebt.setImageResource(R.drawable.camera_on_click)
                    }

                    MotionEvent.ACTION_UP ->{
                        mtakebt.setImageResource(R.drawable.camera_no_click)
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })
        
        mtakebt.setOnClickListener {
            takePhoto()
            StoreVariable.num_of_pic = StoreVariable.num_of_pic!!+ 1
            findViewById<TextView>(R.id.num).setText(StoreVariable.num_of_pic.toString())
        }

        flash.setOnClickListener {
            when(cameraInfo?.torchState?.value){
                TorchState.ON -> {
                    cameraController?.enableTorch(false)
                    findViewById<ImageButton>(R.id.flash).setImageResource(R.drawable.flash_off)
                }
                TorchState.OFF -> {
                    cameraController?.enableTorch(true)
                    findViewById<ImageButton>(R.id.flash).setImageResource(R.drawable.flash_on)
                }
            }

        }

        startCamera()
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(mviewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)

                // We request aspect ratio but no resolution to match preview config, but letting
                // CameraX optimize for whatever specific resolution best fits our use cases
                // 카메라 센서값에 기반하여 최적의 해상도 세팅함. 단 setTargetResolution과는 같이 사용할 수 없음
//                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                // 이미지가 회전되서 찍혔을 경우 EXIF 메타데이터에 회전 정보가 들어가게함
//                .setTargetRotation(rotation)

                // 해상도 조절용
                .setTargetResolution(Size(3024, 4032))
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

                cameraController = camera!!.cameraControl
                cameraInfo = camera!!.cameraInfo

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return       // takePhoto 함수 실행했을 때 imageCapture가 Null이면 return으로 바로 함수 종료.
        // preview가 다 load 되기 전에 바로 take 버튼 누르는 상황같은 것

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name) + StoreVariable.dir_name).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    fun showPopup(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.mode_popup, null)
        view.findViewById<ImageView>(R.id.pu_tire_image).setImageResource(image_id)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("도움말")
            .setPositiveButton("확인", null)
            .create()

        alertDialog.setView(view)
        alertDialog.show()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}