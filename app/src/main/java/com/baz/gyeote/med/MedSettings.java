package com.baz.gyeote.med;

import android.content.Context;
import android.content.SharedPreferences;


public final class MedSettings {
    private static final String PREF = "med_settings";
    private static final String KEY_FAST = "fast_mode";

    // 재알림 횟수. 그다음 차례에 가족에게 알림
    public static final int MAX_REMINDS = 2;

    private MedSettings() { }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static boolean isFastMode(Context ctx) { return prefs(ctx).getBoolean(KEY_FAST, false); }

    public static void setFastMode(Context ctx, boolean on) {
        prefs(ctx).edit().putBoolean(KEY_FAST, on).apply();
    }

    // 평소 10 분 시험 1분
    public static long remindIntervalMillis(Context ctx) {
        return isFastMode(ctx) ? 60_000L : 10 * 60_000L;
    }
}