package com.yaoh.picdiff.socket;

/**
 * Created by yaoh on 2018/4/24.
 */

public interface SocketListener {

    public void onSocketConnected();

    public void onSocketDisConnected();

    public void onSocketResponse(String msg);


}
