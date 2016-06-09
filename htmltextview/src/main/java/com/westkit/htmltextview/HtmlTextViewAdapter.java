package com.westkit.htmltextview;

import android.content.Context;

import com.westkit.htmltextview.viewholder.ImgViewHolder;

public abstract class HtmlTextViewAdapter {
    public ImgViewHolder renderImg(Context context, String src, int width, int height, ImgViewHolder oldViewHolder) {
        return null;
    }

    public void recycleImg(ImgViewHolder viewHolder, String src){

    }
}