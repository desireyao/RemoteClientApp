package com.yaoh.remoteclient.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yaoh.remoteclient.listeners.ShotScreenBitmapListener;
import com.yaoh.remoteclient.listeners.ShotScreenPicDiffListener;
import com.yaoh.remoteclient.model.SliceModel;
import com.yaoh.remoteclient.screenshot.PixelDiffManager;
import com.yaoh.remoteclient.screenshot.ScreenShotActivity;
import com.yaoh.remoteclient.screenshot.Shotter;
import com.yaoh.remoteclient.socket.SocketClientCmdManager;
import com.yaoh.remoteclient.socket.SocketClientDataManager;
import com.yaoh.remoteclient.socket.SocketClientManager;
import com.yaoh.remoteclient.socket.SocketCmd;
import com.yaoh.remoteclient.socket.SocketListener;
import com.yaoh.remoteclient.tools.LogTool;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by yaoh on 2018/4/6.
 */

public class ScreenShotMainService extends Service implements ShotScreenBitmapListener, ShotScreenPicDiffListener {

    private static final String TAG = "ScreenShotMainService";
    private PixelDiffManager pixelDiffManager;

    private SocketClientCmdManager mSocketClientCmdManager;
    private SocketClientDataManager mSocketClientDataManager;

    private Shotter mShotter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        LogTool.LogE_DEBUG(TAG, "ScreenShotMainService onCreate--->");
        EventBus.getDefault().register(this);

        pixelDiffManager = new PixelDiffManager(this);
        connectSocket();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogTool.LogE_DEBUG(TAG, "ScreenShotMainService onDestroy ----->");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogTool.LogE_DEBUG(TAG, "ScreenShotMainService onStartCommand--->");
        return START_STICKY;
    }

    /**
     * 开始截屏 首次弹出权限申请的窗口
     */
    public void startScreenShot() {
        Intent intent = new Intent(this, ScreenShotActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 连接socket 指令通道 数据通道
     */
    private void connectSocket() {
        mSocketClientCmdManager = new SocketClientCmdManager();
        mSocketClientCmdManager.setSocketListener(new SocketCmdListener());
        mSocketClientCmdManager.startConnect();

        mSocketClientDataManager = new SocketClientDataManager();
        mSocketClientDataManager.setSocketListener(new SocketDataListener());
        mSocketClientDataManager.setSendHeartData(true);
        mSocketClientDataManager.startConnect();
    }

    @Override
    public void onShotScreenBitmap(boolean isSucceed, Bitmap bitmap) {
        if (isSucceed) {
            pixelDiffManager.calPicDiffPart(bitmap);
        } else {
            // 失败重新截屏
            startScreenShot();
        }
    }

    @Override
    public void onShotScreenPicDiff(boolean isSucceed, List<SliceModel> dataList) {
        // 获取差异的图片部分
        LogTool.LogE_DEBUG(TAG, "onShotScreenPicDiff ---> isSucceed = " + isSucceed
                + " size = " + dataList.size()
                + " dataList = " + dataList.toString());

        if (mSocketClientDataManager.getStatus()
                != SocketClientManager.Status.STATUS_CONNECTED) {
            return;
        }

        boolean isSendSucced = mSocketClientDataManager.sendScreenShotData(dataList);

        if (isSendSucced) {
            startScreenShot();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShotScreenEvent(Intent data) {
        LogTool.LogE_DEBUG(TAG, "onShotScreenEvent --->");

        mShotter = new Shotter(data);
        mShotter.shotScreenBitmap(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onSocketCmdEvent(SocketCmd socketCmd) {
        LogTool.LogE_DEBUG(TAG, "onShotScreenEvent --->CMD_TYPE =" + socketCmd.getmCmdType().name());

        SocketCmd.CMD_TYPE type = socketCmd.getmCmdType();
        switch (type) {
            case CMD_DEVICE_INFO: {
                // 发送设备信息
                mSocketClientDataManager.sendDeviceInfoData();
                break;
            }
            case CMD_DEVICE_CONNECT: {
                // 开始截屏
                startScreenShot();
                break;
            }
            case CMD_DEVICE_DISCONNECT: {
                mSocketClientDataManager.close();
                mSocketClientCmdManager.close();
                break;
            }
            case CMD_MOUSE_MOVE: {
                byte[] data = socketCmd.getData();
                int x = ((data[1] << 8) & 0xffff) | (data[0] & 0xff);
                int y = ((data[3] << 8) & 0xffff) | (data[2] & 0xff);

                LogTool.LogE_DEBUG(TAG, "CMD_MOUSE_MOVE --->"
                        + LogTool.LogBytes(data, "data")
                        + "\n x = " + x
                        + " y = " + y);
            }
        }

    }


    /**
     * 指令通道的回调
     */
    class SocketCmdListener implements SocketListener {

        @Override
        public void onSocketConnected() {
            LogTool.LogE_DEBUG(TAG, "SocketCmdListener--->onSocketConnected");

            // 连接成功 发送登录数据
            mSocketClientCmdManager.sendloginData();
        }

        @Override
        public void onSocketDisconnected() {
            LogTool.LogE_DEBUG(TAG, "SocketCmdListener--->socket断开");
        }

        @Override
        public void onSocketResponse(byte[] recvData) {

            String rcvMsg = new String(recvData);
            LogTool.LogE_DEBUG(TAG,
                    "\n" + LogTool.LogBytes(recvData, "recvData")
                            + "\n" + LogTool.LogBytes2Hex(recvData, "recvDataHex")
                            + "\nrcvMsg =" + rcvMsg);

            mSocketClientCmdManager.onRecvCmd(recvData);

        }
    }

    /**
     * 数据通道的回调
     */
    class SocketDataListener implements SocketListener {

        @Override
        public void onSocketConnected() {
            LogTool.LogE_DEBUG(TAG, "SocketDataListener--->onSocketConnected");
            mSocketClientDataManager.sendloginData();
        }

        @Override
        public void onSocketDisconnected() {
            LogTool.LogE_DEBUG(TAG, "SocketDataListener--->socket断开");
        }

        @Override
        public void onSocketResponse(byte[] recvData) {
            LogTool.LogE_DEBUG(TAG, "SocketDataListener--->"
                    + LogTool.LogBytes(recvData, "recvData"));
        }
    }
}
