package com.baz.gyeote.med;

public class MedHistoryActivity {
    package com.baz.gyeote.med;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityMedHistoryBinding;
import com.baz.gyeote.databinding.ItemMedBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

    // 날짜 별 복약 기록
    public class MedHistoryActivity extends AppCompatActivity {
        private ActivityMedHistoryBinding binding;
        private MedRepository repo;
        private final Calendar day = Calendar.getInstance();   // 지금보는 날짜

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            binding = ActivityMedHistoryBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            UiUtil.applyInsets(binding.main);

            repo = MedRepository.get(this);
            binding.btnPrev.setOnClickListener(v -> { day.add(Calendar.DAY_OF_YEAR, -1); refresh(); });
            binding.btnNext.setOnClickListener(v -> { day.add(Calendar.DAY_OF_YEAR, 1); refresh(); });
        }

        @Override
        protected void onResume() {
            super.onResume();
            refresh();
        }

        private void refresh() {
            binding.historyContainer.removeAllViews();
            long dayMillis = day.getTimeInMillis();
            long now = System.currentTimeMillis();
            String key = MedRepository.dateKey(dayMillis);
            binding.textDate.setText(new SimpleDateFormat("M월 d일 EEEE", Locale.KOREA).format(new Date(dayMillis)));

            int dayIndex = day.get(Calendar.DAY_OF_WEEK) - 1;
            List<MedSchedule> list = repo.getForDay(dayIndex);    //그 요일에 먹는 약 (회차별)
            int taken = 0;

            for (MedSchedule s : list) {
                ItemMedBinding item = ItemMedBinding.inflate(getLayoutInflater(), binding.historyContainer, false);
                item.textName.setText(s.name);
                item.textTime.setText(s.timeText());
                Long takenAt = repo.getTakenAt(s.id, key);
                if (takenAt != null) {
                    taken++;
                    item.textStatus.setText("확인함 · " + MedRepository.clockText(takenAt));
                } else if (AlarmScheduler.todayTimeMillis(s, dayMillis) > now) {
                    item.textStatus.setText("아직 시간 전이에요");
                } else {
                    item.textStatus.setText("미확인 · 먹었다는 기록이 없어요");
                }
                binding.historyContainer.addView(item.getRoot());
            }

            binding.textEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            binding.textSummary.setText(list.isEmpty() ? "" : list.size() + "번 중 " + taken + "번 확인했어요");
        }
    }
}
