package com.yaoh.remoteclient.socket;

/**
 * Created by yaoh on 2018/4/24.
 */

public interface SocketListener {

    public void onSocketConnected();

    public void onSocketDisconnected();

    public void onSocketResponse(byte[] recvData);

}
