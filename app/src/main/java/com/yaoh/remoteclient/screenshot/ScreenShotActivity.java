package com.yaoh.remoteclient.screenshot;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.Window;

import com.yaoh.remoteclient.tools.LogTool;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by wei on 16-9-18.
 * <p>
 * 完全透明 只是用于弹出权限申请的窗而已
 */
public class ScreenShotActivity extends Activity {
    private static final String TAG = "ScreenShotActivity";

    public static final int REQUEST_MEDIA_PROJECTION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如下代码 只是想 启动一个透明的Activity 而上一个activity又不被pause
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getWindow().setDimAmount(0f);

        // 申请截屏
        requestScreenShot();
    }

    /**
     * 申请截屏
     */
    public void requestScreenShot() {
        startActivityForResult(((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }


    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        LogTool.LogE_DEBUG(TAG, "onActivityResult--->requestCode = " + requestCode
                + " resultCode = " + resultCode);

        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            EventBus.getDefault().post(data);

            finish();
        }
    }

}