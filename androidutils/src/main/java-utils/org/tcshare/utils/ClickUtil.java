package org.tcshare.utils;

import android.os.SystemClock;

public class ClickUtil {
    private final int count;
    private final int millis;
    private final int resetDuration;
    private final Callback cb;
    long[] mHits = null;

    /**
     *
     * @param count 次数
     * @param millisLimit  毫秒
     * @param resetDuration 毫秒
     * @param cb
     */
    public ClickUtil(int count, int millisLimit, int resetDuration, Callback cb) {
        this.count = count;
        this.millis = millisLimit;
        this.resetDuration = resetDuration;
        this.cb = cb;
    }


    public void fastClickCounter() {
        if (mHits == null) {
            mHits = new long[count];
        }
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);//把从第二位至最后一位之间的数字复制到第一位至倒数第一位
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();//记录一个时间
        if (SystemClock.uptimeMillis() - mHits[0] <= millis) {//X秒内连续点击。
            mHits = null;    //这里说明一下，我们在进来以后需要还原状态，否则如果点击过快，第六次，第七次 都会不断进来触发该效果。重新开始计数即可
            cb.onClickNumber();
        }
    }

    public interface Callback{
        void onClickNumber();
    }
}
