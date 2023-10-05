package com.recre.honkai3scanner.mi.utils;

import android.util.Log;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {

    private static final String TAG = "HttpUtils";
    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient();


    public static JSONObject sendPost(String url, String body)  {
        RequestBody requestBody = RequestBody.create(body,JSON);
        Request request = new Request.Builder().header("accept", "*/*").header("Connection","Keep-Alive").url(url).post(requestBody).build();
        try (Response response = client.newCall(request).execute()) {
            String string = response.body().string();
            JSONObject jsonObject = new JSONObject(string);
            if (jsonObject.getInt("retcode")==0) {
                return jsonObject;
            }
            Log.e(TAG,"retcode返回值错误");
        } catch (Exception e) {
            //记录日志
            Log.e(TAG,e.getMessage(),e);
        }
        return null;
    }

    public static String sendGet(String url)  {
        Request request = new Request.Builder().header("Connection","Keep-Alive").url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception e) {
            //记录日志
            Log.e(TAG,e.getMessage(),e);
        }
        return null;
    }

}
