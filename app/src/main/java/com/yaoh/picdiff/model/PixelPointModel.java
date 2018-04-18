package com.yaoh.picdiff.model;

import com.yaoh.picdiff.utils.ConvertUtils;

import java.util.List;

/**
 * Created by yaoh on 2018/4/5.
 */

public class PixelPointModel {
    private int x;
    private int y;
    private int color;

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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "\n{" +
                "x=" + x +
                ", y=" + y +
                ", color=" + String.format("%02x", color) +
                '}';
    }

}
