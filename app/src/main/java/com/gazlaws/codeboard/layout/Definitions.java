package com.gazlaws.codeboard.layout;

import android.content.Context;

import com.gazlaws.codeboard.R;
import com.gazlaws.codeboard.layout.builder.KeyboardLayoutBuilder;

public class Definitions {
    private Context context;
    private static final int CODE_ESCAPE = -2;
    private static final int CODE_SYMBOLS = -1;

    public Definitions(Context current) {
        this.context = current;
    }

    public void addArrowsRow(KeyboardLayoutBuilder keyboard) {
        int CODE_ARROW_LEFT = 5000;
        int CODE_ARROW_DOWN = 5001;
        int CODE_ARROW_UP = 5002;
        int CODE_ARROW_RIGHT = 5003;
        keyboard.newRow()
                .addKey("Esc", CODE_ESCAPE)
                .addTabKey()
                .addKey(context.getDrawable(R.drawable.ic_keyboard_arrow_left_24dp), CODE_ARROW_LEFT).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_keyboard_arrow_down_24dp), CODE_ARROW_DOWN).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_keyboard_arrow_up_24dp), CODE_ARROW_UP).asRepeatable()
                .addKey(context.getDrawable(R.drawable.ic_keyboard_arrow_right_24dp), CODE_ARROW_RIGHT).asRepeatable()
                .addKey("SYM", CODE_SYMBOLS).onCtrlShow("CLIP")
        ;
    }

    public void addCopyPasteRow(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey("Esc", CODE_ESCAPE)
                .addTabKey()
                .addKey(context.getDrawable(R.drawable.ic_select_all_24dp), 53737)
                .addKey(context.getDrawable(R.drawable.ic_cut_24dp), 53738)
                .addKey(context.getDrawable(R.drawable.ic_copy_24dp), 53739)
                .addKey(context.getDrawable(R.drawable.ic_paste_24dp), 53740)
                .addKey("SYM", CODE_SYMBOLS).onCtrlShow("CLIP")
        ;
    }


    public static void addCustomRow(KeyboardLayoutBuilder keyboard, String symbols) {
        keyboard.newRow();
        char[] chars = symbols.toCharArray();
        for (char aChar : chars) keyboard.addKey(aChar);
    }


    public static void addQwertyRows(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey('q').onShiftUppercase()
                .addKey('w').onShiftUppercase()
                .addKey('e').onShiftUppercase()
                .addKey('r').onShiftUppercase()
                .addKey('t').onShiftUppercase()
                .addKey('y').onShiftUppercase()
                .addKey('u').onShiftUppercase()
                .addKey('i').onShiftUppercase()
                .addKey('o').onShiftUppercase()
                .addKey('p').onShiftUppercase()
                .newRow()
                .addKey('a').onShiftUppercase().withSize(1.5f)
                .addKey('s').onShiftUppercase()
                .addKey('d').onShiftUppercase()
                .addKey('f').onShiftUppercase()
                .addKey('g').onShiftUppercase()
                .addKey('h').onShiftUppercase()
                .addKey('j').onShiftUppercase()
                .addKey('k').onShiftUppercase()
                .addKey('l').onShiftUppercase().withSize(1.5f)
                .newRow()
                .addShiftKey()
                .addKey('z').onShiftUppercase()
                .addKey('x').onShiftUppercase()
                .addKey('c').onShiftUppercase()
                .addKey('v').onShiftUppercase()
                .addKey('b').onShiftUppercase()
                .addKey('n').onShiftUppercase()
                .addKey('m').onShiftUppercase()
                .addBackspaceKey()
        ;
    }

    public static void addQwertzRows(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey('q').onShiftUppercase()
                .addKey('w').onShiftUppercase()
                .addKey('e').onShiftUppercase()
                .addKey('r').onShiftUppercase()
                .addKey('t').onShiftUppercase()
                .addKey('z').onShiftUppercase()
                .addKey('u').onShiftUppercase()
                .addKey('i').onShiftUppercase()
                .addKey('o').onShiftUppercase()
                .addKey('p').onShiftUppercase()
                .newRow()
                .addKey('a').onShiftUppercase().withSize(1.5f)
                .addKey('s').onShiftUppercase()
                .addKey('d').onShiftUppercase()
                .addKey('f').onShiftUppercase()
                .addKey('g').onShiftUppercase()
                .addKey('h').onShiftUppercase()
                .addKey('j').onShiftUppercase()
                .addKey('k').onShiftUppercase()
                .addKey('l').onShiftUppercase().withSize(1.5f)
                .newRow()
                .addShiftKey()
                .addKey('y').onShiftUppercase()
                .addKey('x').onShiftUppercase()
                .addKey('c').onShiftUppercase()
                .addKey('v').onShiftUppercase()
                .addKey('b').onShiftUppercase()
                .addKey('n').onShiftUppercase()
                .addKey('m').onShiftUppercase()
                .addBackspaceKey()
        ;
    }

    public static void addAzertyRows(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey('a').onShiftUppercase()
                .addKey('z').onShiftUppercase()
                .addKey('e').onShiftUppercase()
                .addKey('r').onShiftUppercase()
                .addKey('t').onShiftUppercase()
                .addKey('y').onShiftUppercase()
                .addKey('u').onShiftUppercase()
                .addKey('i').onShiftUppercase()
                .addKey('o').onShiftUppercase()
                .addKey('p').onShiftUppercase()
                .newRow()
                .addKey('q').onShiftUppercase()
                .addKey('s').onShiftUppercase()
                .addKey('d').onShiftUppercase()
                .addKey('f').onShiftUppercase()
                .addKey('g').onShiftUppercase()
                .addKey('h').onShiftUppercase()
                .addKey('j').onShiftUppercase()
                .addKey('k').onShiftUppercase()
                .addKey('l').onShiftUppercase()
                .addKey('m').onShiftUppercase()
                .addBackspaceKey()
                .newRow()
                .addShiftKey()
                .addKey('w').onShiftUppercase()
                .addKey('x').onShiftUppercase()
                .addKey('c').onShiftUppercase()
                .addKey('v').onShiftUppercase()
                .addKey('b').onShiftUppercase()
                .addKey('n').onShiftUppercase()
                .addKey('!').withSize(.8f)
                .addKey('?').withSize(.8f)
                .addTabKey();
    }

    public static void addDvorakRows(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey('!')
                .addKey('p').onShiftUppercase()
                .addKey('y').onShiftUppercase()
                .addKey('f').onShiftUppercase()
                .addKey('g').onShiftUppercase()
                .addKey('c').onShiftUppercase()
                .addKey('r').onShiftUppercase()
                .addKey('l').onShiftUppercase()
                .addEnterKey()
                .newRow()
                .addKey('a').onShiftUppercase()
                .addKey('o').onShiftUppercase()
                .addKey('e').onShiftUppercase()
                .addKey('u').onShiftUppercase()
                .addKey('i').onShiftUppercase()
                .addKey('d').onShiftUppercase()
                .addKey('h').onShiftUppercase()
                .addKey('t').onShiftUppercase()
                .addKey('n').onShiftUppercase()
                .addKey('s').onShiftUppercase()
                .addBackspaceKey()
                .newRow()
                .addShiftKey()
                .addKey('q').onShiftUppercase()
                .addKey('j').onShiftUppercase()
                .addKey('k').onShiftUppercase()
                .addKey('x').onShiftUppercase()
                .addKey('b').onShiftUppercase()
                .addKey('m').onShiftUppercase()
                .addKey('w').onShiftUppercase()
                .addKey('v').onShiftUppercase()
                .addKey('z').onShiftUppercase()
        ;
    }

    public void addSymbolRows(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey("Home", -18)
                .addKey("End", -19)
                .addKey("Del", -21)
                .addKey("PgUp", -22)
                .addKey("PgDn", -23)
                .newRow()
                .addShiftKey()
                .addKey("F1", -6)
                .addKey("F2", -7)
                .addKey("F3", -8)
                .addKey("F4", -9)
                .addKey("F5", -10)
                .addKey("F6", -11)
                .addKey("F7", -12)
                .addBackspaceKey()
                .newRow()
                .addKey("Ctrl", 17).asModifier().onCtrlShow("CTRL")
                .addKey("F8", -13)
                .addKey("F9", -14)
                .addKey("F10", -15)
                .addKey(context.getDrawable(R.drawable.ic_space_bar_24dp), 32).withSize(2f)
                .addKey("F11", -16)
                .addKey("F12", -17)
                .addEnterKey()
        ;

    }

    public void addClipboardActions(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey(context.getDrawable(R.drawable.ic_select_all_24dp), 53737)
                .addKey(context.getDrawable(R.drawable.ic_cut_24dp), 53738)
                .addKey(context.getDrawable(R.drawable.ic_copy_24dp), 53739)
                .addKey(context.getDrawable(R.drawable.ic_paste_24dp), 53740)
                .addKey(context.getDrawable(R.drawable.ic_undo_24dp), 53741)
                .addKey(context.getDrawable(R.drawable.ic_redo_24dp), 53742)
        ;
    }

    public void addCustomSpaceRow(KeyboardLayoutBuilder keyboard, String symbols) {
        char[] chars = symbols.toCharArray();

        keyboard.newRow().addKey("Ctrl", 17).asModifier().onCtrlShow("CTRL");

        for (int i = 0; i < (chars.length + 1) / 2 && chars.length > 0; i++) {
            keyboard.addKey(chars[i]).withSize(.7f);
        }
        keyboard.addKey(context.getDrawable(R.drawable.ic_space_bar_24dp), 32).withSize(2f);
        for (int i = (chars.length + 1) / 2; i < chars.length; i++) {
            keyboard.addKey(chars[i]).withSize(.7f);
        }
        keyboard.addEnterKey();

    }

    // Gboard-style QWERTY: every letter carries a corner symbol and a long-press popup.
    // Popup chars are listed in on-screen order (top-left to bottom-right); the second
    // argument of withPopup is the default (typed on a hold-and-lift without sliding).
    public static void addGboardQwertyRows(KeyboardLayoutBuilder keyboard) {
        addGboardQwertyRows(keyboard, false);
    }

    // When split is true, a central gap pushes each row into two halves (for thumb typing on
    // wide screens) and the inner letters G and V are duplicated so both halves have one. When
    // not split, small end gaps inset the home row. The bottom row (addGboardBottomRow) is added
    // separately and never splits.
    public static void addGboardQwertyRows(KeyboardLayoutBuilder keyboard, boolean split) {
        float centerGap = 1.5f; // width of the central split gap, in key-widths (tweakable)

        // Row 1: q w e r t | y u i o p
        keyboard.newRow()
                .addKey('q').onShiftUppercase().withPopup(2, "%", "%", "ʔ")
                .addKey('w').onShiftUppercase().withPopup(1, "\\", "\\")
                .addKey('e').onShiftUppercase().withPopup(6, "|", "ę","ë","ē","ė","ə","ɛ̃", "è","|","é","ê","ɜ","ɛ")
                .addKey('r').onShiftUppercase().withPopup(5, "=", "=","ʁ","ɹ","ɾ","ʀ")
                .addKey('t').onShiftUppercase().withPopup(2, "[", "[", "θ");
        if (split) keyboard.addGap(centerGap);
        keyboard.addKey('y').onShiftUppercase().withPopup(5, "]", "ʏ","ij","]","ÿ","ý")
                .addKey('u').onShiftUppercase().withPopup(4, "<", "ũ","ù","ū","ʊ", "û","<","ú","ü")
                .addKey('i').onShiftUppercase().withPopup(5, ">", "ɪ","ij","į","ì","ĩ", "ī","ï","î",">","í")
                .addKey('o').onShiftUppercase().withPopup(6, "{", "ɔ̃","œ̃","õ","ō","ø","ò", "ɔ","œ","ö","ô","ó","{")
                .addKey('p').onShiftUppercase().withPopup(1, "}", "}");

        // Row 2: a s d f g | g h j k l   (G duplicated when split; small end gaps otherwise)
        keyboard.newRow();
        if (!split) keyboard.addGap(0.5f);
        keyboard.addKey('a').onShiftUppercase().withPopup(6, "@", "æ","ã","å","ā","ɒ","ɑ", "@","à","á","â","ä","ɑ̃")
                .addKey('s').onShiftUppercase().withPopup(3, "#", "#","ß","ʃ")
                .addKey('d').onShiftUppercase().withPopup(6, "€", "€","$","£","¥","¢","ð")
                .addKey('f').onShiftUppercase().withPopup(1, "-", "-")
                .addKey('g').onShiftUppercase().withPopup(2, "&", "&","ɣ");
        if (split) keyboard.addGap(centerGap).addKey('g').onShiftUppercase().withPopup(2, "&", "&","ɣ");
        keyboard.addKey('h').onShiftUppercase().withPopup(2, "-", "-","ɦ")
                .addKey('j').onShiftUppercase().withPopup(2, "+", "+","j́")
                .addKey('k').onShiftUppercase().withPopup(1, "(", "(")
                .addKey('l').onShiftUppercase().withPopup(1, ")", ")");
        if (!split) keyboard.addGap(0.5f);

        // Row 3: shift z x c v | v b n m backspace   (V duplicated when split)
        keyboard.newRow()
                .addShiftKey()
                .addKey('z').onShiftUppercase().withPopup(2, "*", "*","ʒ")
                .addKey('x').onShiftUppercase().withPopup(1, "\"", "\"")
                .addKey('c').onShiftUppercase().withPopup(4, "'", "ć","'","ç","č")
                .addKey('v').onShiftUppercase().withPopup(2, ":", ":","ʌ");
        if (split) keyboard.addGap(centerGap).addKey('v').onShiftUppercase().withPopup(2, ":", ":","ʌ");
        keyboard.addKey('b').onShiftUppercase().withPopup(1, ";", ";")
                .addKey('n').onShiftUppercase().withPopup(5, "!", "ń","!","ñ","ŋ","ɲ")
                .addKey('m').onShiftUppercase().withPopup(1, "?", "?")
                .addBackspaceKey();
    }

    // Bottom row for the Gboard QWERTY: Ctrl, comma, space, period, enter.
    // Period carries the punctuation popup; comma carries the IPA stress/length marks.
    public void addGboardBottomRow(KeyboardLayoutBuilder keyboard) {
        keyboard.newRow()
                .addKey("Ctrl", 17).asModifier().onCtrlShow("CTRL")
                .addKey(',').withPopupNoCorner(3, "ː", "ː","ˈ","ˌ")
                .addKey(context.getDrawable(R.drawable.ic_space_bar_24dp), 32).withSize(4f)
                .addKey('.').withPopupNoCorner(6, ",",
                        "&","%","+","·","\"","_",
                        ";","/","-",":","'","@",
                        "(",")","#","!",",","?")
                .addEnterKey()
        ;
    }

}
