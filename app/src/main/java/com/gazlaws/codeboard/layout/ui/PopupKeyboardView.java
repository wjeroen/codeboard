package com.gazlaws.codeboard.layout.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.gazlaws.codeboard.layout.builder.KeyInfo;
import com.gazlaws.codeboard.theme.UiTheme;

/**
 * The popup shown above a key. Used in two ways: a single bright cell as the instant
 * press preview, and the full grid of alternates on a long-press. It is display-only:
 * the pressed key view keeps the touch gesture and forwards absolute finger
 * coordinates here (updateSelection) so this view can highlight the cell under the
 * finger. On release the key view reads getSelectedChar().
 *
 * The cells are drawn inset by {@link #SHADOW_PAD} on every side: that transparent margin gives the
 * blurred drop shadow somewhere to render without being clipped at the popup edge. KeyboardButtonView
 * sizes and anchors the popup window with the same SHADOW_PAD so the cells still land above the key.
 */
public class PopupKeyboardView extends View {

    // Transparent margin (px) around the cell block so the drop shadow isn't clipped by the popup
    // window's edge. Shared with KeyboardButtonView (it sizes/anchors the window).
    public static final int SHADOW_PAD = 18;
    private static final float SHADOW_RADIUS = 10f;
    private static final float SHADOW_DY = 5f;
    private static final int SHADOW_COLOR = 0x66000000; // ~40% black

    private final UiTheme uiTheme;
    private final Paint cellPreviewPaint;  // dimmer cell fill + drop shadow
    private final Paint cellSelectedPaint; // brighter selected-cell fill + drop shadow
    private String[] chars;
    private int columns = 1;
    private int rows = 1;
    private int selectedIndex = 0;
    private float cellW = 0;
    private float cellH = 0;
    private int originX = 0; // popup top-left in absolute (screen) coordinates
    private int originY = 0;
    private boolean isPreview = false;       // true = single-cell press preview; false = alternates grid
    private Drawable previewIcon = null;     // icon to show in the preview (backspace/enter/arrows)
    private boolean displayUppercase = false; // draw the grid characters uppercased (shift held)

    public PopupKeyboardView(Context context, UiTheme uiTheme) {
        super(context);
        this.uiTheme = uiTheme;
        setWillNotDraw(false);
        // A blurred shadow layer on a drawn shape needs a software layer before API 28.
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        // Local copies of the theme cell paints with a drop shadow, so the shared theme paints (the
        // key bodies draw with the same previewBodyPaint) are never mutated.
        cellPreviewPaint = new Paint(uiTheme.previewBodyPaint);
        cellSelectedPaint = new Paint(uiTheme.popupSelectedPaint);
        cellPreviewPaint.setShadowLayer(SHADOW_RADIUS, 0, SHADOW_DY, SHADOW_COLOR);
        cellSelectedPaint.setShadowLayer(SHADOW_RADIUS, 0, SHADOW_DY, SHADOW_COLOR);
    }

    /** Configure as the full grid of a key's long-press alternates. */
    public void configure(KeyInfo info, boolean uppercase, float cellW, float cellH, int originX, int originY) {
        this.isPreview = false;
        this.previewIcon = null;
        this.displayUppercase = uppercase;
        setCells(info.popupChars, info.popupColumns, info.popupDefaultIndex,
                cellW, cellH, originX, originY);
    }

    /** Configure as the single-cell instant press preview of one character or icon. */
    public void configurePreview(String character, Drawable icon, float cellW, float cellH, int originX, int originY) {
        this.isPreview = true;
        this.previewIcon = icon;
        this.displayUppercase = false;
        setCells(new String[]{ character == null ? "" : character }, 1, 0, cellW, cellH, originX, originY);
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

    public int popupWidth() { return (int) (columns * cellW) + 2 * SHADOW_PAD; }
    public int popupHeight() { return (int) (rows * cellH) + 2 * SHADOW_PAD; }

    /** Highlight the cell under an absolute (screen) finger position. */
    public void updateSelection(float screenX, float screenY) {
        if (chars == null) return;
        // The cell block starts SHADOW_PAD inside the popup (originX/originY is the popup top-left).
        int col = (int) ((screenX - originX - SHADOW_PAD) / cellW);
        int row = (int) ((screenY - originY - SHADOW_PAD) / cellH);
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
        // Always the raw (lowercase) character: the commit path (onPopupCharacter) applies shift.
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
            float left = SHADOW_PAD + col * cellW;
            float top = SHADOW_PAD + row * cellH;
            // Grid: only the cell under the finger (selected) is the brighter highlight. Preview:
            // the single cell keeps the dimmer previewBodyPaint (less bright than the selection).
            Paint cellPaint = (!isPreview && i == selectedIndex) ? cellSelectedPaint : cellPreviewPaint;
            canvas.drawRoundRect(left + pad, top + pad, left + cellW - pad, top + cellH - pad,
                    rx, rx, cellPaint);
            if (isPreview && previewIcon != null) {
                drawPreviewIcon(canvas, left, top);
            } else {
                String text = displayUppercase ? chars[i].toUpperCase() : chars[i];
                float cx = left + cellW / 2f;
                float cy = top + cellH / 2f + uiTheme.fontHeight / 3f;
                canvas.drawText(text, cx, cy, uiTheme.foregroundPaint);
            }
        }
    }

    /** Draw the preview icon centred in its cell, mirroring KeyboardButtonView's icon sizing. */
    private void drawPreviewIcon(Canvas canvas, float cellLeft, float cellTop) {
        Drawable d = previewIcon;
        d.setTint(uiTheme.foregroundPaint.getColor());
        int padding = (int) (uiTheme.buttonBodyPadding * 2);
        int iTop, iLeft, squareSize;
        if (cellW > cellH) {
            iTop = 2 * padding;
            squareSize = (int) (cellH / 2) - iTop;
            iLeft = (int) (cellW / 2) - squareSize;
        } else {
            iLeft = 2 * padding;
            squareSize = (int) (cellW / 2) - iLeft;
            iTop = (int) (cellH / 2) - squareSize;
        }
        int left = (int) cellLeft + iLeft;
        int top = (int) cellTop + iTop;
        d.setBounds(left, top, left + squareSize * 2, top + squareSize * 2);
        d.draw(canvas);
    }
}
