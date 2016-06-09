package com.westkit.htmltextview.container;

import android.content.Context;
import android.widget.LinearLayout;

import com.westkit.htmltextview.viewholder.ViewHolder;

/*
    Think of Container a manager of the space for displaying the resources like <img>.
    It helps to attach/detach the view when it is visible/invisible.
    If you want to add support for other element like <video>, you should extend a Container.
 */
public abstract class Container<VH extends ViewHolder>{
    public VH viewHolder;
    public LinearLayout containerView;
    public int index;
    public boolean visible;
    public int width, height;

    public Container(Context context, int index) {
        this.containerView = new LinearLayout(context);
        this.index = index;
    }

    public LinearLayout getContainerView(){
        return containerView;
    }

    public abstract void attachChild();
    public abstract void detachChild();
}