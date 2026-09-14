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
import android.widget.NumberPicker;
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

public class MainActivityV4 extends Activity {
    private static final int INK = Color.rgb(23, 25, 22);
    private static final int CREAM = Color.rgb(245, 243, 234);
    private static final int LIME = Color.rgb(216, 255, 82);
    private static final int MUTED = Color.rgb(105, 108, 100);
    private static final float TARGET = 64f;
    private final LocalDate goalDate = LocalDate.of(2026, 10, 1);

    private SharedPreferences prefs;
    private LinearLayout root;
    private TextView gapHeroValue, weightMiniValue, countdownMiniValue, liquidValue, trendValue;
    private TextView sleepDurationValue, intakeValue, activityPlanValue, actualActivityValue;

    private NumberPicker sleepHourPicker, sleepMinutePicker, wakeHourPicker, wakeMinutePicker;
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

        root = vertical();
        root.setPadding(dp(18), dp(18), dp(18), dp(36));
        scroll.addView(root);

        TextView brand = text("64KG / DAILY", 13, true, INK);
        brand.setLetterSpacing(.14f);
        root.addView(brand);
        root.addView(space(8));
        root.addView(text("睡好 → 吃清楚 → 动起来", 25, true, INK));
        root.addView(space(18));

        // 视觉中心：距离 64KG
        LinearLayout hero = vertical();
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(12), dp(20), dp(12), dp(20));
        hero.addView(text("距离 64 KG", 14, true, Color.rgb(195, 198, 188)));
        hero.addView(space(4));
        LinearLayout heroLine = row();
        heroLine.setGravity(Gravity.CENTER);
        gapHeroValue = text("--", 56, true, Color.WHITE);
        heroLine.addView(gapHeroValue);
        TextView kg = text("kg", 16, true, LIME);
        LinearLayout.LayoutParams kgp = new LinearLayout.LayoutParams(-2, -2);
        kgp.setMargins(dp(8), dp(24), 0, 0);
        heroLine.addView(kg, kgp);
        hero.addView(heroLine);
        hero.addView(space(2));
        hero.addView(text("目标不是一天完成，是每天靠近一点", 12, false, Color.rgb(195, 198, 188)));
        root.addView(card(hero, INK));
        root.addView(space(12));

        LinearLayout mini = row();
        weightMiniValue = miniStat(mini, "当前体重", "-- kg");
        countdownMiniValue = miniStat(mini, "目标倒数", "-- 天");
        liquidValue = miniStat(mini, "本周液断", "剩 2 次");
        root.addView(card(mini, Color.WHITE));

        section("① 睡眠", "先滑动选择入睡和起床时间，运动建议会自动联动");
        root.addView(buildSleepCard());

        section("② 吃饭", "每一种都同时显示“拳数”和热量估算");
        root.addView(text("统一换算：主食 1拳≈180 kcal｜蛋白质 1拳≈160 kcal｜蔬菜 1拳≈50 kcal｜其他/高油 1拳≈220 kcal", 12, false, MUTED));
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

        section("③ 运动", "根据睡眠 + 今日摄入 + 当前体重动态换算");
        activityPlanValue = text("", 15, true, INK);
        activityPlanValue.setLineSpacing(0, 1.35f);
        root.addView(card(activityPlanValue, Color.WHITE));
        root.addView(space(10));

        LinearLayout exerciseCard = vertical();
        exerciseCard.addView(text("填写实际完成量", 15, true, INK));
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

        section("体重记录", "记录后最上方会立即更新距离 64KG 还有多少");
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
        updateSleepDuration();
        updateDynamicPlan();
    }

    private View buildSleepCard() {
        LinearLayout box = vertical();

        int sleepH = prefs.getInt(dayKey("sleep_hour"), 0);
        int sleepM = prefs.getInt(dayKey("sleep_minute"), 30);
        int wakeH = prefs.getInt(dayKey("wake_hour"), 8);
        int wakeM = prefs.getInt(dayKey("wake_minute"), 0);

        LinearLayout sleepRow = row();
        TextView sleepLabel = text("睡", 20, true, INK);
        sleepRow.addView(sleepLabel, new LinearLayout.LayoutParams(dp(44), -2));
        sleepHourPicker = timePicker(0, 23, sleepH);
        sleepMinutePicker = timePicker(0, 59, sleepM);
        sleepRow.addView(sleepHourPicker, new LinearLayout.LayoutParams(0, dp(116), 1));
        sleepRow.addView(text("时", 14, true, MUTED));
        sleepRow.addView(sleepMinutePicker, new LinearLayout.LayoutParams(0, dp(116), 1));
        sleepRow.addView(text("分", 14, true, MUTED));
        box.addView(sleepRow);

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(232, 230, 220));
        box.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

        LinearLayout wakeRow = row();
        TextView wakeLabel = text("起", 20, true, INK);
        wakeRow.addView(wakeLabel, new LinearLayout.LayoutParams(dp(44), -2));
        wakeHourPicker = timePicker(0, 23, wakeH);
        wakeMinutePicker = timePicker(0, 59, wakeM);
        wakeRow.addView(wakeHourPicker, new LinearLayout.LayoutParams(0, dp(116), 1));
        wakeRow.addView(text("时", 14, true, MUTED));
        wakeRow.addView(wakeMinutePicker, new LinearLayout.LayoutParams(0, dp(116), 1));
        wakeRow.addView(text("分", 14, true, MUTED));
        box.addView(wakeRow);

        sleepDurationValue = text("共 -- 小时 -- 分钟", 17, true, INK);
        sleepDurationValue.setGravity(Gravity.CENTER);
        sleepDurationValue.setPadding(dp(10), dp(10), dp(10), dp(10));
        sleepDurationValue.setBackground(round(LIME));
        box.addView(space(8));
        box.addView(sleepDurationValue);
        box.addView(space(8));
        box.addView(text("上下滑动数字调时间。跨过午夜会自动按第二天起床计算。", 11, false, MUTED));

        NumberPicker.OnValueChangeListener listener = (picker, oldVal, newVal) -> {
            if (rendering) return;
            prefs.edit()
                    .putInt(dayKey("sleep_hour"), sleepHourPicker.getValue())
                    .putInt(dayKey("sleep_minute"), sleepMinutePicker.getValue())
                    .putInt(dayKey("wake_hour"), wakeHourPicker.getValue())
                    .putInt(dayKey("wake_minute"), wakeMinutePicker.getValue())
                    .apply();
            updateSleepDuration();
            updateDynamicPlan();
        };
        sleepHourPicker.setOnValueChangedListener(listener);
        sleepMinutePicker.setOnValueChangedListener(listener);
        wakeHourPicker.setOnValueChangedListener(listener);
        wakeMinutePicker.setOnValueChangedListener(listener);

        return card(box, Color.WHITE);
    }

    private NumberPicker timePicker(int min, int max, int value) {
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setValue(Math.max(min, Math.min(max, value)));
        picker.setWrapSelectorWheel(true);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setFormatter(v -> String.format(Locale.CHINA, "%02d", v));
        return picker;
    }

    private void updateSleepDuration() {
        if (sleepDurationValue == null || sleepHourPicker == null) return;
        int minutes = sleepDurationMinutes();
        int h = minutes / 60;
        int m = minutes % 60;
        sleepDurationValue.setText("共 " + h + " 小时 " + m + " 分钟");
        prefs.edit().putInt(dayKey("sleep_total_minutes"), minutes).apply();
    }

    private int sleepDurationMinutes() {
        if (sleepHourPicker == null) return prefs.getInt(dayKey("sleep_total_minutes"), 0);
        int start = sleepHourPicker.getValue() * 60 + sleepMinutePicker.getValue();
        int end = wakeHourPicker.getValue() * 60 + wakeMinutePicker.getValue();
        int diff = end - start;
        if (diff < 0) diff += 24 * 60;
        if (diff == 0) return 0;
        return diff;
    }

    private float sleepHours() {
        return sleepDurationMinutes() / 60f;
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
        float sleep = sleepHours();
        float weight = prefs.getFloat("weight", 0f);

        intakeValue.setText("今日摄入估算：" + intake + " kcal");
        prefs.edit().putInt(dayKey("estimated_intake"), intake).apply();

        if (weight <= 0) {
            activityPlanValue.setText("先记录体重后，我才能把今天的建议活动量换算成打球时长、快走步数和跑步圈数。");
            actualActivityValue.setText("记录体重后再计算运动消耗。");
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
        else sleepNote = "先设置睡眠时间，运动建议会继续动态调整。";

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

    private boolean isLiquidToday() {
        return liquidDates().contains(LocalDate.now().toString());
    }

    private void refreshStats() {
        float weight = prefs.getFloat("weight", 0f);
        float gap = weight == 0 ? 0 : Math.max(0, weight - TARGET);
        gapHeroValue.setText(weight == 0 ? "--" : trim(gap));
        weightMiniValue.setText(weight == 0 ? "-- kg" : trim(weight) + " kg");
        long days = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), goalDate));
        countdownMiniValue.setText(days + " 天");
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
