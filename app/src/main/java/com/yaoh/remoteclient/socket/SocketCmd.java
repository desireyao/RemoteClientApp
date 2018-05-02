package com.yaoh.remoteclient.socket;

/**
 * Created by yaoh on 2018/4/30.
 */

public class SocketCmd {
    public static final byte PACKET_HEADER = '*';
    public static final byte PACKET_TAIL = '\n';

    public static final String CMD_LOGIN = "*10";

    public static final String CMD_HEART = "*110\n";
    public static final String CMD_DEVICE_INFO = "*120\n";

    public SocketCmd(CMD_TYPE mCmdType, byte[] data) {
        this.data = data;
        this.mCmdType = mCmdType;
    }

    private byte[] data;
    private CMD_TYPE mCmdType = CMD_TYPE.DEFAULT;

    public enum CMD_TYPE {
        DEFAULT,
        CMD_DEVICE_INFO,
        CMD_DEVICE_CONNECT,
        CMD_DEVICE_DISCONNECT,
        CMD_MOUSE_MOVE,
    }


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public CMD_TYPE getmCmdType() {
        return mCmdType;
    }

    public void setmCmdType(CMD_TYPE mCmdType) {
        this.mCmdType = mCmdType;
    }
}
