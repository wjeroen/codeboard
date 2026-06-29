package com.gazlaws.codeboard.layout;

import android.content.Context;

import com.gazlaws.codeboard.R;
import com.gazlaws.codeboard.layout.builder.KeyboardLayoutBuilder;

public class Definitions {
    private Context context;
    private static final int CODE_ESCAPE = -2;
    private static final int CODE_SYMBOLS = -1;
    // Width of the central gap in split mode (in key-widths). Shared by the letter rows and the
    // (non-splitting) bottom row so the spacebar can absorb exactly this much when split.
    private static final float SPLIT_CENTER_GAP = 1.5f;

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


    public static void addCustomRow(KeyboardLayoutBuilder keyboard, String symbols, boolean split) {
        keyboard.newRow();
        char[] chars = symbols.toCharArray();
        for (char aChar : chars) keyboard.addKey(aChar);
        // Split rows of up to 12 keys down the middle; longer rows stay full-width.
        if (split) keyboard.splitCurrentRow(SPLIT_CENTER_GAP, 12);
    }

    // Fixed number row (1-0): no corner symbols (Gboard doesn't either), but each digit long-
    // presses to its superscript (the default) plus common fractions, mirroring Gboard. Splits
    // 1 2 3 4 5 | 6 7 8 9 0. Default is the first cell for 1-5 and the last cell for 6-0.
    public static void addGboardNumberRow(KeyboardLayoutBuilder keyboard, boolean split) {
        keyboard.newRow()
                .addKey('1').withPopupNoCorner(5, "¹", "⅙","⅐","⅛","⅑","⅒", "¹","½","⅓","¼","⅕")
                .addKey('2').withPopupNoCorner(3, "²", "²","⅖","⅔")
                .addKey('3').withPopupNoCorner(4, "³", "³","⅗","¾","⅜")
                .addKey('4').withPopupNoCorner(2, "⁴", "⁴","⅘")
                .addKey('5').withPopupNoCorner(3, "⁵", "⁵","⅝","⅚");
        if (split) keyboard.addGap(SPLIT_CENTER_GAP);
        keyboard.addKey('6').withPopupNoCorner(1, "⁶", "⁶")
                .addKey('7').withPopupNoCorner(2, "⁷", "⅞","⁷")
                .addKey('8').withPopupNoCorner(1, "⁸", "⁸")
                .addKey('9').withPopupNoCorner(1, "⁹", "⁹")
                .addKey('0').withPopupNoCorner(2, "⁰", "∅","⁰");
    }

    // --- Per-letter long-press, split into shared DATA and per-layout ARRANGEMENT ---------------
    //
    // The accent options are a property of the LETTER, not its position, so they are shared across
    // every layout (letterSymbol + letterAccents below). The ARRANGEMENT (how many columns, and
    // which cell is the default) is the part that should depend on where the key sits, so it is
    // kept separate: addLetterKey() is the single seam to make it per-layout later (e.g. put the
    // default on the side nearest the key, or reorder symbols per layout). For now every non-QWERTY
    // layout uses one generic arrangement here; QWERTY keeps its own hand-tuned arrangement in
    // addGboardQwertyRows (the reference). See TODO.md.

    /** The corner/default symbol for a letter (layout-independent), or null for none. */
    private static String letterSymbol(char c) {
        switch (c) {
            case 'q': return "%"; case 'w': return "\\"; case 'e': return "|"; case 'r': return "=";
            case 't': return "["; case 'y': return "]"; case 'u': return "<"; case 'i': return ">";
            case 'o': return "{"; case 'p': return "}"; case 'a': return "@"; case 's': return "#";
            case 'd': return "€"; case 'f': return "-"; case 'g': return "&"; case 'h': return "-";
            case 'j': return "+"; case 'k': return "("; case 'l': return ")"; case 'z': return "*";
            case 'x': return "\""; case 'c': return "'"; case 'v': return ":"; case 'b': return ";";
            case 'n': return "!"; case 'm': return "?";
            default: return null;
        }
    }

