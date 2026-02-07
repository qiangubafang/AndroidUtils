package org.tcshare.app.amodule.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import org.tcshare.app.R;
import org.tcshare.widgets.DragLeft2RightExitFrameLayout;

public class TCDrag2RightExitActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tc_drag2rightexit);

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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



}
