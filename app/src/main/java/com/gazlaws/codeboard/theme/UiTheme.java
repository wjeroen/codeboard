package com.gazlaws.codeboard.theme;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import androidx.core.graphics.ColorUtils;

public class UiTheme {

    public Paint foregroundPaint;
    public int backgroundColor;
    public float fontHeight;

    public float buttonBodyPadding = 5.0f;
    public Paint buttonBodyPaint;
    public Paint previewBodyPaint;
    public Paint popupSelectedPaint;
    public Paint cornerPaint;
    public float buttonBodyBorderRadius = 8.0f;
    public boolean enablePreview = false;
    public boolean enableBorder;
    public float portraitSize;
    public float landscapeSize;

    private UiTheme(){
        this.foregroundPaint = new Paint();
        this.buttonBodyPaint = new Paint();
        this.previewBodyPaint = new Paint();
        this.popupSelectedPaint = new Paint();
        this.cornerPaint = new Paint();
        backgroundColor = 0xff000000;
    }

    public static UiTheme buildFromInfo(ThemeInfo info){
        UiTheme theme = new UiTheme();
        theme.portraitSize = info.size;
        theme.landscapeSize = info.sizeLandscape;
        theme.enablePreview = info.enablePreview;
        theme.enableBorder = info.enableBorder;
        // background - darker border
        if(info.enableBorder){
            theme.backgroundColor = ColorUtils.blendARGB(info.backgroundColor, Color.BLACK, 0.2f);
        } else {
            theme.backgroundColor = info.backgroundColor;
        }
        // button body
        theme.buttonBodyPaint.setColor(info.backgroundColor);
        // brighter "preview" body, reused for long-press popup cells
        theme.previewBodyPaint.setColor(ColorUtils.blendARGB(info.backgroundColor, Color.WHITE, 0.3f));
        // even brighter highlight for the currently-selected popup cell
        theme.popupSelectedPaint.setColor(ColorUtils.blendARGB(info.backgroundColor, Color.WHITE, 0.55f));
        // foreground
        theme.foregroundPaint.setColor(info.foregroundColor);
        theme.fontHeight = info.fontSize;
        theme.foregroundPaint.setTextSize(theme.fontHeight);
        theme.foregroundPaint.setTextAlign(Paint.Align.CENTER);
        theme.foregroundPaint.setAntiAlias(true);
        theme.foregroundPaint.setTypeface(Typeface.DEFAULT);
        // corner symbol: a bit larger now, slightly dimmed foreground, drawn top-right
        theme.cornerPaint.setColor(info.foregroundColor);
        theme.cornerPaint.setAlpha(170);
        theme.cornerPaint.setTextSize(theme.fontHeight * 0.52f);
        theme.cornerPaint.setTextAlign(Paint.Align.RIGHT);
        theme.cornerPaint.setAntiAlias(true);
        theme.cornerPaint.setTypeface(Typeface.DEFAULT);

        return theme;
    }
}
