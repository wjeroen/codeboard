package com.gazlaws.codeboard.layout.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.KeyboardView;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.os.Handler;
import android.os.Looper;
import android.widget.PopupWindow;

import com.gazlaws.codeboard.CodeBoardIME;
import com.gazlaws.codeboard.R;
import com.gazlaws.codeboard.layout.Box;
import com.gazlaws.codeboard.layout.Key;
import com.gazlaws.codeboard.theme.UiTheme;

import java.util.Timer;
import java.util.TimerTask;

public class KeyboardButtonView extends View {

    private static final String TAG = "KeyboardButtonView";
    // Gboard's default "key long press delay" is 300ms; the alternates grid opens then.
    private static final int POPUP_DELAY_MS = 300;
    // Finger travel (in dp) per one-character cursor step when dragging the spacebar (smaller =
    // faster). Tunable.
    private static final float CURSOR_STEP_DP = 11f;

    private final Key key;
    private final KeyboardView.OnKeyboardActionListener inputService;
    private final UiTheme uiTheme;
    private Timer timer;
    private String currentLabel = null;
    private Drawable currentIcon = null;   // the icon currently drawn (Shift swaps it for caps lock)
    private boolean isPressed = false;
    private boolean isPreviewActive = false;
    private boolean popupLongPressFired = false;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;
    private PopupWindow popupWindow;
    private PopupKeyboardView popupKeyboardView;
    private boolean popupIsGrid = false; // false = single-cell press preview, true = alternates grid
    private boolean shiftActive = false; // tracks shift so the popup can show uppercased alternates
    private float spaceDragLastX = 0f;   // last finger X while dragging the spacebar (cursor mode)
    private float spaceDragAccum = 0f;   // unspent horizontal travel toward the next cursor step

    public KeyboardButtonView(Context context, Key key, KeyboardView.OnKeyboardActionListener inputService, UiTheme uiTheme) {
        super(context);
        this.inputService = inputService;
        this.key = key;
        this.uiTheme = uiTheme;
        this.currentLabel = key.info.label;
        this.currentIcon = key.info.icon;
        //Enable shadow
        this.setOutlineProvider(ViewOutlineProvider.BOUNDS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        int action = e.getAction();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                if (isSpaceKey()){
                    spaceDragLastX = e.getRawX();
                    spaceDragAccum = 0f;
                    if (inputService instanceof CodeBoardIME){
                        ((CodeBoardIME) inputService).onSpaceCursorStart();
                    }
                }
                onPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isPopupShowing() && popupIsGrid){
                    popupKeyboardView.updateSelection(e.getRawX(), e.getRawY());
                } else if (isSpaceKey()){
                    handleSpaceCursorDrag(e.getRawX());
                }
                break;
            case MotionEvent.ACTION_UP:
                onRelease();
                break;
            case MotionEvent.ACTION_CANCEL:
                onCancel();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        Box box = key.box;
        int w = r-l;
        int h = b-t;
        int left = (int)(l + w * box.getLeft());
        int right = (int)(l + w * box.getRight());
        int top = (int)(t + h * box.getTop());
        int bottom = (int)(t + h * box.getBottom());
        super.layout(left, top, right, bottom);
    }

    @Override
    public void draw(Canvas canvas){
        drawButtonBody(canvas);
        drawButtonContent(canvas);
        super.draw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        autoReleaseIfPressed();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        autoReleaseIfPressed();
    }

    private void drawButtonContent(Canvas canvas) {
        float x = this.getWidth()/2;
        float y = this.getHeight()/2 + uiTheme.fontHeight/3;
        canvas.drawText(currentLabel, x, y, uiTheme.foregroundPaint);

        if (key.info.cornerLabel != null){
            float pad = uiTheme.buttonBodyPadding * 2.5f;
            canvas.drawText(key.info.cornerLabel,
                    this.getWidth() - pad,
                    pad + uiTheme.cornerPaint.getTextSize(),
                    uiTheme.cornerPaint);
        }

        if (currentIcon != null){
            Drawable d = currentIcon;
            d.setTint(uiTheme.foregroundPaint.getColor());

            int padding = (int)uiTheme.buttonBodyPadding*2;
            int top;
            int left;
            int squareSize;
            if (this.getWidth() > this.getHeight()){
                top = 2*padding;
                squareSize = (this.getHeight()/2) - top;
                left = (this.getWidth()/2) - squareSize;
            } else {
                left = 2*padding;
                squareSize = this.getWidth()/2-(left);
                top = this.getHeight()/2 - squareSize;
            }
            int right = left + (squareSize*2);
            int bottom = top + (squareSize*2);
            d.setBounds(left,top,right,bottom);
            d.draw(canvas);
        }
    }

