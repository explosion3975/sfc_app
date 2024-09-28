package com.example.sfc_front.ui.home

//import com.example.sfc_front.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.example.sfc_front.R
import com.example.sfc_front.databinding.ActivityMainBinding
import com.example.sfc_front.ui.AES.AES256
import com.example.sfc_front.ui.FDAES.FDAES
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors


class NoteActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.note_activity)
//        setContentView(R.layout.fragment_home)

        val sendButton = findViewById<Button>(R.id.noteSendButton)
        val cancelButton = findViewById<Button>(R.id.noteCancelButton)
//        val fileName = findViewById<EditText>(R.id.fileNameEditText).text
//        val fileName = "123"
        val content = findViewById<EditText>(R.id.contentEditText).text
//        val switch:Switch = findViewById(R.id.switchButton)
        val status = intent.getBooleanExtra("status",false)

        when{
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
//                Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
//                request.launch(arrayOf(
//                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    android.Manifest.permission.READ_EXTERNAL_STORAGE
//                ))
            }
        }

        sendButton.setOnClickListener{
            createANote(content, status)
        }
        cancelButton.setOnClickListener{
            finish()
        }
        fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            if (requestCode == 1) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "need your permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun createANote(content:Editable, status:Boolean) {
        try {

//            val switch:Switch = findViewById(R.id.switchButton)
//            val s = switch.isChecked
//                Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show()
            val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExternalFilesDir(null), "$fileName.txt")
//            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
//                val file = File(dir,"$fileName.txt")
                val fileWriter = FileWriter(file, true)
                fileWriter.write(content.toString())
                fileWriter.close()
                //加密
//                val fdaes = FDAES("sixsquare1234567")
                val aes256 = AES256("sixsquare1234567")
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    try {
//                        if (status) {
//                            val outputFile =
//                                File(getExternalFilesDir(null), "FDAES_Encrypted_$fileName.txt")
//                            fdaes.FileEncryption_CBC(file, outputFile)
//                            Log.e("test2", "FDAES")
//
//                        } else {
                        val outputFile =
                                File(getExternalFilesDir(null), "$fileName.txt.pga")
                            // 在線程池中執行加密操作
                        aes256.encryptFile(file, outputFile)
//                        Log.e("test3", "AES")
//                        }


                        // 刪除inputFile
                        if (file.exists()) {
                            file.delete()
                        }
                    } finally {
                        executor.shutdown()
                    }
                }
                //刪除原本檔案
            } catch (e: IOException) {
                e.printStackTrace()
            }
            finish()

    }
//    public fun requestPremission()
//    {
//
//    }

}


