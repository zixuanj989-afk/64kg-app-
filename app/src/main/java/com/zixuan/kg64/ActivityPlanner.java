package com.zixuan.kg64;

final class ActivityPlanner {
    private ActivityPlanner() {}

    static int suggestedActivityKcal(int intakeKcal) {
        if (intakeKcal <= 0) return 0;
        int base = 180;
        int extra = intakeKcal > 1500 ? Math.round((intakeKcal - 1500) * 0.5f) : 0;
        return Math.min(450, base + extra);
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
}
