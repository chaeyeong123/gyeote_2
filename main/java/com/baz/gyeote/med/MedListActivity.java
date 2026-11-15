package com.baz.gyeote.med;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityMedListBinding;
import com.baz.gyeote.databinding.ItemMedBinding;

import java.util.List;

/** 가족 화면: 등록한 약 일정 목록 (9, 10번). 누르면 수정/삭제 화면으로 가요. */
public class MedListActivity extends AppCompatActivity {
    private ActivityMedListBinding binding;
    private MedRepository repo;
    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMedListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtil.applyInsets(binding.main);

        repo = MedRepository.get(this);
        NotificationHelper.ensureChannel(this);
        NotificationHelper.requestPermissionIfNeeded(this, notifPermission);

        binding.btnAdd.setOnClickListener(v -> startActivity(new Intent(this, MedEditActivity.class)));
        binding.btnTest.setOnClickListener(v -> {
            AlarmScheduler.scheduleTest(this, 10_000);
            Toast.makeText(this, "10초 뒤에 알림이 와요. 앱을 닫고 기다려 보세요.", Toast.LENGTH_LONG).show();
        });
        binding.btnExactAllow.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName())));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AlarmScheduler.rescheduleAll(this);
        binding.btnExactAllow.setVisibility(AlarmScheduler.canScheduleExact(this) ? View.GONE : View.VISIBLE);
        refreshList();
    }

    private void refreshList() {
        binding.listContainer.removeAllViews();
        List<MedSchedule> list = repo.getAll();
        binding.textEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        String today = MedRepository.dateKey(System.currentTimeMillis());
        int dayIndex = MedRepository.todayIndex();

        for (MedSchedule s : list) {
            ItemMedBinding item = ItemMedBinding.inflate(getLayoutInflater(), binding.listContainer, false);
            item.textName.setText(s.name);
            item.textTime.setText(s.timeText() + " · " + s.daysText());

            if (!s.isDayEnabled(dayIndex)) {
                item.textStatus.setText("오늘은 먹지 않는 날이에요");
            } else {
                Long takenAt = repo.getTakenAt(s.id, today);   // 어르신 화면에서 누른 기록이 여기에도 보여요
                item.textStatus.setText(takenAt == null
                        ? "오늘 아직 확인 전이에요"
                        : "오늘 " + MedRepository.clockText(takenAt) + "에 먹었어요");
            }

            final long id = s.id;
            item.getRoot().setOnClickListener(v -> {
                Intent i = new Intent(this, MedEditActivity.class);
                i.putExtra(MedEditActivity.EXTRA_ID, id);
                startActivity(i);
            });
            binding.listContainer.addView(item.getRoot());
        }
    }
}