    private void drawButtonBody(Canvas canvas) {
        float left = uiTheme.buttonBodyPadding;
        float top = uiTheme.buttonBodyPadding;
        float right = this.getWidth() - uiTheme.buttonBodyPadding;
        float bottom = this.getHeight() - uiTheme.buttonBodyPadding;
        float rx = uiTheme.buttonBodyBorderRadius;
        float ry = uiTheme.buttonBodyBorderRadius;
        canvas.drawRoundRect(left, top, right, bottom, rx, ry,
                (isPreviewActive && uiTheme.previewBodyPaint != null)
                        ? uiTheme.previewBodyPaint : uiTheme.buttonBodyPaint);
    }

    private void onPress() {
        isPressed = true;
        popupLongPressFired = false;
        inputService.onPress(key.info.code);
        if (key.info.isRepeatable){
            startRepeating();
        }
        if (hasPopup()){
            // Defer output: a quick tap types the key on release, a long hold opens the
            // alternates grid and types the selected alternate instead.
            scheduleLongPress();
        } else {
            submitKeyEvent();
        }
        if (uiTheme.enablePreview && hasPreview()){
            // Instant preview: a single floating popup cell showing the key's content (character,
            // label, or icon). If the key is held and has alternates, it expands into the grid.
            showPreviewPopup();
        }
        animatePress();
    }

    private void onRelease() {
        isPressed = false;
//      NOTE: If the arrow keys move out of the input view, the onRelease is never called
        if (hasPopup()){
            cancelPopupLongPress();
            if (isPopupShowing() && popupIsGrid){
                // Grid is up: type the highlighted cell (the default if the finger didn't slide).
                commitPopupChar(popupKeyboardView.getSelectedChar());
            } else if (popupLongPressFired){
                // Grid failed to show but the hold fired: type the default alternate.
                commitPopupChar(key.info.popupChars[key.info.popupDefaultIndex]);
            } else {
                // Quick tap: type the key itself.
                submitKeyEvent();
            }
        }
        dismissPopup();
        if (key.info.code != 0){
            inputService.onRelease(key.info.code);
        }
        if (key.info.isRepeatable){
            stopRepeating();
        }
        animateRelease();
        popupLongPressFired = false;
    }

    private void submitKeyEvent(){
        if (key.info.code != 0){
            inputService.onKey(key.info.code, null);
        }
        if (this.key.info.outputText != null){
            inputService.onText(key.info.outputText);
        }
    }

    private void autoReleaseIfPressed(){
        if (isPressed){
            onRelease();
        }
    }

    private boolean hasPopup(){
        return key.info.popupChars != null && key.info.popupChars.length > 0;
    }

    /** Every key shows the floating popup-cell press preview, except the spacebar (it is a
     *  cursor-drag control and brightens in place instead). */
    private boolean hasPreview(){
        return !isSpaceKey();
    }

    private boolean isSpaceKey(){
        return key.info.code == 32;
    }

    /** Spacebar drag = cursor control: convert horizontal finger travel into left/right caret
     *  moves. Every CURSOR_STEP_DP of travel nudges the caret one character. */
    private void handleSpaceCursorDrag(float rawX){
        spaceDragAccum += rawX - spaceDragLastX;
        spaceDragLastX = rawX;
        float stepPx = CURSOR_STEP_DP * getResources().getDisplayMetrics().density;
        if (stepPx <= 0){
            return;
        }
        while (Math.abs(spaceDragAccum) >= stepPx){
            boolean right = spaceDragAccum > 0;
            if (inputService instanceof CodeBoardIME){
                ((CodeBoardIME) inputService).onSpaceCursorMove(right);
            }
            spaceDragAccum += right ? -stepPx : stepPx;
        }
    }

