package com.zixuan.kg64;

final class ActivityPlanner {
    private ActivityPlanner() {}

    static int suggestedActivityKcal(int intakeKcal, float sleepHours) {
        if (intakeKcal <= 0) return 0;

        int target;
        if (intakeKcal <= 1200) {
            target = 120;
        } else if (intakeKcal <= 1500) {
            target = 160;
        } else {
            target = 160 + Math.round((intakeKcal - 1500) * 0.25f);
        }
        target = Math.min(350, target);

        if (sleepHours > 0 && sleepHours < 6f) {
            target = Math.min(target, 150);
        } else if (sleepHours >= 6f && sleepHours < 7f) {
            target = Math.min(target, 220);
        }
        return Math.max(80, target);
    }

    static int metCalories(float met, float weightKg, int minutes) {
        if (weightKg <= 0 || minutes <= 0) return 0;
        return Math.max(0, Math.round(met * 3.5f * weightKg / 200f * minutes));
    }

    static int basketballMinutesFor(int kcal, float weightKg) {
        float perMinute = 6.0f * 3.5f * weightKg / 200f;
        return perMinute <= 0 ? 0 : Math.max(1, Math.round(kcal / perMinute));
    }

    static int briskWalkStepsFor(int kcal, float weightKg) {
        float perMinute = 4.3f * 3.5f * weightKg / 200f;
        if (perMinute <= 0) return 0;
        int minutes = Math.max(1, Math.round(kcal / perMinute));
        return minutes * 105;
    }

    static float runningLapsFor(int kcal, float weightKg) {
        if (kcal <= 0 || weightKg <= 0) return 0f;
        float km = kcal / weightKg;
        return km / 0.4f;
    }

    static int runningCalories(float weightKg, float laps) {
        if (weightKg <= 0 || laps <= 0) return 0;
        float km = laps * 0.4f;
        return Math.max(0, Math.round(weightKg * km));
    }

    static int briskWalkCalories(float weightKg, int steps) {
        if (weightKg <= 0 || steps <= 0) return 0;
        int minutes = Math.max(1, Math.round(steps / 105f));
        return metCalories(4.3f, weightKg, minutes);
    }
}
