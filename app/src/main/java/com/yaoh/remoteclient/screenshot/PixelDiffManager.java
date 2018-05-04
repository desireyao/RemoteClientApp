package com.yaoh.remoteclient.screenshot;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.yaoh.remoteclient.listeners.ShotScreenPicDiffListener;
import com.yaoh.remoteclient.model.SliceModel;
import com.yaoh.remoteclient.tools.LogTool;
import com.yaoh.remoteclient.utils.BitmapUtil;
import com.yaoh.remoteclient.utils.ConvertUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by yaoh on 2018/4/5.
 */

public class PixelDiffManager {
    private static final String TAG = "PixelDiffManager";

    // 上一帧图片的记录
    private List<SliceModel> mSlicesLast = new ArrayList<>();
    private List<SliceModel> mSlicesCur = new ArrayList<>();

    // 返回有差异的图片
    private List<SliceModel> mSlicesDiff = Collections.synchronizedList(new ArrayList<SliceModel>());
    private List<Byte> mByteDiffDataList = Collections.synchronizedList(new ArrayList<Byte>());

    private ShotScreenPicDiffListener mListener;

    private String LOCK = "LOCK";

    private int mDiffCount = 0;

    private ScheduledExecutorService mExcutor;
    private static final int mCorePoolSize = 1;

    public PixelDiffManager(ShotScreenPicDiffListener listener) {
        mListener = listener;
    }

    public void startDiffPicTask(Bitmap bitmap) {
        LogTool.LogE_DEBUG(TAG, "startDiffPicTask ------> time = " + System.currentTimeMillis());

        if (mExcutor == null) {
            mExcutor = new ScheduledThreadPoolExecutor(mCorePoolSize);
        }

        mExcutor.execute(new Task(bitmap));
    }

    private byte[] slices2DiffData(List<SliceModel> slices) {

        for (int i = 0; i < slices.size(); i++) {
            addDiffData2Bytes(slices.get(i));
        }

        byte[] byteDiffData = new byte[mByteDiffDataList.size()];
        for (int i = 0; i < mByteDiffDataList.size(); i++) {
            byteDiffData[i] = mByteDiffDataList.get(i);
        }
        return byteDiffData;
    }

    class Task implements Runnable {

        private Bitmap mBitmap;

        public Task(Bitmap mBitmap) {
            this.mBitmap = mBitmap;
        }

        @Override
        public void run() {
            LogTool.LogE_DEBUG(TAG, "TASK BEGIN111--->time = " + System.currentTimeMillis());
            // 把所有的图像数据包括指令 先转List<Byte> 再转byte[]
            mByteDiffDataList.clear();

            if (mSlicesLast.isEmpty()) {
                mSlicesLast = BitmapUtil.splitBitmap(mBitmap);
                mListener.onShotScreenPicDiff(true, slices2DiffData(mSlicesLast));
                return;
            }
            mSlicesDiff.clear();

            mSlicesCur = BitmapUtil.splitBitmap(mBitmap);

            LogTool.LogE_DEBUG(TAG, " mExcutor---> isShutdown = " + mExcutor.isShutdown()
                    + " TASK BEGIN222--->time = " + System.currentTimeMillis());

            for (int i = 0; i < mSlicesCur.size(); i++) {
                mExcutor.execute(new DiffPicTask(i));
            }
        }
    }

    /**
     * 多线程比较两张图片的不同
     */
    class DiffPicTask implements Runnable {

        private int mIndex;

        public DiffPicTask(int mIndex) {
            this.mIndex = mIndex;
        }

