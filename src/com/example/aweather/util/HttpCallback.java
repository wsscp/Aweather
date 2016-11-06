package com.example.aweather.util;

public interface HttpCallback {


    void onFinish(String response);
    void onError(Exception e);
}
