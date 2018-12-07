package com.rockchip.vr.globalactions.util;

import java.io.UnsupportedEncodingException;

/**
 * Created by yhc on 16-7-18.
 */
public class SubString {

    /**
     * 判断一个字符是Ascill字符还是其它字符（如汉，日，韩文字符）
     *
     * @param c
     * @return
     */
    public static boolean isLetter(char c) {
        int k = 0x80;
        return (c / k) == 0;
    }

    public static int length(String s) {
        if (s == null) {
            return 0;
        }
        char[] c = s.toCharArray();
        int len = 0;
        for (int i = 0; i < c.length; i++) {
            len++;
            if (!isLetter(c[i])) {
                len++;
            }
        }
        return len;
    }

    public static String getSubString(String str, int subSLength)
            throws UnsupportedEncodingException {
        if (str == null) {
            return "";
        }
        int tempSubLength = subSLength;
        String subStr = str.substring(0, str.length()<subSLength ? str.length() : subSLength);
        int subStrByetsL = subStr.getBytes("GBK").length;
        while (subStrByetsL > tempSubLength){
            int subSLengthTemp = --subSLength;
            subStr = str.substring(0, subSLengthTemp>str.length() ? str.length() : subSLengthTemp);
            subStrByetsL = subStr.getBytes("GBK").length;
        }
        return subStr;
    }
}
