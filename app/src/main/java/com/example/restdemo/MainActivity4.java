package com.example.restdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity4 extends AppCompatActivity {


    private Button readButton, readCryptedButton;
    private EditText etTableClear, edTableCrypted,edSecretKey;
    private TextView textView;

    String tableNameStrClear, tableNameStrCrypted, keySecretStr;

    boolean resultRequest = false;
    String KeyServerTest = "00112233445566778899AABBCCDDEEFF";

    SecretKey secretKeyBuiltFromServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        readButton = findViewById(R.id.btngetdata);
        readCryptedButton = findViewById(R.id.btgetcrypted);
        etTableClear = findViewById(R.id.ettable);
        edTableCrypted = findViewById(R.id.ettable2);
        //edSecretKey = findViewById(R.id.key);
        textView = findViewById(R.id.tv);


        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableNameStrClear  = etTableClear.getText().toString();
                fetchEmployeeDataAndSaveClear(tableNameStrClear);
            }
        });

        readCryptedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableNameStrCrypted  = edTableCrypted.getText().toString();
                //keySecretStr  = edSecretKey.getText().toString();
                getAndCompareKey("http://web0.sinuslabs.net:8080/api/StorageKey/k", "1234568abcdefgh");

            }
        });
    }

    private void fetchEmployeeDataAndSaveClear( String ParamtableNameStrClear) {

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            OkHttpClient client = clientBuilder.build();
            // Define your API URL
            String apiUrl = "http://web0.sinuslabs.net:8080/api/table/"+ParamtableNameStrClear;

            // Create a request to fetch employee data
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity4.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String jsonData = response.body().string();


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity4.this, jsonData, Toast.LENGTH_SHORT).show();
                            }
                        });


                            // Save JSON to a file
                        String fileName = "employee_data.json";
                            saveJsonToFile(fileName, jsonData);


                        String jsonDataRead = readJsonFileFromDownloadsDirectory(fileName);

                        if (jsonDataRead != null) {
                            // Successfully read JSON data
                            // You can parse it or perform further operations here
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // set Text View
                                    textView.setText(jsonDataRead);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity4.this, "File Not Found", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                }
            });

    }


    private void fetchEmployeeDataAndSaveCrypted( String ParamtableNameStrCrypted, SecretKey keytoUse) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        OkHttpClient client = clientBuilder.build();
        // Define your API URL
        String apiUrl = "http://web0.sinuslabs.net:8080/api/table/"+ParamtableNameStrCrypted;

        // Create a request to fetch employee data
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity4.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String jsonData = response.body().string();



                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity4.this, jsonData, Toast.LENGTH_SHORT).show();
                        }
                    });


                    // Save JSON to a file
                    String fileName = "employee_data_crypted.json";
                    //saveJsonToFileCrypted(fileName, jsonData);
                    FileEncryptionUtils.saveJsonToFileCrypted(getApplicationContext(),fileName,jsonData,keytoUse);

                    String jsonDataRead = readJsonFileFromDownloadsDirectory(fileName);

                    if (jsonDataRead != null) {
                        // Successfully read JSON data
                        // You can parse it or perform further operations here
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // set Text View
                                textView.setText(jsonDataRead);
                            }
                        });


                            String downloadsFolderPath = getDownloadsFolderPath();
                            String encryptedFilePath = downloadsFolderPath + "/" + fileName;
