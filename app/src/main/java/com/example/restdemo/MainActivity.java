package com.example.restdemo;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity{


    TextView txtString;
    Button save;
    ListView lvEmployees;
    ArrayList<Employee> list_emp = new ArrayList<Employee>();

    public String url= "https://web0.sinuslabs.net/rest_api/get_employees.php";
    String invoice_html;

    String toSave;
String responseBodyStr;
    OkHttpClient client = new OkHttpClient();
    ActivityResultLauncher<Intent> createInvoiceActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        save = findViewById(R.id.button);
        lvEmployees = (ListView) findViewById(R.id.listemp);

        invoice_html = toSave;
        Log.i("IIIIIII", "TTTTTTTT");
        createInvoiceActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result-> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Uri uri = null;
                        if (result.getData() != null) {
                            uri = result.getData().getData();
                            createInvoice(uri);
                            Log.i("IIIIIII", uri.toString());
                            // Perform operations on the document using its URI.
                        }
                    }
                });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFile("employee");
            }
        });









        //getDataFromUrl(url);

        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };

        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.i("VVVVVVVV", "Verif");
                    return true;
                }

            });



            //Now start the query
//            Request get = new Request.Builder()
//                    .url(url)
//                    .build();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client = builder.build();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("id_user", "1")
                            .build();

                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            Log.i("EEEEEEEEE", e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) {
                            try {
                                ResponseBody responseBody = response.body();

                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                }


                                //txtString.setText(responseBody.string());
                                responseBodyStr = responseBody.string();

                                ResponseBody responseBodyCopy = response.peekBody(Long.MAX_VALUE);
                                responseBodyStr = responseBodyCopy.string();

                                toSave = responseBodyStr;
                                try {
                                    JSONObject obj = new JSONObject(responseBodyStr);
                                    JSONArray jsonArray;
                                    jsonArray = obj.getJSONArray("employee");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Employee empe = new Employee(Integer.parseInt(jsonArray.getJSONObject(i).getString("id")),
                                                jsonArray.getJSONObject(i).getString("firstname"),
                                                jsonArray.getJSONObject(i).getString("salary"));
                                        list_emp.add(empe);

                                    }
                                    Log.i("DDDDDDD", list_emp.size() + "ere");

                                    //response.close();

                                    EmployeeAdapter adapter = new EmployeeAdapter(getBaseContext(), list_emp);
                                    lvEmployees.setAdapter(adapter);
                                } catch (Throwable t) {
                                    Log.e("My App", "Could not parse malformed JSON: \"" + responseBody.string() + "\"");
                                }




                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }).start();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }







    }

    private void createFile(String title) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/Documents"));
        }
        createInvoiceActivityResultLauncher.launch(intent);
    }

    private void createInvoice(Uri uri) {
        try {
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(uri, "w");
            if (pfd != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                fileOutputStream.write(invoice_html.getBytes());
                fileOutputStream.close();
                pfd.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String error = ""; // string field
    private String getDataFromUrl(String demoIdUrl) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        String result = null;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

            int resCode;
            InputStream in;
            try {
                URL url = new URL(demoIdUrl);
                URLConnection urlConn = url.openConnection();

                HttpsURLConnection httpsConn = (HttpsURLConnection) urlConn;
                httpsConn.setAllowUserInteraction(false);
                httpsConn.setInstanceFollowRedirects(true);
                httpsConn.setRequestMethod("GET");
                httpsConn.connect();
                resCode = httpsConn.getResponseCode();

                if (resCode == HttpURLConnection.HTTP_OK) {
                    in = httpsConn.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            in, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    in.close();
                    result = sb.toString();
                    Toast.makeText(getBaseContext(),result,Toast.LENGTH_LONG).show();

                } else {
                    error += resCode;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;

    }
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    class EmployeeAdapter extends BaseAdapter
    {
        Context context;
        ArrayList<Employee>  tabEmp;

        public EmployeeAdapter(Context context, ArrayList<Employee> tabEmp) {
            this.context = context;
            this.tabEmp = tabEmp;

        }

        @Override
        public int getCount() {
            return tabEmp.size();
        }

        @Override
        public Object getItem(int position) {
            return tabEmp.get(position);
        }

        @Override
        public long getItemId(int position) {
            return tabEmp.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.item_emp,parent, false);
            TextView tvNom = (TextView)view.findViewById(R.id.name);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvNom.setText(tabEmp.get(position).getName());
                }
            });
            // tvNom.setText(tabEmp.get(position).getName());
            return view;
        }
    }


}


