package com.gazlaws.codeboard.layout.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.gazlaws.codeboard.layout.builder.KeyInfo;
import com.gazlaws.codeboard.theme.UiTheme;

/**
 * The popup shown above a key. Used in two ways: a single bright cell as the instant
 * press preview, and the full grid of alternates on a long-press. It is display-only:
 * the pressed key view keeps the touch gesture and forwards absolute finger
 * coordinates here (updateSelection) so this view can highlight the cell under the
 * finger. On release the key view reads getSelectedChar().
 */
public class PopupKeyboardView extends View {

    private final UiTheme uiTheme;
    private String[] chars;
    private int columns = 1;
    private int rows = 1;
    private int selectedIndex = 0;
    private float cellW = 0;
    private float cellH = 0;
    private int originX = 0; // popup top-left in absolute (screen) coordinates
    private int originY = 0;
    private boolean isPreview = false; // true = single-cell press preview; false = alternates grid

    public PopupKeyboardView(Context context, UiTheme uiTheme) {
        super(context);
        this.uiTheme = uiTheme;
        setWillNotDraw(false);
    }

    /** Configure as the full grid of a key's long-press alternates. */
    public void configure(KeyInfo info, float cellW, float cellH, int originX, int originY) {
        this.isPreview = false;
        setCells(info.popupChars, info.popupColumns, info.popupDefaultIndex,
                cellW, cellH, originX, originY);
    }

    /** Configure as the single-cell instant press preview of one character. */
    public void configurePreview(String character, float cellW, float cellH, int originX, int originY) {
        this.isPreview = true;
        setCells(new String[]{ character }, 1, 0, cellW, cellH, originX, originY);
    }

    private void setCells(String[] chars, int columns, int selectedIndex,
                          float cellW, float cellH, int originX, int originY) {
        this.chars = chars;
        this.columns = Math.max(1, columns);
        this.rows = (int) Math.ceil(chars.length / (float) this.columns);
        this.selectedIndex = selectedIndex;
        this.cellW = cellW;
        this.cellH = cellH;
        this.originX = originX;
        this.originY = originY;
        invalidate();
    }

    public int popupWidth() { return (int) (columns * cellW); }
    public int popupHeight() { return (int) (rows * cellH); }

    /** Highlight the cell under an absolute (screen) finger position. */
    public void updateSelection(float screenX, float screenY) {
        if (chars == null) return;
        int col = (int) ((screenX - originX) / cellW);
        int row = (int) ((screenY - originY) / cellH);
        if (col < 0) col = 0;
        if (col > columns - 1) col = columns - 1;
        if (row < 0) row = 0;
        if (row > rows - 1) row = rows - 1;
        int idx = row * columns + col;
        if (idx >= chars.length) idx = chars.length - 1;
        if (idx < 0) idx = 0;
        if (idx != selectedIndex) {
            selectedIndex = idx;
            invalidate();
        }
    }

    public String getSelectedChar() {
        if (chars == null || chars.length == 0) return "";
        return chars[selectedIndex];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(popupWidth(), popupHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (chars == null) return;
        float pad = uiTheme.buttonBodyPadding;
        float rx = uiTheme.buttonBodyBorderRadius;
        for (int i = 0; i < chars.length; i++) {
            int col = i % columns;
            int row = i / columns;
            float left = col * cellW;
            float top = row * cellH;
            // The press preview is a single bright cell; in the grid, only the cell under the
            // finger is bright. (isPreview is also a hook to style the preview differently later.)
            boolean bright = isPreview || i == selectedIndex;
            canvas.drawRoundRect(left + pad, top + pad, left + cellW - pad, top + cellH - pad,
                    rx, rx,
                    bright ? uiTheme.popupSelectedPaint : uiTheme.previewBodyPaint);
            float cx = left + cellW / 2f;
            float cy = top + cellH / 2f + uiTheme.fontHeight / 3f;
            canvas.drawText(chars[i], cx, cy, uiTheme.foregroundPaint);
        }
    }
}
