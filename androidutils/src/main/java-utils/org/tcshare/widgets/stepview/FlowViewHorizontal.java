package org.tcshare.widgets.stepview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.tcshare.androidutils.R;
import org.tcshare.utils.DensityUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paike on 2017/1/9.
 * xyz@163.com
 */

public class FlowViewHorizontal extends View {

    private Paint bgPaint;
    private Paint proPaint;
    private float bgRadius;
    private float proRadius;
    private float startX;
    private float stopX;
    private float bgCenterY;
    private int lineBgWidth;
    private int bgColor;
    private int lineProWidth;
    private int proColor;
    private int textPadding;
    private int timePadding;
    private int maxStep;
    private int textSize;
    private int proStep;
    private int interval;
    private String[] titles = {"提交", "接单", "取件", "配送", "完成"};
    private String[] times = {"12:20"};
    private final Map<Integer, String> mapPos = new HashMap<>(); // 存储位置-> 颜色

    public FlowViewHorizontal(Context context) {
        this(context, null);
    }

    public FlowViewHorizontal(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowViewHorizontal(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlowViewHorizontal);
        bgRadius = ta.getDimension(R.styleable.FlowViewHorizontal_h_bg_radius, 10);
        proRadius = ta.getDimension(R.styleable.FlowViewHorizontal_h_pro_radius, 8);
        lineBgWidth = (int) ta.getDimension(R.styleable.FlowViewHorizontal_h_bg_width, 3f);
        bgColor = ta.getColor(R.styleable.FlowViewHorizontal_h_bg_color, Color.parseColor("#cdcbcc"));
        lineProWidth = (int) ta.getDimension(R.styleable.FlowViewHorizontal_h_pro_width, 2f);
        proColor = ta.getColor(R.styleable.FlowViewHorizontal_h_pro_color, Color.parseColor("#029dd5"));
        textPadding = (int) ta.getDimension(R.styleable.FlowViewHorizontal_h_text_padding, 20);
        timePadding = (int) ta.getDimension(R.styleable.FlowViewHorizontal_h_time_padding, 30);
        maxStep = ta.getInt(R.styleable.FlowViewHorizontal_h_max_step, 5);
        textSize = (int) ta.getDimension(R.styleable.FlowViewHorizontal_h_textsize, 20);
        proStep = ta.getInt(R.styleable.FlowViewHorizontal_h_pro_step, 1);
        ta.recycle();
        init();
    }

    private void init() {
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        bgPaint.setStrokeWidth(lineBgWidth);
        bgPaint.setTextSize(textSize);
        bgPaint.setTextAlign(Paint.Align.CENTER);

        proPaint = new Paint();
        proPaint.setAntiAlias(true);
        proPaint.setStyle(Paint.Style.FILL);
        proPaint.setColor(proColor);
        proPaint.setStrokeWidth(lineProWidth);
        proPaint.setTextSize(textSize);
        proPaint.setTextAlign(Paint.Align.CENTER);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int bgWidth;
        if (widthMode == MeasureSpec.EXACTLY) {
            bgWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        } else
            bgWidth = DensityUtil.dp2px(getContext(), 311);

        int bgHeight;
        if (heightMode == MeasureSpec.EXACTLY) {
            bgHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        } else
            bgHeight = DensityUtil.dp2px(getContext(), 49);
        float left = getPaddingLeft() + bgRadius;
        stopX = bgWidth - bgRadius;
        startX = left;
        bgCenterY = bgHeight / 2;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        interval = (int) ((stopX - startX) / (maxStep - 1));
        drawBg(canvas);
        drawProgress(canvas);
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        for (int i = 0; i < maxStep; i++) {
            if (i < proStep) {
                setPaintColor(i);
                if (null != titles && i < titles.length)
                    canvas.drawText(titles[i], startX + (i * interval), bgCenterY - textPadding, proPaint);
                if (null != times && i < times.length)
                    canvas.drawText(times[i], startX + (i * interval), bgCenterY + timePadding, proPaint);
            } else {
                if (null != titles && i < titles.length) {
                    String title = titles[i];
                    if (null == title) continue;
                    canvas.drawText(title, startX + (i * interval), bgCenterY - textPadding, bgPaint);
                }
            }
        }
    }

    private void setPaintColor(int index) {
        if (mapPos.get(index) != null) {
            proPaint.setColor(Color.parseColor(mapPos.get(index)));
        } else {
            proPaint.setColor(proColor);
        }
    }

    private void drawProgress(Canvas canvas) {
        int linePro;
        float lastLeft = startX;
        for (int i = 0; i < proStep; i++) {
            setPaintColor(i);
            if (i == 0 || i == maxStep - 1)
                linePro = interval / 2;
            else
                linePro = interval;
            canvas.drawLine(lastLeft, bgCenterY, lastLeft + linePro, bgCenterY, proPaint);
            lastLeft = lastLeft + linePro;
            canvas.drawCircle(startX + (i * interval), bgCenterY, proRadius, proPaint);
        }
    }

    private void drawBg(Canvas canvas) {
        canvas.drawLine(startX, bgCenterY, stopX, bgCenterY, bgPaint);
        for (int i = 0; i < maxStep; i++) {
            canvas.drawCircle(startX + (i * interval), bgCenterY, bgRadius, bgPaint);
        }
    }

    /**
     * 进度设置
     *
     * @param progress 已完成到哪部
     * @param maxStep  总步骤
     * @param titles   步骤名称
     * @param times    完成时间
     */
    public void setProgress(int progress, int maxStep, String[] titles, String[] times) {
        proStep = progress;
        this.maxStep = maxStep;
        this.titles = titles;
        this.times = times;
        invalidate();
    }

    /**
     * 颜色设置
     *
     *
     * [a:#AAA,b:#BBB, c:#CCC]
     *
     * 如果 reset = true， map 为[a:#fff], 则b、c的颜色将被清空
     * @param map 标题-颜色
     */
    public void setKeyColorByName(Map<String, String> map, boolean reset) {
        for(int i = 0; i < titles.length; i++){
            if(reset){
                mapPos.put(i, map.get(titles[i]));
            }else {
                if (map.get(titles[i]) != null) {
                    mapPos.put(i, map.get(titles[i]));
                }
            }
        }
    }

    /**
     * 通过索引设置颜色， key 为索引位置
     * @param map
     */
    public void setKeyColorByIndex(Map<Integer, String> map) {
        mapPos.putAll(map);
    }
}
