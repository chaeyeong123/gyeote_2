package com.baz.gyeote.med;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MedDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "gyeote_med.db";
    private static final int DB_VERSION = 3;

    public MedDbHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE med_schedule ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, hour INTEGER NOT NULL, minute INTEGER NOT NULL, "
                + "days_mask INTEGER NOT NULL)");
        // 같은 일정 + 같은 날짜는 한 번만 기록할 수 있게 UNIQUE (12번 중복 입력 처리)
        db.execSQL("CREATE TABLE med_log ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "schedule_id INTEGER NOT NULL, day_key TEXT NOT NULL, taken_at INTEGER NOT NULL, "
                + "UNIQUE(schedule_id, day_key))");
        createFamilyAlertTable(db);
        createGreetingTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) createFamilyAlertTable(db);
        if (oldVersion < 3) createGreetingTable(db);   // 22번: 안부 표 추가
    }
    private void createFamilyAlertTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE family_alert ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "schedule_id INTEGER NOT NULL, day_key TEXT NOT NULL, "
                + "created_at INTEGER NOT NULL, resolved_at INTEGER, "
                + "UNIQUE(schedule_id, day_key))");
    }

    private void createGreetingTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE greeting ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "body TEXT NOT NULL, sent_at INTEGER NOT NULL, "
                + "received_at INTEGER, confirmed_at INTEGER)");
    }
}
