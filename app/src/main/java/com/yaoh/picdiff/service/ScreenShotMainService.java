package com.yaoh.picdiff.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yaoh.picdiff.listeners.ShotScreenBitmapListener;
import com.yaoh.picdiff.listeners.ShotScreenPicDiffListener;
import com.yaoh.picdiff.model.SliceModel;
import com.yaoh.picdiff.screenshot.PixelDiffManager;
import com.yaoh.picdiff.screenshot.ScreenShotActivity;
import com.yaoh.picdiff.socket.SocketClientManager;
import com.yaoh.picdiff.socket.SocketListener;
import com.yaoh.picdiff.tools.LogTool;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by yaoh on 2018/4/6.
 */

public class ScreenShotMainService extends Service implements ShotScreenBitmapListener, ShotScreenPicDiffListener ,SocketListener{

    private static final String TAG = "ScreenShotMainService";
    private PixelDiffManager pixelDiffManager;

    private Handler mHandler = new Handler();

    private  SocketClientManager mSocketClientManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder(this);
    }

    @Override
    public void onSocketConnected() {
        LogTool.LogE_DEBUG(TAG,"onSocketConnected--->");
        //
        mSocketClientManager.sendData("*109\\n0#1#1#123");

    }

    @Override
    public void onSocketDisConnected() {
        LogTool.LogE_DEBUG(TAG,"onSocketDisConnected--->");
    }

    @Override
    public void onSocketResponse(String msg) {
        LogTool.LogE_DEBUG(TAG,"onSocketResponse--->msg = " + msg);
    }


    public class ServiceBinder extends Binder {

        private ScreenShotMainService service;

        protected ServiceBinder(ScreenShotMainService service) {
            this.service = service;
        }

        public ScreenShotMainService getService() {
            return service;
        }
    }

    @Override
    public void onCreate() {
        LogTool.LogE_DEBUG(TAG, "ScreenShotMainService onCreate--->");
        pixelDiffManager = new PixelDiffManager(this);
//        startScreenShot();

          connectSocket();
//        EventBus.getDefault().register(this);
//        EventBus.getDefault().post(EnumNotifyType.SERVICE_ON_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogTool.LogE_DEBUG(TAG, "ScreenShotMainService onDestroy ----->");

//        EventBus.getDefault().unregister(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogTool.LogE_DEBUG(TAG, "ScreenShotMainService onStartCommand--->");
//        EventBus.getDefault().post(EnumNotifyType.SERVICE_ON_START);
        return START_STICKY;
    }

    public void startScreenShot() {
        Intent intent = new Intent(this, ScreenShotActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 连接socket
     */
    private void connectSocket() {
        mSocketClientManager = new SocketClientManager();
        mSocketClientManager.startConnect(this);

    }

    @Override
    public void onShotScreenBitmap(boolean isSucceed, Bitmap bitmap) {

        if (isSucceed) {
            pixelDiffManager.calPicDiffPart(bitmap);
        } else {
            startScreenShot();
        }

    }

    @Override
    public void onShotScreenPicDiff(boolean isSucceed, List<SliceModel> dataList) {

        LogTool.LogE_DEBUG(TAG, "onShotScreenPicDiff ---> isSucceed = " + isSucceed
                + " SIZE = " + dataList.size()
                + " dataList = " + dataList.toString());

        startScreenShot();
    }

}
