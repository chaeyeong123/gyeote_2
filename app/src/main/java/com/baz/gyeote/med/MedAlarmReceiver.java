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
            String today = MedRepository.dateKey(now);
            if (repo.getTakenAt(id, today) == null) {   // 이미 먹었으면 울리지 않아요
                NotificationHelper.show(context, (int) id, "약 드실 시간이에요",
                        s.name + " · " + s.timeText(), id);
                AlarmScheduler.scheduleReminder(context, id, 1, today,
                        MedSettings.remindIntervalMillis(context));
            }
        }
        if (AlarmScheduler.ACTION_REMIND.equals(action)) {
            String day = intent.getStringExtra(AlarmScheduler.EXTRA_DAY);
            int attempt = intent.getIntExtra(AlarmScheduler.EXTRA_ATTEMPT, 1);
            MedSchedule s = repo.getById(id);
            if (s == null || day == null) return;
            if (repo.getTakenAt(id, day) != null) return;    // 그 사이 확인했으면 더 알리지 X

            if (attempt <= MedSettings.MAX_REMINDS) {
                String text = (attempt == 1)
                        ? s.name + " · 아직 확인하지 않았어요. 알림의 '먹었어요' 버튼을 눌러 주세요."
                        : s.name + " · 약을 드셨다면 알림의 '먹었어요'나 앱의 '약 먹었어요' 큰 버튼을 눌러 주세요.";
                NotificationHelper.show(context, (int) id, "약 확인이 필요해요", text, id);
                AlarmScheduler.scheduleReminder(context, id, attempt + 1, day,
                        MedSettings.remindIntervalMillis(context));
            } else {

            }
        }
    }
}
