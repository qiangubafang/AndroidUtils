package org.tcshare.widgets.stepview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.tcshare.androidutils.R;

import java.util.HashMap;
import java.util.Map;
import org.tcshare.utils.DensityUtil;

/**
 * Created by paike on 2017/2/10.
 * xyz@163.com
 */

public class FlowViewVertical extends View {

    private Paint bgPaint;
    private Paint proPaint;
    private TextPaint textPaint;
    private float bgRadius;
    private float proRadius;
    private int lineBgWidth;
    private int bgColor;
    private int lineProWidth;
    private int proColor;
    private int interval;
    private int bgPositionX;
    private int maxStep;
    private int proStep;
    private int textPaddingLeft;
    private int timePaddingRight;
    private int textMoveTop;
    private int timeMoveTop;
    private int textsize;
    private float starY;
    private float stopY;
    private String[] titles;
    private String[] times;
    private int border;
    private final Map<Integer, String> mapPos = new HashMap<>();

    public FlowViewVertical(Context context) {
        this(context, null);
    }

    public FlowViewVertical(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowViewVertical(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlowViewVertical);
        bgRadius = ta.getDimension(R.styleable.FlowViewVertical_v_bg_radius, 10);
        proRadius = ta.getDimension(R.styleable.FlowViewVertical_v_pro_radius, 8);
        lineBgWidth = (int) ta.getDimension(R.styleable.FlowViewVertical_v_bg_width, 3f);
        bgColor = ta.getColor(R.styleable.FlowViewVertical_v_bg_color, Color.parseColor("#cdcbcc"));
        lineProWidth = (int) ta.getDimension(R.styleable.FlowViewVertical_v_pro_width, 2f);
        proColor = ta.getColor(R.styleable.FlowViewVertical_v_pro_color, Color.parseColor("#029dd5"));
        interval = (int) ta.getDimension(R.styleable.FlowViewVertical_v_interval, 140);
        maxStep = ta.getInt(R.styleable.FlowViewVertical_v_max_step, 5);
        proStep = ta.getInt(R.styleable.FlowViewVertical_v_pro_step, 3);
        bgPositionX = (int) ta.getDimension(R.styleable.FlowViewVertical_v_bgPositionX, 200);
        textPaddingLeft = (int) ta.getDimension(R.styleable.FlowViewVertical_v_textPaddingLeft, 40);
        timePaddingRight = (int) ta.getDimension(R.styleable.FlowViewVertical_v_timePaddingRight, 80);
        textMoveTop = (int) ta.getDimension(R.styleable.FlowViewVertical_v_textMoveTop, 10);
        timeMoveTop = (int) ta.getDimension(R.styleable.FlowViewVertical_v_timeMoveTop, 8);
        textsize = (int) ta.getDimension(R.styleable.FlowViewVertical_v_textsize, 17);
        ta.recycle();
        init();
    }

    private void init() {
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        bgPaint.setStrokeWidth(lineBgWidth);

        proPaint = new Paint();
        proPaint.setAntiAlias(true);
        proPaint.setStyle(Paint.Style.FILL);
        proPaint.setColor(proColor);
        proPaint.setStrokeWidth(lineProWidth);

        textPaint = new TextPaint();
        textPaint.setTextSize(textsize);
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int bgWidth;
        if (widthMode == MeasureSpec.EXACTLY) {
            bgWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        } else
            bgWidth = DensityUtil.dp2px(getContext(), 311);
        starY = getPaddingTop() + bgRadius;
        stopY = getPaddingTop() + bgRadius + (maxStep - 1) * interval;
        float bottom = stopY + bgRadius + getPaddingBottom();
        border = bgWidth - (bgPositionX + textPaddingLeft);
        setMeasuredDimension(bgWidth, (int) bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBg(canvas);
        drawProgress(canvas);
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        for (int i = 0; i < maxStep; i++) {
            setPaintColor(i);
            if (null != times && i < proStep)
                canvas.drawText(times[i], bgPositionX - timePaddingRight, stopY - (i * interval) + timeMoveTop, textPaint);
            if (null != titles) {
                canvas.save();
                canvas.translate(bgPositionX + textPaddingLeft, (stopY - (i * interval) - textMoveTop));
                StaticLayout sl = new StaticLayout(titles[i], textPaint, border, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                sl.draw(canvas);
                canvas.restore();
            }
        }
    }

    private void drawProgress(Canvas canvas) {
        int linePro;
        float lastBottom = stopY;
        for (int i = 0; i < proStep; i++) {
            setPaintColor(i);
            if (i == 0 || i == maxStep - 1)
                linePro = interval / 2;
            else
                linePro = interval;
            canvas.drawLine(bgPositionX, lastBottom, bgPositionX, lastBottom - linePro, proPaint);
            lastBottom = lastBottom - linePro;
            canvas.drawCircle(bgPositionX, stopY - (i * interval), proRadius, proPaint);
        }
    }

    private void setPaintColor(int index) {
        if (index < proStep) {
            textPaint.setColor(proColor);
        } else {
            textPaint.setColor(bgColor);
        }
        if (mapPos.get(index) != null) {
            int c = Color.parseColor(mapPos.get(index));
            proPaint.setColor(c);
            textPaint.setColor(c);
        } else {
            proPaint.setColor(proColor);
        }
    }

    private void drawBg(Canvas canvas) {
        canvas.drawLine(bgPositionX, stopY, bgPositionX, starY, bgPaint);
        for (int i = 0; i < maxStep; i++) {
            canvas.drawCircle(bgPositionX, stopY - (i * interval), bgRadius, bgPaint);
        }
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

    /**
     * 进度设置
     *
     * @param progress 当前进行到哪一步
     * @param maxStep  总的步骤
     * @param titles   文字描述(指示线右侧)
     * @param times    时间描述(指示线左侧)
     */
    public void setProgress(int progress, int maxStep, String[] titles, String[] times) {
        proStep = progress;
        this.maxStep = maxStep;
        this.titles = titles;
        this.times = times;
        invalidate();
    }
}