//                            byte[] encryptedDataFromFile = readBytesFromFile(encryptedFilePath);
//                            String decryptedData = decryptData(encryptedDataFromFile, keytoUse);

                            String decryptedData = FileDecryptionUtils.decryptFileContent(getApplicationContext(),fileName,keytoUse);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Toast.makeText(MainActivity4.this, "File Not Found", Toast.LENGTH_SHORT).show();
                                    textView.setText(decryptedData);
                                }
                            });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity4.this, "File Not Found", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                }
            }
        });
    }


    public static String getDownloadsFolderPath() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        }
        return null; // Downloads directory not available
    }
    public static byte[] readBytesFromFile(String filePath) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            int fileSize = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                fileSize = (int) Files.size(Paths.get(filePath));
            }
            byte[] fileData = new byte[fileSize];
            int bytesRead = fileInputStream.read(fileData);
            if (bytesRead != fileSize) {
                throw new IOException("Failed to read the entire file.");
            }
            return fileData;
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    private boolean getAndCompareKey(String apiUrl, String key)
    {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        OkHttpClient client = clientBuilder.build();
        // Create a request to fetch employee data
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity4.this, "Failed to fetch key", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() ) {
                    final String keyCollected = response.body().string();
                    if (KeyValidator.isValidAESKey(keyCollected))
                    {
                    //Building a key Secret
                    byte[] keyBytes = hexToBytes(keyCollected);
                    secretKeyBuiltFromServer = new SecretKeySpec(keyBytes, "AES");
                    fetchEmployeeDataAndSaveCrypted(tableNameStrCrypted,secretKeyBuiltFromServer);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(MainActivity4.this, "Collected Key"+keyCollected+"And"+key, Toast.LENGTH_SHORT).show();
                            }
                        });





                }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity4.this, "Not Valid Key" , Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }


                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity4.this, "Not able to collect Key" , Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        });

        return resultRequest;
    }


    public String readJsonFileFromDownloadsDirectory(String fileName) {
        // Ensure that the external storage is mounted and readable
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Get the path to the Downloads directory
            String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

            // Construct the full file path
            File file = new File(directoryPath, fileName);

            // Check if the file exists
            if (file.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    // Close the readers
                    bufferedReader.close();
                    inputStreamReader.close();
                    fileInputStream.close();

                    // Return the JSON data as a string
                    return stringBuilder.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Return null if the file was not found or if there was an error reading it
        return null;
    }
    private void saveJsonToFile(String filename, String jsonData) {
        // Check if external storage is writable
        if (isExternalStorageWritable()) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(dir,filename );

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(jsonData);
                writer.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity4.this, "JSON saved to Downloads folder", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity4.this, "External storage is not writable.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    // Encrypt the JSON data before saving it
    public byte[] encryptData(String jsonData, SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(jsonData.getBytes());
    }
    // Generate a secure random key for AES encryption
    public SecretKey generateAESKey() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[16]; // 128-bit key
        secureRandom.nextBytes(key);
        return new SecretKeySpec(key, "AES");
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }
        return hexString.toString();
    }

//    public static SecretKey hexToBytes(String hex) {
//        byte[] keyBytes = new byte[hex.length() / 2];
//        for (int i = 0; i < keyBytes.length; i++) {
//            int index = i * 2;
//            int j = Integer.parseInt(hex.substring(index, index + 2), 16);
//            keyBytes[i] = (byte) j;
//        }
//        return new SecretKeySpec(keyBytes, "AES");
//    }

    public static byte[] hexToBytes(String hex) {
        byte[] keyBytes = new byte[hex.length() / 2];
        for (int i = 0; i < keyBytes.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hex.substring(index, index + 2), 16);
            keyBytes[i] = (byte) j;
        }
        return keyBytes;
    }
    // Decrypt the JSON data after reading it from the file
    public String decryptData(byte[] encryptedData, SecretKey secretKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes);
    }

    public SecretKey convertStringToSecretKey(String keyString, String algorithm) {
        try {
            // Convert the string to bytes
            byte[] keyBytes = keyString.getBytes("UTF-8");

            // Create a SecretKey using the bytes and the specified algorithm
            SecretKey secretKey = new SecretKeySpec(keyBytes, algorithm);

            return secretKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveJsonToFileCrypted(String filename, String jsonData) {
        // Check if external storage is writable
        if (isExternalStorageWritable()) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(dir,filename );

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(jsonData);
                writer.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity4.this, "JSON Crypted saved to Downloads folder", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity4.this, "External storage is not writable.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
