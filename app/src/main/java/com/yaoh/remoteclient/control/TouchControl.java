package com.yaoh.remoteclient.control;

import com.yaoh.remoteclient.model.TouchModel;
import com.yaoh.remoteclient.tools.LogTool;
import com.yaoh.remoteclient.utils.ShellUtil;

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yaoh on 2018/5/7.
 */

public class TouchControl {

    private static final String TAG = "TouchControl";

    private ScheduledExecutorService mExcutor;

    private long mStartTouchTime;
    private String mTouchCmd;

    public TouchControl() {
        mExcutor = new ScheduledThreadPoolExecutor(1);
    }

    public void touch(LinkedList<TouchModel> touchModels, TouchModel curtTouchModel) {

        if (touchModels.size() <= 3) {
            mTouchCmd = buiildTapCmd(curtTouchModel);
        } else {
            mTouchCmd = buiildSwipeCmd(touchModels.getFirst(), touchModels.getLast());
        }

        if (System.currentTimeMillis() - mStartTouchTime < 100) {
            return;
        }

        mExcutor.schedule(new Task(mTouchCmd), 0, TimeUnit.MILLISECONDS);
    }

    private String buiildTapCmd(TouchModel downTouchModel) {
        StringBuffer action = new StringBuffer();
        action.append("input tap ")
                .append(downTouchModel.getTouchX() + " ")
                .append(downTouchModel.getTouchY() + " ");
        return action.toString();
    }

    private String buiildSwipeCmd(TouchModel downTouchModel, TouchModel upTouchModel) {
        StringBuffer action = new StringBuffer();

        action.append("input swipe ")
                .append(downTouchModel.getTouchX() + " ")
                .append(downTouchModel.getTouchY() + " ")
                .append(upTouchModel.getTouchX() + " ")
                .append(upTouchModel.getTouchY() + " " + 100);

        return action.toString();
    }

    class Task implements Runnable {

        private String mCmd;

        public Task(String cmd) {
            this.mCmd = cmd;
        }

        @Override
        public void run() {
            mStartTouchTime = System.currentTimeMillis();
            LogTool.LogE_DEBUG(TAG, " mCmd ---> " + mCmd + " mStartTouchTime = " + mStartTouchTime);
            ShellUtil.execShellCmd(mCmd);
        }
    }

}
