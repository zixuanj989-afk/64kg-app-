package com.zixuan.kg64;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    private static final int INK = Color.rgb(23, 25, 22);
    private static final int CREAM = Color.rgb(245, 243, 234);
    private static final int LIME = Color.rgb(216, 255, 82);
    private static final int MUTED = Color.rgb(105, 108, 100);
    private static final float TARGET = 64f;
    private final LocalDate goalDate = LocalDate.of(2026, 10, 1);

    private SharedPreferences prefs;
    private LinearLayout root;
    private TextView weightValue;
    private TextView gapValue;
    private TextView liquidValue;
    private TextView trendValue;
    private EditText caloriesInput;
    private String movement = "未记录";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("kg64_data", MODE_PRIVATE);
        getWindow().setStatusBarColor(CREAM);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        render();
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(CREAM);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(36));
        scroll.addView(root);

        TextView brand = text("64KG / DAILY", 13, true, INK);
        brand.setLetterSpacing(.14f);
        root.addView(brand);
        root.addView(space(8));
        root.addView(text("把今天做好，就在靠近。", 25, true, INK));
        root.addView(space(18));

        LinearLayout hero = row();
        LinearLayout wBox = statBox("当前体重", "--", "kg");
        weightValue = (TextView) ((LinearLayout) wBox.getChildAt(1)).getChildAt(0);
        hero.addView(wBox, new LinearLayout.LayoutParams(0, dp(136), 1));
        LinearLayout dBox = statBox("目标倒数", String.valueOf(Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), goalDate))), "天");
        hero.addView(dBox, new LinearLayout.LayoutParams(0, dp(136), 1));
        root.addView(card(hero, INK));
        root.addView(space(14));

        LinearLayout mini = row();
        gapValue = miniStat(mini, "距离 64KG", "-- kg");
        miniStat(mini, "目标日期", "10 / 01");
        liquidValue = miniStat(mini, "本周液断", "剩 2 次");
        root.addView(card(mini, Color.WHITE));

        section("今日记录", "数据保存在本机，再次打开仍会保留");
        LinearLayout weightRow = row();
        EditText weightInput = input("输入体重 kg", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        weightRow.addView(weightInput, new LinearLayout.LayoutParams(0, dp(52), 1));
        Button saveWeight = button("记录", LIME, INK);
        saveWeight.setOnClickListener(v -> saveWeight(weightInput));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(92), dp(52));
        bp.setMargins(dp(10), 0, 0, 0);
        weightRow.addView(saveWeight, bp);
        root.addView(weightRow);
        root.addView(space(10));

        caloriesInput = input("今日已摄入 kcal", InputType.TYPE_CLASS_NUMBER);
        caloriesInput.setText(prefs.getString("calories_" + LocalDate.now(), ""));
        root.addView(caloriesInput, new LinearLayout.LayoutParams(-1, dp(52)));
        root.addView(space(10));
        root.addView(movementPicker());
        root.addView(space(10));
        Button saveDay = button("保存今日完成情况", INK, Color.WHITE);
        saveDay.setOnClickListener(v -> saveDay());
        root.addView(saveDay, new LinearLayout.LayoutParams(-1, dp(54)));

        section("体重趋势", "最近记录");
        trendValue = text("", 15, true, INK);
        trendValue.setLineSpacing(0, 1.4f);
        root.addView(card(trendValue, Color.WHITE));

        section("液断日", "每周最多 2 次");
        LinearLayout liquid = new LinearLayout(this);
        liquid.setOrientation(LinearLayout.VERTICAL);
        liquid.addView(text(isLiquidToday() ? "今天是液断日" : "今天正常饮食", 19, true, INK));
        liquid.addView(space(10));
        Button toggle = button(isLiquidToday() ? "取消今天液断" : "设为今天液断", LIME, INK);
        toggle.setOnClickListener(v -> toggleLiquidDay());
        liquid.addView(toggle, new LinearLayout.LayoutParams(-1, dp(50)));
        liquid.addView(space(8));
        Button random = button("帮我随机安排本周液断", Color.WHITE, INK);
        random.setOnClickListener(v -> randomLiquid());
        liquid.addView(random, new LinearLayout.LayoutParams(-1, dp(50)));
        root.addView(card(liquid, Color.WHITE));

        section("每日热量分配", "均衡日建议约 1400–1500 kcal");
        root.addView(meal("早餐", "300–350 kcal", "鸡蛋 1–2 个 + 全麦面包 2 片 / 玉米 1 根 + 无糖豆浆"));
        root.addView(space(8));
        root.addView(meal("午餐", "500–550 kcal", "米饭 1 小碗 + 鸡胸 / 牛肉 / 鱼 120–150g + 两拳蔬菜"));
        root.addView(space(8));
        root.addView(meal("晚餐", "400–450 kcal", "杂粮饭半碗 + 虾 / 瘦肉 / 豆腐 120g + 两拳蔬菜"));
        root.addView(space(8));
        root.addView(meal("加餐", "100–150 kcal", "苹果 / 橙子 1 个，或坚果 10g"));

        setContentView(scroll);
        refreshStats();
    }

    private void saveWeight(EditText field) {
        String raw = field.getText().toString().trim();
        if (raw.isEmpty()) { toast("先输入今天的体重"); return; }
        float value;
        try { value = Float.parseFloat(raw); } catch (Exception e) { toast("体重格式不对"); return; }
        if (value < 35 || value > 200) { toast("请确认体重数值"); return; }
        String today = LocalDate.now().toString();
        List<String> kept = new ArrayList<>();
        for (String line : prefs.getString("history", "").split(";")) {
            if (!line.isEmpty() && !line.startsWith(today + ",")) kept.add(line);
        }
        kept.add(today + "," + value);
        prefs.edit().putFloat("weight", value).putString("history", String.join(";", kept)).apply();
        field.setText("");
        refreshStats();
        toast("已记录 " + trim(value) + " kg");
    }

    private void saveDay() {
        String raw = caloriesInput.getText().toString().trim();
        int calories = 0;
        try { if (!raw.isEmpty()) calories = Integer.parseInt(raw); } catch (Exception ignored) {}
        prefs.edit().putString("calories_" + LocalDate.now(), raw).putString("movement_" + LocalDate.now(), movement).apply();
        StringBuilder msg = new StringBuilder("今日记录已保存");
        if (calories > 1500) msg.append("\n摄入超过 1500 kcal。下一餐正常清淡即可。");
        if (movement.equals("未记录") || movement.equals("休息")) msg.append("\n今天没有运动记录。");
        if (calories > 1500 || movement.equals("未记录") || movement.equals("休息")) {
            new AlertDialog.Builder(this).setTitle("今日提醒").setMessage(msg).setPositiveButton("知道了", null).show();
        } else toast("今日记录已保存");
    }

    private View movementPicker() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(text("今日运动", 13, false, MUTED));
        box.addView(space(7));
        LinearLayout choices = row();
        movement = prefs.getString("movement_" + LocalDate.now(), "未记录");
        for (String item : new String[]{"跑步", "打球", "休息"}) {
            Button b = button(item, item.equals(movement) ? LIME : Color.WHITE, INK);
            b.setOnClickListener(v -> { movement = item; prefs.edit().putString("movement_" + LocalDate.now(), item).apply(); render(); });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(46), 1);
            p.setMargins(dp(3), 0, dp(3), 0);
            choices.addView(b, p);
        }
        box.addView(choices);
        return box;
    }

    private void toggleLiquidDay() {
        String today = LocalDate.now().toString();
        List<String> dates = liquidDates();
        if (dates.contains(today)) dates.remove(today);
        else if (currentWeekLiquidCount(dates) >= 2) { toast("本周已经安排 2 次液断"); return; }
        else dates.add(today);
        prefs.edit().putString("liquid_dates", String.join(",", dates)).apply();
        render();
    }

    private void randomLiquid() {
        List<LocalDate> options = new ArrayList<>();
        LocalDate start = LocalDate.now().with(WeekFields.of(Locale.CHINA).dayOfWeek(), 1);
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            if (!d.isBefore(LocalDate.now())) options.add(d);
        }
        LocalDate pick = options.get(new Random().nextInt(options.size()));
        new AlertDialog.Builder(this)
                .setTitle("本周随机液断日")
                .setMessage("建议安排在 " + pick.getMonthValue() + " 月 " + pick.getDayOfMonth() + " 日。")
                .setPositiveButton("设为液断日", (d, w) -> {
                    List<String> dates = liquidDates();
                    if (currentWeekLiquidCount(dates) < 2 && !dates.contains(pick.toString())) {
                        dates.add(pick.toString());
                        prefs.edit().putString("liquid_dates", String.join(",", dates)).apply();
                        render();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private List<String> liquidDates() {
        List<String> out = new ArrayList<>();
        for (String s : prefs.getString("liquid_dates", "").split(",")) if (!s.isEmpty()) out.add(s);
        return out;
    }

    private int currentWeekLiquidCount(List<String> dates) {
        int count = 0;
        WeekFields wf = WeekFields.of(Locale.CHINA);
        LocalDate now = LocalDate.now();
        int week = now.get(wf.weekOfWeekBasedYear());
        int year = now.get(wf.weekBasedYear());
        for (String s : dates) {
            try {
                LocalDate d = LocalDate.parse(s);
                if (d.get(wf.weekOfWeekBasedYear()) == week && d.get(wf.weekBasedYear()) == year) count++;
            } catch (Exception ignored) {}
        }
        return count;
    }

    private boolean isLiquidToday() { return liquidDates().contains(LocalDate.now().toString()); }

    private void refreshStats() {
        float weight = prefs.getFloat("weight", 0);
        weightValue.setText(weight == 0 ? "--" : trim(weight));
        gapValue.setText(weight == 0 ? "-- kg" : trim(Math.max(0, weight - TARGET)) + " kg");
        liquidValue.setText("剩 " + Math.max(0, 2 - currentWeekLiquidCount(liquidDates())) + " 次");
        String raw = prefs.getString("history", "");
        if (raw.isEmpty()) trendValue.setText("还没有体重记录");
        else {
            String[] lines = raw.split(";");
            StringBuilder b = new StringBuilder();
            int start = Math.max(0, lines.length - 7);
            for (int i = start; i < lines.length; i++) {
                String[] p = lines[i].split(",");
                if (p.length == 2) b.append(p[0].substring(5)).append("   ").append(p[1]).append(" kg\n");
            }
            trendValue.setText(b.toString().trim());
        }
    }

    private View meal(String title, String kcal, String foods) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        LinearLayout head = row();
        head.addView(text(title, 17, true, INK), new LinearLayout.LayoutParams(0, -2, 1));
        TextView e = text(kcal, 13, true, INK);
        e.setPadding(dp(10), dp(5), dp(10), dp(5));
        e.setBackground(round(LIME));
        head.addView(e);
        box.addView(head);
        box.addView(space(8));
        box.addView(text(foods, 14, false, MUTED));
        return card(box, Color.WHITE);
    }

    private LinearLayout statBox(String label, String value, String unit) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.addView(text(label, 12, false, Color.rgb(190, 193, 183)));
        LinearLayout line = row();
        line.setGravity(Gravity.CENTER);
        TextView v = text(value, 39, true, Color.WHITE);
        line.addView(v);
        TextView u = text(unit, 13, true, LIME);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
        p.setMargins(dp(6), dp(12), 0, 0);
        line.addView(u, p);
        box.addView(line);
        return box;
    }

    private TextView miniStat(LinearLayout parent, String label, String value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.addView(text(label, 11, false, MUTED));
        TextView v = text(value, 16, true, INK);
        box.addView(v);
        parent.addView(box, new LinearLayout.LayoutParams(0, dp(78), 1));
        return v;
    }

    private void section(String title, String subtitle) {
        root.addView(space(22));
        root.addView(text(title, 21, true, INK));
        root.addView(space(3));
        root.addView(text(subtitle, 13, false, MUTED));
        root.addView(space(10));
    }

    private LinearLayout row() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.HORIZONTAL);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }

    private TextView text(String s, int sp, boolean bold, int color) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private EditText input(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(15);
        e.setInputType(type);
        e.setSingleLine();
        e.setPadding(dp(15), 0, dp(15), 0);
        e.setBackground(round(Color.WHITE));
        return e;
    }

    private Button button(String label, int bg, int fg) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(fg);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setBackground(round(bg));
        return b;
    }

    private View card(View content, int color) {
        LinearLayout c = new LinearLayout(this);
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        c.setBackground(round(color));
        c.addView(content, new LinearLayout.LayoutParams(-1, -2));
        return c;
    }

    private android.graphics.drawable.GradientDrawable round(int color) {
        android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(15));
        return g;
    }

    private View space(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h)));
        return v;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + .5f); }
    private String trim(float value) { return value == (long) value ? String.valueOf((long) value) : String.format(Locale.CHINA, "%.1f", value); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
