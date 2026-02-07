package org.tcshare.app.amodule.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.tcshare.app.R;
import org.tcshare.widgets.DragLeft2RightExitFrameLayout;

import java.util.List;

public class TCContainerActivity extends AppCompatActivity {
    private static final String TAG = TCContainerActivity.class.getSimpleName();
    private TextView tvTitle;
    private ViewGroup appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tc_container);

        setDragExitListener();
        tvTitle = findViewById(R.id.tv_title);
        appBarLayout = findViewById(R.id.appBarLayout);
        findViewById(R.id.ic_back).setOnClickListener(onClickListener);

        replaceFragment();
    }

    private void setDragExitListener() {
        // 向右拖拽退出activity
        DragLeft2RightExitFrameLayout dragExit = findViewById(R.id.dragExit);
        dragExit.setDragExitListner(new DragLeft2RightExitFrameLayout.DragExitListner() {
            @Override
            public void onExit() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        return super.onKeyDown(keyCode, event);
    }


    private void replaceFragment() {
        Intent intent = getIntent();
        Bundle bundle;
        if (intent != null && (bundle = intent.getBundleExtra("bundle")) != null) {
            boolean showHeader = bundle.getBoolean("showHeader", true);
            if (showHeader) {
                String title = bundle.getString("title");
                tvTitle.setText(title);
                appBarLayout.setVisibility(View.VISIBLE);
            } else {
                appBarLayout.setVisibility(View.GONE);
            }

            String fragmentName = bundle.getString("fragmentName");
            try {
                Fragment frag = (Fragment) Class.forName(fragmentName).newInstance();
                frag.setArguments(bundle);
                FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
                trans.replace(R.id.frag_container, frag);
                trans.commit();
            } catch (Exception e) {
                Log.e(TAG, "Exception in create Fragment Instance!!!!");
            }

        } else {
            Log.e(TAG, "Empty args and do nothing!");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        for (Fragment frag : frags) {
            frag.onActivityResult(requestCode, resultCode, data);
        }

    }

    /**
     * 传入需要打开的fragment，以及传给fragment的参数
     */
    public static void openSelf(Context ctx, Class frag, @NonNull Bundle bundle) {
        bundle.putString("fragmentName", frag.getName());
        Intent intent = new Intent();
        intent.putExtra("bundle", bundle);
        intent.setClass(ctx, TCContainerActivity.class);
        ctx.startActivity(intent);
    }

    /**
     * 传入需要打开的fragment
     */
    public static void openSelf(Context ctx, Class frag) {
        openSelf(ctx, frag, "");
    }

    /**
     * 传入需要打开的fragment
     * title 为 "" 则 不显示标题栏
     */
    public static void openSelf(Context ctx, Class frag, @NonNull String title) {
        openSelf(ctx, frag, !TextUtils.isEmpty(title), TextUtils.isEmpty(title) ? "" : title);
    }

    /**
     * 传入需要打开的fragment
     */
    private static void openSelf(Context ctx, Class frag, boolean showHeader, String title) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putBoolean("showHeader", showHeader);
        openSelf(ctx, frag, bundle);
    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.ic_back) {
                onBackPressed();
            }
        }
    };


}
