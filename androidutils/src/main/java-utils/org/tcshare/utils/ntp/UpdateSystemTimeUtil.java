package org.tcshare.utils.ntp;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Pair;

/**
 * 更新系统时间 or 更改系统自动对时 <br/>
 */
public class UpdateSystemTimeUtil {

    /**
     * 需要系统权限：
     * <uses-permission android:name="android.permission.SET_TIME"
     * tools:ignore="ProtectedPermissions" />
     *
     * @param host
     * @return 是否成功，及当前NTP时间
     */
    public static Pair<Boolean, Long> updateSystem(String host) {
        return updateSystem(host, 5000); // 默认超时时间
    }

    /**
     * 需要系统权限：
     * <uses-permission android:name="android.permission.SET_TIME"
     * tools:ignore="ProtectedPermissions" />
     *
     * @param host
     * @param timeout
     * @return 是否成功，及当前NTP时间
     */
    public static Pair<Boolean, Long> updateSystem(String host, int timeout) {
        SntpClient client = new SntpClient();
        if (client.requestTime(host, timeout)) {
            long now = client.getNtpTime();
            return Pair.create(SystemClock.setCurrentTimeMillis(now), now);
        } else {
            return Pair.create(false, -1L);
        }
    }

    /**
     * 设置是否自动对时
     * 需要系统权限：
     * <uses-permission android:name="android.permission.WRITE_SECURE_SETTINGS"
     * tools:ignore="ProtectedPermissions"
     *
     * @param ctx
     * @param auto
     * @return
     */
    public static boolean setAutoTime(Context ctx, boolean auto) {
        return Settings.Global.putInt(ctx.getContentResolver(), Settings.Global.AUTO_TIME, auto ? 1 : 0);
    }

}
