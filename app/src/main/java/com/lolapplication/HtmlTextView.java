package com.lolapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class HtmlTextView extends FrameLayout implements HtmlToSpannedConverter.ConverterProxy {
    private static final String TAG = "HtmlTextView";
    private String html;
    private TextView textView;
    private FrameLayout overlay;

    private Html.TagHandler tagHandler;
    private HtmlToSpannedConverter converter;

    private HtmlTextViewAdapter renderer;

    private SparseArray<ViewHolder> viewMap = new SparseArray<>();

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

    public HtmlTextViewAdapter getRenderer() {
        return renderer;
    }

    public void setRenderer(HtmlTextViewAdapter renderer) {
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw");
    }

    @Override
    public int getViewWidth() {
        return measuredWidth;
    }

    @Override
    public void onCreateImageSpace(int index, String src, int left, int top, int width, int height) {
        ViewHolder v = viewMap.get(index);
        if (v == null){
            v = renderer.renderImg(getContext(), src, v);
            viewMap.put(index, v);
            overlay.addView(v.itemView);
        }

        LayoutParams lp = new LayoutParams(width, height);
        lp.setMargins(left, top, 0, 0);
        v.itemView.setLayoutParams(lp);
    }

    private int[] coordinate = new int[2];
    private static Rect screenRect = null;
    private Rect viewRect = new Rect();
    private void getScreenRect(){
        if (screenRect != null){
            return;
        }

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        screenRect = new Rect(0, 0, width, height);
    }

    public void recycleCheck(){
        getScreenRect();

        for (int i = 0, l = overlay.getChildCount(); i < l; i++) {
            View v = overlay.getChildAt(i);
            v.getLocationOnScreen(coordinate);

            viewRect.set(coordinate[0], coordinate[1], coordinate[0] + v.getMeasuredWidth(), coordinate[1] + v.getMeasuredHeight());

            Log.d(TAG, "Visible ("+i+"): " + viewRect.intersect(screenRect));
//            Log.d(TAG, "Item " + i + ": " + coordinate[0] + ", " + coordinate[1]);
//            if (i == 0) return;
        }
    }

    public static abstract class HtmlTextViewAdapter {
        public ViewHolder renderImg(Context context, String src, ViewHolder oldViewHolder) {
            return null;
        }
    }

    public static abstract class ViewHolder{
        public final View itemView;

        public ViewHolder(View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }
    }

}
