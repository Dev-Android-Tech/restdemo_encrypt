package com.example.restdemo;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class MyHttpClient {

    private OkHttpClient client;

    public MyHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        client = clientBuilder.build();
    }

    public void compareHttpResultWithNameOfKey(String url, final String NameOfKey, final HttpResultCallback callback) {


        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onResult(false);
                    return;
                }

                // Get the response body as a string
                String responseBody = response.body().string();

                // Compare the response with NameOfKey
                boolean result = responseBody.equals(NameOfKey);

                callback.onResult(result);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onResult(false);
            }
        });
    }

    public interface HttpResultCallback {
        void onResult(boolean areEqual);
    }
}
