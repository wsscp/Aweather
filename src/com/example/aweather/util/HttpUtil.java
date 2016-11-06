package com.example.aweather.util;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class HttpUtil {


    //�ӷ�������ȡ���ݣ���һ�д����Ͻ��ͱȽ���ϸ��
    public static void sendHttpRequest(final String address, final HttpCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream inputStream = connection.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    if (callback != null) {
                        callback.onFinish(builder.toString());
                    }

                } catch (Exception e) {
                    if (callback != null)
                        callback.onError(e);
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
            }
        }).start();
    }

}
