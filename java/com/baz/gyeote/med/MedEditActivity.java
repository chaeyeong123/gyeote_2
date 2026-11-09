package com.baz.gyeote.med;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityMedEditBinding;

/** 약 일정 등록 / 수정 / 삭제 화면 (9번) */
public class MedEditActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "id";

    private ActivityMedEditBinding binding;
    private MedRepository repo;
    private CheckBox[] dayBoxes;
    private long editId = 0;       // 0이면 새로 등록
    private int hour = 8;
    private int minute = 0;
    private boolean syncing = false;   // '매일'과 요일 체크박스가 서로 무한 반응하지 않게

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMedEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtil.applyInsets(binding.main);

        repo = MedRepository.get(this);
        dayBoxes = new CheckBox[]{binding.chkSun, binding.chkMon, binding.chkTue, binding.chkWed,
                binding.chkThu, binding.chkFri, binding.chkSat};

        editId = getIntent().getLongExtra(EXTRA_ID, 0);
        if (editId > 0) {
            MedSchedule s = repo.getById(editId);
            if (s == null) { finish(); return; }
            binding.textTitle.setText("복약 일정 수정");
            binding.editName.setText(s.name);
            hour = s.hour;
            minute = s.minute;
            syncing = true;
            for (int i = 0; i < 7; i++) dayBoxes[i].setChecked(s.isDayEnabled(i));
            binding.chkEveryday.setChecked(s.daysMask == MedSchedule.EVERY_DAY);
            syncing = false;
            binding.btnDelete.setVisibility(View.VISIBLE);
        } else {
            setAllDays(true);
        }
        updateTimeButton();

        binding.btnTime.setOnClickListener(v ->
                new TimePickerDialog(this, (view, h, m) -> {
                    hour = h;
                    minute = m;
                    updateTimeButton();
                }, hour, minute, false).show());

        binding.chkEveryday.setOnCheckedChangeListener((btn, checked) -> {
            if (!syncing) setAllDays(checked);
        });
        for (CheckBox c : dayBoxes) c.setOnCheckedChangeListener((btn, checked) -> syncEveryday());

        binding.btnSave.setOnClickListener(v -> save());
        binding.btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void updateTimeButton() {
        binding.btnTime.setText(MedSchedule.timeText(hour, minute) + "  (눌러서 바꾸기)");
    }

    private void setAllDays(boolean on) {
        syncing = true;
        for (CheckBox c : dayBoxes) c.setChecked(on);
        binding.chkEveryday.setChecked(on);
        syncing = false;
    }

    private void syncEveryday() {
        if (syncing) return;
        boolean all = true;
        for (CheckBox c : dayBoxes) if (!c.isChecked()) all = false;
        syncing = true;
        binding.chkEveryday.setChecked(all);
        syncing = false;
    }

    private void save() {
        String name = binding.editName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "약 이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        int mask = 0;
        for (int i = 0; i < 7; i++) if (dayBoxes[i].isChecked()) mask |= (1 << i);
        if (mask == 0) {
            Toast.makeText(this, "요일을 하나 이상 골라 주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        MedSchedule s = new MedSchedule();
        s.id = editId;
        s.name = name;
        s.hour = hour;
        s.minute = minute;
        s.daysMask = mask;
        s.id = repo.save(s);

        AlarmScheduler.cancel(this, s.id);     // 시간을 바꿨을 수 있으니 예전 알람은 지우고
        AlarmScheduler.schedule(this, s);      // 새 알람을 예약해요 (11번)
        Toast.makeText(this, "저장했어요", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setMessage("이 약 일정을 삭제할까요?")
                .setPositiveButton("삭제", (d, w) -> {
                    AlarmScheduler.cancel(this, editId);
                    NotificationHelper.cancel(this, editId);
                    repo.delete(editId);
                    Toast.makeText(this, "삭제했어요", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
