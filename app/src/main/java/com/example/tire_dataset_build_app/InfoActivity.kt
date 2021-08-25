package com.example.tire_dataset_build_app

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class InfoActivity : AppCompatActivity() {
    lateinit var currentPath:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        requestMultiplePermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA))

        val uri = FileProvider.getUriForFile(this, "file_provider", createImageFile())
//        getCameraImage.launch(uri)
    }

    private val requestMultiplePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultsMap ->
        resultsMap.forEach {
            Log.i(ContentValues.TAG, "Permission: ${it.key}, granted: ${it.value}")
        }
    }

    private val getCameraImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.i(ContentValues.TAG, "Got image")
            //Do something with the image uri, go nuts!
        }
    }

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat.getDateTimeInstance().format(Date())
        val path = Environment.getStorageDirectory()
        val storageDir = File(Environment.getExternalStoragePublicDirectory((Environment.DIRECTORY_DCIM), "My_directory") // 안드로이드 11부터는 root 아래에 바로 디렉토리 생성 못함. root 아래 있는 디렉토리 하나를 활용해야함

        if(storageDir.mkdirs()){
            Toast.makeText(this,"mkdirs succeed", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this,"mkdirs failed", Toast.LENGTH_SHORT).show()
        }

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPath = absolutePath
        }
    }
}