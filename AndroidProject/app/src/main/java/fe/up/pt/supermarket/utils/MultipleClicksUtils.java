package fe.up.pt.supermarket.utils;

import android.os.SystemClock;

public abstract class MultipleClicksUtils {
    private static long lastClickTime = 0;

    public static boolean prevent() {
        if(SystemClock.elapsedRealtime() - lastClickTime < 1000)
            return true;
        else
            lastClickTime = SystemClock.elapsedRealtime();

        return false;
    }
}

