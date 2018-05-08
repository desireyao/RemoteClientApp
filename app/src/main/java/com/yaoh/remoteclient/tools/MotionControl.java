package com.yaoh.remoteclient.tools;

import com.yaoh.remoteclient.utils.ShellUtil;

/**
 * Created by yaoh on 2018/5/7.
 */

public class MotionControl {

    private static final String TAG = "MotionControl";

    private int lastX;
    private int lastY;

    public void touch(int curX, int curY) {
        if (lastX == 0 && lastY == 0) {
            lastX = curX;
            lastY = curY;
            return;
        } else {
            if (Math.abs(curX - lastX) < 20
                    && Math.abs(curY - lastY) < 20) {
                return;
            }
        }

        StringBuffer action = new StringBuffer();
        action.append("input touchscreen swipe ")
                .append(lastX + " ")
                .append(lastY + " ")
                .append(curX + " ")
                .append(curY);
        LogTool.LogE_DEBUG(TAG, action.toString());

        ShellUtil.execShellCmd(action.toString());
        lastX = curX;
        lastY = curY;
    }

}
