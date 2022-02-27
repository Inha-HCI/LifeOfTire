package com.example.tire_dataset_build_app

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
    val render = Render(this)
    private var result:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPredictSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_predict_select)


        render.setAnimation(Zoom().In(binding.activityPredictSelectBtSelectImage))
        render.start()

        render.setAnimation(Zoom().In(binding.activityPredictSelectBtTakePhoto))
        render.start()

        binding.activityPredictSelectBtSelectImage.setOnClickListener {
            getContent.launch("image/*")
            Log.d(TAG, "Depth result: $result")
        }

        binding.activityPredictSelectBtTakePhoto.setOnClickListener {
        }
    }

//    @RequiresApi(Build.VERSION_CODES.P)
    @RequiresApi(Build.VERSION_CODES.P)
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri

        Log.d(TAG, "Selected Image uri: " + uri)
//        binding.activityPredictSelectIvImage.setImageURI(uri)
//        val file = File(uri?.path)
        val source = ImageDecoder.createSource(contentResolver, uri!!)
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
//        val bitmap = ImageDecoder.decodeBitmap(source)
        val module = LiteModuleLoader.load(assetFilePath(this, "model_custom.ptl"))

    binding.activityPredictSelectIvImage.setImageBitmap(bitmap)
    val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
        bitmap,
        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
        TensorImageUtils.TORCHVISION_NORM_STD_RGB
    )

    val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
    val scores = outputTensor.dataAsFloatArray // pretrained된 imagenet으로 진행했다면 (1, 1000)으로 output 나옴


    var maxScore = -Float.MAX_VALUE
    var maxScoreIdx = -1
    Log.d(TAG, "hello~ : ${scores.indices}")
    for (i in scores.indices) {       // Inference 값 중 가장 큰 값의 index 구함
        if (scores[i] > maxScore) {
            maxScore = scores[i]
            maxScoreIdx = i
        }
    }

    Log.d(TAG, "hello~ ${maxScoreIdx::class.simpleName} ")
    when(maxScoreIdx){
        0 -> result = "0mm"
        1 -> result = "1mm"
        2 -> result = "2mm"
        3 -> result = "3mm"
        4 -> result = "4mm"
        5 -> result = "5mm"
        6 -> result = "6mm"
    }

    Log.d(TAG, "hello~ $result")
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
}