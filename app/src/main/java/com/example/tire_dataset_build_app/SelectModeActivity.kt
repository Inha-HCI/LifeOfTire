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
        val sid = mIntent.getStringExtra("sid")
        Log.d(TAG, "onCreate: " + dir_name)

        val mode_1 = findViewById<Button>(R.id.mode1)
        val mode_2 = findViewById<Button>(R.id.mode2)
        val mode_3 = findViewById<Button>(R.id.mode3)
        val mode_4 = findViewById<Button>(R.id.mode4)



        mode_1.setOnClickListener {
            val intent = Intent(this, Mode::class.java)
            intent.putExtra("dir_name", dir_name)
            intent.putExtra("sid", sid)
            intent.putExtra("image_id", R.drawable.tire1)
            Log.d(TAG, "selectmode: " + sid)
            startActivity(intent)
        }

        mode_2.setOnClickListener {
            val intent = Intent(this, Mode::class.java)
            intent.putExtra("dir_name", dir_name)
            intent.putExtra("sid", sid)
            intent.putExtra("image_id", R.drawable.tire2)
            startActivity(intent)
        }

        mode_3.setOnClickListener {
            val intent = Intent(this, Mode::class.java)
            intent.putExtra("dir_name", dir_name)
            intent.putExtra("sid", sid)
            intent.putExtra("image_id", R.drawable.tire3)
            startActivity(intent)
        }
    }
}