package com.gazlaws.codeboard.theme;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import androidx.core.graphics.ColorUtils;

public class UiTheme {

    // Above this background luminance (0=black, 1=white) a theme counts as "bright", so the popup
    // cells darken to stand out instead of lifting toward white (which is invisible on a light bg).
    private static final double BRIGHT_THEME_LUMINANCE = 0.5;

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
        // Popup cell shading. Two levels (preview / press = subtler, selected option = stronger).
        // Lift toward white on dark themes; on a bright theme darken toward black instead, so the
        // cell still contrasts with the light keyboard.
        int popupContrast = ColorUtils.calculateLuminance(info.backgroundColor) > BRIGHT_THEME_LUMINANCE
                ? Color.BLACK : Color.WHITE;
        theme.previewBodyPaint.setColor(ColorUtils.blendARGB(info.backgroundColor, popupContrast, 0.3f));
        theme.popupSelectedPaint.setColor(ColorUtils.blendARGB(info.backgroundColor, popupContrast, 0.55f));
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
