package com.yaoh.picdiff.socket;

import android.text.TextUtils;

import com.yaoh.picdiff.tools.LogTool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by yaoh on 2018/4/24.
 */

public class SocketClientManager {

    private static final String TAG = "SocketClientManager";

    private static final String serverIP = "47.100.214.77";
    private static final int serverPort = 6000;

//    private static final String serverIP = "192.168.40.203";
//    private static final int serverPort = 6000;

    private SocketListener mSocketListener;

    private DataOutputStream dataOutputStream;

    private boolean isRun = true;

    /**
     * 开始连接 socket
     *
     * @param socketListener
     */
    public void startConnect(SocketListener socketListener) {
        mSocketListener = socketListener;

        new Thread(new SocketConnectTask()).start();
    }

    /**
     * 发送数据
     *
     * @param data byte[]
     */
    public void sendData(byte[] data) {
        try {
            if (dataOutputStream != null) {
                dataOutputStream.write(data);
            }
        } catch (IOException e) {

        }
    }

    /**
     * 发送数据
     *
     * @param data String
     */
    public void sendData(String data) {
        LogTool.LogE_DEBUG(TAG, "sendData = " + data);

        try {
            if (dataOutputStream != null) {
                dataOutputStream.write(data.getBytes("utf-8"));
            }
        } catch (IOException e) {

        }
    }

    class SocketConnectTask implements Runnable {

        @Override
        public void run() {
            DataInputStream dataInputStream = null;
            Socket mSocket = null;
            try {
                mSocket = new Socket(serverIP, serverPort);
                mSocket.setSoTimeout(1000 * 10);
                if (mSocketListener != null) {
                    mSocketListener.onSocketConnected();
                }

                LogTool.LogE_DEBUG(TAG, "socket 111---> " + mSocket);

                // 输出流
                dataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                // 输入流
                dataInputStream = new DataInputStream(mSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                LogTool.LogE_DEBUG(TAG, e.toString());
                if (mSocketListener != null) {
                    mSocketListener.onSocketDisConnected();
                }
                return;
            }

            byte buff[] = new byte[4096];
            while (isRun) {
                try {
                    int rcvLen = dataInputStream.read(buff);
                    String rcvMsg = new String(buff, 0, rcvLen, "utf-8");
                    LogTool.LogE_DEBUG(TAG, "收到消息--->" + rcvMsg);

                    if (mSocketListener != null) {
                        mSocketListener.onSocketResponse(rcvMsg);
                    }

                } catch (IOException exception) {
                    LogTool.LogE_DEBUG(TAG, exception.toString());

                    if (!(exception instanceof SocketTimeoutException)) {
                        if (mSocketListener != null) {
                            mSocketListener.onSocketDisConnected();
                        }
                        break;
                    }
                }
            }

            try {
                dataOutputStream.close();
                dataInputStream.close();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                LogTool.LogE_DEBUG(TAG, e.toString());
            }
        }
    }

}
