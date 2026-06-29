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
 * The window is ALWAYS sized to the full alternates grid, even for the press preview (which just
 * draws one cell in the slot that sits above the key). That way the long-press does not move or
 * resize the window, it only fills in the rest of the cells, so the popup never slides into place.
 *
 * The cells are drawn inset by {@link #SHADOW_PAD} on every side: that transparent margin gives the
 * blurred drop shadow somewhere to render without being clipped at the popup edge. KeyboardButtonView
 * sizes and anchors the popup window with the same SHADOW_PAD.
 */
public class PopupKeyboardView extends View {

    // Transparent margin (px) around the cell block so the drop shadow isn't clipped by the popup
    // window's edge. Shared with KeyboardButtonView (it sizes/anchors the window).
    public static final int SHADOW_PAD = 44;
    private static final float SHADOW_RADIUS = 30f;
    private static final float SHADOW_DY = 10f;
    private static final int SHADOW_COLOR = 0x66000000; // ~40% black

    private final UiTheme uiTheme;
    private final Paint cellPreviewPaint;  // dimmer cell fill (no shadow)
    private final Paint cellSelectedPaint; // brighter selected-cell fill (no shadow)
    private final Paint shadowPaint;       // casts the drop shadow (drawn behind all cells)
    private String[] chars;
    private int columns = 1;
    private int rows = 1;
    private int selectedIndex = 0;
    private float cellW = 0;
    private float cellH = 0;
    private int originX = 0; // popup top-left in absolute (screen) coordinates
    private int originY = 0;
    private boolean isPreview = false;       // true = single-cell press preview; false = alternates grid
    private String previewChar = "";         // text to show in the preview cell
    private Drawable previewIcon = null;      // icon to show in the preview cell (backspace/enter/arrows)
    private int previewSlot = 0;              // grid index the preview cell occupies (the one above the key)
    private boolean displayUppercase = false; // draw the grid characters uppercased (shift held)

    public PopupKeyboardView(Context context, UiTheme uiTheme) {
        super(context);
        this.uiTheme = uiTheme;
        setWillNotDraw(false);
        // A blurred shadow layer on a drawn shape needs a software layer before API 28.
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        // Plain local copies of the theme cell paints (no shadow), so the shared theme paints (the
        // key bodies draw with the same previewBodyPaint) are never mutated. The shadow is cast by a
        // separate paint, drawn behind every cell in a first pass so it never lands on another cell.
        cellPreviewPaint = new Paint(uiTheme.previewBodyPaint);
        cellSelectedPaint = new Paint(uiTheme.popupSelectedPaint);
        shadowPaint = new Paint(uiTheme.previewBodyPaint); // fill is hidden under the cell bodies
        shadowPaint.setShadowLayer(SHADOW_RADIUS, 0, SHADOW_DY, SHADOW_COLOR);
    }

    /** Configure as the full grid of a key's long-press alternates. */
    public void configure(KeyInfo info, boolean uppercase, float cellW, float cellH, int originX, int originY) {
        this.isPreview = false;
        this.previewIcon = null;
        this.displayUppercase = uppercase;
        this.chars = info.popupChars;
        this.columns = Math.max(1, info.popupColumns);
        this.rows = (int) Math.ceil(this.chars.length / (float) this.columns);
        this.selectedIndex = info.popupDefaultIndex;
        this.cellW = cellW;
        this.cellH = cellH;
        this.originX = originX;
        this.originY = originY;
        invalidate();
    }

    /**
     * Configure as the single-cell press preview. The window still spans the whole grid
     * ({@code columns} x {@code rows}); only the {@code previewSlot} cell is drawn, holding the
     * pressed character or icon.
     */
    public void configurePreview(String character, Drawable icon, int columns, int rows, int previewSlot,
                                 float cellW, float cellH, int originX, int originY) {
        this.isPreview = true;
        this.chars = null;
        this.previewChar = character == null ? "" : character;
        this.previewIcon = icon;
        this.columns = Math.max(1, columns);
        this.rows = Math.max(1, rows);
        this.previewSlot = previewSlot;
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
        if (isPreview) {
            drawCellShadow(canvas, previewSlot);
            drawCellBody(canvas, previewSlot, cellPreviewPaint, previewChar, previewIcon);
            return;
        }
        if (chars == null) return;
        // Pass 1: every cell's shadow, so a shadow never lands on top of another cell body.
        for (int i = 0; i < chars.length; i++) {
            drawCellShadow(canvas, i);
        }
        // Pass 2: every cell body (and its content) on top of the shadows.
        for (int i = 0; i < chars.length; i++) {
            Paint cellPaint = (i == selectedIndex) ? cellSelectedPaint : cellPreviewPaint;
            String text = displayUppercase ? chars[i].toUpperCase() : chars[i];
            drawCellBody(canvas, i, cellPaint, text, null);
        }
    }

    private float cellLeft(int slot) { return SHADOW_PAD + (slot % columns) * cellW; }
    private float cellTop(int slot) { return SHADOW_PAD + (slot / columns) * cellH; }

    /** Draw the drop shadow for one cell. Its fill is opaque but gets covered by the cell body. */
    private void drawCellShadow(Canvas canvas, int slot) {
        float pad = uiTheme.buttonBodyPadding;
        float rx = uiTheme.buttonBodyBorderRadius;
        float left = cellLeft(slot);
        float top = cellTop(slot);
        canvas.drawRoundRect(left + pad, top + pad, left + cellW - pad, top + cellH - pad,
                rx, rx, shadowPaint);
    }

    /** Draw one cell's rounded body, then its icon or text. */
    private void drawCellBody(Canvas canvas, int slot, Paint cellPaint, String text, Drawable icon) {
        float pad = uiTheme.buttonBodyPadding;
        float rx = uiTheme.buttonBodyBorderRadius;
        float left = cellLeft(slot);
        float top = cellTop(slot);
        canvas.drawRoundRect(left + pad, top + pad, left + cellW - pad, top + cellH - pad,
                rx, rx, cellPaint);
        if (icon != null) {
            drawIcon(canvas, icon, left, top);
        } else if (text != null) {
            float cx = left + cellW / 2f;
            float cy = top + cellH / 2f + uiTheme.fontHeight / 3f;
            canvas.drawText(text, cx, cy, uiTheme.foregroundPaint);
        }
    }

    /** Centre an icon in a cell, mirroring KeyboardButtonView's icon sizing. */
    private void drawIcon(Canvas canvas, Drawable d, float cellLeftPx, float cellTopPx) {
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
        int left = (int) cellLeftPx + iLeft;
        int top = (int) cellTopPx + iTop;
        d.setBounds(left, top, left + squareSize * 2, top + squareSize * 2);
        d.draw(canvas);
    }
}
