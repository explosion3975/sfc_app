package com.example.sfc_front

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.sfc_front.MyAdapter

class FileUpgrade : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_gallery)
        val goBack = findViewById<ImageButton>(R.id.goBack)
        goBack.setOnClickListener{
            finish()
        }
        var resultLauncher = this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e("test1","osjoidfjs")
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val fileName = data?.getStringExtra("key")
                Log.e("test","$fileName")
//            val fileDelete = File(context.getExternalFilesDir(null),fileName)
//            fileDelete.delete()
            }
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val topPadding = resources.getDimensionPixelSize(R.dimen.top_padding) // 从资源文件获取内边距值
        recyclerView.setPadding(50, topPadding, 0, 0)
        // 准备模拟的数据集合
        val data = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5","Item 6","Item 7","Item 8","Item 9","Item 10","Item 11","Item 12","Item 13","Item 14","Item 15","Item 16")
        var fileType = ".png"
        // 创建 RecyclerView 的适配器并设置数据


        // 设置 RecyclerView 的布局管理器，例如 LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(this)
        val directoryPath = getExternalFilesDir(null) // 替换为你要读取的目录路径
        val searchFile = findViewById<EditText>(R.id.search_file)
        var userInput =""
        searchFile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Not implemented
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userInput = s.toString()
                val fileNames = directoryPath?.let { listFilesInDirectory(it,userInput,fileType,0) }
                val adapter = fileNames?.let { MyAdapter(it,R.drawable.photo_file, this@FileUpgrade,0,directoryPath = directoryPath, fileType = fileType,resultLauncher = resultLauncher) }
                recyclerView.adapter = adapter
                true
            }


            override fun afterTextChanged(p0: Editable?) {
                // Not implemented
            }
        })
        val fileNames = directoryPath?.let { listFilesInDirectory(it,userInput,".png",0) }
        val adapter = fileNames?.let { MyAdapter(it,R.drawable.photo_file, this@FileUpgrade,0,directoryPath = directoryPath, fileType = ".png",resultLauncher = resultLauncher) }
        recyclerView.adapter = adapter
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_fragment_image_button -> {
                    // 处理点击 item1 的逻辑
                    // 这里可以执行相应的操作
//                    Toast.makeText(this, "Item 1 Clicked", Toast.LENGTH_SHORT).show()
                    fileType=".png"
                    val fileNames = directoryPath?.let { listFilesInDirectory(it,userInput,".png",0) }
                    val adapter = fileNames?.let { MyAdapter(it,R.drawable.photo_file, this@FileUpgrade,0,directoryPath = directoryPath, fileType = ".png",resultLauncher = resultLauncher) }
                    recyclerView.adapter = adapter
                    true
                }
                R.id.navigation_video_button -> {
                    // 处理点击 item2 的逻辑
                    // 这里可以执行相应的操作
//                    Toast.makeText(this, "Item 2 Clicked", Toast.LENGTH_SHORT).show()
                    fileType=".mp4"
                    val fileNames = directoryPath?.let { listFilesInDirectory(it,userInput,".mp4",0) }
                    val adapter = fileNames?.let { MyAdapter(it,R.drawable.video_file, this@FileUpgrade,0,directoryPath = directoryPath, fileType = ".mp4",resultLauncher = resultLauncher) }
                    recyclerView.adapter = adapter
                    true
                }
                R.id.navigation_audio_button -> {
                    // 处理点击 item2 的逻辑
                    // 这里可以执行相应的操作
//                    Toast.makeText(this, "Item 3 Clicked", Toast.LENGTH_SHORT).show()
                    fileType=".mp3"
                    val fileNames = directoryPath?.let { listFilesInDirectory(it,userInput,".mp3",0) }
                    val adapter = fileNames?.let { MyAdapter(it,R.drawable.music_file, this@FileUpgrade,0,directoryPath = directoryPath, fileType = ".mp3",resultLauncher = resultLauncher) }
                    recyclerView.adapter = adapter
                    true
                }
                R.id.navigation_text_file_button -> {
                    // 处理点击 item2 的逻辑
                    // 这里可以执行相应的操作
//                    Toast.makeText(this, "Item 4 Clicked", Toast.LENGTH_SHORT).show()
                    fileType=".txt"
                    val fileNames = directoryPath?.let { listFilesInDirectory(it,userInput,".txt",0) }
                    val adapter = fileNames?.let { MyAdapter(it,R.drawable.txt_file, this@FileUpgrade,0,directoryPath = directoryPath, fileType = ".txt",resultLauncher = resultLauncher) }
                    recyclerView.adapter = adapter
                    true
                }
                R.id.navigation_file_button -> {
                    // 处理点击 item2 的逻辑
                    // 这里可以执行相应的操作
//                    Toast.makeText(this, "Item 5 Clicked", Toast.LENGTH_SHORT).show()
                    fileType=""
                    val fileNames = directoryPath?.let { listFilesInDirectory(it,userInput,"",0) }
                    val adapter = fileNames?.let { MyAdapter(it,R.drawable.file_file, this@FileUpgrade,0,directoryPath = directoryPath, fileType = "",resultLauncher = resultLauncher) }
                    recyclerView.adapter = adapter
                    true
                }
                // 添加其他 item 的处理逻辑
                else -> false
            }
        }
    }

}