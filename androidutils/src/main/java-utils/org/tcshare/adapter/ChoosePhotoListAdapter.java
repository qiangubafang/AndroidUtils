/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.tcshare.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;

import org.tcshare.androidutils.R;
import org.tcshare.utils.SelectOnePicture;
import org.tcshare.utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * yuxiaohei
 */
public class ChoosePhotoListAdapter extends BaseAdapter {
    public int maxSize = 9;
    private LayoutInflater mInflater;
    private Context context;
    private List<Pair<String, Object>> list = new ArrayList<>();
    private BeforeAddListener beforeAddListener;
    private BeforeDelListener beforeDelListener;

    public ChoosePhotoListAdapter(Context context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }
    public void setListener(BeforeAddListener beforeAddListener, BeforeDelListener beforeDelListener){
        this.beforeAddListener = beforeAddListener;
        this.beforeDelListener = beforeDelListener;
    }

    @Override
    public int getCount() {
        if (list.size() < maxSize) {
            return list.size() + 1;
        } else {
            return list.size();
        }
    }

    @Override
    public Pair<String, Object> getItem(int position) {

        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<String> lt) {
        this.list.clear();
        for(String s : lt){
            list.add(Pair.create(s, null));
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_selectpic_showphoto, parent, false);
            holder.item_show_photo = (ImageView) convertView.findViewById(R.id.item_show_photo);
            holder.item_add_photo = (ImageView) convertView.findViewById(R.id.item_add_photo);
            holder.item_del_photo = (ImageView) convertView.findViewById(R.id.item_del_photo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (list.size() < maxSize) {
            if (list.size() == 0 || position >= list.size()) {
                holder.item_add_photo.setVisibility(View.VISIBLE);
                holder.item_show_photo.setVisibility(View.GONE);
                holder.item_del_photo.setVisibility(View.GONE);
            } else {
                holder.item_add_photo.setVisibility(View.GONE);
                holder.item_show_photo.setVisibility(View.VISIBLE);
                holder.item_del_photo.setVisibility(View.VISIBLE);
                String path = list.get(position).first;
                if(path.startsWith("http")){
                    Glide.with(context)
                            .load(path)
                            .into(holder.item_show_photo);
                }else {
                    Glide.with(context)
                            .load(new File(path))
                            .into(holder.item_show_photo);
                }
            }
        } else {
            holder.item_del_photo.setVisibility(View.VISIBLE);
            holder.item_show_photo.setVisibility(View.VISIBLE);
            holder.item_add_photo.setVisibility(View.GONE);
            String path = list.get(position).first;
            if(path.startsWith("http")){
                Glide.with(context)
                        .load(path)
                        .into(holder.item_show_photo);
            }else {
                Glide.with(context)
                        .load(new File(path))
                        .into(holder.item_show_photo);
            }
        }
        holder.item_add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectOnePicture.doSelect(context, new SelectOnePicture.CallBack() {

                    @Override
                    public void onResult(int resultCode, String path) {
                        if(resultCode != Activity.RESULT_OK){
                            return;
                        }else if(beforeAddListener != null){
                            if("".equals(path) || null == path){
                                ToastUtil.showToastShort(context, "无法读取该位置文件！");
                                return;
                            }
                            int pos = list.size();
                            beforeAddListener.onBeforeAdd(pos, path, new Callback() {
                                @Override
                                public void onTaskFinish(boolean success, Object userObj) {
                                    if(!success) return;
                                    list.add(Pair.create(path, userObj));
                                    notifyDataSetChanged();
                                }
                            });
                        }else{
                            if("".equals(path) || null == path){
                                ToastUtil.showToastShort(context, "无法读取该位置文件！");
                                return;
                            }
                            list.add(Pair.create(path, null));
                            notifyDataSetChanged();
                        }

                    }
                });
            }
        });
        holder.item_del_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.title_del_pic))
                        .setMessage(context.getString(R.string.title_del_pic_msg))
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(beforeDelListener != null){
                                    Pair<String, Object> item = list.get(position);
                                    beforeDelListener.onBeforeDel(position, item.first, item.second, new Callback() {
                                        @Override
                                        public void onTaskFinish(boolean success,  Object userObj) {
                                            if(!success) return;
                                            list.remove(position);
                                            notifyDataSetChanged();
                                        }
                                    });
                                }else{
                                    list.remove(position);
                                    notifyDataSetChanged();
                                }

                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });
        return convertView;
    }

    public List<String> getPicList() {
        List<String> result = new ArrayList<>();
        for(Pair<String, Object> p : list){
            result.add(p.first);
        }
        return result;
    }

    public void clear() {
        this.list.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        ImageView item_show_photo;
        ImageView item_add_photo;
        ImageView item_del_photo;
    }
    public interface BeforeAddListener {
        void onBeforeAdd(int pos, String path, Callback callBack);
    }
    public interface Callback{
        void onTaskFinish(boolean success,  Object userObj);
    }
    public interface BeforeDelListener {
        void onBeforeDel(int pos, String path,Object userObj, Callback callBack);
    }
}
