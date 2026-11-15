package com.baz.gyeote.med;

import java.util.Locale;

/** 약 일정 한 줄: 약 이름 + 시간 + 반복 요일 (9번) */
public class MedSchedule {
    public static final int EVERY_DAY = 0x7F;   // 일~토 7개 비트가 모두 켜진 값

    public long id;         // 0이면 아직 저장 안 된 새 일정
    public String name;
    public int hour;        // 0~23
    public int minute;      // 0~59
    public int daysMask;    // 일(0)~토(6) 비트

    public MedSchedule() { daysMask = EVERY_DAY; }

    public boolean isDayEnabled(int dayIndex) { return (daysMask & (1 << dayIndex)) != 0; }

    public String timeText() { return timeText(hour, minute); }

    public static String timeText(int hour, int minute) {
        String ap = hour < 12 ? "오전" : "오후";
        int h = hour % 12;
        if (h == 0) h = 12;
        return String.format(Locale.KOREA, "%s %d:%02d", ap, h, minute);
    }

    public String daysText() {
        if (daysMask == EVERY_DAY) return "매일";
        String[] names = {"일", "월", "화", "수", "목", "금", "토"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (isDayEnabled(i)) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(names[i]);
            }
        }
        return sb.toString();
    }
}
