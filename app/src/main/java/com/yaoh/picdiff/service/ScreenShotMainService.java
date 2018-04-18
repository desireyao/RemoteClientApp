package com.yaoh.picdiff.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketClientDelegate;
import com.vilyever.socketclient.helper.SocketHeartBeatHelper;
import com.vilyever.socketclient.helper.SocketResponsePacket;
import com.vilyever.socketclient.util.CharsetUtil;
import com.yaoh.picdiff.enums.EnumNotifyType;
import com.yaoh.picdiff.screenshot.ScreenShotActivity;
import com.yaoh.picdiff.tools.LogTool;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by yaoh on 2018/4/6.
 */

public class ScreenShotMainService extends Service {

    private static final String TAG = "ScreenShotMainService";
    private SocketClient socketClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder(this);
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
        EventBus.getDefault().post(EnumNotifyType.SERVICE_ON_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogTool.LogE_DEBUG(TAG, "ScreenShotMainService onStartCommand--->");

        EventBus.getDefault().post(EnumNotifyType.SERVICE_ON_START);
        return START_STICKY;
    }

    public void startScreenShot(final int picIndex) {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ScreenShotMainService.this, ScreenShotActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("picIndex", picIndex);
                startActivity(intent);
            }
        }, 1000);
    }

    // 开始 socket 连接
    private void startConnectSockrt() {
        socketClient = new SocketClient();
        socketClient.getAddress().setRemoteIP("120.79.248.218"); // 远程端IP地址
        socketClient.getAddress().setRemotePort("6000"); // 远程端端口号
        socketClient.getAddress().setConnectionTimeout(10 * 1000); // 连接超时时长，单位毫秒
//        socketClient.setHeartBeatHelper(new SocketHeartBeatHelper().);
        socketClient.setCharsetName(CharsetUtil.UTF_8); // 设置编码为UTF-8

        socketClient.removeSocketClientDelegate(new SocketClientDelegate() {

            @Override
            public void onConnected(SocketClient client) {
                LogTool.LogE_DEBUG(TAG, "onConnected--->");
            }

            @Override
            public void onDisconnected(SocketClient client) {
                LogTool.LogE_DEBUG(TAG, "onDisconnected--->");
            }

            @Override
            public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
                LogTool.LogE_DEBUG(TAG, "onResponse--->");
            }
        });

        socketClient.connect();
    }
}
