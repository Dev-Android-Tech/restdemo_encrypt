package com.example.restdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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

public class Main3Activity extends AppCompatActivity {

    private ListView employeeListView;
    private Button fetchAndSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        employeeListView = findViewById(R.id.employeeListView);
        fetchAndSaveButton = findViewById(R.id.fetchAndSaveButton);

        fetchAndSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEmployeeDataAndSave();
            }
        });
    }

    private void fetchEmployeeDataAndSave() {
        // Create a custom TrustManager that trusts all certificates
        TrustManager[] trustAllCertificates = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // No client verification required
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // Accept all server certificates without verification
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};

        try {
            // Create an SSLContext with the custom TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

            // Set the SSLContext as the default SSL socket factory for OkHttpClient
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCertificates[0]);

            clientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.i("VVVVVVVV", "Verif");
                    return true;
                }

            });

            OkHttpClient client = clientBuilder.build();

            // Define your API URL
            String apiUrl = "https://web0.sinuslabs.net/rest_api/get_employees.php";

            // Create a request to fetch employee data
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id_user", "1")
                    .build();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(requestBody)
                    .build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main3Activity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String jsonData = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(jsonData);
                            JSONArray employeeArray = jsonObject.getJSONArray("employee");

                            final ArrayList<String> employeeNames = new ArrayList<>();
                            for (int i = 0; i < employeeArray.length(); i++) {
                                JSONObject employee = employeeArray.getJSONObject(i);
                                String firstName = employee.getString("firstname");
                                employeeNames.add(firstName);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Toast.makeText(Main3Activity.this, "Message server"+jsonObject.getString("mesage"), Toast.LENGTH_SHORT).show();
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(Main3Activity.this,
                                            android.R.layout.simple_list_item_1, employeeNames);
                                    employeeListView.setAdapter(adapter);
                                }
                            });

                            // Save JSON to a file
                            saveJsonToFile(jsonData);
                            saveJsonToFileCrypted(jsonData);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveJsonToFile(String jsonData) {
        // Check if external storage is writable
        if (isExternalStorageWritable()) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(dir, "employee_data.json");

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(jsonData);
                writer.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Main3Activity.this, "JSON saved to Downloads folder", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Main3Activity.this, "External storage is not writable.", Toast.LENGTH_SHORT).show();
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

    // Decrypt the JSON data after reading it from the file
    public String decryptData(byte[] encryptedData, SecretKey secretKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes);
    }

    private void saveJsonToFileCrypted(String jsonData) {
        try {
            // Generate an AES key
            SecretKey secretKey = generateAESKey();

            // Encrypt the JSON data
            byte[] encryptedData = encryptData(jsonData, secretKey);

            // Save the encrypted data to a file
            if (isExternalStorageWritable()) {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(dir, "employee_data_encrypted");

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(encryptedData);
                    fileOutputStream.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main3Activity.this, "Encrypted JSON saved to Downloads folder", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Main3Activity.this, "External storage is not writable.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
