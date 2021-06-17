package com.example.downloadfiledemo.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class WebUtil {

    public static void httpUrlConnection(String address ,HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                try{
                    URL url=new URL(address);
                    connection= (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder sb=new StringBuilder();
                    while ((line=br.readLine())!=null){
                        sb.append(line);
                    }
                    if (listener!=null){
                        listener.onFinish(sb.toString());
                    }
                }catch (Exception e){
                    if (listener!=null){
                        listener.onError(e);
                    }
                }finally {
                    if (connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void OkhttpConnection(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url(address)
                .build();

        client.newCall(request).enqueue(callback);
    }
    public interface HttpCallbackListener{
        public void onFinish(String response);
        public void onError(Exception e);
    }
}
