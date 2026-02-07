package org.tcshare.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;

/**
 * @author 爱T鱼
 * @date 2021年1月5日
 */
public class GaugeView extends View {
    protected int bgRound1 = Color.parseColor("#99122a73");
    protected int bgRound2 = Color.parseColor("#66122a73");
    protected int bgRound3 = Color.parseColor("#22142f80");
    protected int bgRound4 = Color.parseColor("#0e2267");
    protected int circle1 = Color.parseColor("#0d1d4c");
    protected int bgRadius1;
    protected int bgRadius2;
    protected int bgRadius3;
    protected int bgRadius4;
    protected int circleRadius1;
    protected int circleRadius2;

    // 背景圆画笔
    protected Paint roundPaint;
    protected int mWidth;
    protected int mHeight;
    protected int mCx;
    protected int mCy;
    protected int radius;
    // 刻度画笔
    protected Paint pointerPaint;
    protected float textSizeDial = 14; // 刻度字体大小
    protected Paint.FontMetrics pointerFontMetrics;
    // 标题画笔
    protected Paint labelPaint;
    // 长刻度线半径
    protected int radiusDial;
    protected float maxVal = 150f;
    protected float minVal = -50f;

    protected int lowDialLimit = 20; // 低段温度警戒线
    protected int highDialLimit = 80; // 中段警戒线
    protected int colorDialLower = Color.parseColor("#00faff"); // 低中高三个色段表盘刻度
    protected int colorDialMiddle = Color.GREEN;
    protected int colorDialHigh = Color.RED;

    protected int lengthLDial = 16;// 长刻度线的长度
    protected int lengthSDial = 8;// 短刻度线的长度
    protected float strokeLDial = 3;
    protected float strokeSDial = 1;
    protected float circleRadius1StrokeWidth = 2f;
    protected float circleRadius2StrokeWidth = 4f;
    protected float pointerLineDegree = 135; // 多少角度
    protected float arcStartDegree = pointerLineDegree; // 弧线起始角度
    protected Paint valPaint;

    protected Paint.FontMetrics labelFontMetrics;
    protected Paint.FontMetrics valFontMetrics;
    protected String valText = "";
    protected float realVal; // 真实的值
    protected float valTextSize = 40f;// 值字体大小
    protected int valTextColor = Color.WHITE;// 值字体颜色
    protected String labelText = "";
    protected float labelTextSize = 20f;// 标签字体
    protected int labelTextColor = Color.WHITE;// 标签字体颜色
    protected Paint arcPaint;
    protected RectF mArcRect = new RectF(), mCircleArcRect1 = new RectF(), mCircleArcRect2 = new RectF(); // 指针（半扇形）及描边线
    protected float arcPaintStrokeWidth = 2f; // 描边宽度
    protected int arcColor = Color.parseColor("#ff8e00");
    protected int[] colors = new int[]{Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, arcColor};
    protected int[] colorsArcLine = new int[]{arcColor, arcColor, Color.TRANSPARENT};
    protected Shader arcShader;
    protected Shader arcLineShader;
    protected float sweepAngle = 0;
    protected String formatter = "%.0f";
    protected int drawDialNum = 100;
    protected int dialStep = 10; // 分度
    protected float valRange;
    protected float stepDegree;
    protected float sRoateDegree;
    protected DecimalFormat decimalFormat = new DecimalFormat(".0");
    private Bitmap pLineCache; // 绘制点线缓存，该步骤耗时长

    public GaugeView(Context context) {
        super(context);
        init();
    }


    public GaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    protected void init() {
        // 初始化圆背景画笔
        roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 扫描扇
        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);


        // 刻度
        pointerPaint = new Paint();
        pointerPaint.setAntiAlias(true);
        pointerPaint.setTextSize(textSizeDial);
        pointerPaint.setTextAlign(Paint.Align.CENTER);
        pointerFontMetrics = pointerPaint.getFontMetrics();

        // 标题
        labelPaint = new Paint();
        labelPaint.setAntiAlias(true);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
        labelPaint.setTextSize(labelTextSize);
        labelPaint.setColor(labelTextColor);
        labelFontMetrics = labelPaint.getFontMetrics();

