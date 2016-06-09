package com.westkit.htmltextview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.westkit.htmltextview.container.Container;
import com.westkit.htmltextview.container.ImgContainer;
import com.westkit.htmltextview.data.DataSupplier;
import com.westkit.htmltextview.data.ImgData;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class HtmlTextView extends FrameLayout implements HtmlToSpannedConverter.ConverterProxy {
    private static final String TAG = "HtmlTextView";

    public static final int VIEWHOLDER_TYPE_IMG = 1;

    private String html;
    private TextView textView;
    private FrameLayout overlay;

    private Html.TagHandler tagHandler;
    private HtmlToSpannedConverter converter;

    private DataSupplier dataSupplier = new DefaultDataSupplier();

    private HtmlTextViewAdapter adapter;

    private SparseArray<ImgContainer> imgContainerMap = new SparseArray<>();

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

        recycleCheck();
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

    public void setHtml(String html){
        if (TextUtils.equals(html, this.html)){
            return;
        }

        this.html = html;

        cleanup();

        Spanned text = fromHtml(html);
        textView.setText(text);
    }

    private Spanned fromHtml(String source){
        XMLReader parser = null;
        try {
            parser = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
        } catch (SAXException e) {
            return null;
        }

        converter = new HtmlToSpannedConverter(source, this, dataSupplier, tagHandler, parser);
        return converter.convert();
    }

    private void cleanup(){
        for (int i = 0, l = imgContainerMap.size(); i < l; i++){
            ImgContainer container = imgContainerMap.valueAt(i);
            container.detachChild();
        }

        overlay.removeAllViews();
        imgContainerMap.clear();

        if (converter != null) {
            converter.cleanup();
        }
    }

    @Override
    public int getViewWidth() {
        return measuredWidth;
    }

    //callback from converter
    //will be called when the ImageSpan draws
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

    // should be called on scroll event in any scrollable view
    public void recycleCheck(){
        getScreenRect();

        //ok...seriously, I have no idea why I need to wrap it in post()
        //but if I don't, it won't be able to setHtml() again (the second time)
        //TODO investigate what the fuck is going on
        post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0, l = overlay.getChildCount(); i < l; i++) {
                    //TODO check whether it is visible inside the view bound instead of inside the screen bound
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
        });
    }

    private class DefaultDataSupplier implements DataSupplier{
        @Override
        public ImgData getImgData(String src) {
            return null;
        }
    }

}
