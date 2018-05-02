package com.yaoh.remoteclient.utils;

/**
 * Created by yaoh on 2018/4/27.
 */

public class StringUtil {

    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }

//    public static byte[] string2ByteHex(String value){
//
//    }

}
