package org.tcshare.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.content.res.ResourcesCompat;

import org.tcshare.androidutils.R;
import org.tcshare.utils.DensityUtil;
import org.tcshare.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FavorLayout extends RelativeLayout {
    private static final String TAG = FavorLayout.class.getSimpleName();

    private int iHeight = 120;
    private int iWidth = 120;
    private int mHeight;
    private int mWidth;

    private LayoutParams lp;
    private List<Drawable> loves;

    private final List<Interpolator> interpolates = new ArrayList<Interpolator>() {
        {
            add(new LinearInterpolator());
            //add(new AccelerateInterpolator());
            add(new DecelerateInterpolator());
            add(new AccelerateDecelerateInterpolator());
        }
    };
    private PointF startPoint = new PointF();
    private PointF anchorPoint;
    private View anchorView;
    private int favorWidth = -1, favorHeight = -1;
    private boolean stop = false;


    public FavorLayout(Context context) {
        super(context);
        init();
    }

    public FavorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FavorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        startPoint.set((mWidth - iWidth) / 2f, mHeight - iHeight);
    }

    private void init() {
        //底部 并且 水平居中
        lp = new LayoutParams(iWidth, iHeight);
        lp.addRule(CENTER_HORIZONTAL, TRUE); //这里的TRUE 要注意 不是true
        lp.addRule(ALIGN_PARENT_BOTTOM, TRUE);

        Resources res = getResources();
        //初始化显示的图片
        loves = new ArrayList<Drawable>() {
            {
                add(ResourcesCompat.getDrawable(res, R.mipmap.love_a, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.love_b, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.love_c, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.love_d, null));
                add(ResourcesCompat.getDrawable(res, R.mipmap.love_e, null));
            }

        };

    }

    public void setAnchor(final View view) {
        anchorView = view;
        resetAnchorPoint();
    }

    private void resetAnchorPoint() {
        if (anchorView != null) {
            anchorView.post(new Runnable() {
                @Override
                public void run() {
                    int[] outLocation = new int[2];
                    anchorView.getLocationOnScreen(outLocation);
                    float x = outLocation[0] + (anchorView.getWidth() - iWidth) / 2f;
                    float y = outLocation[1] - (anchorView.getHeight() - iHeight) / 2f;
                    anchorPoint = new PointF(x, y);
                }
            });
        }
    }

    /**
     * 点赞
     * 对外暴露的方法
     */
    public void addFavor() {
        if (stop) {
            return;
        }
        ImageView imageView = new ImageView(getContext());
        // 随机选一个
        imageView.setImageDrawable(RandomUtils.getRandomElement(loves));

        if (anchorPoint == null) {
            imageView.setLayoutParams(lp);
        } else {
            imageView.setX(anchorPoint.x - getX());
            imageView.setY(anchorPoint.y - getY());
        }

        addView(imageView);
        Log.d(TAG, "addFavor: " + "add后子view数:" + getChildCount());

        Animator set = getAnimator(imageView);
        set.addListener(new AnimEndListener(imageView));
        set.start();

    }

    /**
     * 设置动画
     */
    private Animator getAnimator(View target) {
        AnimatorSet set = getEnterAnimator(target);

        ValueAnimator bezierValueAnimator = getBezierValueAnimator(target);

        AnimatorSet finalSet = new AnimatorSet();
        finalSet.playSequentially(set);
        finalSet.playSequentially(set, bezierValueAnimator);
        finalSet.setInterpolator(RandomUtils.getRandomElement(interpolates));//实现随机变速
        finalSet.setTarget(target);
        return finalSet;
    }


    /**
     * 设置初始动画
     * 渐变 并且横纵向放大
     */
    private AnimatorSet getEnterAnimator(final View target) {

        ObjectAnimator alpha = ObjectAnimator.ofFloat(target, View.ALPHA, 0.2f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.2f, 1f);
        AnimatorSet enter = new AnimatorSet();
        enter.setDuration(500);
        enter.setInterpolator(new LinearInterpolator());
        enter.playTogether(alpha, scaleX, scaleY);
        enter.setTarget(target);
        return enter;
    }

    public void setFavorWidthHeight(int width, int height) {
        this.favorWidth = DensityUtil.dp2px(getContext(), width);
        this.favorHeight = DensityUtil.dp2px(getContext(), height);
    }

    private PointF getPointLow() {
        PointF pointF = new PointF();
        if (anchorView != null && favorWidth != -1 && favorHeight != -1 && anchorPoint != null) {
            // 中心点
            float x = anchorPoint.x - getX();
            float y = anchorPoint.y - getY();
            pointF.x = x - favorWidth / 2f + RandomUtils.getRandomInt(favorWidth);
            pointF.y = y - favorHeight / 4f - RandomUtils.getRandomInt(favorHeight / 4);

        } else {
            //减去100 是为了控制 x轴活动范围
            pointF.x = RandomUtils.getRandomInt(mWidth - 100);
            //再Y轴上 为了确保第二个控制点 在第一个点之上,我把Y分成了上下两半
            pointF.y = RandomUtils.getRandomInt(mHeight - 100) / 2f;
        }

        return pointF;
    }

    private PointF getPointHeight() {
        PointF pointF = new PointF();
        if (anchorView != null && favorWidth != -1 && favorHeight != -1 && anchorPoint != null) {
            // 中心点
            float x = anchorPoint.x - getX();
            float y = anchorPoint.y - getY();
            pointF.x = x - favorWidth / 2f + RandomUtils.getRandomInt(favorWidth);
            pointF.y = y - favorHeight / 2f - RandomUtils.getRandomInt(favorHeight / 4);

        } else {
            pointF.x = RandomUtils.getRandomInt(mWidth - 100);
            pointF.y = RandomUtils.getRandomInt(mHeight - 100);
        }

        return pointF;
    }

    private PointF getEndPoint() {
        PointF pointF = new PointF();
        if (anchorView != null && favorWidth != -1 && favorHeight != -1 && anchorPoint != null) {
            // 中心点
            float x = anchorPoint.x - getX();
            float y = anchorPoint.y - getY();
            pointF.x = x - favorWidth / 2f + RandomUtils.getRandomInt(favorWidth);
            pointF.y = y - favorHeight;

        } else {
            pointF.set(RandomUtils.getRandomInt(getWidth()), 0);
        }

        return pointF;
    }

    /**
     * 获取贝塞尔曲线动画
     */
    private ValueAnimator getBezierValueAnimator(View target) {

        //初始化一个BezierEvaluator
        BezierEvaluator evaluator = new BezierEvaluator(getPointLow(), getPointHeight());

        // 起点固定，终点随机
        ValueAnimator animator = ValueAnimator.ofObject(evaluator,
                anchorPoint == null ? startPoint : new PointF(anchorPoint.x - getX(), anchorPoint.y - getY()),
                getEndPoint());
        animator.addUpdateListener(new BezierListener(target));
        animator.setTarget(target);
        animator.setDuration(3000);
        return animator;
    }


    /**
     * 设置点赞效果集合
     *
     * @param items 浮动的图片
     */
    public void setFavors(List<Drawable> items) {
        loves.clear();
        loves.addAll(items);
        if (items.size() == 0) {
            throw new UnsupportedOperationException("点赞效果图片不能为空");
        }

        this.iWidth = items.get(0).getIntrinsicWidth();
        this.iHeight = items.get(0).getIntrinsicHeight();
        startPoint = new PointF((mWidth - iWidth) / 2f, mHeight - iHeight);
        resetAnchorPoint();
    }

    public void setStat(boolean stop) {
        this.stop = stop;
    }


    private class AnimEndListener extends AnimatorListenerAdapter {
        private final View target;

        public AnimEndListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            //因为不停的add 导致子view数量只增不减,所以在view动画结束后remove掉
            removeView((target));
            Log.v(TAG, "removeView后子view数:" + getChildCount());
        }
    }


}
