package com.westkit.htmltextview.defaultadapater;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.westkit.htmltextview.HtmlTextView;
import com.westkit.htmltextview.HtmlTextViewAdapter;
import com.westkit.htmltextview.viewholder.ImgViewHolder;

public class HtmlTextViewDefaultAdapter extends HtmlTextViewAdapter {
    private static final String TAG = "HtmlTextViewDefaultAdapter";

    @Override
    public ImgViewHolder renderImg(Context context, String src, int width, int height, ImgViewHolder oldViewHolder) {
        if (oldViewHolder == null){
            SimpleDraweeView view = new SimpleDraweeView(context);

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(src))
                    .setAutoPlayAnimations(true)
            .build();
            view.setController(controller);

            oldViewHolder = new ImgViewHolder(view);
        }

        return oldViewHolder;
    }

    @Override
    public void recycleImg(ImgViewHolder viewHolder, String src) {
        super.recycleImg(viewHolder, src);
    }
}
