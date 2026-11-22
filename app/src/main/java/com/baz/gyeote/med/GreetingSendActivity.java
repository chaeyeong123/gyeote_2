package com.baz.gyeote.med;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityGreetingSendBinding;
import com.baz.gyeote.databinding.ItemGreetingBinding;

import java.util.List;

/** 가족 화면: 버튼으로 안부를 보내고, 수신·확인 시각을 확인 (22번) */
public class GreetingSendActivity extends AppCompatActivity {
    private ActivityGreetingSendBinding binding;
    private MedRepository repo;
    private final ActivityResultLauncher<String> notifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityGreetingSendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtil.applyInsets(binding.main);

        repo = MedRepository.get(this);
        NotificationHelper.ensureChannel(this);
        NotificationHelper.requestPermissionIfNeeded(this, notifPermission);

        // 자주 보내는 안부: 버튼에 적힌 글이 그대로 전달됨
        binding.btnQuick1.setText("오늘도 잘 지내고 계시죠?");
        binding.btnQuick2.setText("식사 잘 챙겨 드세요");
        binding.btnQuick3.setText("따뜻하게 입고 나가세요");
        binding.btnQuick4.setText("저녁에 전화 드릴게요");
        View.OnClickListener quick = v -> send(((TextView) v).getText().toString());
        binding.btnQuick1.setOnClickListener(quick);
        binding.btnQuick2.setOnClickListener(quick);
        binding.btnQuick3.setOnClickListener(quick);
        binding.btnQuick4.setOnClickListener(quick);

        binding.btnSendCustom.setOnClickListener(v -> {
            String text = binding.editCustom.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "보낼 내용을 입력해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            send(text);
            binding.editCustom.setText("");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();      // 어르신 화면에서 확인한 시각이 여기에 반영
    }

    private void send(String text) {
        long id = repo.addGreeting(text, System.currentTimeMillis());
        NotificationHelper.showGreeting(this, id, text);     // 어르신 쪽 알림
        Toast.makeText(this, "안부를 보냈어요", Toast.LENGTH_SHORT).show();
        refresh();
    }

    private void refresh() {
        binding.greetingContainer.removeAllViews();
        List<Greeting> list = repo.getGreetings(20);
        binding.textEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        for (Greeting g : list) {
            ItemGreetingBinding item = ItemGreetingBinding.inflate(getLayoutInflater(), binding.greetingContainer, false);
            item.textGreetingBody.setText(g.text);
            item.textGreetingStatus.setText(g.statusText());     // 보냄 · 수신 · 확인 시각
            binding.greetingContainer.addView(item.getRoot());
        }
    }
}
