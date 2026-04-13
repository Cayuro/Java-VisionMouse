package com.vision.detection;

public class HandLandmarks {
    private int indexX = 0;
    private int indexY = 0;
    private int thumbX = 0;
    private int thumbY = 0;

    public void setIndexFingerTip(int x, int y) {
        this.indexX = x;
        this.indexY = y;
    }

    public void setThumbTip(int x, int y) {
        this.thumbX = x;
        this.thumbY = y;
    }

    public int getIndexX() {
        return indexX;
    }

    public int getIndexY() {
        return indexY;
    }

    public boolean isValid() {
        return indexX != 0 || indexY != 0;
    }
}
