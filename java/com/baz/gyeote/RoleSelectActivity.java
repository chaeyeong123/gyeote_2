package com.baz.gyeote;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.baz.gyeote.common.UiUtil;
import com.baz.gyeote.databinding.ActivityRoleSelectBinding;

public class RoleSelectActivity extends AppCompatActivity {
    private static final String ELDER = "elder";
    private static final String FAMILY = "family";

    private ActivityRoleSelectBinding binding;
    private String selectedRole = null;   // 아직 아무것도 안 골랐으면 null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRoleSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtil.applyInsets(binding.main);

        // 누르면 역할 선택파트
        binding.cardElder.setOnClickListener(v -> selectRole(ELDER));
        binding.cardFamily.setOnClickListener(v -> selectRole(FAMILY));

        // 버튼누르면 역할 홈화면 각각 이동
        binding.btnNext.setOnClickListener(v -> goNext());
    }

    private void selectRole(String role) {
        selectedRole = role;
        binding.cardElder.setSelected(ELDER.equals(role));    // 선택된 카드는 테두리 강조
        binding.cardFamily.setSelected(FAMILY.equals(role));
        binding.btnNext.setEnabled(true);                      // 선택해야 다음 버튼이 켜져요
    }

    private void goNext() {
        if (selectedRole == null) return;
        Class<?> next = ELDER.equals(selectedRole) ? ElderHomeActivity.class : FamilyHomeActivity.class;
        startActivity(new Intent(this, next));
    }
}
