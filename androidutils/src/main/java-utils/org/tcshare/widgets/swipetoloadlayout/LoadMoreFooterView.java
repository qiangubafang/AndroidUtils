package org.tcshare.widgets.swipetoloadlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.tcshare.androidutils.R;

public class LoadMoreFooterView extends SwipeLoadMoreFooterLayout{

    private TextView tvLoadMore;
    private ProgressBar progressBar;
    private int mHeaderHeight;

    public LoadMoreFooterView(Context context) {
        super(context);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderHeight = getResources().getDimensionPixelOffset(R.dimen.refresh_header_height_60);
        tvLoadMore = (TextView) findViewById(R.id.tvLoadMore);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    @Override
    public void onLoadMore() {
        progressBar.setVisibility(VISIBLE);
        tvLoadMore.setText("正在加载...");
    }

    @Override
    public void onPrepare() {
        Log.d("LoadMoreFooterView", "onPrepare()");
    }

    @Override
    public void onMove(int y, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            if (y <= mHeaderHeight) {
                progressBar.setVisibility(VISIBLE);
                tvLoadMore.setText("加载更多");
            } else if(y > mHeaderHeight) {
                progressBar.setVisibility(GONE);
                tvLoadMore.setText("上拉加载");
            }
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
    }

    //加载完成
    @Override
    public void onComplete() {
        progressBar.setVisibility(GONE);
        tvLoadMore.setText("加载完成");
    }

    @Override
    public void onReset() {
        Log.d("LoadMoreFooterView", "onReset()");
    }
}

