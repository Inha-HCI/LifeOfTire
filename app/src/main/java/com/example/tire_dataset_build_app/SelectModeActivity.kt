package com.example.tire_dataset_build_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SelectModeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_mode)

        val mode_1 = findViewById<Button>(R.id.mode1)
        val mode_2 = findViewById<Button>(R.id.mode2)

        mode_1.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            startActivity(intent)
        }

        mode_2.setOnClickListener {
            val intent = Intent(this, ModeSelect1::class.java)
            startActivity(intent)
        }
    }
}