        // 值
        valPaint = new Paint();
        valPaint.setAntiAlias(true);
        valPaint.setTextAlign(Paint.Align.CENTER);
//        valPaint.setFakeBoldText(true);
        valPaint.setTextSize(valTextSize);
        valPaint.setColor(valTextColor);
        valFontMetrics = valPaint.getFontMetrics();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBG(canvas);
        drawArc(canvas);
        drawPointerLine(canvas);
        drawValText(canvas);
        drawLabelText(canvas);

    }

    private void drawArc(Canvas canvas) {
        canvas.save();
        arcPaint.setStyle(Paint.Style.FILL);
        arcPaint.setShader(arcShader);
        canvas.drawArc(mArcRect, arcStartDegree, sweepAngle, true, arcPaint);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setColor(arcColor);
        arcPaint.setShader(null);
        arcPaint.setStrokeWidth(arcPaintStrokeWidth);
        canvas.drawArc(mArcRect, arcStartDegree, sweepAngle, false, arcPaint);

        arcPaint.setStrokeWidth(arcPaintStrokeWidth);
        canvas.drawArc(mCircleArcRect1, arcStartDegree, sweepAngle, false, arcPaint);
        canvas.drawArc(mCircleArcRect2, arcStartDegree, sweepAngle, false, arcPaint);

        arcPaint.setStrokeWidth(arcPaintStrokeWidth);
//        arcPaint.setShader(arcLineShader);
        canvas.translate(mCx, mCy);
        canvas.rotate(arcStartDegree + sweepAngle);
        canvas.drawLine(radiusDial, 0, circleRadius2, 0, arcPaint);
        canvas.restore();
    }

    private void drawLabelText(Canvas canvas) {
        canvas.save();
        canvas.translate(mCx, mCy + (bgRadius3 + circleRadius1) / 2f);
        int textBaseLine = (int) (0 + (labelFontMetrics.bottom - labelFontMetrics.top) / 2 - labelFontMetrics.bottom);
        canvas.drawText(labelText, 0, textBaseLine, labelPaint);
        canvas.restore();
    }

    private void drawValText(Canvas canvas) {
        if (valText != null) {
            canvas.save();
            canvas.translate(mCx, mCy);
            int textBaseLine = (int) (0 + (valFontMetrics.bottom - valFontMetrics.top) / 2 - valFontMetrics.bottom);
            canvas.drawText(valText, 0, textBaseLine, valPaint);
            canvas.restore();
        }
    }

    private void drawPointerLine(Canvas c) {
        if(pLineCache == null) {
            pLineCache = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            Canvas pointerLineCacheCanvas = new Canvas(pLineCache);
//        pointerLineCacheCanvas.save();
            pointerLineCacheCanvas.translate(mCx, mCy);
            pointerLineCacheCanvas.rotate(pointerLineDegree);

            for (int i = 0; i <= valRange; i++) {

                if (i < lowDialLimit) {
                    pointerPaint.setColor(colorDialLower);
                } else if (i <= highDialLimit) {
                    pointerPaint.setColor(colorDialMiddle);
                } else {
                    pointerPaint.setColor(colorDialHigh);
                }

                if (i % dialStep == 0) {     //长表针
                    pointerPaint.setStrokeWidth(strokeLDial);
                    pointerLineCacheCanvas.drawLine(radiusDial, 0, radiusDial - lengthLDial, 0, pointerPaint);

//                String text = String.format("%.0f", minVal + i); // format 性能跟不上
                    drawPointerText(pointerLineCacheCanvas, decimalFormat.format(minVal + i), i);
                } else {    //短表针
                    pointerPaint.setStrokeWidth(strokeSDial);
                    int offset = (lengthLDial - lengthSDial) / 2;
                    pointerLineCacheCanvas.drawLine(radiusDial - offset, 0, radiusDial - lengthSDial, 0, pointerPaint);
                }
                pointerLineCacheCanvas.rotate(sRoateDegree);
            }
//        pointerLineCacheCanvas.restore();
        }
        c.drawBitmap(pLineCache,0,0, null);
    }

    private void drawPointerText(Canvas canvas, String text, int i) {
        canvas.save();

        int currentCenterX = (int) (radiusDial - lengthLDial - strokeLDial - pointerPaint.measureText(String.valueOf(text)) / 2);
        canvas.translate(currentCenterX, 0);
        canvas.rotate(360 - pointerLineDegree - i * sRoateDegree);

        int textBaseLine = (int) (0 + (pointerFontMetrics.bottom - pointerFontMetrics.top) / 2 - pointerFontMetrics.bottom);

        canvas.drawText(text, 0, textBaseLine, pointerPaint);
        canvas.restore();
    }


    private void drawBG(Canvas canvas) {
        canvas.save();
        roundPaint.setStyle(Paint.Style.FILL);
        roundPaint.setColor(bgRound1);
        roundPaint.setStrokeWidth(0);
        canvas.drawCircle(mCx, mCy, bgRadius1, roundPaint);
        roundPaint.setColor(bgRound2);
        canvas.drawCircle(mCx, mCy, bgRadius2, roundPaint);
        roundPaint.setColor(bgRound3);
        canvas.drawCircle(mCx, mCy, bgRadius3, roundPaint);
        roundPaint.setColor(bgRound4);
        canvas.drawCircle(mCx, mCy, bgRadius4, roundPaint);
        roundPaint.setColor(circle1);
        roundPaint.setStyle(Paint.Style.STROKE);
        roundPaint.setStrokeWidth(circleRadius1StrokeWidth);
        canvas.drawCircle(mCx, mCy, circleRadius1, roundPaint);
        roundPaint.setStrokeWidth(circleRadius2StrokeWidth);
        canvas.drawCircle(mCx, mCy, circleRadius2, roundPaint);
        canvas.restore();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取控件区域宽高
        int minimumWidth = getSuggestedMinimumWidth();
        int minimumHeight = getSuggestedMinimumHeight();
        mWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        mHeight = resolveMeasured(heightMeasureSpec, minimumHeight);
        // 获取x/y轴中心点
        mCx = mWidth / 2;
        mCy = mHeight / 2;
        radius = Math.min(mWidth, mHeight) / 2;
        bgRadius1 = radius;
        bgRadius2 = (int) (radius * 0.95f);
        bgRadius3 = (int) (radius * 0.88f);
        bgRadius4 = (int) (radius * 0.6f);
        circleRadius1 = (int) (radius * 0.64f);
        circleRadius2 = (int) (radius * 0.62f);

        // 长刻度线
        radiusDial = bgRadius3;

        mArcRect.set(mCx - radiusDial, mCy - radiusDial, mCx + radiusDial, mCy + radiusDial);
        mCircleArcRect1.set(mCx - circleRadius1, mCy - circleRadius1, mCx + circleRadius1, mCy + circleRadius1);
        mCircleArcRect2.set(mCx - circleRadius2, mCy - circleRadius2, mCx + circleRadius2, mCy + circleRadius2);
        arcShader = new RadialGradient(mCx, mCy, bgRadius2, colors, null, Shader.TileMode.REPEAT);
        arcLineShader = new LinearGradient(radiusDial, 0, circleRadius2, 0, colorsArcLine, null, Shader.TileMode.REPEAT);
        pLineCache = null; // 重建缓存画布， 绘制点线耗时长

    }


    /**
     * @param formatter 中间值格式化
     * @param maxVal 最大值
     * @param minVal 最小值
     * @param dialStep      必须能被最大范围整除 (maxVal - minVal) / dialStep = 0
     * @param lowDialLimit 警戒线1
     * @param highDialLimit 警戒线2
     */
    public void init(String formatter, float maxVal, float minVal, int dialStep, int lowDialLimit, int highDialLimit) {
        this.dialStep = dialStep;
        float drawDegree = (360 - (180 - pointerLineDegree) * 2);
        this.maxVal = maxVal;
        this.minVal = minVal;
        this.valRange = maxVal - minVal;
        this.formatter = formatter;
        this.drawDialNum = (int) Math.ceil(valRange / dialStep);
        this.stepDegree = drawDegree / drawDialNum;
        this.lowDialLimit = (int) (lowDialLimit - minVal);
        this.highDialLimit = (int) (highDialLimit - minVal);
        this.sRoateDegree = stepDegree / dialStep; // 短刻度旋转角度
        invalidate();
    }

    public void updateVal(float val) {
        realVal = Math.max(Math.min(val, maxVal), minVal);
        valText = String.format(formatter, realVal);
        sweepAngle = (realVal - minVal) / valRange * (360 - (180 - pointerLineDegree) * 2);


        invalidate();
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int result;
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            default:
                result = specSize;
                break;
        }

        return result;
    }
    private float dp2px(float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,this.getContext().getResources().getDisplayMetrics());
    }
    public void config(GaugeView.Config cfg) {
        this.bgRound1 = cfg.bgRound1;
        this.bgRound2 = cfg.bgRound2;
        this.bgRound3 = cfg.bgRound3;
        this.bgRound4 = cfg.bgRound4;
        this.circle1 = cfg.circle1;
        this.textSizeDial = dp2px(cfg.textSizeDial); // 刻度字体大小
        this.maxVal = cfg.maxVal;
        this.minVal = cfg.minVal;

        this.lowDialLimit = cfg.lowDialLimit; // 低段温度警戒线
        this.highDialLimit = cfg.highDialLimit; // 中段警戒线
        this.colorDialLower = cfg.colorDialLower; // 低中高三个色段表盘刻度
        this.colorDialMiddle = cfg.colorDialMiddle;
        this.colorDialHigh = cfg.colorDialHigh;

        this.lengthLDial = cfg.lengthLDial;// 长刻度线的长度
        this.lengthSDial = cfg.lengthSDial;// 短刻度线的长度
        this.strokeLDial = cfg.strokeLDial;
        this.strokeSDial = cfg.strokeSDial;
        this.circleRadius1StrokeWidth = cfg.circleRadius1StrokeWidth;
        this.circleRadius2StrokeWidth = cfg.circleRadius2StrokeWidth;
        this.pointerLineDegree = cfg.pointerLineDegree; // 多少角度
        this.arcStartDegree = pointerLineDegree; // 弧线起始角度
        this.valTextSize = dp2px(cfg.valTextSize);// 值字体大小
        this.valTextColor = cfg.valTextColor;// 值字体颜色
        this.labelText = cfg.labelText;
        this.labelTextSize = dp2px(cfg.labelTextSize);// 标签字体
        this.labelTextColor = cfg.labelTextColor;// 标签字体颜色
        this.arcPaintStrokeWidth = cfg.arcPaintStrokeWidth; // 描边宽度
        this.arcColor = cfg.arcColor;
        this.colors = cfg.colors != null ? cfg.colors : new int[]{Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, cfg.arcColor};
        this.colorsArcLine = cfg.colorsArcLine != null ? cfg.colorsArcLine : new int[]{cfg.arcColor, cfg.arcColor, Color.TRANSPARENT};
        ;
        this.formatter = cfg.formatter;
        this.drawDialNum = cfg.drawDialNum;
        this.dialStep = cfg.dialStep; // 分度
        this.decimalFormat= cfg.decimalFormat;
        init();
        invalidate();
    }

    public static class Config {
        public int bgRound1 = Color.parseColor("#99122a73");
        public int bgRound2 = Color.parseColor("#66122a73");
        public int bgRound3 = Color.parseColor("#22142f80");
        public int bgRound4 = Color.parseColor("#0e2267");
        public int circle1 = Color.parseColor("#0d1d4c");
        public float textSizeDial = 12; // 刻度字体大小
        public float maxVal = 150f;
        public float minVal = -50f;

        public int lowDialLimit = 20; // 低段温度警戒线
        public int highDialLimit = 80; // 中段警戒线
        public int colorDialLower = Color.parseColor("#00faff"); // 低中高三个色段表盘刻度
        public int colorDialMiddle = Color.GREEN;
        public int colorDialHigh = Color.RED;

        public int lengthLDial = 16;// 长刻度线的长度
        public int lengthSDial = 8;// 短刻度线的长度
        public float strokeLDial = 3;
        public float strokeSDial = 1;
        public float circleRadius1StrokeWidth = 2f;
        public float circleRadius2StrokeWidth = 4f;
        public float pointerLineDegree = 135; // 多少角度
        public float valTextSize = 30f;// 值字体大小
        public int valTextColor = Color.WHITE;// 值字体颜色
        public String labelText = "";
        public float labelTextSize = 14f;// 标签字体
        public int labelTextColor = Color.WHITE;// 标签字体颜色
        public float arcPaintStrokeWidth = 2f; // 描边宽度
        public int arcColor = Color.parseColor("#f6a800");
        public int[] colors;
        public int[] colorsArcLine;
        public String formatter = "%.0f";
        public int drawDialNum = 100;
        public int dialStep = 10; // 分度
        public DecimalFormat decimalFormat= new DecimalFormat(".0");

    }

}
