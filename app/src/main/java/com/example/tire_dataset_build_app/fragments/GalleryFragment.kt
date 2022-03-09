package com.example.tire_dataset_build_app.fragments

/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.io.File
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.example.tire_dataset_build_app.BuildConfig
import com.example.tire_dataset_build_app.utils.padWithDisplayCutout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.android.example.cameraxbasic.fragments.PhotoFragment
import com.example.tire_dataset_build_app.utils.showImmersive
import com.example.tire_dataset_build_app.R
import com.example.tire_dataset_build_app.databinding.FragmentGalleryBinding
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

val EXTENSION_WHITELIST = arrayOf("JPG")

/** Fragment used to present the user with a gallery of photos taken */
class GalleryFragment internal constructor() : Fragment() {

    /** Android ViewBinding */
    private var _fragmentGalleryBinding: FragmentGalleryBinding? = null

    private val fragmentGalleryBinding get() = _fragmentGalleryBinding!!

    /** For predict result*/
    private var depth: String? = null

    /** AndroidX navigation arguments */
    private val args: GalleryFragmentArgs by navArgs()

    private lateinit var mediaList: MutableList<File>
    private var depthList = mutableListOf<String>()


    /** Adapter class used to present a fragment containing one photo or video as a page */
    inner class MediaPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = mediaList.size

        // 원래 getItem 통해서 이벤트 발생시마다 TextView 변경시켜주려 했으나 viewpager1 이라서 getItem을 그런식으로 사용할 수 없음..
        // 프래그먼트 object안에 depth 값을 넣어두고 직접 참고하는것으로 아이디어 변경
        override fun getItem(position: Int): Fragment = PhotoFragment.create(mediaList[position])
        override fun getItemPosition(obj: Any): Int = POSITION_NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
        retainInstance = true

        // Get root directory of media from navigation arguments
        val rootDirectory = File(args.rootDirectory)

        // Walk through all files in the root directory
        // We reverse the order of the list to present the last photos first
        mediaList = rootDirectory.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
        }?.sortedDescending()?.toMutableList() ?: mutableListOf()

        Log.d(TAG, "onCreate: Called..")

        for (i in 0 until mediaList.size){
            depthList.add("아직 진행하지 않았습니다.")
        }

        Log.d(TAG, "onCreate: check depthList: " + depthList.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentGalleryBinding = FragmentGalleryBinding.inflate(inflater, container, false)
        return fragmentGalleryBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 처음엔 프로그래스바 숨기기
        showProgress(false)

        //Checking media files list
        if (mediaList.isEmpty()) {
            fragmentGalleryBinding.deleteButton.isEnabled = false
            fragmentGalleryBinding.shareButton.isEnabled = false
        }

        // Populate the ViewPager and implement a cache of two media items
        fragmentGalleryBinding.photoViewPager.apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager)
        }

        // Make sure that the cutout "safe area" avoids the screen notch if any
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Use extension method to pad "inside" view containing UI using display cutout's bounds
            fragmentGalleryBinding.cutoutSafeArea.padWithDisplayCutout()
        }

        // Handle back button press
        fragmentGalleryBinding.backButton.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigateUp()
        }

        // Handle share button press
        fragmentGalleryBinding.shareButton.setOnClickListener {

            mediaList.getOrNull(fragmentGalleryBinding.photoViewPager.currentItem)?.let { mediaFile ->

                // Create a sharing intent
                val intent = Intent().apply {
                    // Infer media type from file extension
                    val mediaType = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(mediaFile.extension)
                    // Get URI from our FileProvider implementation
                    val uri = FileProvider.getUriForFile(
                        Objects.requireNonNull(requireContext()),BuildConfig.APPLICATION_ID + ".provider", mediaFile)
                    // Set the appropriate intent extra, type, action and flags
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = mediaType
                    action = Intent.ACTION_SEND
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                // Launch the intent letting the user choose which app to share with
                startActivity(Intent.createChooser(intent, getString(R.string.share_hint)))
            }
        }

        // Handle delete button press
        fragmentGalleryBinding.deleteButton.setOnClickListener {

            mediaList.getOrNull(fragmentGalleryBinding.photoViewPager.currentItem)?.let { mediaFile ->

                AlertDialog.Builder(view.context, android.R.style.Theme_Material_Dialog)
                        .setTitle(getString(R.string.delete_title))
                        .setMessage(getString(R.string.delete_dialog))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes) { _, _ ->

                            // Delete current photo
                            mediaFile.delete()

                            // Send relevant broadcast to notify other apps of deletion
                            MediaScannerConnection.scanFile(
                                    view.context, arrayOf(mediaFile.absolutePath), null, null)

                            // Notify our view pager
                            mediaList.removeAt(fragmentGalleryBinding.photoViewPager.currentItem)
                            depthList.removeAt(fragmentGalleryBinding.photoViewPager.currentItem)
                            fragmentGalleryBinding.photoViewPager.adapter?.notifyDataSetChanged()

                            // If all photos have been deleted, return to camera
                            if (mediaList.isEmpty()) {
                                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigateUp()
                            }

                        }

                        .setNegativeButton(android.R.string.no, null)
                        .create().showImmersive()
            }
        }

        fragmentGalleryBinding.fragmentGalleryPredict.setOnClickListener {
            mediaList.getOrNull(fragmentGalleryBinding.photoViewPager.currentItem)?.let { mediaFile ->

                showProgress(true)
                thread(start = true){
                    val uri = Uri.fromFile(mediaFile)

                    // uri를 통해 Inference가 가능한 Bitmap 생성
                    // 더 쉬운 다른 Bitmap 생성 코드 사용해봤는데 Inference에서 에러났었음
                    val bitmap = BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(uri))
                    val module = LiteModuleLoader.load(assetFilePath(requireActivity(), "model_custom.ptl"))

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
                    Log.d(ContentValues.TAG, "hello~ : ${scores.indices}")
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

                    Log.d(ContentValues.TAG, "Width: ${bitmap.height}, Height: ${bitmap.width}")    // 왜인지 모르겠으나 width, height가 거꾸로 출력되고 있어서..
                    Log.d(ContentValues.TAG, "Depth result: ${depth} ")
                    depthList[fragmentGalleryBinding.photoViewPager.currentItem] = depth!!
//                    Log.d("After predict", depthList.toString())
//                    Log.d("After predict", "index: " + fragmentGalleryBinding.photoViewPager.currentItem.toString())
                    requireActivity().runOnUiThread {
                        showProgress(false)

                        Toast.makeText(requireContext(), "Depth: " + depth + "\n"
                                + "Width: " + bitmap.height.toString() + " Height: " + bitmap.width.toString()
                            , Toast.LENGTH_LONG).show()
//                        fragmentGalleryBinding.fragmentGalleryTvDepth.setText("Depth: " + depth)
                    }
                }
            }
        }


    }

    override fun onDestroyView() {
        _fragmentGalleryBinding = null
        super.onDestroyView()
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

    fun showProgress(isShow:Boolean){
        if(isShow) fragmentGalleryBinding.fragmentGalleryPbar.visibility = View.VISIBLE
        else fragmentGalleryBinding.fragmentGalleryPbar.visibility = View.GONE
    }
}
