package com.yaoh.remoteclient.screenshot;

import android.graphics.Bitmap;

import com.yaoh.remoteclient.listeners.ShotScreenPicDiffListener;
import com.yaoh.remoteclient.model.SliceModel;
import com.yaoh.remoteclient.tools.LogTool;
import com.yaoh.remoteclient.utils.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaoh on 2018/4/5.
 */

public class PixelDiffManager {

    private static final String TAG = "TAG";

    // 上一次的记录
    private List<SliceModel> mSlicesLast = new ArrayList<>();

    // 返回有差异的图片
    private List<SliceModel> mSlicesDiff = new ArrayList<>();

    private ShotScreenPicDiffListener mListener;

    public PixelDiffManager(ShotScreenPicDiffListener listener) {
        mListener = listener;
    }

    public void calPicDiffPart(Bitmap bitmap) {
        new Thread(new DiffPicTask(bitmap)).start();
    }

    class DiffPicTask implements Runnable {

        private Bitmap mBitmap;

        public DiffPicTask(Bitmap mBitmap) {
            this.mBitmap = mBitmap;
        }

        @Override
        public void run() {
            LogTool.LogE_DEBUG(TAG, "TASK BEGIN--->");
//            mSlicesDiff.clear();

            if (mSlicesLast.isEmpty()) {
                mSlicesLast = BitmapUtil.splitBitmap(mBitmap);
                mListener.onShotScreenPicDiff(true, mSlicesLast);
                return;
            }

            List<SliceModel> mSlicesCur = BitmapUtil.splitBitmap(mBitmap);
            if (mSlicesCur.size() != mSlicesLast.size()) {
                mListener.onShotScreenPicDiff(false, mSlicesDiff);
                return;
            }

            mSlicesDiff.clear();
            for (int i = 0; i < mSlicesCur.size(); i++) {
                Bitmap mPartBitmap1 = mSlicesLast.get(i).getBitmap();
                Bitmap mPartBitmap2 = mSlicesCur.get(i).getBitmap();

                int mPartWidth = mPartBitmap1.getWidth();
                int mPartHeight = mPartBitmap1.getHeight();

                int[] pixels1 = new int[mPartWidth * mPartHeight];
                int[] pixels2 = new int[mPartWidth * mPartHeight];
                mPartBitmap1.getPixels(pixels1, 0, mPartWidth, 0, 0, mPartWidth, mPartHeight);
                mPartBitmap2.getPixels(pixels2, 0, mPartWidth, 0, 0, mPartWidth, mPartHeight);

                for (int j = 0; j < pixels1.length; j++) {
                    if (pixels1[j] != pixels2[j]) {
                        mSlicesDiff.add(mSlicesCur.get(i));
                        break;
                    }
                }
            }

            if (mListener != null && mSlicesDiff.size() != 0) {
                mListener.onShotScreenPicDiff(true, mSlicesDiff);
                // 将新的保存到上一帧
                mSlicesLast = mSlicesCur;
            } else {
                mListener.onShotScreenPicDiff(false, mSlicesDiff);
            }

            LogTool.LogE_DEBUG(TAG, "TASK END--->");
        }
    }

}
