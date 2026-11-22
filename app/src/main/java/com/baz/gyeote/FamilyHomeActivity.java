package com.baz.gyeote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityFamilyHomeBinding;
import com.baz.gyeote.databinding.ItemMedBinding;
import com.baz.gyeote.med.AlarmScheduler;
import com.baz.gyeote.med.MedHistoryActivity;
import com.baz.gyeote.med.MedListActivity;
import com.baz.gyeote.med.MedRepository;
import com.baz.gyeote.med.MedSchedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FamilyHomeActivity extends AppCompatActivity {
    private ActivityFamilyHomeBinding binding;
    private MedRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFamilyHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtil.applyInsets(binding.main);

        repo = MedRepository.get(this);
        binding.btnMed.setOnClickListener(v -> startActivity(new Intent(this, MedListActivity.class)));
        binding.btnHistoryHome.setOnClickListener(v -> startActivity(new Intent(this, MedHistoryActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();     // 다른 화면에서 돌아올 때마다 최신 상태로
    }

    private void refreshDashboard() {
        long now = System.currentTimeMillis();
        String today = MedRepository.dateKey(now);
        binding.textDate.setText(new SimpleDateFormat("M월 d일 EEEE", Locale.KOREA).format(new Date(now)));

        // 오늘 일정(= 오늘 먹는 약)과 복약 확인 상태
        List<MedSchedule> list = repo.getForDay(MedRepository.todayIndex());
        binding.todayContainer.removeAllViews();
        int taken = 0;
        int missed = 0;     // 시간이 지났는데 아직 확인이 없는 약
        for (MedSchedule s : list) {
            ItemMedBinding item = ItemMedBinding.inflate(getLayoutInflater(), binding.todayContainer, false);
            item.textName.setText(s.name);
            item.textTime.setText(s.timeText());
            Long takenAt = repo.getTakenAt(s.id, today);
            if (takenAt != null) {
                taken++;
                item.textStatus.setText("확인함 · " + MedRepository.clockText(takenAt));
            } else if (AlarmScheduler.todayTimeMillis(s, now) > now) {
                item.textStatus.setText("아직 시간 전이에요");
            } else {
                missed++;
                item.textStatus.setText("미확인 · 아직 먹었다는 기록이 없어요");
            }
            binding.todayContainer.addView(item.getRoot());
        }
        binding.textEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        binding.textSummary.setText(list.isEmpty() ? "오늘은 먹는 약이 없어요"
                : list.size() + "번 중 " + taken + "번 확인했어요");

        // 미확인 상태: 해제되지 않은 가족 알림(15번)과 합쳐서 한눈에
        int openAlerts = repo.getOpenAlerts().size();
        boolean needCheck = missed > 0 || openAlerts > 0;
        binding.textStatus.setText(needCheck ? "확인이 필요해요" : "오늘은 이상 없어요");
        binding.textStatus.setTextColor(ContextCompat.getColor(this, needCheck ? R.color.alert_text : R.color.ok_text));
        binding.textStatus.setBackgroundColor(ContextCompat.getColor(this, needCheck ? R.color.alert_bg : R.color.ok_bg));
        binding.textAlertInfo.setText(openAlerts == 0 ? "해제 전 가족 알림이 없어요"
                : "해제 전 가족 알림 " + openAlerts + "건 · '복약 일정 관리'에서 처리해요");
    }
}