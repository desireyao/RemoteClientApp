package com.yaoh.remoteclient.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import com.yaoh.remoteclient.Constants;
import com.yaoh.remoteclient.model.SliceModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaoh on 2018/4/17.
 */

public class BitmapUtil {

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
     * @param bitmap
     * @return
     */
    public static byte[] bitmap2JPGBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * 保存 bitmap 到本地
     * @param picName
     * @param bytes
     */
    public static void bitmapSave(String picName, byte[] bytes) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/RemoteClientApp/" + picName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
