package com.westkit.htmltextview.defaultadapater;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.facebook.drawee.drawable.DrawableUtils;
import com.facebook.drawee.drawable.ProgressBarDrawable;

//Adapt from Fresco's ProgressBarDrawable
public class HtmlTextViewProgressBarDrawable extends Drawable {
    private static final String TAG = "HtmlTextViewProgressBarDrawable";
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mBackgroundColor = 0x80CCCCCC;
    private int mColor = 0x800099FF;
    private int mBarWidth = 8;
    private int mLevel = 0;
    private boolean mHideWhenZero = false;

    /** Sets the progress bar color. */
    public void setColor(int color) {
        if (mColor != color) {
            mColor = color;
            invalidateSelf();
        }
    }

    /** Gets the progress bar color. */
    public int getColor() {
        return mColor;
    }

    /** Sets the progress bar background color. */
    public void setBackgroundColor(int backgroundColor) {
        if (mBackgroundColor != backgroundColor) {
            mBackgroundColor = backgroundColor;
            invalidateSelf();
        }
    }

    /** Gets the progress bar background color. */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /** Sets the progress bar width. */
    public void setBarWidth(int barWidth) {
        if (mBarWidth != barWidth) {
            mBarWidth = barWidth;
            invalidateSelf();
        }
    }

    /** Gets the progress bar width. */
    public int getBarWidth() {
        return mBarWidth;
    }

    /** Sets whether the progress bar should be hidden when the progress is 0. */
    public void setHideWhenZero(boolean hideWhenZero) {
        mHideWhenZero = hideWhenZero;
    }

    /** Gets whether the progress bar should be hidden when the progress is 0. */
    public boolean getHideWhenZero() {
        return mHideWhenZero;
    }

    @Override
    protected boolean onLevelChange(int level) {
        mLevel = level;
        invalidateSelf();
        return true;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return DrawableUtils.getOpacityFromColor(mPaint.getColor());
    }

    @Override
    public void draw(Canvas canvas) {
        if (mHideWhenZero && mLevel == 0) {
            return;
        }
        drawBar(canvas, 10000, mBackgroundColor);
        drawBar(canvas, mLevel, mColor);
    }

    private void drawBar(Canvas canvas, int level, int color) {
        Rect bounds = getBounds();
        int length = bounds.width() * level / 10000;
        int xpos = bounds.left;
        int ypos = bounds.top;
        mPaint.setColor(color);
        canvas.drawRect(xpos, ypos, xpos + length, ypos + mBarWidth, mPaint);
    }
}