    /** The accent alternates for a letter (layout-independent), without the symbol. */
    private static String[] letterAccents(char c) {
        switch (c) {
            case 'q': return new String[]{"ʔ"};
            case 'e': return new String[]{"ę","ë","ē","ė","ə","ɛ̃","è","é","ê","ɜ","ɛ"};
            case 'r': return new String[]{"ʁ","ɹ","ɾ","ʀ"};
            case 't': return new String[]{"θ"};
            case 'y': return new String[]{"ʏ","ij","ÿ","ý"};
            case 'u': return new String[]{"ũ","ù","ū","ʊ","û","ú","ü"};
            case 'i': return new String[]{"ɪ","ij","į","ì","ĩ","ī","ï","î","í"};
            case 'o': return new String[]{"ɔ̃","œ̃","õ","ō","ø","ò","ɔ","œ","ö","ô","ó"};
            case 'a': return new String[]{"æ","ã","å","ā","ɒ","ɑ","à","á","â","ä","ɑ̃"};
            case 's': return new String[]{"ß","ʃ"};
            case 'd': return new String[]{"$","£","¥","¢","ð"};
            case 'g': return new String[]{"ɣ"};
            case 'h': return new String[]{"ɦ"};
            case 'j': return new String[]{"j́"};
            case 'z': return new String[]{"ʒ"};
            case 'c': return new String[]{"ć","ç","č"};
            case 'v': return new String[]{"ʌ"};
            case 'n': return new String[]{"ń","ñ","ŋ","ɲ"};
            default: return new String[0];
        }
    }

