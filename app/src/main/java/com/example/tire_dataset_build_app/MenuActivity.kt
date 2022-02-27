package com.example.tire_dataset_build_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        Log.d("SDK Version", Build.VERSION.SDK_INT.toString())

        val build_button = findViewById<Button>(R.id.build)
        val pred_button = findViewById<Button>(R.id.pred)

        build_button.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        pred_button.setOnClickListener {
            val intent = Intent(this, PredictSelectActivity::class.java)
            startActivity(intent)
        }
    }
}