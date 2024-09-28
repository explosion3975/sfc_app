package com.example.sfc_front

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.sfc_front.ui.AES.AES256
import com.example.sfc_front.ui.FDAES.FDAES
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


fun listFilesInDirectory(
    directoryPath: File, input: String, fileExtension: String, open: Int = 1
): List<String> {
    val directory = directoryPath
    var fileExtension2: String

    if (fileExtension == ".png") {

        fileExtension2 = ".jpg"
    } else {
        fileExtension2 = fileExtension
    }
    // 检查目录是否存在
    if (!directory.exists() || !directory.isDirectory) {
        return emptyList()
    }

    // 使用 listFiles() 方法获取目录下的所有文件
    val files = directory.listFiles()

    // 如果没有文件，返回空列表
    if (files == null || files.isEmpty()) {
        return emptyList()
    }

    // 提取文件名并添加到列表中
    val fileNames = mutableListOf<String>()
    for (file in files) {
        val userInput = file.name.contains(input, true)
//        Log.e("nonono", userInput.toString())
        if (file.isFile && userInput && (file.name.endsWith(fileExtension) || file.name.endsWith(
                fileExtension2)||file.name.endsWith("$fileExtension.pga")||file.name.endsWith("$fileExtension.fpga"))
        ) {
            if (open == 0 && file.name.contains(".pga") && !file.name.contains(".fpga")) {
                fileNames.add(file.name)
            } else if (open == 1 && (file.name.contains(".pga") || file.name.contains(".fpga")||file.name.contains(".save"))) {
                fileNames.add(file.name)
            }
        }

    }

    return fileNames
}

class MyAdapter(
    private var data: List<String>,
    private val iconResourceId: Int,
    private val context: AppCompatActivity,
    private val open: Int = 1,
    private val directoryPath: File,
    private val fileType: String,
    private var resultLauncher: ActivityResultLauncher<Intent>
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    val fdaes = FDAES("sixsquare1234567")
    val aes256 = AES256("sixsquare1234567")
    var newFileName: String = ""
    val OPEN_FILE_REQUEST_CODE = 123
    private var fileOpenCallback: FileOpenCallback? = null

    //    val RESULT_OK = 777
    interface FileOpenCallback {
        fun onFileOpenCompleted(file: File)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.file_name) // 通过ID找到文本视图
        val icon: ImageView = itemView.findViewById(R.id.view_icon) // 通过ID找到图标视图

        init {
            if (open == 1) {
                // 在这里初始化 icon，如果需要的话
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val FileName = data[position]
                        val fileToOpen = File(context.getExternalFilesDir(null), FileName)

                        if (FileName.contains(".fpga")) {

                            val subString: String =
                                FileName.subSequence(0, FileName.length - 5) as String
                            val outputFile = File(context.getExternalFilesDir(null), subString)
                            fdaes.FileDecryption_CBC(fileToOpen, outputFile)
                            openFile(outputFile, context)
                            updateData(listFilesInDirectory(directoryPath, "", fileType, open))
                        } else if (FileName.contains(".pga")) {
                            val subString: String =
                                FileName.subSequence(0, FileName.length - 4) as String
                            val outputFile = File(context.getExternalFilesDir(null), subString)
                            aes256.decryptFile(fileToOpen, outputFile)
                            openFile(outputFile, context)
                            updateData(listFilesInDirectory(directoryPath, "", fileType, open))
                        } else {
                            openFile(fileToOpen, context)
                        }


                    }


                }
                itemView.setOnLongClickListener {

                    val position = adapterPosition
                    val fileName = data[position]
                    showContextMenu(it, fileName)
                    true
                }
            } else if (open == 0) {
                itemView.setOnClickListener {

                    showOptionsDialog(context, onRenameClick = { newName ->
                        // 在这里处理重命名操作，使用 newName 变量
                        newFileName = newName
                        continueWithRenamedFile(newFileName)
                    })


                }
            }

        }

        private fun continueWithRenamedFile(oldFileName: String) {
            CompletableFuture.runAsync {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val string = data[position]
                    val fileToOpen = File(context.getExternalFilesDir(null), string)
                    val name = fileToOpen.nameWithoutExtension

                    // 使用字符串处理方法提取副檔名
                    val lastDotIndex = name.lastIndexOf(".")
                    val fileExtension = if (lastDotIndex >= 0) {
                        name.substring(lastDotIndex + 1)
                    } else {
                        // 沒有找到副檔名的情況下的處理
                        "無副檔名"
                    }
                    val newFileName = "$oldFileName.$fileExtension"
                    val outputFile = File(context.getExternalFilesDir(null), newFileName)
                    aes256.decryptFile(fileToOpen, outputFile)
                    fileToOpen.delete()
                    val fdaesOutputFile =
                        File(context.getExternalFilesDir(null), "$newFileName.fpga")
                    fdaes.FileEncryption_CBC(outputFile, fdaesOutputFile)
                    outputFile.delete()
                    updateData(listFilesInDirectory(directoryPath, "", fileType, open))
                    Toast.makeText(context, "加密完成", Toast.LENGTH_SHORT).show()
                }
            }.exceptionally {
                it.printStackTrace()
                return@exceptionally null
            }
            updateData(listFilesInDirectory(directoryPath, "", fileType, open))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.fileName.text = item
        holder.icon.setImageResource(iconResourceId)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun openFile(file: File, context: AppCompatActivity) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val mime = context.contentResolver.getType(uri)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mime)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            val name = file.name
            intent.putExtra("key", name.toString())
            Log.e("test", "$name")
