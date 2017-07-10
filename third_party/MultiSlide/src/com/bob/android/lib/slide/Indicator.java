
package com.bob.android.lib.slide;

import com.jingdong.app.lib.slide.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

public final class Indicator extends View {

    private static final int DEFAULT_BAR_COLOR = 0xaa777777;

    private static final int DEFAULT_FADE_DELAY = 2000;

    private static final int DEFAULT_FADE_DURATION = 500;

    private static final int DEFAULT_HIGHLIGHT_COLOR = 0xaa999999;

    public static final String TAG = "Widget.Indicator";

    private Paint barPaint, highlightPaint;

    private int fadeDelay, fadeDuration;

    private Animation fadeOutAnimation;

    private int numPages, currentPage, position;

    private float ovalRadius;

    public Indicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Indicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Indicator);
        int barColor = a.getColor(R.styleable.Indicator_barColor, DEFAULT_BAR_COLOR);
        int highlightColor = a.getColor(R.styleable.Indicator_highlightColor,
                DEFAULT_HIGHLIGHT_COLOR);
        fadeDelay = a.getInteger(R.styleable.Indicator_fadeDelay, DEFAULT_FADE_DELAY);
        fadeDuration = a.getInteger(R.styleable.Indicator_fadeDuration,
                DEFAULT_FADE_DURATION);
        ovalRadius = a.getDimension(R.styleable.Indicator_roundRectRadius, 0f);
        a.recycle();

        barPaint = new Paint();
        barPaint.setColor(barColor);

        highlightPaint = new Paint();
        highlightPaint.setColor(highlightColor);

        fadeOutAnimation = new AlphaAnimation(1f, 0f);
        fadeOutAnimation.setDuration(fadeDuration);
        fadeOutAnimation.setRepeatCount(0);
        fadeOutAnimation.setInterpolator(new LinearInterpolator());
        fadeOutAnimation.setFillEnabled(true);
        fadeOutAnimation.setFillAfter(true);
    }

    private void fadeOut() {
        if (fadeDuration > 0) {
            clearAnimation();
            fadeOutAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis() + fadeDelay);
            setAnimation(fadeOutAnimation);
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getNumPages() {
        return numPages;
    }

    public int getPageWidth() {
        return getWidth() / numPages;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), ovalRadius, ovalRadius,
                barPaint);
        canvas.drawRoundRect(
                new RectF(position, 0, position + (getWidth() / numPages), getHeight()),
                ovalRadius, ovalRadius, highlightPaint);
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage < 0 || currentPage >= numPages) {
           if(currentPage < 0 ){
        	   currentPage = 0;
           }else if(currentPage >= numPages){
        	   currentPage = numPages-1;
           }
        	// throw new IllegalArgumentException("currentPage parameter out of bounds");
        }
        if (this.currentPage != currentPage) {
            this.currentPage = currentPage;
            this.position = currentPage * getPageWidth();
            invalidate();
            fadeOut();
        }
    }

    public void setNumPages(int numPages) {
        if (numPages <= 0) {
            throw new IllegalArgumentException("numPages must be positive");
        }
        this.numPages = numPages;
        invalidate();
        fadeOut();
    }

    public void setPosition(int position) {
        if (this.position != position) {
            this.position = position;
            invalidate();
            fadeOut();
        }
    }
}
