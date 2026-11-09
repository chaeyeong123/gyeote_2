package com.baz.gyeote.med;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** 폰 안의 작은 데이터베이스(SQLite). 외부 라이브러리 없이 안드로이드 기본 기능만 써요. */
public class MedDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "gyeote_med.db";
    private static final int DB_VERSION = 1;

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS med_log");
        db.execSQL("DROP TABLE IF EXISTS med_schedule");
        onCreate(db);
    }
}
