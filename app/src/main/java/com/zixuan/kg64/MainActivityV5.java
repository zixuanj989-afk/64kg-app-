package com.zixuan.kg64;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
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

public class MainActivityV5 extends Activity {
    private static final int INK = Color.rgb(23, 25, 22);
    private static final int CREAM = Color.rgb(245, 243, 234);
    private static final int LIME = Color.rgb(216, 255, 82);
    private static final int MUTED = Color.rgb(105, 108, 100);
    private static final float TARGET = 64f;

    private final LocalDate goalDate = LocalDate.of(2026, 10, 1);
    private SharedPreferences prefs;
    private LinearLayout root;

    private TextView gapValue;
    private TextView countdownValue;
    private TextView currentWeightValue;
    private TextView liquidValue;
    private TextView sleepDurationValue;
    private TextView intakeValue;
    private TextView activityPlanValue;
    private TextView actualActivityValue;
    private TextView trendValue;

    private NumberPicker sleepHour;
    private NumberPicker sleepMinute;
    private NumberPicker wakeHour;
    private NumberPicker wakeMinute;

    private final EditText[][] mealInputs = new EditText[4][4];
    private EditText ballMinutes;
    private EditText walkSteps;
    private EditText runLaps;
    private boolean rendering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("kg64_data", MODE_PRIVATE);
        getWindow().setStatusBarColor(CREAM);
        getWindow().setNavigationBarColor(INK);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        render();
    }

    private void render() {
        rendering = true;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(CREAM);
        applySafeInsets(scroll);

        root = column();
        root.setPadding(dp(18), dp(16), dp(18), dp(36));
        scroll.addView(root);

        TextView brand = text("64KG / DAILY", 13, true, INK);
        brand.setLetterSpacing(.14f);
        root.addView(brand);
        root.addView(space(7));
        root.addView(text("睡好 → 吃清楚 → 动起来", 25, true, INK));
        root.addView(space(18));

        LinearLayout hero = row();
        LinearLayout left = heroStat("距离 64 KG", "--", "kg");
        gapValue = (TextView) ((LinearLayout) left.getChildAt(1)).getChildAt(0);
        hero.addView(left, new LinearLayout.LayoutParams(0, dp(150), 1));

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(70, 72, 66));
        hero.addView(divider, new LinearLayout.LayoutParams(dp(1), dp(92)));

        LinearLayout right = heroStat("目标倒数", "--", "天");
        countdownValue = (TextView) ((LinearLayout) right.getChildAt(1)).getChildAt(0);
        hero.addView(right, new LinearLayout.LayoutParams(0, dp(150), 1));
        root.addView(card(hero, INK));
        root.addView(space(12));

        LinearLayout mini = row();
        currentWeightValue = miniStat(mini, "当前体重", "-- kg");
        miniStat(mini, "目标日期", "10 / 01");
        liquidValue = miniStat(mini, "本周液断", "剩 2 次");
        root.addView(card(mini, Color.WHITE));

        section("① 睡眠", "上下滑动选择入睡和起床时间");
        root.addView(buildSleepCard());

        section("② 吃饭", "每餐先看建议量，再填写实际拳数");
        root.addView(text("主食 1拳≈180 kcal｜蛋白质 1拳≈160 kcal｜蔬菜 1拳≈50 kcal｜其他/高油 1拳≈220 kcal", 12, false, MUTED));
        root.addView(space(10));

        root.addView(mealCard(0, "早餐", "breakfast", "建议：主食 0.5–1拳｜蛋白质 0.5–1拳｜蔬菜 0–1拳｜其他 0–0.5拳｜约 300–350 kcal"));
        root.addView(space(8));
        root.addView(mealCard(1, "午餐", "lunch", "建议：主食 1拳｜蛋白质 1拳｜蔬菜 2拳｜其他 0–0.5拳｜约 500–550 kcal"));
        root.addView(space(8));
        root.addView(mealCard(2, "晚餐", "dinner", "建议：主食 0.5–1拳｜蛋白质 1拳｜蔬菜 2拳｜其他 0–0.5拳｜约 400–450 kcal"));
        root.addView(space(8));
        root.addView(mealCard(3, "加餐", "snack", "建议：总量约 0.25–0.5拳，优先少油少糖｜约 100–150 kcal"));

        root.addView(space(10));
        intakeValue = text("今日摄入估算：0 kcal", 18, true, INK);
        intakeValue.setPadding(dp(14), dp(13), dp(14), dp(13));
        intakeValue.setBackground(round(LIME));
        root.addView(intakeValue);

        section("③ 运动", "根据睡眠 + 摄入 + 体重动态换算");
        activityPlanValue = text("", 15, true, INK);
        activityPlanValue.setLineSpacing(0, 1.35f);
        root.addView(card(activityPlanValue, Color.WHITE));
        root.addView(space(10));

        LinearLayout exercise = column();
        exercise.addView(text("填写实际完成量", 15, true, INK));
        exercise.addView(space(8));
        ballMinutes = numberInput("打球：分钟");
        ballMinutes.setText(pref("ball_minutes_actual"));
        watch(ballMinutes, "ball_minutes_actual");
        exercise.addView(ballMinutes, new LinearLayout.LayoutParams(-1, dp(50)));
        exercise.addView(space(7));
        walkSteps = numberInput("快走：步数");
        walkSteps.setText(pref("walk_steps_actual"));
        watch(walkSteps, "walk_steps_actual");
        exercise.addView(walkSteps, new LinearLayout.LayoutParams(-1, dp(50)));
        exercise.addView(space(7));
        runLaps = decimalInput("跑步：400m 操场圈数");
        runLaps.setText(pref("run_laps_actual"));
        watch(runLaps, "run_laps_actual");
        exercise.addView(runLaps, new LinearLayout.LayoutParams(-1, dp(50)));
        exercise.addView(space(8));
        actualActivityValue = text("", 13, true, INK);
        actualActivityValue.setLineSpacing(0, 1.3f);
        exercise.addView(actualActivityValue);
        root.addView(card(exercise, Color.WHITE));

        section("体重记录", "记录后上方两个视觉中心会立即更新");
        LinearLayout weightRow = row();
        EditText weightInput = decimalInput("输入体重 kg");
        weightRow.addView(weightInput, new LinearLayout.LayoutParams(0, dp(52), 1));
        Button save = button("记录", LIME, INK);
        save.setOnClickListener(v -> saveWeight(weightInput));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(dp(92), dp(52));
        saveParams.setMargins(dp(10), 0, 0, 0);
        weightRow.addView(save, saveParams);
        root.addView(weightRow);

        section("体重趋势", "最近记录");
        trendValue = text("", 15, true, INK);
        trendValue.setLineSpacing(0, 1.4f);
        root.addView(card(trendValue, Color.WHITE));

        section("液断日", "每周最多 2 次");
        LinearLayout liquid = column();
        TextView todayLiquid = text(isLiquidToday() ? "今天是液断日" : "今天正常饮食", 19, true, INK);
        liquid.addView(todayLiquid);
        liquid.addView(space(10));
        Button toggle = button(isLiquidToday() ? "取消今天液断" : "设为今天液断", LIME, INK);
        toggle.setOnClickListener(v -> toggleLiquid());
        liquid.addView(toggle, new LinearLayout.LayoutParams(-1, dp(50)));
        root.addView(card(liquid, Color.WHITE));

        setContentView(scroll);
        refreshStats();
        rendering = false;
        updateSleepDuration();
        updateDynamicPlan();
    }

    private void applySafeInsets(View view) {
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            int top;
            int bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            v.setPadding(0, top, 0, bottom);
            return insets;
        });
    }

    private View buildSleepCard() {
        LinearLayout box = column();
        int sh = prefs.getInt(dayKey("sleep_hour"), 0);
        int sm = prefs.getInt(dayKey("sleep_minute"), 30);
        int wh = prefs.getInt(dayKey("wake_hour"), 8);
        int wm = prefs.getInt(dayKey("wake_minute"), 0);

        box.addView(timeRow("睡", sh, sm, true));
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(232, 230, 220));
        box.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
        box.addView(timeRow("起", wh, wm, false));

        sleepDurationValue = text("共 -- 小时 -- 分钟", 17, true, INK);
        sleepDurationValue.setGravity(Gravity.CENTER);
        sleepDurationValue.setPadding(dp(10), dp(10), dp(10), dp(10));
        sleepDurationValue.setBackground(round(LIME));
        box.addView(space(8));
        box.addView(sleepDurationValue);
        box.addView(space(8));
        box.addView(text("滚轮按 XX:XX 显示；跨午夜自动按第二天起床计算。", 11, false, MUTED));

        NumberPicker.OnValueChangeListener listener = (picker, oldVal, newVal) -> {
            if (rendering) return;
            prefs.edit()
                    .putInt(dayKey("sleep_hour"), sleepHour.getValue())
                    .putInt(dayKey("sleep_minute"), sleepMinute.getValue())
                    .putInt(dayKey("wake_hour"), wakeHour.getValue())
                    .putInt(dayKey("wake_minute"), wakeMinute.getValue())
                    .apply();
            updateSleepDuration();
            updateDynamicPlan();
        };
        sleepHour.setOnValueChangedListener(listener);
        sleepMinute.setOnValueChangedListener(listener);
        wakeHour.setOnValueChangedListener(listener);
        wakeMinute.setOnValueChangedListener(listener);
        return card(box, Color.WHITE);
    }

    private LinearLayout timeRow(String label, int hour, int minute, boolean isSleep) {
        LinearLayout row = row();
        row.addView(text(label, 20, true, INK), new LinearLayout.LayoutParams(dp(48), -2));

        NumberPicker hourPicker = picker(0, 23, hour);
        NumberPicker minutePicker = picker(0, 59, minute);
        if (isSleep) {
            sleepHour = hourPicker;
            sleepMinute = minutePicker;
        } else {
            wakeHour = hourPicker;
            wakeMinute = minutePicker;
        }

        row.addView(hourPicker, new LinearLayout.LayoutParams(0, dp(112), 1));
        TextView colon = text(":", 30, true, INK);
        colon.setGravity(Gravity.CENTER);
        row.addView(colon, new LinearLayout.LayoutParams(dp(28), dp(112)));
        row.addView(minutePicker, new LinearLayout.LayoutParams(0, dp(112), 1));
        return row;
    }

    private NumberPicker picker(int min, int max, int value) {
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setValue(Math.max(min, Math.min(max, value)));
        picker.setWrapSelectorWheel(true);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setFormatter(v -> String.format(Locale.CHINA, "%02d", v));
        return picker;
    }

    private View mealCard(int index, String title, String key, String recommendation) {
        LinearLayout box = column();
        box.addView(text(title, 18, true, INK));
        box.addView(space(6));

        TextView rec = text(recommendation, 12, true, INK);
        rec.setPadding(dp(10), dp(8), dp(10), dp(8));
        rec.setBackground(round(LIME));
        box.addView(rec);
        box.addView(space(9));

        String[] labels = {
                "主食：实际几拳（1拳≈180 kcal）",
                "蛋白质：实际几拳（1拳≈160 kcal）",
                "蔬菜：实际几拳（1拳≈50 kcal）",
                "其他/高油：实际几拳（1拳≈220 kcal）"
        };
        String[] suffixes = {"_staple", "_protein", "_veg", "_other"};
        for (int i = 0; i < 4; i++) {
            EditText input = decimalInput(labels[i]);
            input.setText(pref(key + suffixes[i]));
            watch(input, key + suffixes[i]);
            mealInputs[index][i] = input;
            box.addView(input, new LinearLayout.LayoutParams(-1, dp(48)));
            if (i < 3) box.addView(space(6));
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

    private void updateSleepDuration() {
        if (sleepDurationValue == null || sleepHour == null) return;
        int minutes = sleepMinutes();
        sleepDurationValue.setText("共 " + minutes / 60 + " 小时 " + minutes % 60 + " 分钟");
        prefs.edit().putInt(dayKey("sleep_total_minutes"), minutes).apply();
    }

    private int sleepMinutes() {
        if (sleepHour == null) return prefs.getInt(dayKey("sleep_total_minutes"), 0);
        int start = sleepHour.getValue() * 60 + sleepMinute.getValue();
        int end = wakeHour.getValue() * 60 + wakeMinute.getValue();
        int diff = end - start;
        if (diff < 0) diff += 24 * 60;
        return diff;
    }

    private void updateDynamicPlan() {
        if (intakeValue == null || activityPlanValue == null || actualActivityValue == null) return;

        int intake = Math.round(mealCalories());
        float sleepHours = sleepMinutes() / 60f;
        float weight = prefs.getFloat("weight", 0f);
        intakeValue.setText("今日摄入估算：" + intake + " kcal");

        if (weight <= 0) {
            activityPlanValue.setText("先记录体重后，我才能把建议活动量换算成打球时长、快走步数和跑步圈数。");
            actualActivityValue.setText("记录体重后再计算运动消耗。");
            return;
        }

        int target = ActivityPlanner.suggestedActivityKcal(intake, sleepHours);
        int ballTarget = ActivityPlanner.basketballMinutesFor(target, weight);
        int walkTarget = ActivityPlanner.briskWalkStepsFor(target, weight);
        float runTarget = ActivityPlanner.runningLapsFor(target, weight);

        String sleepNote;
        if (sleepHours > 0 && sleepHours < 6f) sleepNote = "睡眠不足 6 小时，今天自动降低运动建议。";
        else if (sleepHours >= 6f && sleepHours < 7f) sleepNote = "睡眠略少，运动建议已适度下调。";
        else if (sleepHours >= 7f) sleepNote = "睡眠达到较稳定区间。";
        else sleepNote = "先设置睡眠时间，运动建议会继续动态调整。";

        activityPlanValue.setText(
                "建议活动目标：约 " + target + " kcal\n" +
                "≈ 打球 " + formatMinutes(ballTarget) + "\n" +
                "≈ 快走 " + walkTarget + " 步\n" +
                "≈ 跑步 " + oneDecimal(runTarget) + " 圈（400m/圈）\n" + sleepNote
        );

        int ballKcal = ActivityPlanner.metCalories(6f, weight, intValue(ballMinutes));
        int walkKcal = ActivityPlanner.briskWalkCalories(weight, intValue(walkSteps));
        int runKcal = ActivityPlanner.runningCalories(weight, floatValue(runLaps));
        int actual = ballKcal + walkKcal + runKcal;
        int remaining = Math.max(0, target - actual);

        actualActivityValue.setText(
                "实际运动消耗估算：" + actual + " kcal\n" +
                "打球 " + ballKcal + " + 快走 " + walkKcal + " + 跑步 " + runKcal + " kcal\n" +
                (remaining == 0 ? "已达到今天建议活动目标" : "距离建议活动目标还差约 " + remaining + " kcal")
        );
    }

    private float mealCalories() {
        float total = 0f;
        for (EditText[] meal : mealInputs) {
            total += floatValue(meal[0]) * 180f;
            total += floatValue(meal[1]) * 160f;
            total += floatValue(meal[2]) * 50f;
            total += floatValue(meal[3]) * 220f;
        }
        return total;
    }

    private void saveWeight(EditText field) {
        String raw = field.getText().toString().trim();
        if (raw.isEmpty()) { toast("先输入今天的体重"); return; }
        float value;
        try { value = Float.parseFloat(raw); }
        catch (Exception e) { toast("体重格式不对"); return; }
        if (value < 35 || value > 200) { toast("请确认体重数值"); return; }

        String today = LocalDate.now().toString();
        List<String> history = new ArrayList<>();
        for (String line : prefs.getString("history", "").split(";")) {
            if (!line.isEmpty() && !line.startsWith(today + ",")) history.add(line);
        }
        history.add(today + "," + value);
        prefs.edit().putFloat("weight", value).putString("history", String.join(";", history)).apply();
        field.setText("");
        refreshStats();
        updateDynamicPlan();
        toast("已记录 " + trim(value) + " kg");
    }

    private void toggleLiquid() {
        String today = LocalDate.now().toString();
        List<String> dates = liquidDates();
        if (dates.contains(today)) dates.remove(today);
        else if (weekLiquidCount(dates) >= 2) { toast("本周已经安排 2 次液断"); return; }
        else dates.add(today);
        prefs.edit().putString("liquid_dates", String.join(",", dates)).apply();
        render();
    }

    private List<String> liquidDates() {
        List<String> dates = new ArrayList<>();
        for (String s : prefs.getString("liquid_dates", "").split(",")) if (!s.isEmpty()) dates.add(s);
        return dates;
    }

    private int weekLiquidCount(List<String> dates) {
        int count = 0;
        WeekFields wf = WeekFields.of(Locale.CHINA);
        LocalDate now = LocalDate.now();
        for (String s : dates) {
            try {
                LocalDate date = LocalDate.parse(s);
                if (date.get(wf.weekOfWeekBasedYear()) == now.get(wf.weekOfWeekBasedYear()) &&
                        date.get(wf.weekBasedYear()) == now.get(wf.weekBasedYear())) count++;
            } catch (Exception ignored) {}
        }
        return count;
    }

    private boolean isLiquidToday() {
        return liquidDates().contains(LocalDate.now().toString());
    }

    private void refreshStats() {
        float weight = prefs.getFloat("weight", 0f);
        gapValue.setText(weight == 0 ? "--" : trim(Math.max(0, weight - TARGET)));
        countdownValue.setText(String.valueOf(Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), goalDate))));
        currentWeightValue.setText(weight == 0 ? "-- kg" : trim(weight) + " kg");
        liquidValue.setText("剩 " + Math.max(0, 2 - weekLiquidCount(liquidDates())) + " 次");

        String raw = prefs.getString("history", "");
        if (raw.isEmpty()) trendValue.setText("还没有体重记录");
        else {
            String[] lines = raw.split(";");
            StringBuilder out = new StringBuilder();
            for (int i = Math.max(0, lines.length - 7); i < lines.length; i++) {
                String[] parts = lines[i].split(",");
                if (parts.length == 2) out.append(parts[0].substring(5)).append("   ").append(parts[1]).append(" kg\n");
            }
            trendValue.setText(out.toString().trim());
        }
    }

    private LinearLayout heroStat(String label, String value, String unit) {
        LinearLayout box = column();
        box.setGravity(Gravity.CENTER);
        box.addView(text(label, 13, true, Color.rgb(195, 198, 188)));
        LinearLayout line = row();
        line.setGravity(Gravity.CENTER);
        TextView number = text(value, 48, true, Color.WHITE);
        line.addView(number);
        TextView u = text(unit, 14, true, LIME);
        LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(-2, -2);
        up.setMargins(dp(7), dp(19), 0, 0);
        line.addView(u, up);
        box.addView(line);
        return box;
    }

    private TextView miniStat(LinearLayout parent, String label, String value) {
        LinearLayout box = column();
        box.setGravity(Gravity.CENTER);
        box.addView(text(label, 11, false, MUTED));
        TextView v = text(value, 16, true, INK);
        box.addView(v);
        parent.addView(box, new LinearLayout.LayoutParams(0, dp(78), 1));
        return v;
    }

    private String pref(String key) { return prefs.getString(dayKey(key), ""); }
    private String dayKey(String key) { return key + "_" + LocalDate.now(); }

    private int intValue(EditText field) {
        try { return Integer.parseInt(field.getText().toString().trim()); }
        catch (Exception e) { return 0; }
    }

    private float floatValue(EditText field) {
        if (field == null) return 0f;
        try { return Float.parseFloat(field.getText().toString().trim()); }
        catch (Exception e) { return 0f; }
    }

    private String formatMinutes(int minutes) {
        if (minutes < 60) return minutes + " 分钟";
        int hours = minutes / 60;
        int remain = minutes % 60;
        return remain == 0 ? hours + " 小时" : hours + " 小时 " + remain + " 分钟";
    }

    private String oneDecimal(float value) { return String.format(Locale.CHINA, "%.1f", value); }
    private String trim(float value) { return value == (long) value ? String.valueOf((long) value) : String.format(Locale.CHINA, "%.1f", value); }

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

    private LinearLayout column() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        return v;
    }

    private TextView text(String value, int sp, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private EditText decimalInput(String hint) {
        return input(hint, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    private EditText numberInput(String hint) { return input(hint, InputType.TYPE_CLASS_NUMBER); }

    private EditText input(String hint, int type) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextSize(14);
        field.setInputType(type);
        field.setSingleLine();
        field.setPadding(dp(15), 0, dp(15), 0);
        field.setBackground(round(Color.WHITE));
        return field;
    }

    private Button button(String label, int bg, int fg) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(fg);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setBackground(round(bg));
        return button;
    }

    private View card(View content, int color) {
        LinearLayout card = column();
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(round(color));
        card.addView(content, new LinearLayout.LayoutParams(-1, -2));
        return card;
    }

    private GradientDrawable round(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(15));
        return drawable;
    }

    private View space(int height) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(height)));
        return v;
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
