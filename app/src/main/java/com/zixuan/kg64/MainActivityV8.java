package com.zixuan.kg64;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;

import java.time.LocalTime;

public class MainActivityV8 extends MainActivityV7 {
    private static final int JASMINE_AMERICANO_KCAL = 15;
    private final EditText[] extraKcal = new EditText[4];

    @Override
    void render() {
        int y = 0;
        if (root != null && root.getParent() instanceof ScrollView) {
            y = ((ScrollView) root.getParent()).getScrollY();
        }
        super.render();
        if (y > 0 && root != null && root.getParent() instanceof ScrollView) {
            ScrollView sc = (ScrollView) root.getParent();
            final int keep = y;
            sc.post(() -> sc.scrollTo(0, keep));
        }
    }

    @Override
    View mealBox(int ix, String rec) {
        String k = mealKeys[ix];
        boolean confirmed = p.getBoolean(key(k + "_confirmed"), false);
        LinearLayout b = col();

        LinearLayout head = row();
        head.addView(t(mealNames[ix], 18, true, INK), new LinearLayout.LayoutParams(0, -2, 1));
        TextView total = t("本餐 0 kcal", 14, true, INK);
        head.addView(total);
        b.addView(head);
        b.addView(sp(6));

        TextView rv = t(rec, 12, true, INK);
        rv.setPadding(dp(10), dp(8), dp(10), dp(8));
        rv.setBackground(round(LIME));
        b.addView(rv);
        b.addView(sp(9));

        String[] names = {
                "主食：实际几拳（1拳≈180 kcal）",
                "蛋白质：实际几拳（1拳≈160 kcal）",
                "蔬菜：实际几拳（1拳≈50 kcal）",
                "其他/高油：实际几拳（1拳≈220 kcal）"
        };
        String[] suffix = {"_staple", "_protein", "_veg", "_other"};

        for (int j = 0; j < 4; j++) {
            EditText e = dec(names[j]);
            e.setText(pref(k + suffix[j]));
            e.setEnabled(!confirmed);
            meal[ix][j] = e;
            watch(e, k + suffix[j]);
            e.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int bfr, int c) {}
                public void afterTextChanged(Editable s) {
                    if (!rendering) total.setText("本餐 " + Math.round(mealKcal(ix)) + " kcal");
                }
            });
            b.addView(e, new LinearLayout.LayoutParams(-1, dp(48)));
            if (j < 3) b.addView(sp(6));
        }

        if (ix < 3) {
            b.addView(sp(6));
            EditText extra = num("其他热量：直接填写 kcal（难分开算的食物）");
            extra.setText(pref(k + "_extra_kcal"));
            extra.setEnabled(!confirmed);
            extraKcal[ix] = extra;
            watch(extra, k + "_extra_kcal");
            extra.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                public void onTextChanged(CharSequence s, int st, int bfr, int c) {}
                public void afterTextChanged(Editable s) {
                    if (!rendering) total.setText("本餐 " + Math.round(mealKcal(ix)) + " kcal");
                }
            });
            b.addView(extra, new LinearLayout.LayoutParams(-1, dp(48)));
        } else {
            extraKcal[ix] = null;
        }

        if (ix == 0) {
            coffee = new CheckBox(this);
            coffee.setText("瑞幸茉莉花香美式 ≈15 kcal（冷饮、不另外加糖参考）");
            coffee.setTextSize(14);
            coffee.setChecked(p.getBoolean(key("breakfast_coffee"), false));
            coffee.setEnabled(!confirmed);
            coffee.setOnCheckedChangeListener((v, c) -> {
                p.edit().putBoolean(key("breakfast_coffee"), c).apply();
                total.setText("本餐 " + Math.round(mealKcal(ix)) + " kcal");
                updateAll();
            });
            b.addView(coffee);
        }

        b.addView(sp(8));
        b.addView(t("进食时间", 14, true, INK));
        b.addView(sp(3));

        int h = p.getInt(key(k + "_hour"), -1);
        int m = p.getInt(key(k + "_minute"), -1);
        LocalTime now = LocalTime.now();
        if (h < 0) { h = now.getHour(); m = now.getMinute(); }

        LinearLayout time = row();
        NumberPicker hp = picker(0, 23, h);
        NumberPicker mp = picker(0, 59, m);
        hp.setEnabled(!confirmed);
        mp.setEnabled(!confirmed);
        time.setAlpha(confirmed ? .48f : 1f);
        time.addView(hp, new LinearLayout.LayoutParams(0, dp(112), 1));
        TextView colon = t(":", 30, true, INK);
        colon.setGravity(Gravity.CENTER);
        time.addView(colon, new LinearLayout.LayoutParams(dp(28), dp(112)));
        time.addView(mp, new LinearLayout.LayoutParams(0, dp(112), 1));
        b.addView(time);

        NumberPicker.OnValueChangeListener listener = (picker, oldVal, newVal) -> {
            if (rendering || confirmed) return;
            p.edit().putInt(key(k + "_hour"), hp.getValue())
                    .putInt(key(k + "_minute"), mp.getValue()).apply();
            updateAll();
        };
        hp.setOnValueChangedListener(listener);
        mp.setOnValueChangedListener(listener);

        if (p.getInt(key(k + "_hour"), -1) < 0 && !confirmed) {
            p.edit().putInt(key(k + "_hour"), h).putInt(key(k + "_minute"), m).apply();
        }

        Button confirm = btn(confirmed ? "✓ 本餐已确认 · 点此修改" : "确认本餐", LIME, INK);
        confirm.setOnClickListener(v -> {
            boolean old = p.getBoolean(key(k + "_confirmed"), false);
            if (!old) {
                p.edit().putInt(key(k + "_hour"), hp.getValue())
                        .putInt(key(k + "_minute"), mp.getValue())
                        .putBoolean(key(k + "_confirmed"), true).apply();
                saveDailySnapshot();
            } else {
                p.edit().putBoolean(key(k + "_confirmed"), false).apply();
            }
            render();
        });
        b.addView(sp(7));
        b.addView(confirm, new LinearLayout.LayoutParams(-1, dp(46)));

        total.setText("本餐 " + Math.round(mealKcal(ix)) + " kcal");
        return card(b, Color.WHITE);
    }

    @Override
    float mealKcal(int i) {
        float sum = 0f;
        if (meal[i][0] != null) sum += fv(meal[i][0]) * 180f;
        if (meal[i][1] != null) sum += fv(meal[i][1]) * 160f;
        if (meal[i][2] != null) sum += fv(meal[i][2]) * 50f;
        if (meal[i][3] != null) sum += fv(meal[i][3]) * 220f;
        if (i < 3 && extraKcal[i] != null) sum += fv(extraKcal[i]);
        if (i == 0 && p.getBoolean(key("breakfast_coffee"), false)) sum += JASMINE_AMERICANO_KCAL;
        return sum;
    }
}
