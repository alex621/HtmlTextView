package com.lolapplication;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;

public class HtmlTextViewDefaultAdapter extends HtmlTextView.HtmlTextViewAdapter {
    private static final String TAG = "HtmlTextViewDefaultAdapter";

    @Override
    public HtmlTextView.ImgViewHolder renderImg(Context context, String src, int width, int height, HtmlTextView.ImgViewHolder oldViewHolder) {
        if (oldViewHolder == null){
            SimpleDraweeView view = new SimpleDraweeView(context);
            view.setImageURI(Uri.parse(src));

            oldViewHolder = new HtmlTextView.ImgViewHolder(view);
        }

        return oldViewHolder;
    }

    @Override
    public void recycleImg(HtmlTextView.ImgViewHolder viewHolder, String src) {
        super.recycleImg(viewHolder, src);
    }
}
