package com.flavienlaurent.muzei.hash.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Flavien Laurent (flavienlaurent.com) on 16/02/14.
 */
public class AlegrevaTextView extends TextView {

    public AlegrevaTextView(Context context) {
        super(context);
        setTypeFace(context);
    }

    public AlegrevaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace(context);
    }

    public AlegrevaTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeFace(context);
    }

    private void setTypeFace(Context context) {
        if(isInEditMode()) {
            return;
        }
        Typeface tf = TypefaceUtil.getAndCache(context, "Alegreya-BlackItalic.ttf");
        setTypeface(tf);
    }
}
