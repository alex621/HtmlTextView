package com.westkit.htmltextview;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import com.westkit.htmltextview.data.DataSupplier;
import com.westkit.htmltextview.data.ImgData;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;

public class HtmlToSpannedConverter implements ContentHandler {
    private static final String TAG = "HtmlToSpannedConverter";

    private static final float[] HEADER_SIZES = {
            1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f,
    };

    private String source;
    private XMLReader reader;
    private SpannableStringBuilder spannableStringBuilder;
    private ConverterProxy proxy;
    private Html.TagHandler tagHandler;
    private DataSupplier dataSupplier;

    private int currentImageIndex = 0;

    public HtmlToSpannedConverter(
            String source, ConverterProxy proxy, DataSupplier dataSupplier, Html.TagHandler tagHandler,
            XMLReader parser) {
        this.spannableStringBuilder = new SpannableStringBuilder();
        this.source = source;
        this.proxy = proxy;
        this.dataSupplier = dataSupplier;
        this.tagHandler = tagHandler;
        this.reader = parser;
    }

    public void cleanup(){
        this.proxy = null;
        this.dataSupplier = null;
        this.tagHandler = null;
        this.reader = null;
    }

    public Spanned convert() {
        spannableStringBuilder.clearSpans();
        spannableStringBuilder.clear();

        currentImageIndex = 0;

        reader.setContentHandler(this);
        try {
            reader.parse(new InputSource(new StringReader(source)));
        } catch (IOException e) {
            // We are reading from a string. There should not be IO problems.
            throw new RuntimeException(e);
        } catch (SAXException e) {
            // TagSoup doesn't throw parse exceptions.
            throw new RuntimeException(e);
        }

        // Fix flags and range for paragraph-type markup.
        Object[] obj = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), ParagraphStyle.class);
        for (int i = 0; i < obj.length; i++) {
            int start = spannableStringBuilder.getSpanStart(obj[i]);
            int end = spannableStringBuilder.getSpanEnd(obj[i]);

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0) {
                if (spannableStringBuilder.charAt(end - 1) == '\n' &&
                        spannableStringBuilder.charAt(end - 2) == '\n') {
                    end--;
                }
            }

            if (end == start) {
                spannableStringBuilder.removeSpan(obj[i]);
            } else {
                spannableStringBuilder.setSpan(obj[i], start, end, Spannable.SPAN_PARAGRAPH);
            }
        }

        return spannableStringBuilder;
    }

    private void handleStartTag(String tag, Attributes attributes) {
        if (tag.equalsIgnoreCase("br")) {
            // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
            // so we can safely emite the linebreaks when we handle the close tag.
        } else if (tag.equalsIgnoreCase("p")) {
            handleP(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            handleP(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            start(spannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("b")) {
            start(spannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("em")) {
            start(spannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("cite")) {
            start(spannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("dfn")) {
            start(spannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("i")) {
            start(spannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("big")) {
            start(spannableStringBuilder, new Big());
        } else if (tag.equalsIgnoreCase("small")) {
            start(spannableStringBuilder, new Small());
        } else if (tag.equalsIgnoreCase("font")) {
            startFont(spannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            handleP(spannableStringBuilder);
            start(spannableStringBuilder, new Blockquote());
        } else if (tag.equalsIgnoreCase("tt")) {
            start(spannableStringBuilder, new Monospace());
        } else if (tag.equalsIgnoreCase("a")) {
            startA(spannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("u")) {
            start(spannableStringBuilder, new Underline());
        } else if (tag.equalsIgnoreCase("sup")) {
            start(spannableStringBuilder, new Super());
        } else if (tag.equalsIgnoreCase("sub")) {
            start(spannableStringBuilder, new Sub());
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            handleP(spannableStringBuilder);
            start(spannableStringBuilder, new Header(tag.charAt(1) - '1'));
        } else if (tag.equalsIgnoreCase("img")) {
            startImg(spannableStringBuilder, attributes);
        } else if (tagHandler != null) {
            tagHandler.handleTag(true, tag, spannableStringBuilder, reader);
        }
    }

    private void handleEndTag(String tag) {
        if (tag.equalsIgnoreCase("br")) {
            handleBr(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("p")) {
            handleP(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            handleP(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            end(spannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("b")) {
            end(spannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("em")) {
            end(spannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("cite")) {
            end(spannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("dfn")) {
            end(spannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("i")) {
            end(spannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("big")) {
            end(spannableStringBuilder, Big.class, new RelativeSizeSpan(1.25f));
        } else if (tag.equalsIgnoreCase("small")) {
            end(spannableStringBuilder, Small.class, new RelativeSizeSpan(0.8f));
        } else if (tag.equalsIgnoreCase("font")) {
            endFont(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            handleP(spannableStringBuilder);
            end(spannableStringBuilder, Blockquote.class, new QuoteSpan());
        } else if (tag.equalsIgnoreCase("tt")) {
            end(spannableStringBuilder, Monospace.class,
                    new TypefaceSpan("monospace"));
        } else if (tag.equalsIgnoreCase("a")) {
            endA(spannableStringBuilder);
        } else if (tag.equalsIgnoreCase("u")) {
            end(spannableStringBuilder, Underline.class, new UnderlineSpan());
        } else if (tag.equalsIgnoreCase("sup")) {
            end(spannableStringBuilder, Super.class, new SuperscriptSpan());
        } else if (tag.equalsIgnoreCase("sub")) {
            end(spannableStringBuilder, Sub.class, new SubscriptSpan());
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            handleP(spannableStringBuilder);
            endHeader(spannableStringBuilder);
        } else if (tagHandler != null) {
            tagHandler.handleTag(false, tag, spannableStringBuilder, reader);
        }
    }

    private static void handleP(SpannableStringBuilder text) {
        int len = text.length();

        if (len >= 1 && text.charAt(len - 1) == '\n') {
            if (len >= 2 && text.charAt(len - 2) == '\n') {
                return;
            }

            text.append("\n");
            return;
        }

        if (len != 0) {
            text.append("\n\n");
        }
    }

    private static void handleBr(SpannableStringBuilder text) {
        text.append("\n");
    }

    private static Object getLast(Spanned text, Class kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void start(SpannableStringBuilder text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void end(SpannableStringBuilder text, Class kind, Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void startImg(SpannableStringBuilder text, Attributes attributes) {
        final String src = attributes.getValue("", "src");
        int width = 0, height = 0;

        //prefer data from dataSupplier over inline attribute
        ImgData data = dataSupplier.getImgData(src, attributes);
        if (data != null){
            width = data.getWidth();
            height = data.getHeight();
        } else {
            try {
                width = Integer.parseInt(attributes.getValue("", "width"));
                height = Integer.parseInt(attributes.getValue("", "height"));
            } catch (Exception e) {

            }
        }

        int viewWidth = proxy.getViewWidth();

        //max-width: 100% for the img
        if (viewWidth > 0){
            if (width > viewWidth){
                height = viewWidth * height / width;
                width = viewWidth;
            }
        }

        Drawable d = new ColorDrawable(Color.TRANSPARENT);
        // Using a gradient drawable is more convenient for debugging
        // Drawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0xFFFF0000, 0xFF000000});
        d.setBounds(0, 0, width, height);

        int len = text.length();
        text.append("\uFFFC");

        final int finalWidth = width;

        final int index = currentImageIndex;

        ImageSpan span = new ImageSpan(d, src){
            @Override
            public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
                super.draw(canvas, text, start, end, x, top, y, bottom, paint);

                proxy.onCreateImageSpace(index, src, (int) x, bottom - y + top, finalWidth, y - top);
            }
        };

        text.setSpan(span, len, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        currentImageIndex++;
    }

    private static void startFont(SpannableStringBuilder text,
                                  Attributes attributes) {
        String color = attributes.getValue("", "color");
        String face = attributes.getValue("", "face");

        int len = text.length();
        text.setSpan(new Font(color, face), len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void endFont(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Font.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            Font f = (Font) obj;

            if (!TextUtils.isEmpty(f.mColor)) {
                if (f.mColor.startsWith("@")) {
                    Resources res = Resources.getSystem();
                    String name = f.mColor.substring(1);
                    int colorRes = res.getIdentifier(name, "color", "android");
                    if (colorRes != 0) {
                        ColorStateList colors = res.getColorStateList(colorRes);
                        text.setSpan(new TextAppearanceSpan(null, 0, 0, colors, null),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    int c = -1;
                    try {
                        c = Color.parseColor(f.mColor);
                    }catch (Exception e){
                    }
                    if (c != -1) {
                        text.setSpan(new ForegroundColorSpan(c | 0xFF000000),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            if (f.mFace != null) {
                text.setSpan(new TypefaceSpan(f.mFace), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void startA(SpannableStringBuilder text, Attributes attributes) {
        String href = attributes.getValue("", "href");

        int len = text.length();
        text.setSpan(new Href(href), len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void endA(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Href.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            Href h = (Href) obj;

            if (h.mHref != null) {
                text.setSpan(new URLSpan(h.mHref), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void endHeader(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Header.class);

        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        // Back off not to change only the text, not the blank line.
        while (len > where && text.charAt(len - 1) == '\n') {
            len--;
        }

        if (where != len) {
            Header h = (Header) obj;

            text.setSpan(new RelativeSizeSpan(HEADER_SIZES[h.mLevel]),
                    where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new StyleSpan(Typeface.BOLD),
                    where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        handleStartTag(localName, attributes);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        handleEndTag(localName);
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        StringBuilder sb = new StringBuilder();

        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         */

        for (int i = 0; i < length; i++) {
            char c = ch[i + start];

            if (c == ' ' || c == '\n') {
                char pred;
                int len = sb.length();

                if (len == 0) {
                    len = spannableStringBuilder.length();

                    if (len == 0) {
                        pred = '\n';
                    } else {
                        pred = spannableStringBuilder.charAt(len - 1);
                    }
                } else {
                    pred = sb.charAt(len - 1);
                }

                if (pred != ' ' && pred != '\n') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }

        spannableStringBuilder.append(sb);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    private static class Bold { }
    private static class Italic { }
    private static class Underline { }
    private static class Big { }
    private static class Small { }
    private static class Monospace { }
    private static class Blockquote { }
    private static class Super { }
    private static class Sub { }

    private static class Font {
        public String mColor;
        public String mFace;

        public Font(String color, String face) {
            mColor = color;
            mFace = face;
        }
    }

    private static class Href {
        public String mHref;

        public Href(String href) {
            mHref = href;
        }
    }

    private static class Header {
        private int mLevel;

        public Header(int level) {
            mLevel = level;
        }
    }

    interface ConverterProxy{
        int getViewWidth();
        void onCreateImageSpace(int index, String src, int left, int top, int width, int height);
    }
}