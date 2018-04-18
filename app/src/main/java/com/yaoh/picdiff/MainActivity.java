package com.yaoh.picdiff;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yaoh.picdiff.enums.EnumNotifyType;
import com.yaoh.picdiff.model.SliceModel;
import com.yaoh.picdiff.screenshot.PixelDiffManager;
import com.yaoh.picdiff.service.ScreenShotMainService;
import com.yaoh.picdiff.tools.LogTool;
import com.yaoh.picdiff.utils.PicUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , PixelDiffManager.OnDiffPicListener {

    private static final String TAG = "MainActivity";

    private Button btn_compare;
    private Button btn_startScreenShot1;
    private Button btn_startScreenShot2;

    private PixelDiffManager pixelDiffManager;

    private ScreenShotMainService mService;

    private TextView tv_content;
    private TextView tv_img_path;

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
        pixelDiffManager = new PixelDiffManager();
    }

    private void initUI() {
        EventBus.getDefault().register(this);
        btn_compare = findViewById(R.id.btn_compare);
        btn_compare.setOnClickListener(this);

        btn_startScreenShot1 = findViewById(R.id.btn_startScreenShot1);
        btn_startScreenShot1.setOnClickListener(this);
        btn_startScreenShot2 = findViewById(R.id.btn_startScreenShot2);
        btn_startScreenShot2.setOnClickListener(this);

        tv_content = findViewById(R.id.text);
        tv_img_path = findViewById(R.id.tv_img_path);
        tv_img_path.setText("截图路径: " + Constants.BASE_DIR_PATH);

        startService(new Intent(MainActivity.this, ScreenShotMainService.class));
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_compare) {
//            pixelDiffManager.readPixels(this);
            ImageView img1 = findViewById(R.id.img1);
            ImageView img2 = findViewById(R.id.img2);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.img_desktop);
            List<SliceModel> sliceModelList = PicUtil.splitBitmap(bitmap,10,10);
            LogTool.LogE_DEBUG(TAG,"sliceModelList SIZE: " + sliceModelList.size());

            img1.setImageBitmap(sliceModelList.get(0).getBitmap());
            img2.setImageBitmap(sliceModelList.get(1).getBitmap());


        } else if (id == R.id.btn_startScreenShot1) {
            finish();

            if (mService != null) {
                mService.startScreenShot(1);
            }
        } else if (id == R.id.btn_startScreenShot2) {
            finish();

            if (mService != null) {
                mService.startScreenShot(2);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        unbindService(mBandServiceConnection);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumNotifyType type) {
        if (type == EnumNotifyType.SERVICE_ON_CREATE
                || type == EnumNotifyType.SERVICE_ON_START) {
            // 绑定service
            Intent intentService = new Intent(this, ScreenShotMainService.class);
            bindService(intentService, mBandServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * service 连接
     */
    ServiceConnection mBandServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogTool.LogE(TAG, "onServiceConnected--->" + name.getClassName());
            ScreenShotMainService.ServiceBinder binder = (ScreenShotMainService.ServiceBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogTool.LogE(TAG, "onServiceDisconnected--->" + name.getClassName());
        }
    };

    @Override
    public void onDiffPic(final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                tv_content.setText(msg);
            }
        });
    }
}
