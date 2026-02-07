package org.tcshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tcshare.androidutils.R;

public class LoadMoreAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FOOT = 2580;
    private final RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;
    private final OnLoadMoreListener mLoadMoreListener;
    private LoadingState mState = LoadingState.LOADING_STATE_NORMAL;

    //是否正在上拉数据
    private boolean loading = true;
    //当前页，从0开始
    private int currentPage = 0;

    //主要用来存储上一个totalItemCount
    private int previousTotal = 0;

    public <T extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> LoadMoreAdapterWrapper(T mAdapter, OnLoadMoreListener mLoadMoreListener) {
        this.mAdapter = (RecyclerView.Adapter<RecyclerView.ViewHolder>) mAdapter;
        this.mLoadMoreListener = mLoadMoreListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOT) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.default_footer_layout, parent, false);
            return new FooterViewHolder(itemView);
        }
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            FooterViewHolder viewHolder = (FooterViewHolder) holder;
            viewHolder.changState(mState);
            return;
        }
        mAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount() + (mState == LoadingState.LOADING_STATE_NORMAL ? 0 : 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mAdapter.getItemCount()) {
            return TYPE_FOOT;
        }
        return mAdapter.getItemViewType(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
        mAdapter.setHasStableIds(hasStableIds);
    }

    @Override
    public long getItemId(int position) {
        if (position > mAdapter.getItemCount() - 1) {
            return super.getItemId(position);
        }
        return mAdapter.getItemId(position);
    }

    public void updateLoadMoreState(LoadingState state) {
        mState = state;
        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof FooterViewHolder) {
            super.onViewRecycled(holder);
            return;
        }
        mAdapter.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof FooterViewHolder) {
            return super.onFailedToRecycleView(holder);
        }
        return mAdapter.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof FooterViewHolder) {
            super.onViewAttachedToWindow(holder);
            return;
        }
        mAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof FooterViewHolder) {
            super.onViewDetachedFromWindow(holder);
        }
        mAdapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        mAdapter.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        mAdapter.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mAdapter.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //声明一个LinearLayoutManager
                LinearLayoutManager mLinearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //在屏幕上可见的item数量
                int visibleItemCount = recyclerView.getChildCount();
                //已经加载出来的Item的数量
                int totalItemCount = mLinearLayoutManager.getItemCount();
                //在屏幕可见的Item中的第一个
                int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        //说明数据已经加载结束
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem) {
                    currentPage++;
                    if (mLoadMoreListener != null && mState != LoadingState.LOADING_STATE_LOADING) {
                        mState = LoadingState.LOADING_STATE_LOADING;

                        recyclerView.post(() -> notifyItemChanged(mAdapter.getItemCount()));
                        mLoadMoreListener.onLoadMore(currentPage, new LoadFinishCallback() {
                            @Override
                            public void loadSuccess() {
                                updateLoadMoreState(LoadingState.LOADING_STATE_NORMAL);
                            }

                            @Override
                            public void loadFailed() {
                                updateLoadMoreState(LoadingState.LOADING_STATE_ERROR);
                            }

                            @Override
                            public void loadFinish() {
                                updateLoadMoreState(LoadingState.LOADING_STATE_NO_MORE);
                            }
                        });
                    }
                    loading = true;
                }
            }

        });
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int page, LoadFinishCallback cb);
    }

    public interface LoadFinishCallback {
        void loadSuccess();

        void loadFailed();

        void loadFinish();
    }


    public class FooterViewHolder extends RecyclerView.ViewHolder implements IFooterView {
        private final ProgressBar mLoadingView;
        private final TextView mLoadingTextView;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            mLoadingView = (ProgressBar) itemView.findViewById(R.id.loading_view);
            mLoadingTextView = (TextView) itemView.findViewById(R.id.loading_text_view);
        }

        @Override
        public void changState(LoadingState state) {
            if (state == LoadingState.LOADING_STATE_LOADING) {
                mLoadingView.setVisibility(View.VISIBLE);
                mLoadingTextView.setVisibility(View.VISIBLE);
                mLoadingTextView.setText("正在加载，请稍后！");
            } else if (state == LoadingState.LOADING_STATE_NO_MORE) {
                mLoadingView.setVisibility(View.GONE);
                mLoadingTextView.setVisibility(View.VISIBLE);
                mLoadingTextView.setText("没有更多数据啦！");
                resetDelay();
            }else if(state == LoadingState.LOADING_STATE_ERROR){
                mLoadingView.setVisibility(View.GONE);
                mLoadingTextView.setVisibility(View.VISIBLE);
                mLoadingTextView.setText("加载出错啦！");
                resetDelay();
            }else if(state == LoadingState.LOADING_STATE_NORMAL){
                mLoadingView.setVisibility(View.GONE);
                mLoadingTextView.setText("");
                mLoadingTextView.setVisibility(View.GONE);
            }
        }
        private void resetDelay(){
            itemView.post(() -> {
                loading = false;
                notifyItemChanged(getItemCount());
            });
        }
    }

    public void resetState(){
        updateLoadMoreState(LoadingState.LOADING_STATE_NORMAL);
        loading = false;
        currentPage = 0;
        previousTotal = 0;
    }

    public interface IFooterView {
        void changState(LoadingState state);
    }

    public enum LoadingState {
        LOADING_STATE_NO_MORE, // 没有更多
        LOADING_STATE_LOADING, // 加载中
        LOADING_STATE_ERROR, // 加载错误
        LOADING_STATE_NORMAL // 不显示footer
    }
}
