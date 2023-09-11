package com.example.restdemo;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FileDecryptionUtils {
    private static final String TAG = "FileDecryptionUtils";

    public static String decryptFileContent(Context context, String fileName, SecretKey decryptionKey) {
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File inputFile = new File(downloadsFolder, fileName);

            // Read the encrypted file content into a byte array
            byte[] encryptedData = readBytesFromFile(inputFile);

            // Extract the IV (Initialization Vector) from the beginning of the data
            byte[] iv = new byte[16]; // IV size for AES
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);

            // Remove the IV from the data, leaving only the encrypted content
            byte[] encryptedContent = new byte[encryptedData.length - iv.length];
            System.arraycopy(encryptedData, iv.length, encryptedContent, 0, encryptedContent.length);

            // Initialize the cipher for decryption
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, new IvParameterSpec(iv));

            // Decrypt the content
            byte[] decryptedData = cipher.doFinal(encryptedContent);

            // Convert the decrypted data to a string
            return new String(decryptedData, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting file content: " + e.getMessage());
            return null; // Return null to indicate decryption failure
        }
    }

    private static byte[] readBytesFromFile(File file) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fileInputStream.read(data);
        fileInputStream.close();
        return data;
    }
}

