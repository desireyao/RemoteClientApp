package com.yaoh.picdiff.screenshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.yaoh.picdiff.model.PixelPointModel;
import com.yaoh.picdiff.tools.LogTool;

import java.util.ArrayList;
import java.util.List;
import static com.yaoh.picdiff.Constants.BASE_DIR_PATH;

/**
 * Created by yaoh on 2018/4/5.
 */

public class PixelDiffManager {

    private static final String TAG = "TAG";
    private List<PixelPointModel> pixelPoints = new ArrayList<>();

    public void readPixels(DiffPicListener listener) {
        mListener = listener;
        new Thread(new DiffPicTask()).start();
    }

    class DiffPicTask implements Runnable {

        @Override
        public void run() {
            LogTool.LogE_DEBUG(TAG, "TASK BEGIN--->");
            Long startTime = System.currentTimeMillis();
            // 图片1
            Bitmap  mBitmap1 = BitmapFactory.decodeFile(BASE_DIR_PATH + "/pic1.png");

            //  图片2
            Bitmap  mBitmap2 = BitmapFactory.decodeFile(BASE_DIR_PATH + "/pic2.png");
            LogTool.LogE_DEBUG(TAG, "TASK decodeFile time --->" + (System.currentTimeMillis() - startTime) + " ms");

            int mImgWidth = mBitmap1.getWidth();
            int mImgHeight = mBitmap1.getHeight();
            LogTool.LogE_DEBUG(TAG, "图片的大小 width = " + mImgWidth + "  height = " + mImgHeight);

            int[] pixels1 = new int[mImgWidth * mImgHeight];
            int[] pixels2 = new int[mImgWidth * mImgHeight];
            mBitmap1.getPixels(pixels1, 0, mImgWidth, 0, 0, mImgWidth, mImgHeight);
            mBitmap2.getPixels(pixels2, 0, mImgWidth, 0, 0, mImgWidth, mImgHeight);
            mBitmap1.recycle();
            mBitmap2.recycle();
            LogTool.LogE_DEBUG(TAG, "TASK getPixels time --->" + (System.currentTimeMillis() - startTime) +  " ms");

            pixelPoints.clear();
            for (int i = 0; i < pixels1.length; i++) {
                if (pixels1[i] != pixels2[i]) {
                    PixelPointModel point = new PixelPointModel();
                    int x = i % mImgWidth;
                    int y = i / mImgWidth;
                    point.setX(x);
                    point.setY(y);
                    point.setColor(pixels1[i]);
                    pixelPoints.add(point);
                }
            }

            long diffTime = (System.currentTimeMillis() - startTime);
            LogTool.LogE_DEBUG(TAG, "TASK END---> TIME = "
                    +  diffTime + " ms"
                    + " pixelPoints.size() = " + pixelPoints.size());

            if(mListener != null){
                mListener.onDiffPic("耗时: " + diffTime + " ms"
                        + " \t 差异的像素个数: " + pixelPoints.size());
            }

//            LogTool.LogE_DEBUG(TAG, " \n pixelPoints = " + pixelPoints.toString());
        }
    }

    private DiffPicListener mListener;

    public interface DiffPicListener{
        public void onDiffPic(String msg);
    }

}
