package com.baz.gyeote;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityElderHomeBinding;
import com.baz.gyeote.med.ElderMedActivity;

public class ElderHomeActivity extends AppCompatActivity {
    private ActivityElderHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityElderHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtil.applyInsets(binding.main);

        // 복약화면으로 이동파트
        binding.btnToday.setOnClickListener(v -> startActivity(new Intent(this, ElderMedActivity.class)));
    }
}
