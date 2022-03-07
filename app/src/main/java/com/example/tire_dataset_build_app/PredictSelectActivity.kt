package com.example.tire_dataset_build_app

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.tire_dataset_build_app.databinding.ActivityPredictSelectBinding
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils
import render.animations.Render
import render.animations.Zoom
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class PredictSelectActivity : AppCompatActivity() {
    private val GALLERY = 1
    private lateinit var binding: ActivityPredictSelectBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    val render = Render(this)
    private var depth: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPredictSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_predict_select)

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->        // Intent 반환하는 람다식 선언
                                                                                                                        // registerForActivityResult 함수는 Contract, Interface 인자를 받으며
                                                                                                                        // 마지막 인자에 대해 SAM 변환을 주어 람다식을 바깥에서 선언하는것임
                if (result.resultCode == Activity.RESULT_OK) {
                    handleCameraImage(result.data)
                    Log.d(TAG, "이게 되나? ${depth}")
                }
            }

        render.setAnimation(Zoom().In(binding.activityPredictSelectBtSelectImage))
        render.start()

        render.setAnimation(Zoom().In(binding.activityPredictSelectBtTakePhoto))
        render.start()

        binding.activityPredictSelectBtSelectImage.setOnClickListener {
            getContent.launch("image/*")
//            Log.d(TAG, "Depth result: ${depth}")        // 여기서 detph 값 조회하면 아직 콜백 메서드에서 depth 처리가 안되서 null로 나오는 것 주의
        }

        binding.activityPredictSelectBtTakePhoto.setOnClickListener {

            // custom-fragment 적용을 위한 주석
//            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            resultLauncher.launch(cameraIntent)
            val intent = Intent(this, PredictCameraMainActivity::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the returned Uri

            Log.d(TAG, "Selected Image uri: " + uri)

            val source = ImageDecoder.createSource(contentResolver, uri!!)
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
            val module = LiteModuleLoader.load(assetFilePath(this, "model_custom.ptl"))

            binding.activityPredictSelectIvImage.setImageBitmap(bitmap)
            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )

            val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
            val scores =
                outputTensor.dataAsFloatArray // pretrained된 imagenet으로 진행했다면 (1, 1000)으로 output 나옴


            var maxScore = -Float.MAX_VALUE
            var maxScoreIdx = -1
            Log.d(TAG, "hello~ : ${scores.indices}")
            for (i in scores.indices) {       // Inference 값 중 가장 큰 값의 index 구함
                if (scores[i] > maxScore) {
                    maxScore = scores[i]
                    maxScoreIdx = i
                }
            }

            when (maxScoreIdx) {
                0 -> depth = "0mm"
                1 -> depth = "1mm"
                2 -> depth = "2mm"
                3 -> depth = "3mm"
                4 -> depth = "4mm"
                5 -> depth = "5mm"
                6 -> depth = "6mm"
            }

            Log.d(TAG, "Width: ${bitmap.width}, Height: ${bitmap.height}")
            Log.d(TAG, "Depth??: ${depth} ")
        }

    @Throws(IOException::class)
    fun assetFilePath(context: Context, assetName: String?): String? {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(assetName!!).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handleCameraImage(intent: Intent?) {
        val bitmap = intent?.extras?.get("data") as Bitmap
        val module = LiteModuleLoader.load(assetFilePath(this, "model_custom.ptl"))

        binding.activityPredictSelectIvImage.setImageBitmap(bitmap)

        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            bitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB
        )

        val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
        val scores =
            outputTensor.dataAsFloatArray // pretrained된 imagenet으로 진행했다면 (1, 1000)으로 output 나옴


        var maxScore = -Float.MAX_VALUE
        var maxScoreIdx = -1
        Log.d(TAG, "hello~ : ${scores.indices}")
        for (i in scores.indices) {       // Inference 값 중 가장 큰 값의 index 구함
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }

        when (maxScoreIdx) {
            0 -> depth = "0mm"
            1 -> depth = "1mm"
            2 -> depth = "2mm"
            3 -> depth = "3mm"
            4 -> depth = "4mm"
            5 -> depth = "5mm"
            6 -> depth = "6mm"
        }

        Log.d(TAG, "Width: ${bitmap.width}, Height: ${bitmap.height}")
        Log.d(TAG, "handleCameraImage: Depth 결과 -> ${depth}")

//        val result = intent?.extras?.get("data")
//        val uri = intent?.getData
//        val uri = MediaStore.getMediaUri(this, result as Uri)
//        Log.d(TAG, "handleCameraImage: ${uri}")
//        iv_photo.setImageBitmap(bitmap)
//        binding.activityPredictSelectIvImage.setImageURI(result)
    }
}