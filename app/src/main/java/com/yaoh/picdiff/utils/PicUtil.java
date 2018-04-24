package com.yaoh.picdiff.utils;

import android.graphics.Bitmap;

import com.yaoh.picdiff.model.SliceModel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaoh on 2018/4/17.
 */

public class PicUtil {

    private static final int SLICEX = 8;
    private static final int SLICEY = 8;

    /**
     * 切割图片
     *
     * @param bitmap
     * @return
     */
    public static List<SliceModel> splitBitmap(Bitmap bitmap) {
        List<SliceModel> sliceModelList = new ArrayList<>();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int sliceW = width / SLICEX;
        int sliceH = height / SLICEY;

        for (int i = 0; i < SLICEX; i++) {
            for (int j = 0; j < SLICEY; j++) {
                int x = i * sliceW;
                int y = j * sliceH;
                Bitmap sliceBitmap = Bitmap.createBitmap(bitmap, x, y, sliceW, sliceH);
                SliceModel sliceModel = new SliceModel(x, y, sliceBitmap);
                sliceModelList.add(sliceModel);
            }
        }

        return sliceModelList;
    }


    /**
     * bitmap 转jpg 二进制数据
     *
     * @param bm
     * @return
     */
    public byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

}
