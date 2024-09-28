package com.example.sfc_front.ui.AES;

import android.net.Uri;

import com.example.sfc_front.ui.home.HomeViewModel;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class AES256 {
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CTR/NoPadding";
    String secretKey; // 16字元長度的密鑰
    public AES256(String PW){
        secretKey = PW;
    }


    // 加密函式
    public void encryptFile(File inputFile, File outputFile) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        // 生成隨機初始化向量
        SecureRandom random = new SecureRandom();
        byte[] ivBytes = new byte[16];
        random.nextBytes(ivBytes);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

        FileInputStream inputStream = new FileInputStream(inputFile.getPath());
        FileOutputStream outputStream = new FileOutputStream(outputFile.getPath());
        long fileSize = inputFile.length();
        long ii = 0;
        // 寫入初始化向量到輸出檔案（用於解密）
        outputStream.write(ivBytes);
        HomeViewModel.currentProgress =0;
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            ii+=bytesRead;
            HomeViewModel.currentProgress = (int) (ii*100/fileSize);
            byte[] encryptedBytes = cipher.update(buffer, 0, bytesRead);
            outputStream.write(encryptedBytes);
        }

        byte[] finalEncryptedBytes = cipher.doFinal();
        outputStream.write(finalEncryptedBytes);

        inputStream.close();
        outputStream.close();
    }

    // 解密函式
    public void decryptFile(File inputFile, File outputFile) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        FileInputStream inputStream = new FileInputStream(inputFile.getPath());
        byte[] ivBytes = new byte[16];

        // 讀取初始化向量
        inputStream.read(ivBytes);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

        FileOutputStream outputStream = new FileOutputStream(outputFile.getPath());

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] decryptedBytes = cipher.update(buffer, 0, bytesRead);
            outputStream.write(decryptedBytes);
        }

        byte[] finalDecryptedBytes = cipher.doFinal();
        outputStream.write(finalDecryptedBytes);

        inputStream.close();
        outputStream.close();
    }
}
