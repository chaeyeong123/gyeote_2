package com.baz.gyeote.common;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.baz.gyeote.R;

/** 모든 화면 공통: 상태바/하단바를 피하고, 화면 여백(screen_padding)을 줘요. */
public final class UiUtil {
    private UiUtil() { }

    public static void applyInsets(View root) {
        final int pad = root.getResources().getDimensionPixelSize(R.dimen.screen_padding);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left + pad, bars.top + pad, bars.right + pad, bars.bottom + pad);
            return insets;
        });
    }
}
