package com.baz.gyeote;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityFamilyHomeBinding;
import com.baz.gyeote.med.MedListActivity;

public class FamilyHomeActivity extends AppCompatActivity {
    private ActivityFamilyHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFamilyHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtil.applyInsets(binding.main);

        // 복약 화면으로 이동
        binding.btnMed.setOnClickListener(v -> startActivity(new Intent(this, MedListActivity.class)));
    }
}
