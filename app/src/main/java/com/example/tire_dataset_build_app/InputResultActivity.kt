package com.example.tire_dataset_build_app

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.io.File
import com.example.tire_dataset_build_app.ConnectFTP
import org.apache.commons.net.ftp.FTP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


import java.nio.file.Files.walk
import kotlin.concurrent.thread

class InputResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_result)
        var mIntent = getIntent()

        val sid = mIntent.getStringExtra("sid").toString()
        Log.d(TAG, "inputreslutact: " + sid)
        val depth1 = findViewById<EditText>(R.id.num1)
        val depth2 = findViewById<EditText>(R.id.num2)
        val depth3 = findViewById<EditText>(R.id.num3)
        val depth4 = findViewById<EditText>(R.id.num4)
        val depth5 = findViewById<EditText>(R.id.num5)
        val depth6 = findViewById<EditText>(R.id.num6)
        val depth7 = findViewById<EditText>(R.id.num7)
        val depth8 = findViewById<EditText>(R.id.num8)
        val depth9 = findViewById<EditText>(R.id.num9)
        val depth10 = findViewById<EditText>(R.id.num10)
        val depth11 = findViewById<EditText>(R.id.num11)
        val depth12 = findViewById<EditText>(R.id.num12)

        val mstorebt = findViewById<Button>(R.id.ftp_store)

        val dir_name = mIntent.getStringExtra("dir_name")
        val path =externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name) + dir_name)}

        val mConnectFTP = ConnectFTP()
        thread(start=true){
            var status:Boolean = false
            status = mConnectFTP.ftpConnect("1.214.35.242", "ainetworks", "ainetworks123", 1314)
            if(status == true){
                Log.d(TAG, "Conection success")
            }
            else{
                Log.d(TAG, "Connection failed")
            }
        }

        // 쓰레드에서 파일 저장을 호출하지 말고 이렇게 리스너로만 호출해보자
        // 이렇게 분리하니까 되는데 왜 되는거지..?
        mstorebt.setOnClickListener {
            val BASE_URL_HyungJeong_API = "http://1.214.35.242:80/"
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_HyungJeong_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(HyunjeongAPI::class.java)
            val callResult = api.insert_ex_data(sid, depth1.text.toString(), depth2.text.toString(),depth3.text.toString(),depth4.text.toString(),depth5.text.toString(),depth6.text.toString(),depth7.text.toString(),depth8.text.toString(),depth9.text.toString(),depth10.text.toString(),depth11.text.toString(),depth12.text.toString())

            callResult.enqueue(object: Callback<Result_insert_ex_data> {
                override fun onResponse(call: Call<Result_insert_ex_data>, response: Response<Result_insert_ex_data>) {
                    Log.d("final 결과", "성공!" + response.body()?.result_msg)
                    Log.d("final sid 값", sid)
                }

                override fun onFailure(call: Call<Result_insert_ex_data>, t: Throwable) {
                    Log.d("결과", "실패: $t")
                }
            })

            thread(start=true){
                var imgList = path?.listFiles()
                val len:Int? = imgList?.lastIndex
                mConnectFTP.ftpCreateDirectory(dir_name)        // dir_name인 directory 생성

                for(i:Int in 0..len!!){
                    var imgFile_path = imgList?.get(i)?.path
                    var saved_name = imgFile_path?.split('/')?.last()
                    mConnectFTP.ftpUploadFile(imgFile_path, saved_name, "/" + dir_name)
                }
                runOnUiThread {
                    Toast.makeText(this, "Upload Done", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
