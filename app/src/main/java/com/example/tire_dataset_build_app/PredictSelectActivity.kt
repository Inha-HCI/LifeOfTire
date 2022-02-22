package com.example.tire_dataset_build_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Gallery
import androidx.activity.result.contract.ActivityResultContracts

class PredictSelectActivity : AppCompatActivity() {
    private val GALLERY = 1

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri

        Log.d(TAG, "uri: " + uri)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predict_select)

        var bt_select_image = findViewById<Button>(R.id.bt_select_image)
        var bt_take_photo = findViewById<Button>(R.id.bt_take_photo)

//        bt_select_image.setOnClickListener {
//            val intent:Intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.setType("image/*")
//            startActivityForResult(intent, GALLERY)
//        }

        bt_select_image.setOnClickListener {
            getContent.launch("image/*")
        }
    }
}