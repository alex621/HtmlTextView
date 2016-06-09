package com.westkit.htmltextview.container;

import android.content.Context;
import android.widget.FrameLayout;

import com.westkit.htmltextview.HtmlTextViewAdapter;
import com.westkit.htmltextview.viewholder.ImgViewHolder;

public class ImgContainer extends Container<ImgViewHolder>{
    private String src;
    private HtmlTextViewAdapter adapter;

    public ImgContainer(Context context, HtmlTextViewAdapter adapter, int index, String src) {
        super(context, index);

        this.adapter = adapter;
        this.src = src;
    }

    @Override
    public void attachChild() {
        viewHolder = adapter.renderImg(containerView.getContext(), src, width, height, viewHolder);
        containerView.addView(viewHolder.itemView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void detachChild() {
        containerView.removeAllViews();
        adapter.recycleImg(viewHolder, src);
    }
}