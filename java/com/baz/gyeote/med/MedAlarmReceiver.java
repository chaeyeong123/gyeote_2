package com.baz.gyeote.med;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 알람 시각이 되었을 때, 알림의 '먹었어요' 버튼을 눌렀을 때 호출돼요. */
public class MedAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long id = intent.getLongExtra(AlarmScheduler.EXTRA_ID, 0);
        MedRepository repo = MedRepository.get(context);
        long now = System.currentTimeMillis();

        if (AlarmScheduler.ACTION_TAKEN.equals(action)) {
            // 알림에서 바로 '먹었어요' (이미 기록돼 있으면 중복이라 무시돼요)
            repo.confirm(id, MedRepository.dateKey(now), now);
            NotificationHelper.cancel(context, id);
            return;
        }

        if (AlarmScheduler.ACTION_ALARM.equals(action)) {
            if (intent.getBooleanExtra(AlarmScheduler.EXTRA_TEST, false)) {
                NotificationHelper.show(context, NotificationHelper.TEST_NOTIFICATION_ID,
                        "시험 알림", "알림 화면과 소리가 잘 나오나요?", 0);
                return;
            }
            MedSchedule s = repo.getById(id);
            if (s == null) return;                      // 삭제된 일정이면 끝
            AlarmScheduler.schedule(context, s);        // 다음 반복 알림을 바로 예약
            if (repo.getTakenAt(id, MedRepository.dateKey(now)) == null) {   // 이미 먹었으면 울리지 않아요
                NotificationHelper.show(context, (int) id, "약 드실 시간이에요",
                        s.name + " · " + s.timeText(), id);
            }
        }
    }
}
