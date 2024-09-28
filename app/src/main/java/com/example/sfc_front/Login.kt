package com.example.sfc_front

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class Login : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var failAuthentication = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // 創建一個執行緒池以處理生物識別驗證
        val executor: Executor = Executors.newSingleThreadExecutor()

        // 創建生物識別驗證對話框
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    runOnUiThread {

                    Toast.makeText(
                        applicationContext,
                        "Authentication error:  $errString", Toast.LENGTH_SHORT
                    ).show()
                        moveTaskToBack(true);
                        exitProcess(-1)
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Authentication succeeded!", Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@Login, MainActivity::class.java)
                        startActivity(intent)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    runOnUiThread {

                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                        failAuthentication+=1
                        if (failAuthentication==3){
                            moveTaskToBack(true);
                            exitProcess(-1)
                        }
                }
                }
            })

        // 設置生物識別驗證提示信息
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirm Using Your Fingerprint")
            .setSubtitle("You can use your fingerprint to confirm making payments through this app.")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .setNegativeButtonText("Exit")

            .build()

        // 開始生物識別驗證
        biometricPrompt.authenticate(promptInfo)
    }
}
