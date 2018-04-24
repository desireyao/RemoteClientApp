package com.yaoh.picdiff.model;

import android.graphics.Bitmap;

/**
 * Created by yaoh on 2018/4/17.
 */

public class SliceModel {
    private int x;
    private int y;
    private Bitmap bitmap;

    public SliceModel(int x, int y, Bitmap bitmap) {
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "\n {" + " x = " + x
                      + " y = " + y + '}';
    }
}
