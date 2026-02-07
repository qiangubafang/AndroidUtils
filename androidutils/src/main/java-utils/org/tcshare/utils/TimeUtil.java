package org.tcshare.utils;

public class TimeUtil {

    /**
     * 获取19位的 纳秒 时间戳
     * the milliseconds since January 1, 1970, 00:00:00 GMT.
     * @return
     */
    public static long getNanoTimeStamp(){
        return System.currentTimeMillis() * 1000_000L + System.nanoTime() % 1000_000L;
    }

    /**
     * 把19位的时间戳，截取为ms时间戳，java就可以直接用了。
     * the milliseconds since January 1, 1970, 00:00:00 GMT.
     * @param ts
     * @return
     */
    public static long parseNanoTimeStampToMS(long ts){
        return ts / 1000_000L;
    }
}
