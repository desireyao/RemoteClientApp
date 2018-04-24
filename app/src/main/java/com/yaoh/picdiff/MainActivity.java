package com.yaoh.picdiff;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.yaoh.picdiff.screenshot.Shotter;
import com.yaoh.picdiff.service.ScreenShotMainService;
import com.yaoh.picdiff.tools.LogTool;
import com.yaoh.picdiff.utils.PicUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    public static final int REQUEST_MEDIA_PROJECTION = 1001;

    private Button btn_startScreenShot;

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
//            pixelDiffManager.readPixels(this);
//            ImageView img1 = findViewById(R.id.img1);
//            ImageView img2 = findViewById(R.id.img2);
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_desktop);
//            List<SliceModel> sliceModelList = PicUtil.splitBitmap(bitmap, 10, 10);
//            LogTool.LogE_DEBUG(TAG, "sliceModelList SIZE: " + sliceModelList.size());
//
//            img1.setImageBitmap(sliceModelList.get(0).getBitmap());
//            img2.setImageBitmap(sliceModelList.get(1).getBitmap());
            startService(new Intent(this, ScreenShotMainService.class));
            finish();
        }
    }


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onServiceEvent(final Bitmap bitmap) {
//
////        if (type == EnumNotifyType.SERVICE_ON_CREATE || type == EnumNotifyType.SERVICE_ON_START) {
////            // 绑定service
////            Intent intentService = new Intent(this, ScreenShotMainService.class);
////            bindService(intentService, mBandServiceConnection, Context.BIND_AUTO_CREATE);
////        }
//    }

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

}
