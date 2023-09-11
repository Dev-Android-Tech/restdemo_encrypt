package com.example.restdemo;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class FileEncryptionUtils {
    private static final String TAG = "FileEncryptionUtils";

    public static void saveJsonToFileCrypted(Context context, String fileName, String jsonData, SecretKey keyToUse) {
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File outputFile = new File(downloadsFolder, fileName);

            // Generate a random initialization vector (IV)
            byte[] iv = generateRandomIV();

            // Encrypt the JSON data using the provided key
            byte[] encryptedData = encrypt(jsonData.getBytes(), keyToUse, iv);

            // Combine IV and encrypted data into a single byte array
            byte[] ivAndData = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, ivAndData, 0, iv.length);
            System.arraycopy(encryptedData, 0, ivAndData, iv.length, encryptedData.length);

            // Save the encrypted data to the file
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(ivAndData);
            outputStream.close();

            // Display a toast message on the UI thread
            showToastOnUIThread(context, "File saved to Downloads folder: " + fileName);
        } catch (Exception e) {
            Log.e(TAG, "Error saving encrypted file: " + e.getMessage());

            // Display an error toast message on the UI thread
            showToastOnUIThread(context, "Error saving file.");
        }
    }

    private static byte[] generateRandomIV() {
        byte[] iv = new byte[16]; // AES block size is 16 bytes
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }

    private static byte[] encrypt(byte[] data, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    private static void showToastOnUIThread(final Context context, final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
