package com.example.tire_dataset_build_app

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import java.io.File
import com.example.tire_dataset_build_app.ConnectFTP
import org.apache.commons.net.ftp.FTP
import java.nio.file.Files.walk
import kotlin.concurrent.thread

class InputResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_result)
        
        val mstorebt = findViewById<Button>(R.id.ftp_store)

        val path =externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name))}

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
            thread(start=true){
                var imgList = path?.listFiles()
                val len:Int? = imgList?.lastIndex

                for(i:Int in 0..len!!){
                    var imgFile_path = imgList?.get(i)?.path
                    var saved_name = imgFile_path?.split('/')?.last()
                    mConnectFTP.ftpUploadFile(imgFile_path, saved_name, "/")
                }
                runOnUiThread {
                    Toast.makeText(this, "Upload Done", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
