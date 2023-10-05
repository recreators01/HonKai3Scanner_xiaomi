package com.recre.honkai3scanner.mi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Tools {

    //解析二维码参数
    public static Map<String, Object> parseQRCodeUrl(String uri) {
        HashMap<String, Object> map = new HashMap<>();
        String substring = uri.substring(uri.indexOf("?") + 1);
        if (!substring.equals("")) {
            for (String s : substring.split("&")) {
                String[] split = s.split("=");
                map.put(split[0], split[1]);
            }
        }
        return map;
    }


    //sign创建
    public static String signNew(Map<String, Object> map, String str) {
    TreeMap<String, Object> treeMap = new TreeMap<>(map);
    StringBuilder stringBuilder = new StringBuilder();
        for (String str1 : treeMap.keySet()) {
        stringBuilder.append(str1);
        stringBuilder.append("=");
        stringBuilder.append(map.get(str1));
        stringBuilder.append("&");
    }
        return HMACUtils.sha256HMAC(stringBuilder.substring(0, stringBuilder.length() - 1), str);
    }
}