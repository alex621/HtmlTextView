package com.lolapplication;

import android.content.Context;
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

import java.util.HashMap;

public class HtmlTextView extends FrameLayout implements HtmlToSpannedConverter.ConverterProxy {
    private static final String TAG = "HtmlTextView";
    private String html;
    private TextView textView;
    private FrameLayout overlay;

    private Html.TagHandler tagHandler;
    private HtmlToSpannedConverter converter;

    private DataSupplier dataSupplier = new DefaultDataSupplier();

    private HtmlTextViewAdapter adapter = new HtmlTextViewDefaultAdapter();

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int newMeasuredWidth = getMeasuredWidth();
        if (measuredWidth != newMeasuredWidth) {
            measuredWidth = newMeasuredWidth;

            //to trigger re-rendering
            textView.setText(converter.convert());
        }
    }

    public void setHtml(String html){
        if (TextUtils.equals(html, this.html)){
            return;
        }

        this.html = html;

        Spanned text = fromHtml(html, tagHandler);
        textView.setText(text);
    }

    public DataSupplier getDataSupplier() {
        return dataSupplier;
    }

    public void setDataSupplier(DataSupplier dataSupplier) {
        this.dataSupplier = dataSupplier;

        if (dataSupplier == null){
            this.dataSupplier = new DefaultDataSupplier();
        }
    }

    public HtmlTextViewAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(HtmlTextViewAdapter adapter) {
        this.adapter = adapter;
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

        converter = new HtmlToSpannedConverter(source, this, dataSupplier, tagHandler, parser);
        return converter.convert();
    }

    @Override
    public int getViewWidth() {
        return measuredWidth;
    }

    public static final int VIEWHOLDER_TYPE_IMG = 1;

    private SparseArray<ImgContainer> imgContainerMap = new SparseArray<>();
    @Override
    public void onCreateImageSpace(int index, String src, int left, int top, int width, int height) {
        ImgContainer container = imgContainerMap.get(index);
        if (container == null){
            container = new ImgContainer(getContext(), adapter, index, src);
            container.width = width;
            container.height = height;
            container.containerView.setTag(R.id.htmltextview_viewholder_index, index);
            container.containerView.setTag(R.id.htmltextview_viewholder_type, VIEWHOLDER_TYPE_IMG);
            overlay.addView(container.containerView);

            imgContainerMap.put(index, container);
        }


        LayoutParams lp = new LayoutParams(width, height);
        lp.setMargins(left, top, 0, 0);
        container.containerView.setLayoutParams(lp);

        recycleCheck();
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

            boolean isVisible = viewRect.intersect(screenRect);
            Integer index = (Integer) v.getTag(R.id.htmltextview_viewholder_index);
            Integer type = (Integer) v.getTag(R.id.htmltextview_viewholder_type);
            if (index == null || type == null){
                //WTF?
                continue;
            }

            Container container = null;
            switch (type){
                case VIEWHOLDER_TYPE_IMG:
                default:
                    container = imgContainerMap.get(index);
                    break;
            }
            if (isVisible){
                if (container.visible){
                    //fine
                }else{
                    //was invisible, make it visible
                    container.attachChild();
                    container.visible = true;
                }
            }else{
                if (container.visible){
                    //was visible, recycle it
                    container.detachChild();
                    container.visible = false;
                }else{
                    //fine
                }
            }
        }
    }


    public static abstract class Container<VH extends ViewHolder>{
        public VH viewHolder;
        public FrameLayout containerView;
        public int index;
        public boolean visible;
        public int width, height;

        public Container(Context context, int index) {
            containerView = new FrameLayout(context);
            this.index = index;
        }

        public FrameLayout getContainerView(){
            return containerView;
        }

        public abstract void attachChild();
        public abstract void detachChild();
    }
    public static class ImgContainer extends Container<ImgViewHolder>{
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
            containerView.addView(viewHolder.itemView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        @Override
        public void detachChild() {
            containerView.removeAllViews();
            adapter.recycleImg(viewHolder, src);
        }
    }

    public static abstract class HtmlTextViewAdapter {
        public ImgViewHolder renderImg(Context context, String src, int width, int height, ImgViewHolder oldViewHolder) {
            return null;
        }

        public void recycleImg(ImgViewHolder viewHolder, String src){

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

    public static class ImgViewHolder extends ViewHolder{

        public ImgViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class ImgData{
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
    interface DataSupplier{
        ImgData getImgData(String src);
    }

    public static class MapDataSupplier implements DataSupplier{
        private HashMap<String, ImgData> map;

        public MapDataSupplier(){
            this(null);
        }

        public MapDataSupplier(HashMap<String, ImgData> map) {
            this.map = map;

            if (this.map == null){
                this.map = new HashMap<>();
            }
        }

        public MapDataSupplier put(String src, ImgData data){
            map.put(src, data);
            return this;
        }

        @Override
        public ImgData getImgData(String src) {
            return map.get(src);
        }
    }

    private class DefaultDataSupplier implements DataSupplier{
        @Override
        public ImgData getImgData(String src) {
            return null;
        }
    }

}
