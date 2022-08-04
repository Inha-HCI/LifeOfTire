package com.example.tire_dataset_build_app.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.core.app.ShareCompat.getCallingActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.tire_dataset_build_app.PredictCameraMainActivity
import com.example.tire_dataset_build_app.R
import com.example.tire_dataset_build_app.StoreVariable
import com.example.tire_dataset_build_app.databinding.FragmentSegmentationBinding
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SegmentationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SegmentationFragment : Fragment() {

    /** Android ViewBinding */
    private var _fragmentSegmentationBinding: FragmentSegmentationBinding? = null
    private val fragmentSegmentationBinding get() =_fragmentSegmentationBinding!!

    private val args: SegmentationFragmentArgs by navArgs()

    lateinit var predictCameraMainActivity: PredictCameraMainActivity

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val CLASSNUM = 21

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentSegmentationBinding = FragmentSegmentationBinding.inflate(inflater, container, false)
        val view = fragmentSegmentationBinding.root
        return view
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_segmentation, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showProgress(false)

        // imageUri는 String. 이 String 가져와서 File 객체로 만든다음에 다시 Uri로 만드니까 되네.. 정말 어지럽다
        val uri = Uri.fromFile(File(args.imageUri))
        fragmentSegmentationBinding.originalImage.setImageURI(uri)

        var bitmap = BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(uri))
//        Log.d("before bitmap height, width", "onViewCreated: ${bitmap.height}, ${bitmap.width}")
//        bitmap = rotatedBitmap(bitmap, args.imageUri)
        Log.d("after bitmap height, width", "onViewCreated: ${bitmap.height}, ${bitmap.width}")


        // 가중치 파일 Load
        // DeeplabV3
        val module = LiteModuleLoader.load(assetFilePath(requireActivity(), "deeplabv3_scripted_optimized.ptl"))

        val seg_inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            bitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB
        )

        val reg_inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            bitmap,
            floatArrayOf(0.0f, 0.0f, 0.0f),
            floatArrayOf(1.0f, 1.0f, 1.0f)
        )

        val outTensors = module.forward(IValue.from(seg_inputTensor)).toDictStringKey()
        val outputTensor: Tensor = outTensors.get("out")!!.toTensor()

        //        final Tensor outputTensor = outTensors.toTensor();
        val scores = outputTensor.dataAsFloatArray // 32bit floatTensor를 Float Array로 변환

        val width: Int = bitmap.getWidth()
        val height: Int = bitmap.getHeight()
        val intValues = IntArray(width * height)
        for (j in 0 until height) {
            for (k in 0 until width) {
                var maxi = 0
                var maxj = 0
                var maxk = 0
                var maxnum = -Double.MAX_VALUE
                for (i in 0 until CLASSNUM) {
                    val score =
                        scores[i * (width * height) + j * width + k] // 내 생각에 최종 mask는 채널이 여러개임. 각 채널이 클래스별로 구분되고,
                    // 그렇기 때문에 클래스 인덱스변수 i가 바뀔때마다 (width*height)만큼 이동함.

                    if (score > maxnum) {
                        maxnum = score.toDouble()
                        maxi = i
                        maxj = j
                        maxk = k
                    }
                }
                if (maxi == 0) intValues[maxj * width + maxk] = -0x1000000 // black
//                else if (maxi == 1) intValues[maxj * width + maxk] = -0xff0100 // green
//                else intValues[maxj * width + maxk] = -0x10000 // red
                else intValues[maxj * width + maxk] = 0x7f100000 // red
                //                intValues[maxj*width + maxk] = 0xFF0000FF; // blue
            }
        }

        val bmpSegmentation = Bitmap.createScaledBitmap(bitmap, width, height, true)

        val outputBitmap = bmpSegmentation.copy(bmpSegmentation.config, true)
        outputBitmap.setPixels(
            intValues,
            0,
            outputBitmap.width,
            0,
            0,
            outputBitmap.width,
            outputBitmap.height
        )
        var transferredBitmap =
            Bitmap.createScaledBitmap(outputBitmap, bitmap.getWidth(), bitmap.getHeight(), true)
        transferredBitmap = rotatedBitmap(transferredBitmap, args.imageUri)     // 이미지 회전 시키기 위함

        fragmentSegmentationBinding.segmentationImage.setImageBitmap(transferredBitmap)

        fragmentSegmentationBinding.fragmentSelectPhotoButton.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
            SegmentationFragmentDirections.actionSegmentationFragmentToCameraFragment())
        }

        fragmentSegmentationBinding.fragmentSegPredictButton.setOnClickListener {

        showProgress(true)
            thread(start = true){

                // efficientNet
                val module = LiteModuleLoader.load(assetFilePath(requireActivity(), "Efficientnet_scripted_optimized.ptl"))
                val outTensors = module.forward(IValue.from(reg_inputTensor))
                val outputTensor: Tensor = outTensors.toTensor()
                val score = outputTensor.dataAsFloatArray[0]    // regression 이므로 값이 하나임
                                                                // outputTensor size: (1, 1)

                activity?.runOnUiThread {
                    showProgress(false)

                    if (score <= 2.5){
                        fragmentSegmentationBinding.badTireImage.visibility = View.VISIBLE
                    }

                    else if(2.5 < score && score <= 5.0){
                        fragmentSegmentationBinding.sosoTireImage.visibility = View.VISIBLE
                    }

                    else{
                        fragmentSegmentationBinding.goodTireImage.visibility = View.VISIBLE
                    }

                    fragmentSegmentationBinding.fragmentSegmentationDepth.setText(score.toString().slice(0..2) + " mm")
                    fragmentSegmentationBinding.fragmentSegmentationDepth.visibility = View.VISIBLE
                    fragmentSegmentationBinding.fragmentSegmentationGuide.visibility = View.INVISIBLE
                    
                    if (StoreVariable.calledByJBNI == true){
                        Log.d(TAG, "onViewCreated: called by JBNI App")
                        var resultIntent = Intent()
                        var value = String.format("%.2f", score)
                        resultIntent.putExtra("result", value.toDouble())
                        requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                        requireActivity().finish()
                    }
                }
            }
        }
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

    fun getOrientationOfImage(filepath : String): Int? {
        var exif : ExifInterface? = null
        var result: Int? = null
        try{
            exif = ExifInterface(filepath)
        }catch (e: Exception){
            e.printStackTrace()
            return -1
        }
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
        if(orientation != -1){
            result = when(orientation){
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }
        return result
    }

    fun rotatedBitmap(bitmap: Bitmap?, filepath: String): Bitmap? {
        val matrix = Matrix()
        var resultBitmap : Bitmap? = null
        when(getOrientationOfImage(filepath)){
            0 -> matrix.setRotate(0F)
            90 -> matrix.setRotate(90F)
            180 -> matrix.setRotate(180F)
            270 -> matrix.setRotate(270F)
        }

        resultBitmap = try{
            bitmap?.let {
                Bitmap.createBitmap(it, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
        return resultBitmap
    }

    fun showProgress(isShow:Boolean){
        if(isShow) fragmentSegmentationBinding.fragmentSegPbar.visibility = View.VISIBLE
        else fragmentSegmentationBinding.fragmentSegPbar.visibility = View.GONE
    }
}