package com.lolapplication;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.util.HashMap;

public class HtmlTextView extends FrameLayout implements HtmlToSpannedConverter.ConverterProxy {
    private static final String TAG = "HtmlTextView";
    private String html;
    private TextView textView;
    private FrameLayout overlay;

    private Html.TagHandler tagHandler;
    private HtmlToSpannedConverter converter;

    private HtmlTextViewRenderer renderer;

    private int measuredWidth = -1;

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
    }

    public void setHtml(String html){
        if (TextUtils.equals(html, this.html)){
            return;
        }

        this.html = html;

        Spanned text = fromHtml(html, tagHandler);
        textView.setText(text);
    }

    public HtmlTextViewRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(HtmlTextViewRenderer renderer) {
        this.renderer = renderer;
    }

    public Html.TagHandler getTagHandler() {
        return tagHandler;
    }

    public void setTagHandler(Html.TagHandler tagHandler) {
        this.tagHandler = tagHandler;
    }

    private Spanned fromHtml(String source, Html.TagHandler tagHandler){
        XMLReader parser = null;
        try {
            parser = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        }

        converter = new HtmlToSpannedConverter(source, this, tagHandler, parser);
        return converter.convert();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.d(TAG, "onMeasure: " + measuredWidth);

        int newMeasuredWidth = getMeasuredWidth();
        if (measuredWidth != newMeasuredWidth) {
            measuredWidth = newMeasuredWidth;

            //to trigger re-rendering
            textView.setText(converter.convert());
        }
    }

    private SparseArray<View> viewMap = new SparseArray<>();

    @Override
    public int getViewWidth() {
        return measuredWidth;
    }

    @Override
    public void onCreateImageSpace(int index, String src, int left, int top, int width, int height) {
        View v = viewMap.get(index);
        if (v == null){
            v = renderer.renderImage(getContext(), src);
            viewMap.put(index, v);
            overlay.addView(v);
        }

        LayoutParams lp = new LayoutParams(width, height);
        lp.setMargins(left, top, 0, 0);
        v.setLayoutParams(lp);
    }

    public interface HtmlTextViewRenderer{
        View renderImage(Context context, String src);
    }


}