    /** Adds a letter key with the shared symbol + accents in a GENERIC arrangement (symbol is the
     *  default, then the accents, up to 6 columns). This arrangement is the per-layout seam; QWERTY
     *  overrides it with its own hand-tuned positions in addGboardQwertyRows. */
    public static KeyboardLayoutBuilder addLetterKey(KeyboardLayoutBuilder kb, char c) {
        kb.addKey(c).onShiftUppercase();
        String symbol = letterSymbol(c);
        if (symbol == null) {
            return kb;
        }
        String[] accents = letterAccents(c);
        String[] chars = new String[1 + accents.length];
        chars[0] = symbol;
        System.arraycopy(accents, 0, chars, 1, accents.length);
        int columns = Math.min(chars.length, 6);
        return kb.withPopup(columns, symbol, chars);
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

    // QWERTZ: structurally identical to the Gboard QWERTY (Z and Y swapped), same treatment
    // (corner symbols, popups, home-row gaps, G/V duplication on split).
    public static void addQwertzRows(KeyboardLayoutBuilder keyboard, boolean split) {
        float centerGap = SPLIT_CENTER_GAP;
        float shiftSize = split ? 1.0f : 1.5f;
        keyboard.newRow();
        addLetterKey(keyboard, 'q'); addLetterKey(keyboard, 'w'); addLetterKey(keyboard, 'e');
        addLetterKey(keyboard, 'r'); addLetterKey(keyboard, 't');
        if (split) keyboard.addGap(centerGap);
        addLetterKey(keyboard, 'z'); addLetterKey(keyboard, 'u'); addLetterKey(keyboard, 'i');
        addLetterKey(keyboard, 'o'); addLetterKey(keyboard, 'p');

        keyboard.newRow();
        if (!split) keyboard.addGap(0.5f);
        addLetterKey(keyboard, 'a'); addLetterKey(keyboard, 's'); addLetterKey(keyboard, 'd');
        addLetterKey(keyboard, 'f'); addLetterKey(keyboard, 'g');
        if (split) { keyboard.addGap(centerGap); addLetterKey(keyboard, 'g'); }
        addLetterKey(keyboard, 'h'); addLetterKey(keyboard, 'j'); addLetterKey(keyboard, 'k');
        addLetterKey(keyboard, 'l');
        if (!split) keyboard.addGap(0.5f);

        keyboard.newRow().addShiftKey().withSize(shiftSize);
        addLetterKey(keyboard, 'y'); addLetterKey(keyboard, 'x'); addLetterKey(keyboard, 'c');
        addLetterKey(keyboard, 'v');
        if (split) { keyboard.addGap(centerGap); addLetterKey(keyboard, 'v'); }
        addLetterKey(keyboard, 'b'); addLetterKey(keyboard, 'n'); addLetterKey(keyboard, 'm');
        keyboard.addBackspaceKey().withSize(shiftSize);
    }

    // AZERTY with the Gboard treatment (corner symbols + popups). Splits each row down the middle
    // (generic midpoint, no key duplication). The old !/?/Tab tail of the last row is dropped: !
    // and ? live on the period popup, Tab on the top action row.
    public static void addAzertyRows(KeyboardLayoutBuilder keyboard, boolean split) {
        float shiftSize = split ? 1.0f : 1.5f;
        keyboard.newRow();
        addLetterKey(keyboard, 'a'); addLetterKey(keyboard, 'z'); addLetterKey(keyboard, 'e');
        addLetterKey(keyboard, 'r'); addLetterKey(keyboard, 't'); addLetterKey(keyboard, 'y');
        addLetterKey(keyboard, 'u'); addLetterKey(keyboard, 'i'); addLetterKey(keyboard, 'o');
        addLetterKey(keyboard, 'p');
        if (split) keyboard.splitCurrentRow(SPLIT_CENTER_GAP, 12);

        keyboard.newRow();
        addLetterKey(keyboard, 'q'); addLetterKey(keyboard, 's'); addLetterKey(keyboard, 'd');
        addLetterKey(keyboard, 'f'); addLetterKey(keyboard, 'g'); addLetterKey(keyboard, 'h');
        addLetterKey(keyboard, 'j'); addLetterKey(keyboard, 'k'); addLetterKey(keyboard, 'l');
        addLetterKey(keyboard, 'm');
        if (split) keyboard.splitCurrentRow(SPLIT_CENTER_GAP, 12);

        keyboard.newRow().addShiftKey().withSize(shiftSize);
        addLetterKey(keyboard, 'w'); addLetterKey(keyboard, 'x'); addLetterKey(keyboard, 'c');
        addLetterKey(keyboard, 'v'); addLetterKey(keyboard, 'b'); addLetterKey(keyboard, 'n');
        keyboard.addBackspaceKey().withSize(shiftSize);
        if (split) keyboard.splitCurrentRow(SPLIT_CENTER_GAP, 12);
    }

    // Dvorak with the Gboard treatment (corner symbols + popups), generic midpoint split. The
    // leading "!" stays a plain key; the row-1 Enter is dropped (the shared bottom row has Enter).
    public static void addDvorakRows(KeyboardLayoutBuilder keyboard, boolean split) {
        float shiftSize = split ? 1.0f : 1.5f;
        keyboard.newRow().addKey('!');
        addLetterKey(keyboard, 'p'); addLetterKey(keyboard, 'y'); addLetterKey(keyboard, 'f');
        addLetterKey(keyboard, 'g'); addLetterKey(keyboard, 'c'); addLetterKey(keyboard, 'r');
        addLetterKey(keyboard, 'l');
        if (split) keyboard.splitCurrentRow(SPLIT_CENTER_GAP, 12);

        keyboard.newRow();
        addLetterKey(keyboard, 'a'); addLetterKey(keyboard, 'o'); addLetterKey(keyboard, 'e');
        addLetterKey(keyboard, 'u'); addLetterKey(keyboard, 'i'); addLetterKey(keyboard, 'd');
        addLetterKey(keyboard, 'h'); addLetterKey(keyboard, 't'); addLetterKey(keyboard, 'n');
        addLetterKey(keyboard, 's');
        keyboard.addBackspaceKey().withSize(shiftSize);
        if (split) keyboard.splitCurrentRow(SPLIT_CENTER_GAP, 12);

        keyboard.newRow().addShiftKey().withSize(shiftSize);
        addLetterKey(keyboard, 'q'); addLetterKey(keyboard, 'j'); addLetterKey(keyboard, 'k');
        addLetterKey(keyboard, 'x'); addLetterKey(keyboard, 'b'); addLetterKey(keyboard, 'm');
        addLetterKey(keyboard, 'w'); addLetterKey(keyboard, 'v'); addLetterKey(keyboard, 'z');
        if (split) keyboard.splitCurrentRow(SPLIT_CENTER_GAP, 12);
    }

    public void addSymbolRows(KeyboardLayoutBuilder keyboard, boolean split) {
        keyboard.newRow()
                .addKey("Home", -18)
                .addKey("End", -19)
                .addKey("Del", -21)
                .addKey("PgUp", -22)
                .addKey("PgDn", -23);
        if (split) keyboard.splitCurrentRow(SPLIT_CENTER_GAP, 12);
        keyboard.newRow()
                .addShiftKey()
                .addKey("F1", -6)
                .addKey("F2", -7)
                .addKey("F3", -8)
                .addKey("F4", -9)
                .addKey("F5", -10)
                .addKey("F6", -11)
                .addKey("F7", -12)
                .addBackspaceKey();
        if (split) keyboard.splitCurrentRow(SPLIT_CENTER_GAP, 12);
        // The F8-F12 row carries the spacebar, so it is left whole (splitting around the bar
        // looks odd); this is the "best effort" SYM-page split noted in TODO.
        keyboard.newRow()
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
        float centerGap = SPLIT_CENTER_GAP; // width of the central split gap, in key-widths
        // Shift and Backspace are equal width and sized so the z..m letters stay letter-width,
        // matching the home row total (10 normal, 11.5 split). Up-arrow icon on Shift (addShiftKey).
        float shiftSize = split ? 1.0f : 1.5f;

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
                .addShiftKey().withSize(shiftSize)
                .addKey('z').onShiftUppercase().withPopup(2, "*", "*","ʒ")
                .addKey('x').onShiftUppercase().withPopup(1, "\"", "\"")
                .addKey('c').onShiftUppercase().withPopup(4, "'", "ć","'","ç","č")
                .addKey('v').onShiftUppercase().withPopup(2, ":", ":","ʌ");
        if (split) keyboard.addGap(centerGap).addKey('v').onShiftUppercase().withPopup(2, ":", ":","ʌ");
        keyboard.addKey('b').onShiftUppercase().withPopup(1, ";", ";")
                .addKey('n').onShiftUppercase().withPopup(5, "!", "ń","!","ñ","ŋ","ɲ")
                .addKey('m').onShiftUppercase().withPopup(1, "?", "?")
                .addBackspaceKey().withSize(shiftSize);
    }

    // Bottom row for the Gboard QWERTY: Ctrl, comma, space, period, enter.
    // Period carries the punctuation popup; comma carries the IPA stress/length marks.
    public void addGboardBottomRow(KeyboardLayoutBuilder keyboard, boolean split) {
        // Symmetric bottom row: Ctrl and Enter are equal width (1.25), comma and period are letter
        // width (1.0), the spacebar takes the rest. The total matches the letter rows (10 normal,
        // 10 + the central gap split), so comma/period stay letter-width in both modes. Comma's
        // corner/hold-default is the backtick (rehomed from the old number row), with the IPA marks
        // kept as slide alternates; period shows its comma hold-default as a corner symbol.
        float rowTotal = split ? (10f + SPLIT_CENTER_GAP) : 10f;
        float ctrlEnterSize = 1.25f;
        float spaceSize = rowTotal - 2f * ctrlEnterSize - 2f; // Ctrl+Enter (2.5) + comma+period (2)
        keyboard.newRow()
                .addKey("Ctrl", 17).asModifier().onCtrlShow("CTRL").withSize(ctrlEnterSize)
                .addKey(',').withPopup(4, "`", "`","ː","ˈ","ˌ")
                .addKey(context.getDrawable(R.drawable.ic_space_bar_24dp), 32).withSize(spaceSize)
                .addKey('.').withPopup(6, ",",
                        "·","_","&","%","\"","+",
                        "-",":","@","'","/",";",
                        "(",")","#","!",",","?")
                .addEnterKey().withSize(ctrlEnterSize)
        ;
    }

}
