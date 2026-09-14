package com.zixuan.kg64;

final class HealthScorer {
    private HealthScorer() {}

    static int dietHealth(float[] mealValues) {
        if (mealValues == null || mealValues.length < 16) return 0;
        float score = 0f;
        score += mealScore(mealValues, 0, .5f, 1f, .5f, 1f, 0f, 1f, 0f, .5f);
        score += mealScore(mealValues, 4, .75f, 1.25f, .75f, 1.25f, 1.5f, 2.5f, 0f, .5f);
        score += mealScore(mealValues, 8, .5f, 1f, .75f, 1.25f, 1.5f, 2.5f, 0f, .5f);
        score += mealScore(mealValues, 12, 0f, .5f, 0f, .5f, 0f, .5f, 0f, .5f);
        return Math.round(score / 4f);
    }

    private static float mealScore(float[] v, int i,
                                   float sMin, float sMax,
                                   float pMin, float pMax,
                                   float gMin, float gMax,
                                   float oMin, float oMax) {
        return (rangeScore(v[i], sMin, sMax) + rangeScore(v[i+1], pMin, pMax)
                + rangeScore(v[i+2], gMin, gMax) + rangeScore(v[i+3], oMin, oMax)) / 4f;
    }

    private static float rangeScore(float value, float min, float max) {
        if (value >= min && value <= max) return 100f;
        if (value < min) {
            if (min == 0f) return 100f;
            return Math.max(0f, 100f - (min - value) / Math.max(.25f, min) * 70f);
        }
        return Math.max(0f, 100f - (value - max) / Math.max(.25f, max) * 70f);
    }

    static int fastingWindowScore(int firstMinute, int lastMinute) {
        if (firstMinute < 0 || lastMinute < 0) return 0;
        int span = lastMinute - firstMinute;
        if (span < 0) span += 24 * 60;
        if (span <= 8 * 60) return 100;
        if (span <= 9 * 60) return 75;
        if (span <= 10 * 60) return 50;
        return 25;
    }

    static int sleepHealth(int minutes) {
        if (minutes >= 7 * 60 && minutes <= 9 * 60) return 100;
        if (minutes >= 6 * 60 && minutes < 7 * 60) return 75;
        if (minutes > 9 * 60 && minutes <= 10 * 60) return 75;
        if (minutes >= 5 * 60 && minutes < 6 * 60) return 45;
        return minutes > 0 ? 30 : 0;
    }

    static int sleepStability(int bedtimeMinute, int wakeMinute, int avgBedMinute, int avgWakeMinute, boolean hasHistory) {
        if (!hasHistory) return 100;
        int bedDiff = circularDiff(bedtimeMinute, avgBedMinute);
        int wakeDiff = circularDiff(wakeMinute, avgWakeMinute);
        int avgDiff = (bedDiff + wakeDiff) / 2;
        if (avgDiff <= 30) return 100;
        if (avgDiff <= 60) return 80;
        if (avgDiff <= 90) return 60;
        if (avgDiff <= 120) return 40;
        return 20;
    }

    private static int circularDiff(int a, int b) {
        int d = Math.abs(a - b);
        return Math.min(d, 1440 - d);
    }

    static int exerciseHealth(int actualKcal, int targetKcal) {
        if (targetKcal <= 0) return 0;
        float ratio = actualKcal / (float) targetKcal;
        if (ratio >= .8f && ratio <= 1.35f) return 100;
        if (ratio >= .6f && ratio < .8f) return 80;
        if (ratio >= .4f && ratio < .6f) return 60;
        if (ratio > 1.35f && ratio <= 1.7f) return 75;
        return actualKcal > 0 ? 40 : 20;
    }

    static int exerciseStability(int actualKcal, int targetKcal) {
        if (targetKcal <= 0) return 0;
        float ratio = actualKcal / (float) targetKcal;
        if (ratio >= .7f && ratio <= 1.3f) return 100;
        if (ratio >= .5f && ratio < .7f) return 70;
        if (ratio > 1.3f && ratio <= 1.6f) return 70;
        return actualKcal > 0 ? 40 : 10;
    }

    static int waterCompletion(int ml, int goalMl) {
        if (goalMl <= 0) return 0;
        return Math.min(100, Math.round(ml * 100f / goalMl));
    }
}