//            context.startActivityForResult(intent, OPEN_FILE_REQUEST_CODE)
//            context.setResult(RESULT_OK,intent)
//            context.finish()
            resultLauncher.launch(intent)

//            context.startActivity(intent)
//            fileOpenCallback?.onFileOpenCompleted(file)

        } catch (e: Exception) {
            Toast.makeText(context, "無法開啟檔案", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateData(newData: List<String>) {
        data = newData
        notifyDataSetChanged()
    }

    fun listFilesInDirectory(
        directoryPath: File, input: String, fileExtension: String, open: Int = 1
    ): List<String> {
        val directory = directoryPath
        var fileExtension2: String

        if (fileExtension == ".png") {

            fileExtension2 = ".jpg"
        } else {
            fileExtension2 = fileExtension
        }
        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        // 使用 listFiles() 方法获取目录下的所有文件
        val files = directory.listFiles()

        // 如果没有文件，返回空列表
        if (files == null || files.isEmpty()) {
            return emptyList()
        }

        // 提取文件名并添加到列表中
        val fileNames = mutableListOf<String>()
        for (file in files) {
            val userInput = file.name.contains(input, true)
//        Log.e("nonono", userInput.toString())
            if (file.isFile && userInput && (file.name.endsWith(fileExtension) || file.name.endsWith(
                    fileExtension2)||file.name.endsWith("$fileExtension.pga")||file.name.endsWith("$fileExtension.fpga"))
            ) {
                if (open == 0 && file.name.contains(".pga") && !file.name.contains(".fpga")) {
                    fileNames.add(file.name)
                } else if (open == 1 && (file.name.contains(".pga") || file.name.contains(".fpga")||file.name.contains(".save"))) {
                    fileNames.add(file.name)
                }
            }

        }

        return fileNames
    }

    fun setFileOpenCallback(callback: FileOpenCallback) {
        fileOpenCallback = callback
    }

    private fun showContextMenu(view: View, fileName: String) {
        val popupMenu = PopupMenu(context, view)
        val menu = popupMenu.menu

        // 添加上下文菜单项
        menu.add("Rename")
        menu.add("Delete")
        menu.add("Save")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Rename" -> {
                    // 处理重命名操作
                    // 在这里执行重命名的逻辑
                    showOptionsDialog(context, onRenameClick = { newName ->
                        // 在这里处理重命名操作，使用 newName 变量
                        Toast.makeText(context, "Rename to $newName", Toast.LENGTH_SHORT).show()
                        val fileToOpen = File(context.getExternalFilesDir(null), fileName)
                        val oldFileName = fileToOpen.nameWithoutExtension
                        val extension = fileToOpen.extension
                        val lastDotIndex = oldFileName.lastIndexOf(".")
                        val fileExtension = if (lastDotIndex >= 0) {
                            oldFileName.substring(lastDotIndex + 1)
                        } else {
                            // 沒有找到副檔名的情況下的處理
                            "無副檔名"
                        }
                        val firstDotIndex = oldFileName.indexOf(".")
                        val nameBeforeFirstDot = if (firstDotIndex >= 0) {
                            oldFileName.substring(0, firstDotIndex)
                        } else {
                            // 没有找到点的情况下的处理
                            "无名称"
                        }
                        val newFileName = if (extension.contains("fpga")) {
                            "$newName.$fileExtension.fpga"
                        } else if (extension.contains("pga")) {
                            "$newName.$fileExtension.pga"
                        } else {
                            Toast.makeText(context, "else$extension", Toast.LENGTH_SHORT).show()
                            "$newName.$fileExtension.fpga"
                        }
                        val newFile = File(fileToOpen.parent, newFileName)
                        fileToOpen.renameTo(newFile)
                        updateData(listFilesInDirectory(directoryPath, "", fileType, open))

                    })
                    true
                }

                "Delete" -> {
                    // 处理删除操作
                    // 在这里执行删除的逻辑

                    val fileToOpen = File(context.getExternalFilesDir(null), fileName)
                    fileToOpen.delete()
                    updateData(listFilesInDirectory(directoryPath, "", fileType, open))
                    true
                }

                "Save" -> {
                    // 处理保存操作
                    // 在这里执行保存的逻辑
                    val fileToOpen = File(context.getExternalFilesDir(null), fileName)
                    val oldFileName = fileToOpen.nameWithoutExtension
                    val extension = fileToOpen.extension
                    // 使用字符串处理方法提取副檔名
                    val lastDotIndex = oldFileName.lastIndexOf(".")
                    val fileExtension = if (lastDotIndex >= 0) {
                        oldFileName.substring(lastDotIndex + 1)
                    } else {
                        // 沒有找到副檔名的情況下的處理
                        "無副檔名"
                    }
                    val firstDotIndex = oldFileName.indexOf(".")
                    val nameBeforeFirstDot = if (firstDotIndex >= 0) {
                        oldFileName.substring(0, firstDotIndex)
                    } else {
                        // 没有找到点的情况下的处理
                        "未命名"
                    }
                    if (extension.contains("fpga")) {
                        val outputFile = File(
                            context.getExternalFilesDir(null),
                            "$nameBeforeFirstDot.save.$fileExtension"
                        )
                        fdaes.FileDecryption_CBC(fileToOpen, outputFile)
                        updateData(listFilesInDirectory(directoryPath, "", fileType, open))

                    } else if (extension.contains("pga")) {
                        val outputFile = File(
                            context.getExternalFilesDir(null),
                            "$nameBeforeFirstDot.save.$fileExtension"
                        )
                        aes256.decryptFile(fileToOpen, outputFile)
                        updateData(listFilesInDirectory(directoryPath, "", fileType, open))
                    } else {
                        Toast.makeText(context, "already in plain text.", Toast.LENGTH_SHORT).show()
                    }

                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    fun showOptionsDialog(
        context: Context,
        onRenameClick: (String) -> Unit,
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        val inputEditText = EditText(context)
        inputEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

        // 设置对话框标题
        alertDialogBuilder.setTitle("Rename your file")

        // 设置文本输入框
        alertDialogBuilder.setView(inputEditText)

        // 添加重命名按钮
        alertDialogBuilder.setPositiveButton("Rename") { dialog, which ->
            val newName = inputEditText.text.toString()
            onRenameClick(newName)
            dialog.dismiss()
        }


        // 创建并显示对话框
        val alertDialog = alertDialogBuilder.create()

        // 设置按钮的颜色
        alertDialog.setOnShowListener { dialog ->
            val renameButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

            renameButton.setTextColor(context.resources.getColor(R.color.dialog_button_positive_color))
        }

        alertDialog.show()
    }


}
