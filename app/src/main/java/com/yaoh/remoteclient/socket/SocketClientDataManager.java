package com.yaoh.remoteclient.socket;

import android.graphics.Bitmap;

import com.yaoh.remoteclient.model.SliceModel;
import com.yaoh.remoteclient.tools.LogTool;
import com.yaoh.remoteclient.utils.BitmapUtil;
import com.yaoh.remoteclient.utils.ConvertUtils;
import com.yaoh.remoteclient.utils.ScreenUtils;

import java.util.List;

/**
 * Created by yaoh on 2018/4/24.
 */

public class SocketClientDataManager extends SocketClientManager {

    @Override
    public void sendloginData() {
        sendData(createLoginData(1, 100, "123"));
    }

    /***
     * 发送设备信息
     */
    public void sendDeviceInfoData() {
        String deviceInfo = String.format("%d#%d", ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        int len = deviceInfo.length();

        StringBuffer buffer = new StringBuffer();
        buffer.append("*")
                .append("12")
                .append(len)
                .append("\n")
                .append(deviceInfo);

        sendData(buffer.toString());
    }


    /**
     * 发送截屏数据
     *
     * @param dataList
     * @return
     */
    public boolean sendScreenShotData(List<SliceModel> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            SliceModel sliceModel = dataList.get(i);
            Bitmap bitmap = sliceModel.getBitmap();
            byte[] bitmapData = BitmapUtil.bitmap2JPGBytes(bitmap);
//            BitmapUtil.bitmapSave("pic" + i + ".jpg", bitmapData);
            int bitmapLength = bitmapData.length;
            LogTool.LogSave(TAG, " bitmapLength = " + bitmapLength
                    + " bitmapW = " + bitmap.getWidth()
                    + " bitmapH = " + bitmap.getHeight()
                    + " X =" + sliceModel.getX()
                    + " Y =" + sliceModel.getY());

            if (bitmapLength == 0) {
                return false;
            }

//            byte x0 = (byte) (sliceModel.getX() & 0xff);
//            byte x1 = (byte) ((sliceModel.getX() >> 8) & 0xff);
//            byte y1 = (byte) ((sliceModel.getY() >> 8) & 0xff);
//            byte y0 = (byte) (sliceModel.getY() & 0xff);

            byte[] xx = ConvertUtils.intToBytes(sliceModel.getX(), 2);
            byte[] yy = ConvertUtils.intToBytes(sliceModel.getY(), 2);
            byte x0 = xx[0];
            byte x1 = xx[1];
            byte y0 = yy[0];
            byte y1 = yy[1];

            StringBuffer headerBuffer = new StringBuffer();
            headerBuffer.append("*")
                    .append("51")
                    .append(String.valueOf(bitmapData.length + 4))
                    .append("\n");

            LogTool.LogSave(TAG, "send picData header = " + headerBuffer.toString()
                    + LogTool.LogBytes2Hex(xx, " xx")
                    + LogTool.LogBytes2Hex(xx, " yy"));

            byte[] headerData = headerBuffer.toString().getBytes();
            byte[] sendData = new byte[headerData.length + bitmapData.length + 4];
            System.arraycopy(headerData, 0, sendData, 0, headerData.length);
            sendData[headerData.length] = x0;
            sendData[headerData.length + 1] = x1;
            sendData[headerData.length + 2] = y0;
            sendData[headerData.length + 3] = y1;
            System.arraycopy(bitmapData, 0, sendData, headerData.length + 4, bitmapData.length);

            sendData(sendData);
        }

        // 刷新图像数据
        sendData("*520\n");

        return true;
    }
}
