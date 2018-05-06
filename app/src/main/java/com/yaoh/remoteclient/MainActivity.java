package com.yaoh.remoteclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yaoh.remoteclient.service.ScreenShotMainService;
import com.yaoh.remoteclient.shell.ShellUtils;
import com.yaoh.remoteclient.socket.SocketCmd;
import com.yaoh.remoteclient.tools.LogTool;

import org.greenrobot.eventbus.EventBus;

import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Button btn_socketConnect;
    private Button btn_startScreenShot;
    private Button btn_closeConnect;

    private ScreenShotMainService mService;
    private TextView tv_content;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermisson();
        initData();
        initUI();
    }

    private void requestPermisson() {
        AndPermission.with(this)
                .permission(Permission.Group.STORAGE)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        LogTool.LogE_DEBUG(TAG, "onGranted: " + permissions.toString());
                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        List<String> permissionNames = Permission.transformText(App.getApp(), permissions);
                        Toast.makeText(getApplicationContext(), "请打开:" + permissionNames + "相关权限"
                                , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        finish();
                    }
                }).start();
    }

    private void initData() {

    }

    private void initUI() {
        btn_startScreenShot = findViewById(R.id.btn_startScreenShot);
        btn_startScreenShot.setOnClickListener(this);
        btn_socketConnect = findViewById(R.id.btn_socketConnect);
        btn_socketConnect.setOnClickListener(this);

        btn_closeConnect = findViewById(R.id.btn_closeConnect);
        btn_closeConnect.setOnClickListener(this);

        tv_content = findViewById(R.id.tv_content);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_startScreenShot) {
//            EventBus.getDefault().post(0);

            String path = "export CLASSPATH=/sdcard/Main.dex";
            String app = "exec app_process /sdcard com.main.Main";

            ShellUtils.execCommand(new String[]{path, app}, true);

        } else if (id == R.id.btn_socketConnect) {
            startService(new Intent(this, ScreenShotMainService.class));
            finish();
        } else if (id == R.id.btn_closeConnect) {
            EventBus.getDefault().post(new SocketCmd(SocketCmd.CMD_TYPE.CMD_DEVICE_DISCONNECT, null));
        }
    }

    /**
     * service 连接
     */
//    ServiceConnection mBandServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            LogTool.LogE(TAG, "onServiceConnected--->" + name.getClassName());
//            ScreenShotMainService.ServiceBinder binder = (ScreenShotMainService.ServiceBinder) service;
//            mService = binder.getService();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            LogTool.LogE(TAG, "onServiceDisconnected--->" + name.getClassName());
//        }
//    };

}
