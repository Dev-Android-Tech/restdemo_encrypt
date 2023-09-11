package com.example.restdemo;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyValidator {
    public static boolean isValidAESKey(String keyString) {
        try {
            // Convert the key string to bytes
            byte[] keyBytes = hexToBytes(keyString);

            // Check if the key length is valid for AES (128, 192, or 256 bits)
            if (keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32) {
                // Try to create a SecretKey from the bytes
                SecretKey key = new SecretKeySpec(keyBytes, "AES");
                return true;
            }
        } catch (Exception e) {
            // An exception occurred, indicating an invalid key
        }
        return false;
    }

    public static byte[] hexToBytes(String hex) {
        byte[] keyBytes = new byte[hex.length() / 2];
        for (int i = 0; i < keyBytes.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hex.substring(index, index + 2), 16);
            keyBytes[i] = (byte) j;
        }
        return keyBytes;
    }

    public static void main(String[] args) {
        String keyString = "your_aes_key_as_hex_string_here";
        boolean isValid = isValidAESKey(keyString);
        System.out.println("Is AES Key Valid: " + isValid);
    }
}
