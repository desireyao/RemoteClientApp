package com.yaoh.remoteclient.socket;

import com.yaoh.remoteclient.BuildConfig;
import com.yaoh.remoteclient.tools.LogTool;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by yaoh on 2018/4/30.
 */

public abstract class SocketClientManager {
    public String TAG = "";

    private static final String serverIP = BuildConfig.SOCKET_IP;
    private static final int serverPort = BuildConfig.SOCKET_PORT;

    public SocketClientManager() {
        TAG = getClass().getSimpleName();
    }

    public enum Status {
        STATUS_DISCONNECTED,
        STATUS_CONNECTED,
        STATUS_SCREEN_SHOTTING
    }

    private Status mStatus = Status.STATUS_DISCONNECTED;

    private Socket mSocket;
    private SocketListener mSocketListener;
    private DataOutputStream dataOutputStream;
    private boolean isRun = true;

    private boolean isSendHeartData;

    private ScheduledExecutorService mExecutor;

    public void setSocketListener(SocketListener mSocketListener) {
        this.mSocketListener = mSocketListener;
    }


    public void setSendHeartData(boolean sendHeartData) {
        isSendHeartData = sendHeartData;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status mStatus) {
        this.mStatus = mStatus;
    }

    /**
     * 登录
     *
     * @param clientId *1013\n0#100#100#123
     */
    public String createLoginData(int channel, int clientId, String password) {
        String loginData = String.format("%s#%s#%s#%s", channel, clientId, clientId, password);
        StringBuffer buffer = new StringBuffer();
        buffer.append(SocketCmd.CMD_LOGIN)
                .append(loginData.length())
                .append("\n")
                .append(loginData);
        return buffer.toString();
    }


    public abstract void sendloginData();

//    public abstract void connected();
//
//    public abstract void disConnected();

    /**
     * 发送数据
     *
     * @param data
     */
    public void sendData(String data) {
        LogTool.LogE_DEBUG(TAG, "sendData--->" + data);
        byte[] bytes = data.getBytes();
        sendData(bytes);
    }

    /**
     * 发送数据
     *
     * @param data byte[]
     */
    public boolean sendData(byte[] data) {
//        LogTool.LogE_DEBUG(TAG, "sendData length = " + data.length);
//        LogTool.LogE_DEBUG(TAG, LogTool.LogBytes2Hex(data, "sendData"));
        try {
            if (dataOutputStream != null) {
                dataOutputStream.write(data);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    /**
     * 开始连接 socket
     */
    public void startConnect() {
        new Thread(new SocketTask()).start();
    }


    /**
     * 关闭 socket
     */
    public void close() {
        if (mSocket != null) {
            try {
                mStatus = Status.STATUS_DISCONNECTED;
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class SocketTask implements Runnable {

        @Override
        public void run() {
            DataInputStream inputStream = null;
            BufferedReader buf;

            try {
                mSocket = new Socket(serverIP, serverPort);
                mSocket.setSoTimeout(1000 * 10);

                LogTool.LogE_DEBUG(TAG, "socket 连接成功------>");

                // 输出流
                dataOutputStream = new DataOutputStream(mSocket.getOutputStream());
                // 输入流
                inputStream = new DataInputStream(mSocket.getInputStream());

                if (mSocketListener != null) {
                    mStatus = Status.STATUS_CONNECTED;
                    mSocketListener.onSocketConnected();
                }
            } catch (IOException e) {
                LogTool.LogE_DEBUG(TAG, e.toString());
                if (mSocketListener != null) {
                    mStatus = Status.STATUS_DISCONNECTED;
                    mSocketListener.onSocketDisconnected();
                }
                return;
            }

            while (isRun) {
                try {
//                    LogTool.LogD(TAG, "run ---------------> START");
                    byte[] buffer = new byte[1024];
                    int rcvLen = inputStream.read(buffer);
                    if (rcvLen == -1) {
                        LogTool.LogD(TAG, " rcvLen ---> " + rcvLen);
                        mSocket.close();
                        break;
                    }

                    byte[] recvData = new byte[rcvLen];
                    System.arraycopy(buffer, 0, recvData, 0, rcvLen);
//                  LogTool.LogE_DEBUG(TAG, LogTool.LogBytes(recvData, "recvData")
//                            + "\n" + LogTool.LogBytes2Hex(recvData, "recvDataHex"));
                    if (mSocketListener != null) {
                        mSocketListener.onSocketResponse(recvData);
                    }
                } catch (IOException exception) {
                    LogTool.LogD(TAG, "IOException 222--->" + exception.toString());

                    if (!(exception instanceof SocketTimeoutException)) {
                        if (mSocketListener != null) {
                            mStatus = Status.STATUS_DISCONNECTED;
                            mSocketListener.onSocketDisconnected();
                        }
                        break;
                    } else {
                        if (isSendHeartData) {
                            // 发送心跳包
                            sendData(SocketCmd.CMD_HEART);
                        }
                    }
                }
            }

            try {
                mStatus = Status.STATUS_DISCONNECTED;
                dataOutputStream.close();
                inputStream.close();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                mStatus = Status.STATUS_DISCONNECTED;
                LogTool.LogE_DEBUG(TAG, e.toString());
            }
        }
    }

}
