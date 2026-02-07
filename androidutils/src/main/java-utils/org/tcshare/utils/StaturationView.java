package org.tcshare.utils;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;

/**
 * 去掉色彩，变灰
 * @author 爱T鱼
 */
public class StaturationView {
    private Paint paint = new Paint();
    private ColorMatrix cm = new ColorMatrix();
    private StaturationView(){

    }
    private static StaturationView instance;
    public static StaturationView getInstance(){
        synchronized (StaturationView.class) {
            if (instance == null) {
                instance = new StaturationView();
            }
        }
        return instance;
    }
    public void staturationView(View view, float staturation){
        cm.setSaturation(staturation);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        view.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

}
