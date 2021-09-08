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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.tire_dataset_build_app.ConnectFTP
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InfoActivity : AppCompatActivity() {
    lateinit var currentPath:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        requestMultiplePermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA))

        val start_shoot = findViewById<Button>(R.id.start_shoot)

        start_shoot.setOnClickListener {
            val intent = Intent(this, SelectModeActivity::class.java)

//            val ex_date = findViewById<TextView>(R.id.ex_date).text.toString()
//            val experimenter = findViewById<TextView>(R.id.experimenter).text.toString()
//            val ex_place = findViewById<TextView>(R.id.ex_place).text.toString()
//            val tire_model = findViewById<TextView>(R.id.tire_model).text.toString()
//            val ex_round = findViewById<TextView>(R.id.ex_round).text.toString().toInt()

            val BASE_URL_HyungJeong_API = "http://1.214.35.242:80/"
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_HyungJeong_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(HyunjeongAPI::class.java)
//            val callResult = api.getResult(ex_date, experimenter, ex_place, tire_model, ex_round)
            val callResult = api.getResult()

            callResult.enqueue(object: Callback<ResultFromAPI>{
                override fun onResponse(call: Call<ResultFromAPI>, response: Response<ResultFromAPI>) {
                    Log.d("결과", "성공: ${response.body()?.result_code}")
                }

                override fun onFailure(call: Call<ResultFromAPI>, t: Throwable) {
                    Log.d("결과", "실패: $t")
                }
            })
            startActivity(intent)
        }
    }

    val REQUEST_IMAGE_CAPTURE = 1

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