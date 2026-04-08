package org.tcshare.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import org.tcshare.androidutils.R;


/**
 * @Description TODO
 * @Author B站：千古八方的玩具
 * @CreateTime 2025年11月14日 15:02:39
 */
public class BatteryView extends AppCompatImageView {
    public static final float DEFAULT_BORDER_WIDTH = 0.06f;
    public static final float DEFAULT_INTERNAL_SPACING = 0.5f;
    private final Context context;

    private final Paint batteryLevelPaint = new Paint();
    private final RectF rect = new RectF(0f, 0f, 0f, 0f);
    private float borderThickness = 0f;
    private float batteryLevelCornerRadius = 0f;
    private int batteryLevel = 0;
    private boolean isCharging = false;
    private int infillColor = Color.WHITE;
    private int borderColor = Color.BLACK;
    private float internalSpacing = DEFAULT_INTERNAL_SPACING;
    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (attrs != null) {
            obtainAttributes(attrs);
        }
        super.setScaleType(ScaleType.FIT_XY);
        super.setImageResource(R.drawable.ic_battery);

        batteryLevelPaint.setColor(infillColor);
        batteryLevelPaint.setAntiAlias(true);

        setWillNotDraw(false);
    }

    public static int limit(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    public void setBatteryLevel(int value) {
        batteryLevel = limit(value, 0, 100);
        invalidate();
    }

    public void setCharging(boolean value) {
        isCharging = value;
        setImageResource(isCharging ? R.drawable.ic_charging : R.drawable.ic_battery);
        if (isCharging) {
            setColorFilter(infillColor);
        } else {
            setColorFilter(borderColor);
        }
        invalidate();
    }

    private void obtainAttributes(AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BatteryView,
                0, 0
        );
        try {
            isCharging = a.getBoolean(R.styleable.BatteryView_bv_charging, false);
            borderColor = a.getColor(R.styleable.BatteryView_bv_borderColor, Color.BLACK);
            infillColor = a.getColor(R.styleable.BatteryView_bv_infillColor, Color.WHITE);
            batteryLevel = a.getInteger(R.styleable.BatteryView_bv_percent, 0);
            internalSpacing = a.getFloat(R.styleable.BatteryView_bv_internalSpacing, DEFAULT_INTERNAL_SPACING);
        } finally {
            a.recycle();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpec = widthMeasureSpec;
        int heightSpec = heightMeasureSpec;
        int min = Math.min(MeasureSpec.getSize(widthSpec), MeasureSpec.getSize(heightSpec));
        widthSpec = MeasureSpec.makeMeasureSpec((int) ((min * 17f) / 22f), MeasureSpec.EXACTLY);
        heightSpec = MeasureSpec.makeMeasureSpec((int) ((min * 22f) / 17f), MeasureSpec.EXACTLY);
        borderThickness = min * DEFAULT_BORDER_WIDTH;
        batteryLevelCornerRadius = borderThickness;
        super.onMeasure(widthSpec, heightSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isCharging) {
            float rectMarginBorderMultiplier = 1 + internalSpacing;
            float width = getWidth() - 2 * rectMarginBorderMultiplier * borderThickness;
            float height = (getHeight() - (2 + 2 * rectMarginBorderMultiplier) * borderThickness) * batteryLevel / 100f;

            float left = rectMarginBorderMultiplier * borderThickness;
            float top = getHeight() - borderThickness * rectMarginBorderMultiplier - height;

            float right = left + width;
            float bottom = top + height;
            rect.set(left, top, right, bottom);
            canvas.drawRoundRect(
                    rect,
                    batteryLevelCornerRadius,
                    batteryLevelCornerRadius,
                    batteryLevelPaint
            );
        }
        super.onDraw(canvas);
    }
}
