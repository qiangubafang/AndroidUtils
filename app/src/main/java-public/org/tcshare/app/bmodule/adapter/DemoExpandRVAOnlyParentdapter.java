package org.tcshare.app.bmodule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tcshare.adapter.ExpandableRecyclerViewAdapter;
import org.tcshare.app.R;
import org.tcshare.app.bmodule.StrUtil;
import org.tcshare.app.bmodule.bean.EquipListBean;
import org.tcshare.app.bmodule.bean.EquipListChildBean;


/**
 * 测试 可展开列表只有父列表时的功能，以及filter的功能
 * yuxiaohei
 */
public class DemoExpandRVAOnlyParentdapter extends ExpandableRecyclerViewAdapter<EquipListBean, EquipListChildBean> {
    private OnGoClick listener;

    @Override
    protected void onBindChild(RecyclerView.ViewHolder holder, EquipListChildBean item) {
    }

    @Override
    protected boolean doFilter(CharSequence filter, ItemWrapper wrapper) {
        if(wrapper.type == VIEW_TYPE_CHILD ){
            return true;
        }else if(wrapper.type == VIEW_TYPE_PARENT){
            String name = wrapper.parent.getRawData().getName();
            String code = wrapper.parent.getRawData().getCode();
            return (!StrUtil.isEmpty(name) && name.contains(filter)) || (!StrUtil.isEmpty(code) && code.contains(filter)) ;
        }
        return false;
    }

    @Override
    protected void onBindParent(RecyclerView.ViewHolder holder, EquipListBean item, boolean isExpand) {
        ParentViewHolder h = (ParentViewHolder) holder;
        h.groupTitle.setText(item.getName());
        h.groupType.setText(item.getType());
    }

    @Override
    public RecyclerView.ViewHolder getParentViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_expandableact_textview_group, parent, false);
        return new ParentViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder getChildViewHolder(ViewGroup parent) {
        return new ChildViewHolder(parent);
    }


    public class ParentViewHolder extends  RecyclerView.ViewHolder{
        private final TextView groupTitle;
        private final TextView groupType;

        public ParentViewHolder(@NonNull View itemView) {
            super(itemView);
            groupTitle = itemView.findViewById(R.id.groupTitle);
            groupType = itemView.findViewById(R.id.groupType);
            itemView.setOnClickListener(v -> {
                if(listener != null){
                    ItemWrapper item = getItemFromFlatList(getAdapterPosition());
                    listener.onClick(item.parent);
                }
            });
        }
    }

    // 无用到
    public static class ChildViewHolder extends  RecyclerView.ViewHolder{

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setListener(OnGoClick listener) {
        this.listener = listener;
    }

    public interface OnGoClick{
        void onClick(EquipListBean pData);
    }
}
