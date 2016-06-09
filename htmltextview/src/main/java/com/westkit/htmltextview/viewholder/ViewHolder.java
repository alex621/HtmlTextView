package com.westkit.htmltextview.viewholder;

import android.view.View;

public abstract class ViewHolder{
    public final View itemView;

    public ViewHolder(View itemView) {
        if (itemView == null) {
            throw new IllegalArgumentException("itemView may not be null");
        }
        this.itemView = itemView;
    }
}
