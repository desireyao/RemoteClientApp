package com.yaoh.remoteclient.screenshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;

import com.yaoh.remoteclient.App;
import com.yaoh.remoteclient.listeners.ShotScreenBitmapListener;
import com.yaoh.remoteclient.tools.LogTool;
import com.yaoh.remoteclient.utils.ScreenUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wei on 16-12-1.
 */
public class Shotter {
    private static final String TAG = "Shotter";

    public ShotScreenBitmapListener mListener;

    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;

    private ScheduledExecutorService mExecutor;

    private static final int PERIOD_TIME = 500;

    public Shotter(Intent resultData, ShotScreenBitmapListener listener) {
        mListener = listener;

        mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, resultData);
        mImageReader = ImageReader.newInstance(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(),
                PixelFormat.RGBA_8888, 2);

        mMediaProjection.createVirtualDisplay("screen-mirror",
                ScreenUtils.getScreenWidth(),
                ScreenUtils.getScreenHeight(),
                Resources.getSystem().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),
                null,
                null);
    }

    private MediaProjectionManager getMediaProjectionManager() {
        Context mContext = App.getApp();
        return (MediaProjectionManager) mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }


    public void startShotScreenTask() {
        LogTool.LogE_DEBUG(TAG, "startShotScreenBitmap--->");
//        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//            @Override
//            public void onImageAvailable(ImageReader reader) {
//                LogTool.LogD(TAG, "onImageAvailable--->");
//                acquireLatestImage();
//            }
//        }, mHandler);

        if (mExecutor != null) {
            mExecutor.shutdown();
        }

        mExecutor = new ScheduledThreadPoolExecutor(1);
        mExecutor.scheduleAtFixedRate(new ScreenShotTask(), 0, PERIOD_TIME, TimeUnit.MILLISECONDS);
//        mExecutor.execute(new ScreenShotTask());
    }

    /**
     * 停止截屏
     */
    public void stopShotScreen() {
        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }

    /**
     * 开始截屏
     */
    public void startShotScreen() {
        if (mExecutor == null) {
            mExecutor = new ScheduledThreadPoolExecutor(1);
        }
        mExecutor.execute(new ScreenShotTask());
    }

    // 在后台线程里保存文件
    private Handler backgroundHandler;

    private Handler getBackgroundHandler() {
        if (backgroundHandler == null) {
            HandlerThread backgroundThread = new HandlerThread("easyscreenshot",
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
        }

        return backgroundHandler;
    }

    class ScreenShotTask implements Runnable {

        @Override
        public void run() {
//            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    LogTool.LogE_DEBUG(TAG, "onImageAvailable--->");
//                    acquireLatestImage();
//                }
//            }, getBackgroundHandler());

            acquireLatestImage();
        }
    }

    /**
     * 获取截屏的 bitmap
     */
    private void acquireLatestImage() {
        Image image = mImageReader.acquireLatestImage();
        if (image == null) {
            LogTool.LogE_DEBUG(TAG, "image == null");
            return;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();   // 每个像素的间距
        int rowStride = planes[0].getRowStride();       // 总的间距
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);

        image.close();

//        if (mVirtualDisplay != null) {
//            mVirtualDisplay.release();
//        }

        if (mListener != null) {
            mListener.onShotScreenBitmap(bitmap);
        }
    }

}
