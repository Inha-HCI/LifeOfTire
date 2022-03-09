package com.example.tire_dataset_build_app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.tire_dataset_build_app.databinding.ActivityMenuBinding
import render.animations.*

class MenuActivity : AppCompatActivity() {
    val render = Render(this)
    private lateinit var binding:ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_menu)

        Log.d("SDK Version", Build.VERSION.SDK_INT.toString())

        val build_button = findViewById<Button>(R.id.build)
        val pred_button = findViewById<Button>(R.id.pred)

        build_button.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        pred_button.setOnClickListener {
//            val intent = Intent(this, PredictSelectActivity::class.java)
            val intent = Intent(this, PredictCameraMainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        render.setAnimation(Flip().InX(binding.imageView))
        render.start()

        render.setAnimation(Bounce().InDown(binding.build))
        render.start()

        render.setAnimation(Bounce().InDown(binding.pred))
        render.start()

        render.setAnimation(Bounce().InDown(binding.analyze))
        render.start()
    }
}