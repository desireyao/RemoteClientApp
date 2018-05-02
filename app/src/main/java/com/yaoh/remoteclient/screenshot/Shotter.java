package com.yaoh.remoteclient.screenshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

import com.yaoh.remoteclient.App;
import com.yaoh.remoteclient.listeners.ShotScreenBitmapListener;
import com.yaoh.remoteclient.utils.ScreenUtils;

import java.nio.ByteBuffer;

/**
 * Created by wei on 16-12-1.
 */
public class Shotter {
    private static final String TAG = "Shotter";

    public ShotScreenBitmapListener mListener;

    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    public Shotter(Intent resultData) {
        mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, resultData);
        mImageReader = ImageReader.newInstance(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(),
                PixelFormat.RGBA_8888, 1);
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("screen-mirror",
                ScreenUtils.getScreenWidth(),
                ScreenUtils.getScreenHeight(),
                Resources.getSystem().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    /**
     * 截屏生成的 Bitmap
     *
     * @return
     */
    public void shotScreenBitmap(ShotScreenBitmapListener listener) {
        mListener = listener;

        mVirtualDisplay = createVirtualDisplay();
        new Thread(new ShotScreenTask()).start();
    }


    private MediaProjectionManager getMediaProjectionManager() {
        Context mContext = App.getApp();
        return (MediaProjectionManager) mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    class ShotScreenTask implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Image image = mImageReader.acquireLatestImage();
            if (image == null) {
                if (mListener != null) {
                    mListener.onShotScreenBitmap(false, null);
                }
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
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
            }

            if (mListener != null) {
                mListener.onShotScreenBitmap(true, bitmap);
            }

        }
    }


}