        @Override
        public void run() {
            Bitmap mPartBitmap1 = mSlicesLast.get(mIndex).getBitmap();
            Bitmap mPartBitmap2 = mSlicesCur.get(mIndex).getBitmap();

            int mPartWidth = mPartBitmap1.getWidth();
            int mPartHeight = mPartBitmap1.getHeight();

            int[] pixels1 = new int[mPartWidth * mPartHeight];
            int[] pixels2 = new int[mPartWidth * mPartHeight];

            mPartBitmap1.getPixels(pixels1, 0, mPartWidth, 0, 0, mPartWidth, mPartHeight);
            mPartBitmap2.getPixels(pixels2, 0, mPartWidth, 0, 0, mPartWidth, mPartHeight);

            for (int j = 0; j < pixels1.length; j++) {
                if (pixels1[j] != pixels2[j]) {
                    mSlicesDiff.add(mSlicesCur.get(mIndex));
                    addDiffData2Bytes(mSlicesCur.get(mIndex));
//                  LogTool.LogE_DEBUG(TAG, "slicePic---> x = " + mSlicesCur.get(i).getX() + " y = " + mSlicesCur.get(i).getY());
                    break;
                }
            }

            synchronized (LOCK) {
                mDiffCount++;
//                LogTool.LogD(TAG, " mDiffCount111 ---> " + mDiffCount);

                if (mDiffCount == mSlicesCur.size()) {    // 说明判断完所有的图片
                    mDiffCount = 0;
                    // 组成所有的图像数据一次发送
                    byte[] byteDiffData = new byte[mByteDiffDataList.size()];
                    for (int i = 0; i < mByteDiffDataList.size(); i++) {
                        byteDiffData[i] = mByteDiffDataList.get(i);
                    }

                    LogTool.LogE_DEBUG(TAG, "mDiffCount END 222 ---> " + mDiffCount
                            + " time = " + System.currentTimeMillis()
                            + " mSlicesDiff.SIZE = " + mSlicesDiff.size());

                    if (mListener != null) {
                        mListener.onShotScreenPicDiff(true, byteDiffData);
                        mSlicesLast = mSlicesCur;        // 将新的保存到上一帧
                    }
                }
            }

//          LogTool.LogE_DEBUG(TAG, "TASK 333--->time = " + System.currentTimeMillis());
        }
    }

    /**
     * 添加每一帧数据到 数组中
     *
     * @param slice
     */
    private void addDiffData2Bytes(SliceModel slice) {
//        LogTool.LogE_DEBUG(TAG, "sendScreenShotData111--->" + System.currentTimeMillis());

        Bitmap bitmap = slice.getBitmap();
//        LogTool.LogE_DEBUG(TAG, "sendScreenShotData222--->" + System.currentTimeMillis());
        // bitmap 转二进制数据

        byte[] bitmapData = BitmapUtil.bitmap2JPGBytes(bitmap);
//            LogTool.LogE_DEBUG(TAG, "sendScreenShotData222333--->" + System.currentTimeMillis());

//        int bitmapLength = bitmapData.length;
//      LogTool.LogSave(TAG, " bitmapLength = " + bitmapLength
//                    + " bitmapW = " + bitmap.getWidth()
//                    + " bitmapH = " + bitmap.getHeight()
//                    + " X =" + sliceModel.getX()
//                    + " Y =" + sliceModel.getY());


        byte[] xx = ConvertUtils.intToBytes(slice.getX(), 2);
        byte[] yy = ConvertUtils.intToBytes(slice.getY(), 2);
        byte x0 = xx[0];
        byte x1 = xx[1];
        byte y0 = yy[0];
        byte y1 = yy[1];

        StringBuffer headerBuffer = new StringBuffer();
        headerBuffer.append("*")
                .append("51")
                .append(bitmapData.length + 4)
                .append("\n");

//            LogTool.LogSave(TAG, "send picData header = " + headerBuffer.toString()
//                    + LogTool.LogBytes2Hex(xx, " xx")
//                    + LogTool.LogBytes2Hex(xx, " yy"));

        byte[] headerData = headerBuffer.toString().getBytes();
        byte[] sendData = new byte[headerData.length + bitmapData.length + 4];
        System.arraycopy(headerData, 0, sendData, 0, headerData.length);
        sendData[headerData.length] = x0;
        sendData[headerData.length + 1] = x1;
        sendData[headerData.length + 2] = y0;
        sendData[headerData.length + 3] = y1;
        System.arraycopy(bitmapData, 0, sendData, headerData.length + 4, bitmapData.length);

//        LogTool.LogE_DEBUG(TAG, "X = " + slice.getX() + " Y = " + slice.getY()
//                + " sendData--->" + sendData.length);

        for (int j = 0; j < sendData.length; j++) {
            mByteDiffDataList.add(sendData[j]);
        }

//      LogTool.LogE_DEBUG(TAG, "sendScreenShotData333--->" + System.currentTimeMillis());

    }

}
