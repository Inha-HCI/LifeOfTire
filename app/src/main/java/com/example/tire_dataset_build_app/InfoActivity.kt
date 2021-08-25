package com.example.tire_dataset_build_app

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
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

        requestMultiplePermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA))
        val makedir = findViewById<Button>(R.id.makedir)
        makedir.setOnClickListener {
            getAppSpecificAlbumStorageDir(this, "hojuneeee")
        }
        val uri = FileProvider.getUriForFile(this, "com.example.tire_dataset_build_app.FileProvider", createImageFile())
        getCameraImage.launch(uri)
    }
    fun getAppSpecificAlbumStorageDir(context: Context, albumName: String): File? {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        val file = File(context.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES), albumName)
        if (!file?.mkdirs()) {
            Log.e(TAG, "Directory not created")
        }
        else{
            Log.e(TAG, "getAppSpecificAlbumStorageDir: success", )
        }
        return file
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
        val storageDir = getAppSpecificAlbumStorageDir(this, "tire_set") // 안드로이드 11부터는 root 아래에 바로 디렉토리 생성 못함. 노션에 정리해놨음

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPath = absolutePath
        }
    }
}