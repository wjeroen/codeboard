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
    public float buttonBodyBorderRadius = 8.0f;
    public boolean enablePreview = false;
    public boolean enableBorder;
    public float portraitSize;
    public float landscapeSize;

    private UiTheme(){
        this.foregroundPaint = new Paint();
        this.buttonBodyPaint = new Paint();
        this.previewBodyPaint = new Paint();
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
        // brighter "preview" body: a lightened version of the key, drawn while a key is
        // lifted on press (only when previews are enabled) so the preview clearly stands
        // out from the unpressed keys.
        theme.previewBodyPaint.setColor(ColorUtils.blendARGB(info.backgroundColor, Color.WHITE, 0.3f));
        // foreground
        theme.foregroundPaint.setColor(info.foregroundColor);
        theme.fontHeight = info.fontSize;
        theme.foregroundPaint.setTextSize(theme.fontHeight);
        theme.foregroundPaint.setTextAlign(Paint.Align.CENTER);
        theme.foregroundPaint.setAntiAlias(true);
        theme.foregroundPaint.setTypeface(Typeface.DEFAULT);

        return theme;
    }
}
