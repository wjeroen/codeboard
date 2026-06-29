package com.gazlaws.codeboard.layout.builder;


import android.graphics.drawable.Drawable;

/**
 * contains information on how to build up the real key
 */
public class KeyInfo {
    /**
     * key press code sent when pressing the key
     */
    public int code;

    /**
     * label is shown on the keyboard
     */
    public String label;

    /**
     * size relative to other keys in the same row
     */
    public float size;

    /**
     * Key can be held to repeat
     */
    public boolean isRepeatable;

    /**
     * This key is a modifier (Shift/Ctrl)
     */
    public boolean isModifier;

    /**
     * When key is pressed output this text
     */
    public String outputText;

    /**
     * When shift modifier is pressed, show this label instead
     */
    public String onShiftLabel;

    /**
     * When control modifier is pressed, show this label instead
     */
    public String onCtrlLabel;

    /**
     * Drawable is shown on the keyboard
     */
    public Drawable icon;

    /**
     * Small secondary symbol drawn in the top-right corner (Gboard-style)
     */
    public String cornerLabel;

    /**
     * Long-press popup alternates, in on-screen order (top-left to bottom-right).
     * Null or empty means the key has no long-press popup.
     */
    public String[] popupChars;

    /**
     * Index in popupChars selected when the finger lifts without sliding
     */
    public int popupDefaultIndex;

    /**
     * Number of columns the popup is laid out in
     */
    public int popupColumns;

    /**
     * Spacer/gap: reserves horizontal space in its row but renders no key (no view, no
     * touch target). Used to inset a row, e.g. half-key gaps on the ends of the home row.
     */
    public boolean isSpacer;
}
