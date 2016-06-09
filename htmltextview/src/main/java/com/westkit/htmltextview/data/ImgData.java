package com.westkit.htmltextview.data;

public class ImgData{
    private int width = 0, height = 0;

    public ImgData(){}

    public ImgData(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth(){
        return width;
    }
    public int getHeight(){
        return height;
    }
}