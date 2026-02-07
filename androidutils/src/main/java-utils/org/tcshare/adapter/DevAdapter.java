package org.tcshare.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.tcshare.androidutils.R;

import java.util.ArrayList;
import java.util.List;


public class DevAdapter extends RecyclerView.Adapter<DevAdapter.ViewHolder> {

    private final List<BluetoothDevice> datas = new ArrayList<>();


    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickLitener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setDatas(List<BluetoothDevice> list) {
        datas.clear();
        datas.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice item = datas.get(position);
        holder.mTvName.setText(item.getName());
        holder.mTvMac.setText(item.getAddress());

    }

    @Override
    public int getItemViewType(int position) {

        return 3;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void addItem(BluetoothDevice device) {
        datas.add(device);
        notifyItemChanged(datas.size()  - 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        datas.clear();
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTvName;
        TextView mTvMac;
        Button mBtnConnect;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvMac = itemView.findViewById(R.id.tv_mac);
            mBtnConnect =  itemView.findViewById(R.id.btn_connect);

            View.OnClickListener onItemClickListener = v -> {
                int pos = getAdapterPosition();
                try {
                    BluetoothDevice item = datas.get(pos);
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(itemView, item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            mBtnConnect.setOnClickListener(onItemClickListener);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, BluetoothDevice item);
    }
}
