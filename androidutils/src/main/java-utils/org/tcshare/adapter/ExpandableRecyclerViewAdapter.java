package org.tcshare.adapter;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 可展开列表.
 * <p>
 * by : yxh
 */
public abstract class ExpandableRecyclerViewAdapter<P, C> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_PARENT = 1;
    public static final int VIEW_TYPE_CHILD = 2;

    protected final List<P> parentList = new ArrayList<>();
    protected final List<List<C>> childList = new ArrayList<>();


    /**
     * 显示列表，第一个表示类型
     * 展平后的数据
     */
    protected final List<ItemWrapper> flatFilterList = new ArrayList<>();
    protected final List<ItemWrapper> flatList = new ArrayList<>();

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ItemWrapper item = flatFilterList.get(position);
        if (item.type == VIEW_TYPE_PARENT) {
            onBindParent(holder, item.parent, item.isExpand);
        } else {
            onBindChild(holder, item.child);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if (viewType == VIEW_TYPE_PARENT) {
            holder = getParentViewHolder(parent);
            GestureDetector gestureDetector = new GestureDetector(parent.getContext(), new SingleTapConfirm());
            holder.itemView.setOnTouchListener((arg0, arg1) -> {// 不影响，子类itemView的onclick事件
                if (gestureDetector.onTouchEvent(arg1)) {
                    int flatListPos = holder.getAdapterPosition();
                    collapseOrExpandList(flatListPos);
                    arg0.performClick();
                }
                return true;
            });

        } else {
            holder = getChildViewHolder(parent);
        }

        return holder;
    }

    protected void collapseOrExpandList(int flatListPos) {
        if(childList.size() == 0){
            return;
        }
        ItemWrapper item = flatFilterList.get(flatListPos);
        if (item.isExpand) {
            doCollapseList(flatListPos, item);
        } else {
            doExpandList(flatListPos, item);
        }
    }

    private void doExpandList(int pos, ItemWrapper item) {
        item.isExpand = true;
        List<C> cList = item.cList;
        List<ItemWrapper> wrapperList = new ArrayList<>();
        for (C o : cList) {
            wrapperList.add(new ItemWrapper(VIEW_TYPE_CHILD, item.parent, o, false, null));
        }
        flatFilterList.addAll(pos + 1, wrapperList);
        notifyItemRangeInserted(pos + 1, wrapperList.size());
        notifyItemChanged(pos);
    }

    private void doCollapseList(int pos, ItemWrapper item) {
        item.isExpand = false;
        flatFilterList.removeAll(flatFilterList.subList(pos + 1, pos + 1 + item.cList.size()));
        notifyItemRangeRemoved(pos + 1, item.cList.size());
        notifyItemChanged(pos);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<P> parent, List<List<C>> child, boolean isExpand) {
        if (parent.size() != child.size() && child.size() != 0) {
            throw new IllegalArgumentException("父长度与子长度不一致！");
        }
        parentList.clear();
        parentList.addAll(parent);
        childList.clear();
        childList.addAll(child);

        flatList.clear();
        int pSize = parent.size();
        boolean isChildISEmpty = child.size() == 0;
        for (int i = 0; i < pSize; i++) {
            P item = parentList.get(i);
            List<C> cList = isChildISEmpty ? new ArrayList<>() : childList.get(i);
            flatList.add(new ItemWrapper(VIEW_TYPE_PARENT, item, null, isExpand, cList));
            if (isExpand) {
                for (C o : cList) {
                    flatList.add(new ItemWrapper(VIEW_TYPE_CHILD, item, o, false, null));
                }
            }
        }
        flatFilterList.clear();
        flatFilterList.addAll(flatList);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void append(List<P> parent, List<List<C>> child){
        if (parent.size() != child.size() && child.size() != 0) {
            throw new IllegalArgumentException("父长度与子长度不一致！");
        }
        parentList.addAll(parent);
        childList.addAll(child);

        int pSize = parent.size();
        boolean isChildISEmpty = child.size() == 0;
        for (int i = 0; i < pSize; i++) {
            P item = parentList.get(i);
            List<C> cList = isChildISEmpty ? new ArrayList<>() : childList.get(i);
            flatList.add(new ItemWrapper(VIEW_TYPE_PARENT, item, null, false, cList));
        }

        flatFilterList.clear();
        flatFilterList.addAll(flatList);
        notifyDataSetChanged();
    }

    public ItemWrapper getItemFromFlatList(int pos) {
        return flatFilterList.get(pos);
    }


    @Override
    public int getItemViewType(int position) {
        return flatFilterList.get(position).type;
    }

    @Override
    public int getItemCount() {
        return flatFilterList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void performFilter(CharSequence filter){
        flatFilterList.clear();
        for(ItemWrapper wrapper : flatList){
            if(doFilter(filter, wrapper)) {
                flatFilterList.add(wrapper);
            }
        }
        notifyDataSetChanged();
    }


    protected boolean doFilter(CharSequence filter, ItemWrapper wrapper){
        return true;
    }

    protected abstract void onBindChild(RecyclerView.ViewHolder holder, C item);

    protected abstract void onBindParent(RecyclerView.ViewHolder holder, P item, boolean isExpand);

    public abstract RecyclerView.ViewHolder getParentViewHolder(ViewGroup parent);

    public abstract RecyclerView.ViewHolder getChildViewHolder(ViewGroup parent);

    public class ItemWrapper {
        public boolean isExpand;
        public int type;
        public C child;
        public P parent;
        public List<C> cList;

        public ItemWrapper(int type, P parent, C child, boolean isExpand, List<C> cList) {
            this.isExpand = isExpand;
            this.type = type;
            this.child = child;
            this.parent = parent;
            this.cList = cList;
        }
    }

    private static class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }
}
