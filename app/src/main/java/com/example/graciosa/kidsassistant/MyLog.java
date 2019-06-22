package com.example.graciosa.kidsassistant;

import android.util.Log;

public class MyLog {

    public static void d (String tag, String log){
        Log.d(prependAppTag(tag), log);
    }

    static private String prependAppTag(String tag){
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.KIDS_ASSISTANT + ": " + tag);
        return sb.toString();
    }
}
