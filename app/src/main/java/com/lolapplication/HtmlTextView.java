package com.lolapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class HtmlTextView extends FrameLayout {
    private static final String TAG = "HtmlTextView";
    private String html;
    private TextView textView;
    private FrameLayout overlay;

    private HtmlTextViewImageGetter imageGetter;
    private Html.TagHandler tagHandler;
    private HtmlToSpannedConverter converter;

    private HtmlTextViewRenderer renderer;

    public HtmlTextView(Context context) {
        super(context);
        init();
    }

    public HtmlTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HtmlTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected FrameLayout createOverlay(){
        return (FrameLayout) inflate(getContext(), R.layout.htmltextview_overlay, this).findViewById(R.id.htmltextview_overlay);
    }

    protected TextView createTextView(){
        return (TextView) inflate(getContext(), R.layout.htmltextview_textview, this).findViewById(R.id.htmltextview_textview);
    }

    private void init(){
        textView = createTextView();
        overlay = createOverlay();
        imageGetter = new HtmlTextViewImageGetter();
        tagHandler = new HtmlTextViewTagHandler();
    }

    public void setHtml(String html){
        this.html = html;

        Spanned text = fromHtml(html, imageGetter, tagHandler);
        textView.setText(text);
    }

    public HtmlTextViewRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(HtmlTextViewRenderer renderer) {
        this.renderer = renderer;
    }

    private Spanned fromHtml(String source, HtmlTextViewImageGetter imageGetter, Html.TagHandler tagHandler){
        XMLReader parser = null;
        try {
            parser = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        }

        converter = new HtmlToSpannedConverter(source, imageGetter, tagHandler, parser);
        return converter.convert();
    }

    private int measuredWidth = -1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.d(TAG, "onMeasure: " + measuredWidth);

        int newMeasuredWidth = getMeasuredWidth();
        if (measuredWidth != newMeasuredWidth) {
            measuredWidth = newMeasuredWidth;

            imageGetter.viewWidth = measuredWidth;

            textView.setText(converter.convert());
        }
    }

    public interface HtmlTextViewRenderer{
        View renderImage(Context context, String src);
    }

    public class HtmlTextViewImageGetter {
        public int viewWidth = -1;

        private HashMap<Integer, View> viewMap = new HashMap<>();

        public void onUpdatePosition(int index, String src, int left, int top, int width, int height) {
            View v;
            if (! viewMap.containsKey(index)){
                v = renderer.renderImage(getContext(), src);
                viewMap.put(index, v);
                overlay.addView(v);
            }else{
                v = viewMap.get(index);
            }

            LayoutParams lp = new LayoutParams(width, height);
            lp.setMargins(left, top, 0, 0);
            v.setLayoutParams(lp);
        }
    }

    private static class HtmlTextViewTagHandler implements Html.TagHandler{
        private static final String TAG = "TagHandler";

        private int depth = 0;
        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            Log.d(TAG, "handleTag: " + tag);
//            String ret = "";
//            for (int i = 0; i < depth; i++){
//                ret += "  ";
//            }
//
//            if (opening){
//                ret += "<" + tag + ">";
//                depth++;
//            }else{
//                ret += "</" + tag + ">";
//                depth--;
//            }
//
//            Log.d(TAG, ret);
        }
    }

}
