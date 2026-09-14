package com.zixuan.kg64;

import android.app.AlertDialog;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.time.LocalTime;
import java.util.Locale;

public class MainActivityV7 extends MainActivityV6 {

    @Override
    void updateSleep() {
        if (sleepDur == null || sh == null) return;
        int m = sleepMinutes();
        boolean locked = p.getBoolean(key("sleep_confirmed"), false);
        sleepDur.setText("共 " + m / 60 + " 小时 " + m % 60 + " 分钟" + (locked ? "  ✓" : "  · 点此确认"));
        p.edit().putInt(key("sleep_total_minutes"), m).apply();
    }

    @Override
    View mealBox(int ix, String rec) {
        String k = mealKeys[ix];
        LinearLayout b = col();
        boolean confirmed = p.getBoolean(key(k + "_confirmed"), false);

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
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                public void afterTextChanged(Editable s) {
                    if (!rendering) total.setText("本餐 " + Math.round(mealKcal(ix)) + " kcal");
                }
            });
            b.addView(e, new LinearLayout.LayoutParams(-1, dp(48)));
            if (j < 3) b.addView(sp(6));
        }

        if (ix == 0) {
            coffee = new CheckBox(this);
            coffee.setText("咖啡");
            coffee.setTextSize(14);
            coffee.setChecked(p.getBoolean(key("breakfast_coffee"), false));
            coffee.setEnabled(!confirmed);
            coffee.setOnCheckedChangeListener((v, c) -> p.edit().putBoolean(key("breakfast_coffee"), c).apply());
            b.addView(coffee);
        }

        b.addView(sp(8));
        b.addView(t("进食时间", 14, true, INK));
        b.addView(sp(3));

        int savedH = p.getInt(key(k + "_hour"), -1);
        int savedM = p.getInt(key(k + "_minute"), -1);
        LocalTime now = LocalTime.now();
        if (savedH < 0) {
            savedH = now.getHour();
            savedM = now.getMinute();
        }

        LinearLayout time = row();
        NumberPicker hp = picker(0, 23, savedH);
        NumberPicker mp = picker(0, 59, savedM);
        hp.setEnabled(!confirmed);
        mp.setEnabled(!confirmed);
        time.setAlpha(confirmed ? .48f : 1f);
        time.addView(hp, new LinearLayout.LayoutParams(0, dp(112), 1));
        TextView colon = t(":", 30, true, INK);
        colon.setGravity(Gravity.CENTER);
        time.addView(colon, new LinearLayout.LayoutParams(dp(28), dp(112)));
        time.addView(mp, new LinearLayout.LayoutParams(0, dp(112), 1));
        b.addView(time);

        NumberPicker.OnValueChangeListener timeListener = (picker, oldVal, newVal) -> {
            if (rendering || confirmed) return;
            p.edit()
                    .putInt(key(k + "_hour"), hp.getValue())
                    .putInt(key(k + "_minute"), mp.getValue())
                    .apply();
            updateAll();
        };
        hp.setOnValueChangedListener(timeListener);
        mp.setOnValueChangedListener(timeListener);

        if (p.getInt(key(k + "_hour"), -1) < 0 && !confirmed) {
            p.edit().putInt(key(k + "_hour"), savedH).putInt(key(k + "_minute"), savedM).apply();
        }

        Button confirm = btn(confirmed ? "✓ 本餐已确认 · 点此修改" : "确认本餐", LIME, INK);
        confirm.setOnClickListener(v -> {
            boolean old = p.getBoolean(key(k + "_confirmed"), false);
            if (!old) {
                p.edit()
                        .putInt(key(k + "_hour"), hp.getValue())
                        .putInt(key(k + "_minute"), mp.getValue())
                        .putBoolean(key(k + "_confirmed"), true)
                        .apply();
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
    View waterCard() {
        LinearLayout b = col();
        boolean confirmed = p.getBoolean(key("water_confirmed"), false);

        waterV = t("今日 0 / 2000 ml", 20, true, INK);
        b.addView(waterV);
        b.addView(sp(8));

        LinearLayout plus = row();
        Button p250 = btn("+250 ml", LIME, INK);
        Button p500 = btn("+500 ml", LIME, INK);
        p250.setEnabled(!confirmed);
        p500.setEnabled(!confirmed);
        p250.setOnClickListener(v -> changeWater(250));
        p500.setOnClickListener(v -> changeWater(500));
        plus.addView(p250, new LinearLayout.LayoutParams(0, dp(46), 1));
        plus.addView(p500, new LinearLayout.LayoutParams(0, dp(46), 1));
        b.addView(plus);

        b.addView(sp(6));
        LinearLayout minus = row();
        Button m250 = btn("−250 ml", Color.WHITE, INK);
        Button m500 = btn("−500 ml", Color.WHITE, INK);
        m250.setEnabled(!confirmed);
        m500.setEnabled(!confirmed);
        m250.setOnClickListener(v -> changeWater(-250));
        m500.setOnClickListener(v -> changeWater(-500));
        minus.addView(m250, new LinearLayout.LayoutParams(0, dp(46), 1));
        minus.addView(m500, new LinearLayout.LayoutParams(0, dp(46), 1));
        b.addView(minus);

        b.addView(sp(8));
        EditText custom = num("直接输入今日总饮水 ml");
        int current = p.getInt(key("water_ml"), 0);
        custom.setText(current == 0 ? "" : String.valueOf(current));
        custom.setEnabled(!confirmed);
        custom.setOnFocusChangeListener((v, has) -> {
            if (!has && !confirmed) {
                int ml = Math.max(0, iv(custom));
                p.edit().putInt(key("water_ml"), ml).apply();
                updateAll();
            }
        });
        b.addView(custom, new LinearLayout.LayoutParams(-1, dp(48)));

        b.addView(sp(8));
        Button confirm = btn(confirmed ? "✓ 饮水已确认 · 点此修改" : "确认今日饮水", LIME, INK);
        confirm.setOnClickListener(v -> {
            boolean old = p.getBoolean(key("water_confirmed"), false);
            if (!old) {
                int ml = custom.getText().toString().trim().isEmpty()
                        ? p.getInt(key("water_ml"), 0)
                        : Math.max(0, iv(custom));
                p.edit().putInt(key("water_ml"), ml).putBoolean(key("water_confirmed"), true).apply();
                saveDailySnapshot();
            } else {
                p.edit().putBoolean(key("water_confirmed"), false).apply();
            }
            render();
        });
        b.addView(confirm, new LinearLayout.LayoutParams(-1, dp(48)));

        return card(b, Color.WHITE);
    }

    private void changeWater(int delta) {
        int cur = p.getInt(key("water_ml"), 0);
        cur = Math.max(0, cur + delta);
        p.edit().putInt(key("water_ml"), cur).apply();
        updateAll();
    }

    @Override
    void saveWeight(EditText e) {
        String raw = e.getText().toString().trim();
        if (raw.isEmpty()) {
            toast("先输入今天的体重");
            return;
        }
        final float value;
        try {
            value = Float.parseFloat(raw);
        } catch (Exception ex) {
            toast("体重格式不对");
            return;
        }
        if (value < 35 || value > 200) {
            toast("请确认体重数值");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("确认今天体重")
                .setMessage("确认记录 " + trim(value) + " kg？")
                .setNegativeButton("再改一下", null)
                .setPositiveButton("确认", (dialog, which) -> {
                    String td = java.time.LocalDate.now().toString();
                    java.util.List<String> keep = new java.util.ArrayList<>();
                    for (String line : p.getString("history", "").split(";")) {
                        if (!line.isEmpty() && !line.startsWith(td + ",")) keep.add(line);
                    }
                    keep.add(td + "," + value);
                    p.edit()
                            .putFloat("weight", value)
                            .putBoolean(key("weight_confirmed"), true)
                            .putString("history", String.join(";", keep))
                            .apply();
                    e.setText("");
                    saveDailySnapshot();
                    refresh();
                    updateAll();
                    toast("✓ 已确认 " + trim(value) + " kg");
                })
                .show();
    }
}
