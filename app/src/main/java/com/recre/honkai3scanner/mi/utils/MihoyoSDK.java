package com.recre.honkai3scanner.mi.utils;

import android.util.Log;

import com.recre.honkai3scanner.mi.data.UserData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MihoyoSDK {

    private final static String TAG = "MihoyoSDK";
    private final static String BH3_APP_KEY = "0ebc517adb1b62c6b408df153331f9aa";

    private static Map<String, Object> signNewMap;

    public static int status = 0;  //0,未登录 1,游戏登录 2,扫码成功 3,登录成功

    public static void verify(String deviceId, String uid, String session) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("device", deviceId);
        hashMap.put("app_id", 1);
        hashMap.put("channel_id", UserData.channelId);
        String data = "{\"session\":\"" +
                session +
                "\",\"uid\":\"" +
                uid +
                "\"}";
        hashMap.put("data", data);
        String sign = Tools.signNew(hashMap, BH3_APP_KEY);

        ArrayList<String> arrayList = new ArrayList<>(hashMap.keySet());
        Collections.sort(arrayList);

        JSONObject loginJson = new JSONObject();
        try {
            for (String str : arrayList) {
                loginJson.put(str, hashMap.get(str));
            }
            loginJson.put("sign", sign);
        } catch (JSONException e) {
            //记录日志
            Log.e(TAG, e.getMessage(), e);
            return;
        }
        JSONObject loginResultJson = HttpUtils.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login", loginJson.toString());
        if (loginResultJson==null){
            return;
        }
        try {
            JSONObject dataJson = loginResultJson.getJSONObject("data");
            UserData.comboId = dataJson.getString("combo_id");
            UserData.openId = dataJson.getString("open_id");
            UserData.comboToken = dataJson.getString("combo_token");
            UserData.accountType = dataJson.getString("account_type");
        } catch (JSONException e) {
            //记录日志
            Log.e(TAG, e.getMessage(), e);
            return;
        }
        status = 1;
    }


    public static void scanLogin(Object ticket, String deviceId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("app_id", 1);
        hashMap.put("ticket", ticket);
        hashMap.put("device", deviceId);
        hashMap.put("ts", System.currentTimeMillis());

        signNewMap = new HashMap<>(hashMap);

        hashMap.put("sign", Tools.signNew(hashMap, BH3_APP_KEY));
        if (HttpUtils.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/panda/qrcode/scan", new JSONObject(hashMap).toString()) == null) {
            return;
        }

        status = 2;
    }

    public static void scanLoginConfirm(Object ticket, String deviceId) {
        JSONObject rawJson = new JSONObject();
        JSONObject payloadJson = new JSONObject();
        JSONObject extJson = new JSONObject();
        JSONObject dataJson = new JSONObject();
        JSONObject confirmJson = new JSONObject();

        try {
            rawJson.put("heartbeat", UserData.heartBeat)
                    .put("open_id", UserData.openId)
                    .put("device_id", deviceId)
                    .put("app_id", "1")
                    .put("channel_id", UserData.channelId)
                    .put("asterisk_name", "HonKai3Scanner")
                    .put("combo_id", Long.valueOf(UserData.comboId))
                    .put("account_type", UserData.accountType)
                    .put("combo_token", UserData.comboToken);


            String dispatch = HttpUtils.sendGet("https://outer-dp-hun02.bh3.com/query_gameserver?version=" + bh3Version() + "_gf_android_xiaomi&t=" + (System.currentTimeMillis() / 1000));

            dataJson.put("accountType", UserData.miAccountType)
                    .put("accountID", UserData.openId)
                    .put("dispatch", dispatch)
                    .put("accountToken", UserData.comboToken);

            extJson.put("data", dataJson);

            payloadJson.put("raw", rawJson.toString())
                    .put("proto", "Combo")
                    .put("ext", extJson.toString().replace("\\", ""));

            confirmJson.put("device", deviceId)
                    .put("app_id", 1)
                    .put("ts", System.currentTimeMillis())
                    .put("ticket", ticket)
                    .put("payload", payloadJson);

            signNewMap.put("payload", payloadJson);
            String sign = Tools.signNew(signNewMap, BH3_APP_KEY);
            confirmJson.put("sign", sign);
        } catch (JSONException e) {
            Log.e(TAG,e.getMessage(),e);
            return;
        }
        String replace = confirmJson.toString().replace("\\/", "/");
        if (HttpUtils.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/panda/qrcode/confirm", replace)==null){
            return;
        }
        status = 3;
    }

    public static String bh3Version() {
        String body = HttpUtils.sendGet("https://bh3-launcher-static.mihoyo.com/bh3_cn/mdk/launcher/api/resource?launcher_id=4");
        if (body != null) {
            try {
                JSONObject jsonObject = new JSONObject(body);
                if (jsonObject.getInt("retcode") == 0) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONObject game = data.getJSONObject("game");
                    JSONObject latest = game.getJSONObject("latest");
                    return latest.getString("version");
                }
            } catch (JSONException e) {
                //记录日志
                Log.e(TAG,e.getMessage(),e);
            }
        }
        return null;
    }
}
