package com.example.tire_dataset_build_app

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class SelectModeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_mode)
        var mIntent = getIntent()
        val dir_name = mIntent.getStringExtra("dir_name")
        Log.d(TAG, "onCreate: " + dir_name)

        val mode_1 = findViewById<Button>(R.id.mode1)
        val mode_2 = findViewById<Button>(R.id.mode2)
        val mode_3 = findViewById<Button>(R.id.mode3)
        val mode_4 = findViewById<Button>(R.id.mode4)



        mode_1.setOnClickListener {
            val intent = Intent(this, Mode1::class.java)
            intent.putExtra("dir_name", dir_name)
            startActivity(intent)
        }

        mode_2.setOnClickListener {
            val intent = Intent(this, Mode2::class.java)
            intent.putExtra("dir_name", dir_name)
            startActivity(intent)
        }
    }
}