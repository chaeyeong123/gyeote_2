package com.baz.gyeote.med;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/** 화면 위에 뜨는 알림(소리 포함)을 만들어요 (11번). */
public final class NotificationHelper {
    public static final String CHANNEL_ID = "med_alarm";
    public static final int TEST_NOTIFICATION_ID = 99999;

    private NotificationHelper() { }

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "복약 알림",
                    NotificationManager.IMPORTANCE_HIGH);   // 소리와 화면 팝업
            ch.setDescription("약 먹을 시간을 알려드려요");
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);
        }
    }

    public static boolean canPost(Context ctx) {
        return Build.VERSION.SDK_INT < 33
                || ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** 안드로이드 13 이상에서는 알림 권한을 물어봐야 해요. 화면(Activity)에서 한 번 불러요. */
    public static void requestPermissionIfNeeded(Activity activity, ActivityResultLauncher<String> launcher) {
        if (!canPost(activity)) launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    /** scheduleIdForAction이 0보다 크면 '먹었어요' 버튼이 알림에 붙어요. */
    @SuppressLint("MissingPermission")
    public static void show(Context ctx, int notificationId, String title, String text, long scheduleIdForAction) {
        if (!canPost(ctx)) return;
        ensureChannel(ctx);

        Intent open = new Intent(ctx, ElderMedActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(ctx, notificationId, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)   // 소리 + 진동
                .setAutoCancel(true)
                .setContentIntent(openPi);

        if (scheduleIdForAction > 0) {
            Intent taken = new Intent(ctx, MedAlarmReceiver.class);
            taken.setAction(AlarmScheduler.ACTION_TAKEN);
            taken.putExtra(AlarmScheduler.EXTRA_ID, scheduleIdForAction);
            PendingIntent takenPi = PendingIntent.getBroadcast(ctx, (int) scheduleIdForAction + 100000, taken,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            b.addAction(0, "먹었어요", takenPi);
        }
        NotificationManagerCompat.from(ctx).notify(notificationId, b.build());
    }

    public static void cancel(Context ctx, long id) {
        NotificationManagerCompat.from(ctx).cancel((int) id);
    }
}
