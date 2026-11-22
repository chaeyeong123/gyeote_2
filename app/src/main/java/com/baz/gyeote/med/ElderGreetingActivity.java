package com.baz.gyeote.med;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityElderGreetingBinding;
import com.baz.gyeote.databinding.ItemGreetingBinding;

import java.util.List;

/** 어르신 화면: 가족이 보낸 안부를 받아서 보고, '잘 읽었어요'로 확인 (22번) */
public class ElderGreetingActivity extends AppCompatActivity {
    private ActivityElderGreetingBinding binding;
    private MedRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityElderGreetingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtil.applyInsets(binding.main);
        repo = MedRepository.get(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        binding.greetingContainer.removeAllViews();
        long now = System.currentTimeMillis();
        List<Greeting> list = repo.getGreetings(20);
        binding.textEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        for (Greeting g : list) {
            if (g.receivedAt == 0) {                  // 화면에서 처음 본 시각을 '수신 시각'으로 기록
                repo.markReceived(g.id, now);
                g.receivedAt = now;
            }
            ItemGreetingBinding item = ItemGreetingBinding.inflate(getLayoutInflater(), binding.greetingContainer, false);
            item.textGreetingBody.setText(g.text);
            item.textGreetingStatus.setText(g.statusText());
            item.btnRead.setVisibility(View.VISIBLE);

            if (g.confirmedAt != 0) {
                item.btnRead.setEnabled(false);       // 이미 확인했으면 다시 못 눌러요
                item.btnRead.setText("잘 읽었어요 (확인함)");
            } else {
                final long id = g.id;
                item.btnRead.setOnClickListener(v -> {
                    repo.markConfirmed(id, System.currentTimeMillis());
                    NotificationHelper.cancelGreeting(this, id);
                    Toast.makeText(this, "가족에게 알려드렸어요", Toast.LENGTH_SHORT).show();
                    refresh();
                });
            }
            binding.greetingContainer.addView(item.getRoot());
        }
    }
}