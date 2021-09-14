package com.example.tire_dataset_build_app

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class SelectModeActivity : AppCompatActivity() {
    private lateinit var mIntent:Intent
    private var dir_name:String? = null
    private var sid:String? = null
    private var num_of_pic:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_mode)
        mIntent = getIntent()

        val mode_1 = findViewById<Button>(R.id.mode1)
        val mode_2 = findViewById<Button>(R.id.mode2)
        val mode_3 = findViewById<Button>(R.id.mode3)
        val mode_4 = findViewById<Button>(R.id.mode4)

        mode_1.setOnClickListener {
            val intent = Intent(this, Mode::class.java)
            intent.putExtra("image_id", R.drawable.tire1)
            startActivity(intent)
        }

        mode_2.setOnClickListener {
            val intent = Intent(this, Mode::class.java)
            intent.putExtra("image_id", R.drawable.tire2)
            startActivity(intent)
        }

        mode_3.setOnClickListener {
            val intent = Intent(this, Mode::class.java)
            intent.putExtra("image_id", R.drawable.tire3)
            startActivity(intent)
        }

        mode_4.setOnClickListener {
            val intent = Intent(this, Mode::class.java)
            intent.putExtra("image_id", R.drawable.tire4)
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: called")
        Log.d(TAG, "onRestart: dir_name check" + dir_name)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
    }
}