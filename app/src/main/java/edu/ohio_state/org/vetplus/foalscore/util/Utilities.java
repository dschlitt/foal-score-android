package edu.ohio_state.org.vetplus.foalscore.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.webkit.WebView;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by vivek on 4/10/15.
 */
public class Utilities {

    public static void changeFontSize(WebView webView, Context context) {
        int fontSize = webView.getSettings().getDefaultFontSize();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;
        float density = metrics.density;
        if(displayWidth >= 1080) {
            fontSize += 4;
        }
        if(displayWidth >= 1200) {
            fontSize += 10;
        }
        webView.getSettings().setDefaultFontSize(fontSize);
    }

    public static String createHash(String text) {
        return hash( hash(text) + "e2f7337be7aaa1de9d25d79d679e6913");
    }

    public static String hash(String text) {
        String hashResult = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes("UTF-8"));
            //System.out.println("Hash: " + toHex(hash));
            hashResult = toHex(hash);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return hashResult;
    }
    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }

    public static int equalsIndex (String temp) {
        for (int i = 0; i<temp.length();i++) {
            if (temp.charAt(i)=='=') {
                return i;

            }
        }
        return -1;
    }

}
