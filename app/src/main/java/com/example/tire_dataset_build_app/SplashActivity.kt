package com.example.tire_dataset_build_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.delay
import java.util.*
import kotlin.concurrent.schedule

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timer("SettingUp", false).schedule(2000){
            intent = Intent(this@SplashActivity, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}