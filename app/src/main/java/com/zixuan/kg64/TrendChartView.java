package com.zixuan.kg64;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class TrendChartView extends View {
    public static class Point {
        public final String label;
        public final float value;
        public final String detail;
        public Point(String label, float value, String detail) {
            this.label = label;
            this.value = value;
            this.detail = detail;
        }
    }

    public interface OnPointClickListener {
        void onPointClick(Point point);
    }

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Point> points = new ArrayList<>();
    private final List<Float> pointXs = new ArrayList<>();
    private final List<Float> pointYs = new ArrayList<>();
    private OnPointClickListener listener;

    public TrendChartView(Context context) {
        super(context);
        linePaint.setColor(0xff171916);
        linePaint.setStrokeWidth(dp(2.2f));
        linePaint.setStyle(Paint.Style.STROKE);
        pointPaint.setColor(0xffd8ff52);
        pointPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(0xff696c64);
        textPaint.setTextSize(dp(11));
        textPaint.setTextAlign(Paint.Align.CENTER);
        gridPaint.setColor(0xffe8e6dd);
        gridPaint.setStrokeWidth(dp(1));
        setMinimumHeight(Math.round(dp(190)));
    }

    public void setData(List<Point> data) {
        points.clear();
        if (data != null) points.addAll(data);
        invalidate();
    }

    public void setOnPointClickListener(OnPointClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        pointXs.clear();
        pointYs.clear();
        if (points.isEmpty()) return;

        float left = dp(18), right = getWidth() - dp(18), top = dp(18), bottom = getHeight() - dp(34);
        float chartH = bottom - top;
        for (int i = 0; i <= 4; i++) {
            float y = top + chartH * i / 4f;
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        float step = points.size() == 1 ? 0 : (right - left) / (points.size() - 1f);
        Path path = new Path();
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            float x = points.size() == 1 ? (left + right) / 2f : left + step * i;
            float clamped = Math.max(0, Math.min(100, p.value));
            float y = bottom - chartH * (clamped / 100f);
            pointXs.add(x); pointYs.add(y);
            if (i == 0) path.moveTo(x, y);
            else {
                float px = pointXs.get(i - 1), py = pointYs.get(i - 1);
                float mid = (px + x) / 2f;
                path.cubicTo(mid, py, mid, y, x, y);
            }
        }
        canvas.drawPath(path, linePaint);

        for (int i = 0; i < points.size(); i++) {
            float x = pointXs.get(i), y = pointYs.get(i);
            canvas.drawCircle(x, y, dp(6), pointPaint);
            canvas.drawCircle(x, y, dp(6), linePaint);
            canvas.drawText(points.get(i).label, x, getHeight() - dp(10), textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || points.isEmpty()) return true;
        float best = Float.MAX_VALUE;
        int bestIndex = -1;
        for (int i = 0; i < pointXs.size(); i++) {
            float dx = event.getX() - pointXs.get(i);
            float dy = event.getY() - pointYs.get(i);
            float d = dx * dx + dy * dy;
            if (d < best) { best = d; bestIndex = i; }
        }
        if (bestIndex >= 0 && best <= dp(34) * dp(34) && listener != null) {
            listener.onPointClick(points.get(bestIndex));
        }
        return true;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
