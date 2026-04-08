package org.tcshare.utils;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;

/**
 * 去掉色彩，变灰
 * @author 爱T鱼
 */
public class SaturationViewUtil {
    private final Paint paint = new Paint();
    private final ColorMatrix cm = new ColorMatrix();
    private SaturationViewUtil(){

    }
    private static SaturationViewUtil instance;
    public static SaturationViewUtil getInstance(){
        synchronized (SaturationViewUtil.class) {
            if (instance == null) {
                instance = new SaturationViewUtil();
            }
        }
        return instance;
    }
    public void saturationView(View view, float saturation){
        cm.setSaturation(saturation);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        view.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

}
