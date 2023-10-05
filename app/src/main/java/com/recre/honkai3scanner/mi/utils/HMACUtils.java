package com.recre.honkai3scanner.mi.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMACUtils {

    public static String sha256HMAC(String str1, String str2) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(str2.getBytes(), "HmacSHA256"));
            return byteArrayToHexString(mac.doFinal(str1.getBytes()));
        } catch (Exception exception) {
            return "";
        }
    }

    public static String byteArrayToHexString(byte[] paramArrayOfByte) {
        StringBuilder sb = new StringBuilder();
        for (byte b = 0; paramArrayOfByte != null && b < paramArrayOfByte.length; b++) {
            String str = Integer.toHexString(0xFF & paramArrayOfByte[b]);
            if (str.length() == 1)
                sb.append('0');
            sb.append(str);
        }
        return sb.toString().toLowerCase();
    }

}
