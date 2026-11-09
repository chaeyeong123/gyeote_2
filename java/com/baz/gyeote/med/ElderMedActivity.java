package com.baz.gyeote.med;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityElderMedBinding;
import com.baz.gyeote.databinding.ItemDoseBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** 어르신 화면: 오늘 먹을 약 + '약 먹었어요' 버튼 (10, 12번) */
public class ElderMedActivity extends AppCompatActivity {
    private ActivityElderMedBinding binding;
    private MedRepository repo;
    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityElderMedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtil.applyInsets(binding.main);

        repo = MedRepository.get(this);
        NotificationHelper.ensureChannel(this);
        NotificationHelper.requestPermissionIfNeeded(this, notifPermission);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AlarmScheduler.rescheduleAll(this);
        refresh();     // 알림의 '먹었어요'로 기록된 것도 여기서 바로 반영돼요
    }

    private void refresh() {
        binding.doseContainer.removeAllViews();
        long now = System.currentTimeMillis();
        String today = MedRepository.dateKey(now);
        binding.textDate.setText(new SimpleDateFormat("M월 d일 EEEE", Locale.KOREA).format(new Date(now)));

        List<MedSchedule> list = repo.getForDay(MedRepository.todayIndex());
        binding.textEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        for (MedSchedule s : list) {
            ItemDoseBinding item = ItemDoseBinding.inflate(getLayoutInflater(), binding.doseContainer, false);
            item.textDoseName.setText(s.name);
            item.textDoseTime.setText(s.timeText());

            Long takenAt = repo.getTakenAt(s.id, today);
            if (takenAt != null) {
                item.textDoseStatus.setText(MedRepository.clockText(takenAt) + "에 먹었어요");
                item.btnTook.setEnabled(false);          // 이미 확인했으면 다시 못 눌러요 (중복 방지)
                item.btnTook.setText("확인했어요");
            } else {
                boolean late = now > AlarmScheduler.todayTimeMillis(s, now);
                item.textDoseStatus.setText(late ? "시간이 지났어요. 드셨다면 눌러 주세요" : "아직 안 먹었어요");
                final long id = s.id;
                item.btnTook.setOnClickListener(v -> {
                    long t = System.currentTimeMillis();
                    boolean first = repo.confirm(id, MedRepository.dateKey(t), t);
                    NotificationHelper.cancel(this, id);
                    Toast.makeText(this, first ? "잘하셨어요! 기록했어요" : "이미 확인했어요",
                            Toast.LENGTH_SHORT).show();
                    refresh();
                });
            }
            binding.doseContainer.addView(item.getRoot());
        }
    }
}
