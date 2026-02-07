package org.tcshare.app.amodule.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.tcshare.app.R;
import org.tcshare.app.amodule.adapter.RVListAdapter;
import org.tcshare.widgets.ItemDecorations;
import org.tcshare.widgets.swipetoloadlayout.OnLoadMoreListener;
import org.tcshare.widgets.swipetoloadlayout.OnRefreshListener;
import org.tcshare.widgets.swipetoloadlayout.SwipeToLoadLayout;

import java.util.ArrayList;

/**
 * Created by yuxiaohei on 2018/4/8.
 */

public class RVListFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rv_list, container, false);
        SwipeToLoadLayout swipeToLoadLayout = view.findViewById(R.id.swipeToLoadLayout);

        //增加刷新头、更多的背景色 swipe_load
        RecyclerView recyclerView = view.findViewById(R.id.swipe_target);
        recyclerView.addItemDecoration(ItemDecorations.vertical(getContext()).create());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RVListAdapter<String> rvListAdapter = new RVListAdapter<String>();
        recyclerView.setAdapter(rvListAdapter);

        swipeToLoadLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeToLoadLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rvListAdapter.setDatas(new ArrayList<String>() {
                            {
                                add("rrrrrrrrrrrr");
                            }
                        });
                        swipeToLoadLayout.setRefreshing(false);
                    }
                }, 2000);

            }
        });
        swipeToLoadLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                swipeToLoadLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rvListAdapter.addDatas(new ArrayList<String>() {
                            {
                                add("mmmmmmmmmmmm");
                            }
                        });
                        swipeToLoadLayout.setLoadingMore(false);
                    }
                }, 2000);
            }
        });

        swipeToLoadLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeToLoadLayout.setRefreshing(true);
            }
        }, 100);


        return view;
    }

}
