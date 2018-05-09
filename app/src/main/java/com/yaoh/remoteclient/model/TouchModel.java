package com.yaoh.remoteclient.model;

/**
 * Created by yaoh on 2018/5/8.
 *
 */

public class TouchModel {

    private int touchX;
    private int touchY;

    public TouchModel() {
    }

    public TouchModel(int touchX, int touchY) {
        this.touchX = touchX;
        this.touchY = touchY;
    }

    public int getTouchX() {
        return touchX;
    }

    public void setTouchX(int touchX) {
        this.touchX = touchX;
    }

    public int getTouchY() {
        return touchY;
    }

    public void setTouchY(int touchY) {
        this.touchY = touchY;
    }
}
