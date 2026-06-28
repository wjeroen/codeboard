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
import android.view.ViewConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.widget.PopupWindow;

import com.gazlaws.codeboard.CodeBoardIME;
import com.gazlaws.codeboard.layout.Box;
import com.gazlaws.codeboard.layout.Key;
import com.gazlaws.codeboard.theme.UiTheme;

import java.util.Timer;
import java.util.TimerTask;

public class KeyboardButtonView extends View {

    private static final String TAG = "KeyboardButtonView";

    private final Key key;
    private final KeyboardView.OnKeyboardActionListener inputService;
    private final UiTheme uiTheme;
    private Timer timer;
    private String currentLabel = null;
    private boolean isPressed = false;
    private boolean isPreviewActive = false;
    private boolean popupLongPressFired = false;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;
    private PopupWindow popupWindow;
    private PopupKeyboardView popupKeyboardView;

    public KeyboardButtonView(Context context, Key key, KeyboardView.OnKeyboardActionListener inputService, UiTheme uiTheme) {
        super(context);
        this.inputService = inputService;
        this.key = key;
        this.uiTheme = uiTheme;
        this.currentLabel = key.info.label;
        //Enable shadow
        this.setOutlineProvider(ViewOutlineProvider.BOUNDS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        int action = e.getAction();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                onPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isPopupShowing()){
                    popupKeyboardView.updateSelection(e.getRawX(), e.getRawY());
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

        if (key.info.icon != null){
            Drawable d = key.info.icon;
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
            // popup (Stage 2) and types the selected alternate instead.
            scheduleLongPress();
        } else {
            submitKeyEvent();
        }
        animatePress();
    }

    private void onRelease() {
        isPressed = false;
//      NOTE: If the arrow keys move out of the input view, the onRelease is never called
        if (hasPopup()){
            cancelPopupLongPress();
            if (isPopupShowing()){
                // Slid onto an alternate (or rested on the default): type the highlighted cell.
                commitPopupChar(popupKeyboardView.getSelectedChar());
                dismissPopup();
            } else if (popupLongPressFired){
                // Held a key whose popup has no real alternates: type its default char.
                commitPopupChar(key.info.popupChars[key.info.popupDefaultIndex]);
            } else {
                submitKeyEvent();
            }
        }
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

    private void scheduleLongPress(){
        cancelPopupLongPress();
        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                popupLongPressFired = true;
                if (key.info.popupChars != null && key.info.popupChars.length >= 2){
                    showPopup();
                }
            }
        };
        longPressHandler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
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

    private void showPopup(){
        if (key.info.popupChars == null || key.info.popupChars.length < 2){
            return;
        }
        float cellW = getWidth();
        float cellH = getHeight();
        int columns = Math.max(1, key.info.popupColumns);
        int rowCount = (int) Math.ceil(key.info.popupChars.length / (float) columns);
        int defaultCol = key.info.popupDefaultIndex % columns;

        int[] screenLoc = new int[2];
        getLocationOnScreen(screenLoc);
        int[] windowLoc = new int[2];
        getLocationInWindow(windowLoc);

        int popupW = (int) (columns * cellW);
        int popupH = (int) (rowCount * cellH);

        // Anchor the popup so its default cell sits directly above the key (column-aligned),
        // with the bottom row just above the key top. Resting the finger then keeps the default
        // selected; sliding up/sideways moves the highlight. These are screen-absolute coords.
        int originX = (int) (screenLoc[0] - defaultCol * cellW);
        int originY = (int) (screenLoc[1] - rowCount * cellH);

        // Keep the popup fully on screen horizontally.
        int screenW = getResources().getDisplayMetrics().widthPixels;
        if (originX + popupW > screenW) originX = screenW - popupW;
        if (originX < 0) originX = 0;

        popupKeyboardView = new PopupKeyboardView(getContext(), uiTheme);
        popupKeyboardView.configure(key.info, cellW, cellH, originX, originY);

        popupWindow = new PopupWindow(popupKeyboardView, popupW, popupH, false);
        popupWindow.setClippingEnabled(false);
        popupWindow.setTouchable(false);
        popupWindow.setFocusable(false);
        popupWindow.setBackgroundDrawable(null);

        // showAtLocation wants window coordinates; convert from screen by removing the window's
        // on-screen offset (screenLoc - windowLoc).
        int winX = originX - (screenLoc[0] - windowLoc[0]);
        int winY = originY - (screenLoc[1] - windowLoc[1]);
        try {
            popupWindow.showAtLocation(this, Gravity.NO_GRAVITY, winX, winY);
        } catch (Exception e){
            // If the window token isn't ready yet, fall back to typing the default on release.
            popupWindow = null;
            popupKeyboardView = null;
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
        if (uiTheme.enablePreview && hasPopup()){
            // Popup keys: just brighten on press. The alternates grid drawn above the key is
            // the real preview, so we skip the big "lift" to keep the key's on-screen position
            // stable (the popup is anchored to it).
            isPreviewActive = true;
            invalidate();
        } else if (uiTheme.enablePreview){
            isPreviewActive = true;
            this.setTranslationY(-200.0f);
            this.setScaleX(1.2f);
            this.setScaleY(1.2f);
            this.setElevation(21.0f);
            invalidate();
        } else {
            this.setAlpha(.1f);
        }
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

    private void setCurrentLabel(String nextLabel) {
        if (nextLabel != currentLabel){
            currentLabel = nextLabel;
            this.invalidate();
        }
    }
}
