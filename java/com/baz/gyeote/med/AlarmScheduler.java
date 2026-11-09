package com.baz.gyeote.med;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/** 정해진 시각에 알림을 울리도록 폰의 알람(AlarmManager)에 예약해요 (11번). */
public final class AlarmScheduler {
    public static final String ACTION_ALARM = "com.baz.gyeote.ACTION_MED_ALARM";
    public static final String ACTION_TAKEN = "com.baz.gyeote.ACTION_MED_TAKEN";
    public static final String EXTRA_ID = "schedule_id";
    public static final String EXTRA_TEST = "is_test";

    private AlarmScheduler() { }

    /** 지금 이후로 가장 가까운 알림 시각. 켜진 요일이 없으면 -1. */
    public static long nextTriggerMillis(MedSchedule s, long nowMillis) {
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < 8; i++) {
            c.setTimeInMillis(nowMillis);
            c.add(Calendar.DAY_OF_YEAR, i);
            c.set(Calendar.HOUR_OF_DAY, s.hour);
            c.set(Calendar.MINUTE, s.minute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            int dayIndex = c.get(Calendar.DAY_OF_WEEK) - 1;
            if (s.isDayEnabled(dayIndex) && c.getTimeInMillis() > nowMillis) return c.getTimeInMillis();
        }
        return -1;
    }

    /** 오늘 날짜에 이 일정의 시각(밀리초) */
    public static long todayTimeMillis(MedSchedule s, long nowMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(nowMillis);
        c.set(Calendar.HOUR_OF_DAY, s.hour);
        c.set(Calendar.MINUTE, s.minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static void schedule(Context ctx, MedSchedule s) {
        long at = nextTriggerMillis(s, System.currentTimeMillis());
        if (at < 0) return;
        setAlarm(ctx, at, pendingFor(ctx, s.id, false));
    }

    public static void cancel(Context ctx, long scheduleId) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingFor(ctx, scheduleId, false));
    }

    /** 앱을 열거나 폰을 다시 켰을 때 모든 일정의 알림을 다시 예약 */
    public static void rescheduleAll(Context ctx) {
        for (MedSchedule s : MedRepository.get(ctx).getAll()) schedule(ctx, s);
    }

    /** 시험용: 몇 초 뒤에 알림이 오는지 확인 */
    public static void scheduleTest(Context ctx, long delayMillis) {
        setAlarm(ctx, System.currentTimeMillis() + delayMillis, pendingFor(ctx, -1, true));
    }

    /** 안드로이드 12 이상에서는 '정확한 알람' 허용이 필요해요. */
    public static boolean canScheduleExact(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        return am.canScheduleExactAlarms();
    }

    private static void setAlarm(Context ctx, long at, PendingIntent pi) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (canScheduleExact(ctx)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);   // 허용 전에는 몇 분 오차가 있을 수 있어요
        }
    }

    private static PendingIntent pendingFor(Context ctx, long scheduleId, boolean test) {
        Intent i = new Intent(ctx, MedAlarmReceiver.class);
        i.setAction(ACTION_ALARM);
        i.putExtra(EXTRA_ID, scheduleId);
        i.putExtra(EXTRA_TEST, test);
        int requestCode = test ? Integer.MAX_VALUE : (int) scheduleId;   // 일정마다 다른 번호
        return PendingIntent.getBroadcast(ctx, requestCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
