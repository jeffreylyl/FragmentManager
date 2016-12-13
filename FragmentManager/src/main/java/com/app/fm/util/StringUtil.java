package com.app.fm.util;

import android.content.Context;

public class StringUtil {
    private static StringUtil instance;
    private Context mContext;

    private StringUtil(Context context) {
        mContext = context;
    }

    public static StringUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (StringUtil.class) {
                if (instance == null) {
                    instance = new StringUtil(context);
                }
            }
        }
        return instance;
    }

    public String getString(int paramInt) {
        return mContext.getResources().getString(paramInt);
    }
}