package com.zixuan.kg64;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
    private static final int SOFT = Color.rgb(235, 233, 224);
    private static final float TARGET = 64f;
    private final LocalDate goalDate = LocalDate.of(2026, 10, 1);

    private SharedPreferences prefs;
    private LinearLayout root;
    private TextView weightValue;
    private TextView gapValue;
    private TextView liquidValue;
    private TextView trendValue;
    private TextView intakeValue;
    private TextView activityPlanValue;
    private TextView actualActivityValue;

    private EditText sleepInput;
    private EditText breakfastStaple, breakfastProtein, breakfastVeg, breakfastOther;
    private EditText lunchStaple, lunchProtein, lunchVeg, lunchOther;
    private EditText dinnerStaple, dinnerProtein, dinnerVeg, dinnerOther;
    private EditText snackStaple, snackProtein, snackVeg, snackOther;
    private EditText ballMinutesInput, walkStepsInput, runLapsInput;

    private boolean rendering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("kg64_data", MODE_PRIVATE);
        getWindow().setStatusBarColor(CREAM);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        render();
    }

    private void render() {
        rendering = true;

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
        root.addView(text("睡好 → 吃清楚 → 动起来", 25, true, INK));
        root.addView(space(18));

        LinearLayout hero = row();
        LinearLayout wBox = statBox("当前体重", "--", "kg");
        weightValue = (TextView) ((LinearLayout) wBox.getChildAt(1)).getChildAt(0);
        hero.addView(wBox, new LinearLayout.LayoutParams(0, dp(136), 1));

        LinearLayout dBox = statBox(
                "目标倒数",
                String.valueOf(Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), goalDate))),
                "天"
        );
        hero.addView(dBox, new LinearLayout.LayoutParams(0, dp(136), 1));
        root.addView(card(hero, INK));
        root.addView(space(14));

        LinearLayout mini = row();
        gapValue = miniStat(mini, "距离 64KG", "-- kg");
        miniStat(mini, "目标日期", "10 / 01");
        liquidValue = miniStat(mini, "本周液断", "剩 2 次");
        root.addView(card(mini, Color.WHITE));

        section("① 睡眠", "每天先填睡眠；后面的运动建议会跟着变化");
        LinearLayout sleepCard = vertical();
        sleepInput = decimalInput("昨晚睡了多少小时，例如 7.5");
        sleepInput.setText(pref("sleep_hours"));
        watch(sleepInput, "sleep_hours");
        sleepCard.addView(sleepInput, new LinearLayout.LayoutParams(-1, dp(52)));
        sleepCard.addView(space(8));
        sleepCard.addView(text("睡眠不足时，App 会自动降低当天运动建议，不会要求你用运动硬抵消摄入。", 12, false, MUTED));
        root.addView(card(sleepCard, Color.WHITE));

        section("② 吃饭", "全部用“1拳”做统一体积单位；每拳热量是估算值");
        root.addView(text("统一换算：主食 1拳≈180 kcal｜蛋白质 1拳≈160 kcal｜蔬菜 1拳≈50 kcal｜其他/高油食物 1拳≈220 kcal", 12, false, MUTED));
        root.addView(space(10));

        root.addView(mealEditor("早餐", "breakfast"));
        root.addView(space(8));
        root.addView(mealEditor("午餐", "lunch"));
        root.addView(space(8));
        root.addView(mealEditor("晚餐", "dinner"));
        root.addView(space(8));
        root.addView(mealEditor("加餐", "snack"));

        root.addView(space(10));
        intakeValue = text("今日摄入估算：0 kcal", 18, true, INK);
        intakeValue.setPadding(dp(14), dp(13), dp(14), dp(13));
        intakeValue.setBackground(round(LIME));
        root.addView(intakeValue);

        section("③ 运动", "根据今天吃了多少 + 睡了多久，自动给出活动目标与等价运动");
        activityPlanValue = text("", 15, true, INK);
        activityPlanValue.setLineSpacing(0, 1.35f);
        root.addView(card(activityPlanValue, Color.WHITE));
        root.addView(space(10));

        LinearLayout exerciseCard = vertical();
        exerciseCard.addView(text("填写你实际完成的运动量", 15, true, INK));
        exerciseCard.addView(space(8));

        ballMinutesInput = numberInput("打球：分钟");
        ballMinutesInput.setText(pref("ball_minutes_actual"));
        watch(ballMinutesInput, "ball_minutes_actual");
        exerciseCard.addView(ballMinutesInput, new LinearLayout.LayoutParams(-1, dp(50)));
        exerciseCard.addView(space(7));

        walkStepsInput = numberInput("快走：步数");
        walkStepsInput.setText(pref("walk_steps_actual"));
        watch(walkStepsInput, "walk_steps_actual");
        exerciseCard.addView(walkStepsInput, new LinearLayout.LayoutParams(-1, dp(50)));
        exerciseCard.addView(space(7));

        runLapsInput = decimalInput("跑步：400m 操场圈数");
        runLapsInput.setText(pref("run_laps_actual"));
        watch(runLapsInput, "run_laps_actual");
        exerciseCard.addView(runLapsInput, new LinearLayout.LayoutParams(-1, dp(50)));
        exerciseCard.addView(space(8));

        actualActivityValue = text("", 13, true, INK);
        actualActivityValue.setLineSpacing(0, 1.3f);
        exerciseCard.addView(actualActivityValue);
        root.addView(card(exerciseCard, Color.WHITE));

        section("体重记录", "记录后会自动更新上方距离目标");
        LinearLayout weightRow = row();
        EditText weightInput = decimalInput("输入体重 kg");
        weightRow.addView(weightInput, new LinearLayout.LayoutParams(0, dp(52), 1));
        Button saveWeight = button("记录", LIME, INK);
        saveWeight.setOnClickListener(v -> saveWeight(weightInput));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(92), dp(52));
        bp.setMargins(dp(10), 0, 0, 0);
        weightRow.addView(saveWeight, bp);
        root.addView(weightRow);

        section("体重趋势", "最近记录");
        trendValue = text("", 15, true, INK);
        trendValue.setLineSpacing(0, 1.4f);
        root.addView(card(trendValue, Color.WHITE));

        section("液断日", "每周最多 2 次");
        LinearLayout liquid = vertical();
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

        setContentView(scroll);
        refreshStats();
        rendering = false;
        updateDynamicPlan();
    }

    private View mealEditor(String title, String key) {
        LinearLayout box = vertical();
        box.addView(text(title, 18, true, INK));
        box.addView(space(8));

        EditText staple = decimalInput("主食：几拳（1拳≈180 kcal）");
        EditText protein = decimalInput("蛋白质：几拳（1拳≈160 kcal）");
        EditText veg = decimalInput("蔬菜：几拳（1拳≈50 kcal）");
        EditText other = decimalInput("其他/高油：几拳（1拳≈220 kcal）");

        staple.setText(pref(key + "_staple"));
        protein.setText(pref(key + "_protein"));
        veg.setText(pref(key + "_veg"));
        other.setText(pref(key + "_other"));

        watch(staple, key + "_staple");
        watch(protein, key + "_protein");
        watch(veg, key + "_veg");
        watch(other, key + "_other");

        box.addView(staple, new LinearLayout.LayoutParams(-1, dp(48)));
        box.addView(space(6));
        box.addView(protein, new LinearLayout.LayoutParams(-1, dp(48)));
        box.addView(space(6));
        box.addView(veg, new LinearLayout.LayoutParams(-1, dp(48)));
        box.addView(space(6));
        box.addView(other, new LinearLayout.LayoutParams(-1, dp(48)));

        if ("breakfast".equals(key)) {
            breakfastStaple = staple; breakfastProtein = protein; breakfastVeg = veg; breakfastOther = other;
        } else if ("lunch".equals(key)) {
            lunchStaple = staple; lunchProtein = protein; lunchVeg = veg; lunchOther = other;
        } else if ("dinner".equals(key)) {
            dinnerStaple = staple; dinnerProtein = protein; dinnerVeg = veg; dinnerOther = other;
        } else {
            snackStaple = staple; snackProtein = protein; snackVeg = veg; snackOther = other;
        }

        return card(box, Color.WHITE);
    }

    private void watch(EditText field, String key) {
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (rendering) return;
                prefs.edit().putString(dayKey(key), s.toString().trim()).apply();
                updateDynamicPlan();
            }
        });
    }

    private void updateDynamicPlan() {
        if (intakeValue == null || activityPlanValue == null || actualActivityValue == null) return;

        int intake = Math.round(
                mealKcal(breakfastStaple, breakfastProtein, breakfastVeg, breakfastOther) +
                mealKcal(lunchStaple, lunchProtein, lunchVeg, lunchOther) +
                mealKcal(dinnerStaple, dinnerProtein, dinnerVeg, dinnerOther) +
                mealKcal(snackStaple, snackProtein, snackVeg, snackOther)
        );
        float sleep = f(sleepInput);
        float weight = prefs.getFloat("weight", 0f);

        intakeValue.setText("今日摄入估算：" + intake + " kcal");
        prefs.edit().putInt(dayKeyInt("estimated_intake"), intake).apply();

        if (weight <= 0) {
            activityPlanValue.setText(
                    "先记录体重后，我才能把建议活动量换算成打球时长、快走步数和跑步圈数。\n" +
                    "当前已根据饮食和睡眠记录保存今日数据。"
            );
            actualActivityValue.setText("记录体重后再计算运动消耗。 ");
            return;
        }

        int targetKcal = ActivityPlanner.suggestedActivityKcal(intake, sleep);
        int ballMin = ActivityPlanner.basketballMinutesFor(targetKcal, weight);
        int walkSteps = ActivityPlanner.briskWalkStepsFor(targetKcal, weight);
        float runLaps = ActivityPlanner.runningLapsFor(targetKcal, weight);

        String sleepNote;
        if (sleep > 0 && sleep < 6f) sleepNote = "睡眠不足 6 小时，今天自动降低运动建议。";
        else if (sleep >= 6f && sleep < 7f) sleepNote = "睡眠略少，运动建议已适度下调。";
        else if (sleep >= 7f) sleepNote = "睡眠达到较稳定区间。";
        else sleepNote = "先填睡眠，运动建议会继续动态调整。";

        activityPlanValue.setText(
                "建议活动目标：约 " + targetKcal + " kcal（不是要求把摄入全部抵消）\n" +
                "≈ 打球 " + formatMinutes(ballMin) + "\n" +
                "≈ 快走 " + walkSteps + " 步\n" +
                "≈ 跑步 " + oneDecimal(runLaps) + " 圈（400m/圈）\n" +
                sleepNote
        );

        int actualBallMin = i(ballMinutesInput);
        int actualWalkSteps = i(walkStepsInput);
        float actualRunLaps = f(runLapsInput);

        int ballKcal = ActivityPlanner.metCalories(6.0f, weight, actualBallMin);
        int walkKcal = ActivityPlanner.briskWalkCalories(weight, actualWalkSteps);
        int runKcal = ActivityPlanner.runningCalories(weight, actualRunLaps);
        int actual = ballKcal + walkKcal + runKcal;
        int remaining = Math.max(0, targetKcal - actual);

        String status = remaining == 0 ? "已达到今天建议活动目标" : "距离建议活动目标还差约 " + remaining + " kcal";
        actualActivityValue.setText(
                "实际运动消耗估算：" + actual + " kcal\n" +
                "打球 " + ballKcal + " + 快走 " + walkKcal + " + 跑步 " + runKcal + " kcal\n" +
                status
        );
    }

    private float mealKcal(EditText staple, EditText protein, EditText veg, EditText other) {
        return f(staple) * 180f + f(protein) * 160f + f(veg) * 50f + f(other) * 220f;
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
        updateDynamicPlan();
        toast("已记录 " + trim(value) + " kg");
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
        new android.app.AlertDialog.Builder(this)
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

    private String pref(String key) {
        return prefs.getString(dayKey(key), "");
    }

    private String dayKey(String key) {
        return key + "_" + LocalDate.now();
    }

    private String dayKeyInt(String key) {
        return key + "_" + LocalDate.now();
    }

    private int i(EditText e) {
        try { return Integer.parseInt(e.getText().toString().trim()); }
        catch (Exception ex) { return 0; }
    }

    private float f(EditText e) {
        try { return Float.parseFloat(e.getText().toString().trim()); }
        catch (Exception ex) { return 0f; }
    }

    private String formatMinutes(int minutes) {
        if (minutes < 60) return minutes + " 分钟";
        int h = minutes / 60;
        int m = minutes % 60;
        return m == 0 ? h + " 小时" : h + " 小时 " + m + " 分钟";
    }

    private String oneDecimal(float value) {
        return String.format(Locale.CHINA, "%.1f", value);
    }

    private LinearLayout statBox(String label, String value, String unit) {
        LinearLayout box = vertical();
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
        LinearLayout box = vertical();
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

    private LinearLayout vertical() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
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

    private EditText decimalInput(String hint) {
        return input(hint, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    private EditText numberInput(String hint) {
        return input(hint, InputType.TYPE_CLASS_NUMBER);
    }

    private EditText input(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(14);
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

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + .5f);
    }

    private String trim(float value) {
        return value == (long) value ? String.valueOf((long) value) : String.format(Locale.CHINA, "%.1f", value);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
