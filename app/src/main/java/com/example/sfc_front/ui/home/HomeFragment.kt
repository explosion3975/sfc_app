package com.example.sfc_front.ui.home


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.sfc_front.FileUpgrade
import com.example.sfc_front.MainActivity
import com.example.sfc_front.R
import com.example.sfc_front.ReadFile
import com.example.sfc_front.databinding.FragmentHomeBinding
import com.example.sfc_front.ui.AES.AES256
import com.example.sfc_front.ui.FDAES.FDAES
import com.example.sfc_front.ui.library.JsonFileManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private lateinit var progressObserver: Observer<Int>
    private val binding get() = _binding!!
    val aes256 = AES256("sixsquare1234567")
    //    val fdaes = FDAES("sixsquare1234567")
    private val password = "1234"



    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var failAuthentication = 0
    private var FileName =""
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val activity = requireActivity()
        val ball = activity.findViewById<ProgressBar>(R.id.progressBar)
        val ballText = activity.findViewById<TextView>(R.id.ball_text)

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {
            val inputFile = File(activity.getExternalFilesDir(null), FileName)
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                try {
                    activity.runOnUiThread {
                        ballText.setTextColor(Color.parseColor("#FFFFFFFF"))
                        ball.progressDrawable = resources.getDrawable(R.drawable.ball, null)
                    }
                    val outputFile = File(activity.getExternalFilesDir(null), "$FileName.pga")
                    aes256.encryptFile(inputFile, outputFile)
                    if (inputFile.exists()) {
                        inputFile.delete()
                    }
                } finally {
                    activity.runOnUiThread {
                        ballText.setTextColor(Color.parseColor("#00FFFFFF"))
                        ball.progressDrawable = resources.getDrawable(R.drawable.logo, null)
                    }
                    executor.shutdown()
                }
            }
        } else if (requestCode == HomeFragment.PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            val selectedFileUri = data?.data
            val FileName = selectedFileUri?.let { getFileNameFromUri(it) }
            if (selectedFileUri != null) {
                try {
                    val contentResolver = activity.contentResolver
                    val inputStream = contentResolver.openInputStream(selectedFileUri)

                    if (inputStream != null) {
                        val tempFile = createTempFile("temp_", ".tmp")

                        inputStream.use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        val outputFile = File(activity.getExternalFilesDir(null), "$FileName.pga")
                        aes256.encryptFile(tempFile, outputFile)

                        tempFile.delete()
                        inputStream.close()
                    }
                } catch (e: Exception) {
                    Log.e("Error", "Error while processing file: ${e.message}")
                }
            }
        }
    }

    @SuppressLint("Range")
    fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null

        val contentResolver = requireActivity().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                if (!displayName.isNullOrEmpty()) {
                    fileName = displayName
                }
            }
        }

        return fileName
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val ball = root.findViewById<ProgressBar>(R.id.progressBar)
        val ballText = root.findViewById<TextView>(R.id.ball_text)
        progressObserver = Observer { progressInt ->
            ballText.text = "$progressInt%"
            ball.progress = progressInt
        }
        viewModel.progressInt.observe(requireActivity(), progressObserver)
        viewModel.startTask()
        val noteButton = root.findViewById<ImageButton>(R.id.note_button)
        noteButton.setOnClickListener {
            val intent = Intent(getActivity(), NoteActivity::class.java)
            startActivity(intent)
        }
        val takePictureButton = root.findViewById<ImageButton>(R.id.camera_button)
        // 设置点击事件，调用拍照方法
        takePictureButton.setOnClickListener {
            takeAPhoto()
        }
        val takeVideoButton = root.findViewById<ImageButton>(R.id.video_button)
        takeVideoButton.setOnClickListener {
            takeAVideo()
        }
        val readFileButton = root.findViewById<ImageButton>(R.id.read_file_button)
        readFileButton.setOnClickListener {

            val intent = Intent(getActivity(), ReadFile::class.java)
            val executor: Executor = Executors.newSingleThreadExecutor()

            // 創建生物識別驗證對話框
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        requireActivity().runOnUiThread {

                            Toast.makeText(
                                requireContext(),
                                "Authentication error:  $errString", Toast.LENGTH_SHORT
                            ).show()
//                                moveTaskToBack(true);
//                                exitProcess(-1)
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        requireActivity().runOnUiThread {
                            val maxAttempts = 3 // 最大尝试次数
                            var time = JsonFileManager.readJsonFile(requireContext()).getInt("IncorrectPasswordAttempts")

                            if (time < maxAttempts) {
                                showInputDialog(
                                    requireContext(),
                                    "Please Enter Your Password",
                                    "Confirm",
                                    "Cancel",
                                    { userInput ->
                                        if (userInput == password) {
                                            JsonFileManager.updateJsonKey(requireContext(), "IncorrectPasswordAttempts", "0")
                                            Toast.makeText(requireContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(requireContext(), "Authentication failed!", Toast.LENGTH_SHORT).show()
                                            JsonFileManager.updateJsonKey(requireContext(), "IncorrectPasswordAttempts", (time + 1).toString())
                                            time = JsonFileManager.readJsonFile(requireContext()).getInt("IncorrectPasswordAttempts")
                                            // 如果尝试次数未达到最大次数，再次显示密码输入对话框
                                            if (time < maxAttempts) {
                                                onAuthenticationSucceeded(result)
                                            } else {
                                                Toast.makeText(requireContext(), "Exceeded maximum attempts. Your phone has been locked.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    {
                                        // 用户点击取消按钮后的处理逻辑
                                    }
                                )
                            } else {
                                Toast.makeText(requireContext(), "Your phone has been locked.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        requireActivity().runOnUiThread {

                            Toast.makeText(
                                requireContext(), "Authentication failed",
                                Toast.LENGTH_SHORT
                            ).show()
                            failAuthentication += 1
                            if (failAuthentication == 3){
//                                    moveTaskToBack(true);
//                                    exitProcess(-1)
                            }
                        }
                    }
                })
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm Using Your Fingerprint")
                .setSubtitle("You can use your fingerprint to confirm making payments through this app.")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .setNegativeButtonText("Exit")

                .build()

            // 開始生物識別驗證
            biometricPrompt.authenticate(promptInfo)




//            val intent = Intent(this, ReadFile::class.java)
//            startActivity(intent)
        }
        val fileUpgradeButton = root.findViewById<ImageButton>(R.id.file_upgrade_button)
        fileUpgradeButton.setOnClickListener {

            val executor: Executor = Executors.newSingleThreadExecutor()
            val intent = Intent(getActivity(), FileUpgrade::class.java)


            // 創建生物識別驗證對話框
            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        requireActivity().runOnUiThread {

                            Toast.makeText(
                                requireContext(),
                                "Authentication error:  $errString", Toast.LENGTH_SHORT
                            ).show()
//                                moveTaskToBack(true);
//                                exitProcess(-1)
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        requireActivity().runOnUiThread {
                            val maxAttempts = 3 // 最大尝试次数
                            var time = JsonFileManager.readJsonFile(requireContext()).getInt("IncorrectPasswordAttempts")

                            if (time < maxAttempts) {
                                showInputDialog(
                                    requireContext(),
                                    "Please Enter Your Password",
                                    "Confirm",
                                    "Cancel",
                                    { userInput ->
                                        if (userInput == password) {
                                            JsonFileManager.updateJsonKey(requireContext(), "IncorrectPasswordAttempts", "0")
                                            Toast.makeText(requireContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(requireContext(), "Authentication failed!", Toast.LENGTH_SHORT).show()
                                            JsonFileManager.updateJsonKey(requireContext(), "IncorrectPasswordAttempts", (time + 1).toString())
                                            time = JsonFileManager.readJsonFile(requireContext()).getInt("IncorrectPasswordAttempts")
                                            // 如果尝试次数未达到最大次数，再次显示密码输入对话框
                                            if (time < maxAttempts) {
                                                onAuthenticationSucceeded(result)
                                            } else {
                                                Toast.makeText(requireContext(), "Exceeded maximum attempts. Your phone has been locked.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    {
                                        // 用户点击取消按钮后的处理逻辑
                                    }
                                )
                            } else {
                                Toast.makeText(requireContext(), "Your phone has been locked.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        requireActivity().runOnUiThread {

                            Toast.makeText(
                                requireContext(), "Authentication failed",
                                Toast.LENGTH_SHORT
                            ).show()
                            failAuthentication += 1
                            if (failAuthentication == 3){
//                                    moveTaskToBack(true);
//                                    exitProcess(-1)
                            }
                        }
                    }
                })
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm Using Your Face")
                .setSubtitle("You can use your face to confirm making payments through this app.")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .setNegativeButtonText("Exit")
                .build()

            // 開始生物識別驗證
            biometricPrompt.authenticate(promptInfo)
        }
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isTaken ->
            if (isTaken) {
                Toast.makeText(getActivity(), "Photo has been taken and saved", Toast.LENGTH_SHORT).show()
                val context = requireContext()
                val inputFile  = File(context.getExternalFilesDir(null), FileName)

                val executor = Executors.newSingleThreadExecutor()

                executor.execute {
                    try {
                        requireActivity().runOnUiThread {
                            ballText.setTextColor(Color.parseColor("#FFFFFFFF"))
                            ball.progressDrawable = resources.getDrawable(R.drawable.ball, null)
                        }
//                        val switch : Switch = findViewById<Switch>(R.id.switchButton)

//                        if (switch.isChecked){
//                            val outputFile=File(getExternalFilesDir(null),"FDAES_Encrypted_$FileName")
//                            fdaes.FileEncryption_CBC(inputFile,outputFile)
//                        }
//                        else{
                        val outputFile=File(context.getExternalFilesDir(null),"$FileName.pga")
                        // 在線程池中執行加密操作
                        aes256.encryptFile(inputFile, outputFile)
//                        }


                        // 刪除inputFile
                        if (inputFile.exists()) {
                            inputFile.delete()
                        }
                    } finally {
                        requireActivity().runOnUiThread {
                            ballText.setTextColor(Color.parseColor("#00FFFFFF"))
                            ball.progressDrawable = resources.getDrawable(R.drawable.logo, null)
                        }
                        executor.shutdown()
                    }
                }
            } else {
                Toast.makeText(getActivity(), "Unable to take a photo", Toast.LENGTH_SHORT).show()
            }
        }
        val fileProtectButton : ImageButton = root.findViewById(R.id.file_protection_button)
        fileProtectButton.setOnClickListener{

            protectFile()
        }

        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()

        // 在 onDestroyView 中移除观察者，以避免内存泄漏
        viewModel.progressInt.removeObserver(progressObserver)

        // 清理资源
        _binding = null
    }
    private fun protectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        startActivityForResult(intent, 2)
    }
    private fun takeAPhoto() {
        // 检查相机权限
        if (getActivity()?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CAMERA
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            getActivity()?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(Manifest.permission.CAMERA),
                    MainActivity.REQUEST_CAMERA_PERMISSION
                )
            }
            return
        }
        val context = requireContext()
        // 创建保存照片的目录
        val photoDirectory = File(context.getExternalFilesDir(null), "")
        // 创建文件名
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFileName = "IMG_$timeStamp.png"
        //等關閉後執行加密用
        FileName = photoFileName
        // 创建文件
        val photoFile = File(photoDirectory, photoFileName)

        val photoUri =
            getActivity()?.let { FileProvider.getUriForFile(it, "com.example.sfc_front.fileprovider", photoFile) }


        // 启动拍照 Intent
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        // 启动拍照
        takePictureLauncher.launch(photoUri)

    }
    private fun takeAVideo() {
        // 检查相机和录制视频的权限
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                MainActivity.REQUEST_VIDEO_PERMISSION
            )
            return
        }
        val context = requireContext()
        // 创建保存视频的目录
        val videoDirectory = File(context.getExternalFilesDir(null), "")
        if (!videoDirectory.exists()) {
            videoDirectory.mkdirs()
        }

        // 创建文件名
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VID_$timeStamp.mp4"
        //等關閉後執行加密用
        FileName = videoFileName
        // 创建文件
        val videoFile = File(videoDirectory, videoFileName)

        val videoUri = FileProvider.getUriForFile(requireActivity(), "com.example.sfc_front.fileprovider", videoFile)

        // 启动视频录制 Intent
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)

        // 启动录制视频
        startActivityForResult(takeVideoIntent, MainActivity.REQUEST_VIDEO_CAPTURE)
    }
    fun showInputDialog(context: Context, title: String, positiveButtonText: String, negativeButtonText: String, onPositiveClick: (String) -> Unit, onNegativeClick: () -> Unit) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        val inputEditText = EditText(context)
        inputEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // 设置对话框标题
        alertDialogBuilder.setTitle(title)

        // 设置文本输入框
        alertDialogBuilder.setView(inputEditText)

        // 添加确定按钮
        alertDialogBuilder.setPositiveButton(positiveButtonText) { dialog, which ->
            val userInput = inputEditText.text.toString()

            onPositiveClick(userInput)
            dialog.dismiss()
        }


        // 添加取消按钮
        alertDialogBuilder.setNegativeButton(negativeButtonText) { dialog, which ->
            onNegativeClick()
            dialog.dismiss()
        }

        // 创建并显示对话框
        val alertDialog = alertDialogBuilder.create()

        // 设置按钮的颜色
        alertDialog.setOnShowListener { dialog ->
            val positiveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)

            positiveButton.setTextColor(context.resources.getColor(R.color.dialog_button_positive_color))
            negativeButton.setTextColor(context.resources.getColor(R.color.dialog_button_negative_color))
        }


        alertDialog.show()
    }

    companion object {
        const val PICK_PDF_FILE = 2
        private const val REQUEST_CAMERA_PERMISSION = 101
        private const val REQUEST_VIDEO_PERMISSION = 102
        private const val REQUEST_VIDEO_CAPTURE = 103
    }
}

