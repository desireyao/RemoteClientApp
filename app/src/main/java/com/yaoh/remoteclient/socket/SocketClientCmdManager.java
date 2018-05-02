package com.yaoh.remoteclient.socket;

import com.yaoh.remoteclient.tools.LogTool;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaoh on 2018/4/30.
 */

public class SocketClientCmdManager extends SocketClientManager {

    private List<Byte> mRecvCmdList = new ArrayList<>();

    @Override
    public void sendloginData() {
        sendData(createLoginData(0, 100, "123"));
    }

    public synchronized void onRecvCmd(byte[] recvCmd) {
        for (Byte data : recvCmd) {
            // 添加指令到队尾
            mRecvCmdList.add(data);
        }

        // *120\n
        if (mRecvCmdList.size() < 5) {
            return;
        }

        boolean isParseEnd = false;
        while (!isParseEnd) {
            LogTool.LogE_DEBUG(TAG, LogTool.LogBytes2Hex(mRecvCmdList, "mRecvCmdList---111"));

            // 进入包头
            if (mRecvCmdList.get(0) == SocketCmd.PACKET_HEADER) {
                byte cmd0 = mRecvCmdList.get(1);
                byte cmd1 = mRecvCmdList.get(2);
                byte dataLen = (byte) (mRecvCmdList.get(3) - '0');
                byte cmdTail = mRecvCmdList.get(4);

                if (cmdTail != SocketCmd.PACKET_TAIL) {
                    mRecvCmdList.clear();
                    break;
                }

                if (mRecvCmdList.size() == 5) {
                    if (dataLen == 0) {
                        onActionWithCmd(cmd0, cmd1, dataLen, null);
                        mRecvCmdList.clear();
                    }
                    isParseEnd = true;
                } else {
                    List<Byte> dataList = mRecvCmdList.subList(5, dataLen + 5);
                    byte[] data = new byte[dataLen];
                    for (int i = 0; i < dataList.size(); i++) {
                        data[i] = dataList.get(i);
                    }
                    onActionWithCmd(cmd0, cmd1, dataLen, data);
                    mRecvCmdList = mRecvCmdList.subList(dataLen + 5, mRecvCmdList.size());

                    if (mRecvCmdList.isEmpty()) {
                        isParseEnd = true;
                    }
                }
            } else {
                LogTool.LogE_DEBUG(TAG, "解析数据错误--->");
                mRecvCmdList.clear();
            }
        }
    }

    private void onActionWithCmd(byte cmd0, byte cmd1, byte cmdLen, byte[] data) {
        LogTool.LogE_DEBUG(TAG, "onActionWithCmd ="
                + " cmd0 = " + cmd0
                + " cmd1 = " + cmd1
                + LogTool.LogBytes2Hex(data, " data"));

        if (cmd0 == '1' && cmd1 == '2') {
            // 收到设备指令
            EventBus.getDefault().post(new SocketCmd(SocketCmd.CMD_TYPE.CMD_DEVICE_INFO, data));

        } else if (cmd0 == '1' && cmd1 == '3') {
            if (data[0] == '1') {
                // 发送桌面连接指令 或者 桌面断开指令
                EventBus.getDefault().post(new SocketCmd(SocketCmd.CMD_TYPE.CMD_DEVICE_CONNECT, data));
            } else {
                EventBus.getDefault().post(new SocketCmd(SocketCmd.CMD_TYPE.CMD_DEVICE_DISCONNECT, data));
            }
        } else if (cmd0 == '1' && cmd1 == '7') {
            EventBus.getDefault().post(new SocketCmd(SocketCmd.CMD_TYPE.CMD_MOUSE_MOVE, data));
        }
    }
}
