package com.baz.gyeote.med;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 약 일정과 복용 기록을 저장/조회하는 곳. 어르신 화면과 가족 화면이 모두 여기서 읽음(10번)
 */
public class MedRepository {
    private static MedRepository instance;
    private final MedDbHelper helper;

    private MedRepository(Context appContext) { helper = new MedDbHelper(appContext); }

    public static synchronized MedRepository get(Context context) {
        if (instance == null) instance = new MedRepository(context.getApplicationContext());
        return instance;
    }

    //  일정 (9번)
    /** id가 0이면 새로 저장, 아니면 수정. 저장된 id를 돌려줌. */
    public long save(MedSchedule s) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", s.name);
        v.put("hour", s.hour);
        v.put("minute", s.minute);
        v.put("days_mask", s.daysMask);
        if (s.id == 0) return db.insert("med_schedule", null, v);
        db.update("med_schedule", v, "id = ?", new String[]{String.valueOf(s.id)});
        return s.id;
    }

    public void delete(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("med_log", "schedule_id = ?", new String[]{String.valueOf(id)});
            db.delete("med_schedule", "id = ?", new String[]{String.valueOf(id)});
            db.delete("family_alert", "schedule_id = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public MedSchedule getById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query("med_schedule", null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    public List<MedSchedule> getAll() {
        List<MedSchedule> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query("med_schedule", null, null, null, null, null, "hour, minute, id")) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    /** dayIndex: 일(0)~토(6). 그 요일에 먹는 약만 시간 순서로 돌려줘요. */
    public List<MedSchedule> getForDay(int dayIndex) {
        List<MedSchedule> list = new ArrayList<>();
        for (MedSchedule s : getAll()) if (s.isDayEnabled(dayIndex)) list.add(s);
        return list;
    }

    private MedSchedule fromCursor(Cursor c) {
        MedSchedule s = new MedSchedule();
        s.id = c.getLong(c.getColumnIndexOrThrow("id"));
        s.name = c.getString(c.getColumnIndexOrThrow("name"));
        s.hour = c.getInt(c.getColumnIndexOrThrow("hour"));
        s.minute = c.getInt(c.getColumnIndexOrThrow("minute"));
        s.daysMask = c.getInt(c.getColumnIndexOrThrow("days_mask"));
        return s;
    }

    // ---------- 복용 기록 (12번) ----------
    /** 그날 확인한 시각(밀리초). 아직 안 먹었으면 null. */
    public Long getTakenAt(long scheduleId, String dayKey) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query("med_log", new String[]{"taken_at"},
                "schedule_id = ? AND day_key = ?",
                new String[]{String.valueOf(scheduleId), dayKey}, null, null, null)) {
            return c.moveToFirst() ? c.getLong(0) : null;
        }
    }

    public boolean confirm(long scheduleId, String dayKey, long takenAt) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("schedule_id", scheduleId);
        v.put("day_key", dayKey);
        v.put("taken_at", takenAt);
        boolean first = db.insertWithOnConflict("med_log", null, v, SQLiteDatabase.CONFLICT_IGNORE) != -1;
        resolveAlertsFor(scheduleId, dayKey, takenAt);   // 확인했으니 가족 알림도 해제
        return first;    }

    //  가족 알림
    /** 처음 만들면 true, 같은 날 같은 일정으로 이미 만들었으면 false */
    public boolean addFamilyAlert(long scheduleId, String dayKey, long createdAt) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("schedule_id", scheduleId);
        v.put("day_key", dayKey);
        v.put("created_at", createdAt);
        return db.insertWithOnConflict("family_alert", null, v, SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    /** 아직 해제되지 않은 가족 알림 */
    public List<FamilyAlert> getOpenAlerts() {
        List<FamilyAlert> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query("family_alert", null, "resolved_at IS NULL", null, null, null, "created_at")) {
            while (c.moveToNext()) {
                FamilyAlert a = new FamilyAlert();
                a.id = c.getLong(c.getColumnIndexOrThrow("id"));
                a.scheduleId = c.getLong(c.getColumnIndexOrThrow("schedule_id"));
                a.dayKey = c.getString(c.getColumnIndexOrThrow("day_key"));
                a.createdAt = c.getLong(c.getColumnIndexOrThrow("created_at"));
                list.add(a);
            }
        }
        return list;
    }

    public void resolveAlert(long alertId, long now) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("resolved_at", now);
        db.update("family_alert", v, "id = ?", new String[]{String.valueOf(alertId)});
    }

    public void resolveAlertsFor(long scheduleId, String dayKey, long now) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("resolved_at", now);
        db.update("family_alert", v, "schedule_id = ? AND day_key = ? AND resolved_at IS NULL",
                new String[]{String.valueOf(scheduleId), dayKey});
    }
    //  날짜/시간 도우미
    public static String dateKey(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(millis));
    }

    /** 오늘 요일: 일(0)~토(6) 일부터*/
    public static int todayIndex() { return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1; }

    public static String clockText(long millis) {
        return new SimpleDateFormat("a h:mm", Locale.KOREA).format(new Date(millis));
    }
}
