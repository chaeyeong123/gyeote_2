package com.baz.gyeote.med;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MedAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long id = intent.getLongExtra(AlarmScheduler.EXTRA_ID, 0);
        MedRepository repo = MedRepository.get(context);
        long now = System.currentTimeMillis();

        if (AlarmScheduler.ACTION_TAKEN.equals(action)) {

            repo.confirm(id, MedRepository.dateKey(now), now);
            NotificationHelper.cancel(context, id);
            AlarmScheduler.cancelReminder(context, id);          // 남은 재알림 취소
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
            if (repo.getTakenAt(id, MedRepository.dateKey(now)) == null) {   // 이미 먹었으면 울리지 X
                NotificationHelper.show(context, (int) id, "약 드실 시간이에요",
                        s.name + " · " + s.timeText(), id);
            }
        }
    }
}