    private void scheduleLongPress(){
        cancelPopupLongPress();
        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                popupLongPressFired = true;
                if (key.info.popupChars != null && key.info.popupChars.length >= 1){
                    showPopup();
                }
            }
        };
        longPressHandler.postDelayed(longPressRunnable, POPUP_DELAY_MS);
    }

    private void cancelPopupLongPress(){
        if (longPressRunnable != null){
            longPressHandler.removeCallbacks(longPressRunnable);
            longPressRunnable = null;
        }
    }

    private void commitPopupChar(String text){
        if (inputService instanceof CodeBoardIME){
            ((CodeBoardIME) inputService).onPopupCharacter(text);
        } else {
            inputService.onText(text);
        }
    }

    private boolean isPopupShowing(){
        return popupWindow != null && popupWindow.isShowing();
    }

    /** Instant press preview: one bright cell (in the grid's footprint) showing the key's content. */
    private void showPreviewPopup(){
        popupIsGrid = false;
        showOrUpdatePopup();
    }

    /** Long-press: fill the same window with the full grid of alternates (no move/resize). */
    private void showPopup(){
        if (!hasPopup()){
            return;
        }
        popupIsGrid = true;
        showOrUpdatePopup();
    }

    /**
     * Show the popup above the key, or refresh it in place. The window is ALWAYS sized to the full
     * alternates grid (1x1 for keys with no alternates), so the long-press never moves or resizes
     * it: the preview just draws one cell and the grid fills in the rest. popupIsGrid (set by the
     * caller) picks which.
     */
    private void showOrUpdatePopup(){
        float cellW = getWidth();
        float cellH = getHeight();

        // The popup window spans the full grid even during the preview, so it never has to reposition.
        int columns, rowCount, defaultCol;
        if (hasPopup()){
            columns = Math.max(1, key.info.popupColumns);
            rowCount = (int) Math.ceil(key.info.popupChars.length / (float) columns);
            defaultCol = key.info.popupDefaultIndex % columns;
        } else {
            columns = 1; rowCount = 1; defaultCol = 0;
        }

        int[] screenLoc = new int[2];
        getLocationOnScreen(screenLoc);
        int[] windowLoc = new int[2];
        getLocationInWindow(windowLoc);

        // The popup is the cell block plus a transparent margin all round for the drop shadow.
        int pad = PopupKeyboardView.SHADOW_PAD;
        int popupW = (int) (columns * cellW) + 2 * pad;
        int popupH = (int) (rowCount * cellH) + 2 * pad;

        // Anchor so the default column sits directly above the key, with the cell block floating half
        // a key height above the key top. The -pad backs out the shadow margin so the cells still
        // land in the same place. These are screen-absolute coordinates.
        float lift = cellH * 0.5f; // half a key height
        int originX = (int) (screenLoc[0] - defaultCol * cellW) - pad;
        int originY = (int) (screenLoc[1] - rowCount * cellH - lift) - pad;

        // Keep the popup fully on screen horizontally.
        int screenW = getResources().getDisplayMetrics().widthPixels;
        if (originX + popupW > screenW) originX = screenW - popupW;
        if (originX < 0) originX = 0;

        if (popupKeyboardView == null){
            popupKeyboardView = new PopupKeyboardView(getContext(), uiTheme);
        }
        if (popupIsGrid){
            popupKeyboardView.configure(key.info, shiftActive, cellW, cellH, originX, originY);
        } else {
            // The preview cell sits in the bottom row, default column (directly above the key).
            int previewSlot = (rowCount - 1) * columns + defaultCol;
            popupKeyboardView.configurePreview(currentLabel, currentIcon, columns, rowCount, previewSlot,
                    cellW, cellH, originX, originY);
        }

        if (popupWindow == null){
            popupWindow = new PopupWindow(popupKeyboardView, popupW, popupH, false);
            popupWindow.setClippingEnabled(false);
            popupWindow.setTouchable(false);
            popupWindow.setFocusable(false);
            popupWindow.setBackgroundDrawable(null);
            // No window animation: stops the popup sliding/jumping into place when the preview
            // resizes into the grid (the per-key reposition some keys showed on long-press).
            popupWindow.setAnimationStyle(0);
        }

        // showAtLocation/update want window coordinates; convert from screen by removing the
        // window's on-screen offset (screenLoc - windowLoc).
        int winX = originX - (screenLoc[0] - windowLoc[0]);
        int winY = originY - (screenLoc[1] - windowLoc[1]);
        try {
            if (popupWindow.isShowing()){
                popupWindow.update(winX, winY, popupW, popupH);
            } else {
                popupWindow.setWidth(popupW);
                popupWindow.setHeight(popupH);
                popupWindow.showAtLocation(this, Gravity.NO_GRAVITY, winX, winY);
            }
        } catch (Exception e){
            // If the window token isn't ready yet, drop the popup and fall back to plain typing.
            popupWindow = null;
            popupKeyboardView = null;
            popupIsGrid = false;
        }
    }

    private void dismissPopup(){
        if (popupWindow != null){
            try {
                popupWindow.dismiss();
            } catch (Exception ignored){
            }
            popupWindow = null;
        }
        popupKeyboardView = null;
        popupIsGrid = false;
    }

    private void onCancel(){
        isPressed = false;
        cancelPopupLongPress();
        dismissPopup();
        popupLongPressFired = false;
        if (key.info.isRepeatable){
            stopRepeating();
        }
        animateRelease();
    }

    private void stopRepeating() {
        if (timer == null){
            return;
        }
        timer.cancel();
        timer = null;
    }

    private void startRepeating() {
        if (timer != null){
            stopRepeating();
            return;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                submitKeyEvent();
            }
        },400, 50);
    }

    private void animatePress(){
        if (!uiTheme.enablePreview){
            this.setAlpha(.1f);
            return;
        }
        if (isSpaceKey()){
            // The spacebar is a cursor-drag control, so just brighten it (don't lift it, which
            // would look odd while sliding horizontally).
            isPreviewActive = true;
            invalidate();
            return;
        }
        // Every other key now shows the floating preview popup cell (started in onPress) as its
        // feedback, so the key itself stays put (no more lift-in-place).
    }
    private void animateRelease() {
        if (uiTheme.enablePreview){
            isPreviewActive = false;
            this.setTranslationY(0.0f);
            this.setScaleX(1.0f);
            this.setScaleY(1.0f);
            this.setElevation(0.0f);
            invalidate();
        } else {
            this.animate().alpha(1.0f).setDuration(400);
        }
    }

    public void applyShiftModifier(boolean shiftPressed) {
        // Remember shift for every key (even ones with no shift label) so an open alternates popup
        // can display its characters uppercased to match what will actually be typed.
        this.shiftActive = shiftPressed;
        if (this.key.info.onShiftLabel != null){
            String nextLabel = shiftPressed
                    ? this.key.info.onShiftLabel
                    : this.key.info.label;
            setCurrentLabel(nextLabel);
        }
    }

    public void applyCtrlModifier(boolean ctrlPressed) {
        if (this.key.info.onCtrlLabel != null){
            String nextLabel = ctrlPressed
                    ? this.key.info.onCtrlLabel
                    : this.key.info.label;
            setCurrentLabel(nextLabel);
        }
    }

    /** Only the Shift key reacts: it swaps to the underlined caps-lock arrow while locked. */
    public void applyCapsLock(boolean locked) {
        if (key.info.code != 16) {
            return;
        }
        Drawable nextIcon = locked
                ? getContext().getDrawable(R.drawable.ic_caps_lock_24dp)
                : key.info.icon;
        if (nextIcon != currentIcon) {
            currentIcon = nextIcon;
            invalidate();
            // If the press preview is up (caps lock just toggled by a held long-press), refresh it
            // so the floating cell shows the new symbol you switched to.
            if (isPopupShowing() && !popupIsGrid) {
                showPreviewPopup();
            }
        }
    }

    private void setCurrentLabel(String nextLabel) {
        if (nextLabel != currentLabel){
            currentLabel = nextLabel;
            this.invalidate();
        }
    }
}
