package com.yaoh.remoteclient.socket;

import android.graphics.Bitmap;

import com.yaoh.remoteclient.listeners.ScreenShotSendDataListener;
import com.yaoh.remoteclient.model.SliceModel;
import com.yaoh.remoteclient.tools.LogTool;
import com.yaoh.remoteclient.utils.BitmapUtil;
import com.yaoh.remoteclient.utils.ConvertUtils;
import com.yaoh.remoteclient.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by yaoh on 2018/4/24.
 */

public class SocketClientDataManager extends SocketClientManager {

    private ScheduledExecutorService mExecutor;
    private List<SliceModel> mDataList = new ArrayList<>();

    private final String LOCK = "LOCK";
    private int mCount;

    private ScreenShotSendDataListener mListener;

    @Override
    public void sendloginData() {
        sendData(createLoginData(1, 100, "123"));
    }

    /***
     * 发送设备信息
     */
    public void sendDeviceInfoData() {
        String deviceInfo = String.format("%d#%d", ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        int len = deviceInfo.length();

        StringBuffer buffer = new StringBuffer();
        buffer.append("*")
                .append("12")
                .append(len)
                .append("\n")
                .append(deviceInfo);

        sendData(buffer.toString());
    }


    /**
     * 发送并刷新 差异的截图数据
     *
     * @param diffData
     */
    public void sendDiffData(byte[] diffData) {
        sendData(diffData);

        LogTool.LogE_DEBUG(TAG, " diffData.length = " + diffData.length);
        sendData("*520\n");
    }

//    public synchronized void sendDiffData(byte[] diffData) {
//        String refrshIamgeCmd = "*520\n";
//        byte[] refreshImageCmdBytes = refrshIamgeCmd.getBytes();
//        byte[] sendData = new byte[diffData.length + refreshImageCmdBytes.length];
//
//        System.arraycopy(diffData, 0, sendData, 0, diffData.length);
//        System.arraycopy(refreshImageCmdBytes, 0, sendData, diffData.length,
//                refreshImageCmdBytes.length);
//        sendData(sendData);
////        sendData("*520\n");
//    }

}
