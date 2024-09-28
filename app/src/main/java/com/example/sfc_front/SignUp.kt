package com.example.sfc_front

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import com.example.sfc_front.ui.library.JsonFileManager

class SignUp: AppCompatActivity() {
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        if(JsonFileManager.readJsonFile(this)!=null){
            val intent = Intent(this@SignUp, Login::class.java)
            startActivity(intent)
        }
        else{
            JsonFileManager.createJsonFile(this);
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up) // 替换为您的布局文件

        val textView = findViewById<TextView>(R.id.sign_up)
        val fullText = getString(R.string.sign_up_title)

        // 定义要改变颜色的文字范围
        val start = fullText.indexOf("YOUR") // "YOUR" 文本的起始位置

        // 创建一个 SpannableString 来设置不同颜色
        val spannableString = SpannableString(fullText)

        // 设置 "YOUR" 文本的颜色为 #9A77D5
        val color = Color.parseColor("#9A77D5")
        spannableString.setSpan(
            ForegroundColorSpan(color),
            start,
            start + "YOUR".length, // 计算结束位置
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        // 找到Spinner元素
        // 找到Spinner元素
        val genderSpinner = findViewById<Spinner>(R.id.gender_spinner)

        // 創建適配器

        // 創建適配器
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_options, R.layout.spinner_item
        )

        // 設置下拉式清單的樣式

        // 設置下拉式清單的樣式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 設置Spinner的適配器

        // 設置Spinner的適配器
        genderSpinner.adapter = adapter
        // 将 SpannableString 设置为 TextView 的文本
        textView.text = spannableString
        val idText = findViewById<EditText>(R.id.id_text)
        val product_keyText = findViewById<EditText>(R.id.product_key_text)
        val nameEditText = findViewById<EditText>(R.id.name_)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val retypePasswordEditText = findViewById<EditText>(R.id.retype_password)
        val phoneEditText = findViewById<EditText>(R.id.phone)
        val button = findViewById<Button>(R.id.button)

        val gender_text: String = genderSpinner.getSelectedItem().toString()
        button.setOnClickListener {
            val id = idText.text.toString()
            val product_key = product_keyText.text.toString()
            val name = nameEditText.text.toString()

            val password = passwordEditText.text.toString()
            val retypePassword = retypePasswordEditText.text.toString()
            val phone = phoneEditText.text.toString()
            var s : String = ""
                if (id.isNotEmpty()&& product_key.isNotEmpty()&& name.isNotEmpty() && gender_text.isNotEmpty() && password.isNotEmpty() && retypePassword.isNotEmpty() && phone.isNotEmpty()) {
                    if (password == retypePassword) {
                        val client = OkHttpClient()
                        val json = JSONObject()
                        json.put("id", id)
                        json.put("production_key", product_key)
                        json.put("name", name)
                        json.put("gender", gender_text)
                        json.put("phone_number", phone)
                        json.put("password", password)
                        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
                        val request = Request.Builder()
                            .url("https://subject.explosion.nmg.cs.thu.edu.tw/init")
                            .post(requestBody)
                            .build()
                        var haveAccount=0;
                        // 发起请求
                        var status: String
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                // 检查异常类型和消息
                                val errorMessage = when {
                                    e.message?.contains("Unable to resolve host") == true -> "Please ensure your internet connection."
                                    else -> "Server is broken."
                                }

                                // 在主线程中显示Toast消息
                                runOnUiThread {
                                    val duration = Toast.LENGTH_SHORT
                                    Toast.makeText(this@SignUp, errorMessage, duration).show()
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                // 请求成功处理
                                val responseBody = response.body?.string()
                                if (response.isSuccessful) {
                                    val jsonResponse = JSONObject(responseBody)

                                    // 从JSON对象中获取"status"字段
                                    status = jsonResponse.getString("status")
                                    Log.e("kkkkkkk",status)
                                    if (status=="1"){
                                        val intent = Intent(this@SignUp, Login::class.java)
                                        startActivity(intent)

                                    }
                                    else if(status=="2"){
                                        runOnUiThread {
                                            val message = "this account has benn used!"
                                            val duration =
                                                Toast.LENGTH_SHORT // 或 Toast.LENGTH_LONG，指定消息的显示时长
                                            Toast.makeText(this@SignUp, message, duration).show()
                                        }
                                    }else if(status=="0"){

                                        runOnUiThread {
                                            val message = "Can't found this account!"
                                            val duration = Toast.LENGTH_SHORT
                                            Toast.makeText(this@SignUp, message, duration).show()
                                        }
                                    }
                                }

                            }
                        })
                    } else {
                        val message = "password mismatch"
                        val duration = Toast.LENGTH_SHORT // 或 Toast.LENGTH_LONG，指定消息的显示时长
                        val toast = Toast.makeText(this, message, duration)
                        toast.show()
                    }
                } else {
                    val message = "value can't be null"
                    val duration = Toast.LENGTH_SHORT // 或 Toast.LENGTH_LONG，指定消息的显示时长
                    val toast = Toast.makeText(this, message, duration)
                    toast.show()
                }

        }
    }
}
