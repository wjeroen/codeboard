package com.gazlaws.codeboard.layout;

import android.content.Context;

import com.gazlaws.codeboard.R;
import com.gazlaws.codeboard.layout.builder.KeyboardLayoutBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Definitions {
    private Context context;
    private static final int CODE_ESCAPE = -2;
    private static final int CODE_SYMBOLS = -1;

    // Action codes for the special keys that can live on customizable rows (via the {token}
    // syntax, see addCustomRow). Handled in CodeBoardIME.onKey. The 537xx range continues the
    // existing select-all/cut/copy/paste/undo/redo block (53737..53742).
    public static final int CODE_SETTINGS = 53743;       // open the app's settings screen
    public static final int CODE_EMOJI_PAGE = 53744;     // open the emoji page
    public static final int CODE_SCROLL_UP = 53745;      // one mouse-wheel-like scroll click up
    public static final int CODE_SCROLL_DOWN = 53746;    // one mouse-wheel-like scroll click down
    public static final int CODE_GHOST_MODE = 53747;     // toggle the transparent non-pushing keyboard
    public static final int CODE_CLIPBOARD_PAGE = 53748; // open the clipboard-pins page directly
    // Split-mode stagger inset: the home/bottom letter rows are inset by this many key-widths at
    // each outer end, and Shift/Backspace (plus the bottom row's Ctrl/Enter) grow by the same
    // amount so the columns line up. About 2/5 of a key, per the Gboard reference. One knob to tune.
    private static final float SPLIT_END_SPACER = 0.4f;
    // Split mode is signalled by the central-gap FRACTION passed to each row method (splitGap <= 0
    // means "not split"). The fraction is computed per-build in CodeBoardIME so that each half of
    // the keyboard is capped at 5 key-heights wide (the gap absorbs the rest on very wide screens);
    // see CodeBoardIME.computeSplitGapFraction. Rows reserve the gap as a fixed fraction of their
    // width (KeyboardLayoutRowBuilder), so it stays identical across rows regardless of key count.

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


    // --- Customizable rows with special-key tokens ----------------------------------------------
    //
    // Every editable row (main top/second, the SYM rows, the SYM-page bottom row) accepts special
    // keys through a token syntax: any {token} from TOKENS below becomes that action key, every
    // other character stays a plain typing key. Unknown tokens and lone braces fall back to
    // literal characters, so an existing row like the default "\\|/[]{}<>:" builds exactly as
    // before. This is the "foundation" from TODO.md: the letter rows, Shift/Ctrl and the fixed
    // bottom row stay hardcoded.

    /** Every recognised token name (lowercase, without braces). Keep in sync with addTokenKey. */
    private static final Set<String> TOKENS = new HashSet<>(Arrays.asList(
            "esc", "tab", "sym", "clip", "left", "right", "up", "down", "home", "end",
            "pgup", "pgdn", "ins", "del", "selectall", "cut", "copy", "paste", "undo", "redo",
            "space", "enter", "bksp", "backspace", "settings", "emoji", "scrollup", "scrolldown",
            "ghost", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10", "f11", "f12"));

    private static boolean isKnownToken(String token) {
        return TOKENS.contains(token.toLowerCase(Locale.ROOT));
    }

    /**
     * Splits a custom-row string into units: one "{token}" unit per recognised token, otherwise
     * one unit per character. A brace that does not open a known token stays a literal character.
     */
    private static List<String> parseUnits(String symbols) {
        List<String> units = new ArrayList<>();
        int i = 0;
        while (i < symbols.length()) {
            char c = symbols.charAt(i);
            if (c == '{') {
                int close = symbols.indexOf('}', i + 1);
                if (close > i + 1 && isKnownToken(symbols.substring(i + 1, close))) {
                    units.add(symbols.substring(i, close + 1));
                    i = close + 1;
                    continue;
                }
            }
            units.add(String.valueOf(c));
            i++;
        }
        return units;
    }

    /** Adds one parsed unit to the current row: a special key for a "{token}" unit, otherwise a
     *  plain character key (digits keep their Gboard fraction popup). */
    private void addUnitKey(KeyboardLayoutBuilder keyboard, String unit) {
        if (unit.length() > 1 && unit.charAt(0) == '{') {
            addTokenKey(keyboard, unit.substring(1, unit.length() - 1).toLowerCase(Locale.ROOT));
        } else {
            char aChar = unit.charAt(0);
            keyboard.addKey(aChar);
            addDigitFractionPopup(keyboard, aChar); // digits get Gboard's superscript/fraction popup
        }
    }

    /** The action-key registry: builds the key a token stands for. Keep TOKENS in sync. */
    private void addTokenKey(KeyboardLayoutBuilder kb, String token) {
        switch (token) {
            case "esc": kb.addKey("Esc", CODE_ESCAPE); break;
            case "tab": kb.addTabKey(); break;
            case "sym": kb.addKey("SYM", CODE_SYMBOLS).onCtrlShow("CLIP"); break;
            case "clip": kb.addKey("CLIP", CODE_CLIPBOARD_PAGE); break;
            case "left": kb.addKey(context.getDrawable(R.drawable.ic_keyboard_arrow_left_24dp), 5000).asRepeatable(); break;
            case "down": kb.addKey(context.getDrawable(R.drawable.ic_keyboard_arrow_down_24dp), 5001).asRepeatable(); break;
            case "up": kb.addKey(context.getDrawable(R.drawable.ic_keyboard_arrow_up_24dp), 5002).asRepeatable(); break;
            case "right": kb.addKey(context.getDrawable(R.drawable.ic_keyboard_arrow_right_24dp), 5003).asRepeatable(); break;
            case "home": kb.addKey("Home", -18); break;
            case "end": kb.addKey("End", -19); break;
            case "ins": kb.addKey("Ins", -20); break;
            case "del": kb.addKey("Del", -21).asRepeatable(); break;
            case "pgup": kb.addKey("PgUp", -22).asRepeatable(); break;
            case "pgdn": kb.addKey("PgDn", -23).asRepeatable(); break;
            case "selectall": kb.addKey(context.getDrawable(R.drawable.ic_select_all_24dp), 53737); break;
            case "cut": kb.addKey(context.getDrawable(R.drawable.ic_cut_24dp), 53738); break;
            case "copy": kb.addKey(context.getDrawable(R.drawable.ic_copy_24dp), 53739); break;
            case "paste": kb.addKey(context.getDrawable(R.drawable.ic_paste_24dp), 53740); break;
            case "undo": kb.addKey(context.getDrawable(R.drawable.ic_undo_24dp), 53741); break;
            case "redo": kb.addKey(context.getDrawable(R.drawable.ic_redo_24dp), 53742); break;
            case "space": kb.addKey(context.getDrawable(R.drawable.ic_space_bar_24dp), 32); break;
            case "enter": kb.addKey(context.getDrawable(R.drawable.ic_keyboard_return_24dp), -4); break;
            case "bksp":
            case "backspace": kb.addBackspaceKey(); break;
            case "settings": kb.addKey(context.getDrawable(R.drawable.ic_settings_24dp), CODE_SETTINGS); break;
            case "emoji": kb.addKey(context.getDrawable(R.drawable.ic_emoji_24dp), CODE_EMOJI_PAGE); break;
            case "scrollup": kb.addKey(context.getDrawable(R.drawable.ic_scroll_up_24dp), CODE_SCROLL_UP).asRepeatable(); break;
            case "scrolldown": kb.addKey(context.getDrawable(R.drawable.ic_scroll_down_24dp), CODE_SCROLL_DOWN).asRepeatable(); break;
            case "ghost": kb.addKey("👻", CODE_GHOST_MODE); break;
            default:
                // f1..f12 (F1 = -6 ... F12 = -17, the existing SYM-page codes)
                if (token.length() >= 2 && token.charAt(0) == 'f') {
                    int n = Integer.parseInt(token.substring(1));
                    kb.addKey("F" + n, -5 - n);
                }
                break;
        }
    }

    public void addCustomRow(KeyboardLayoutBuilder keyboard, String symbols, float splitGap) {
        keyboard.newRow();
        for (String unit : parseUnits(symbols)) {
            addUnitKey(keyboard, unit);
        }
        // Split rows of up to 12 keys down the middle; longer rows stay full-width.
        if (splitGap > 0) keyboard.splitCurrentRow(splitGap, 12);
    }

    // Attaches Gboard's superscript + fraction long-press popup to a digit key (no-op for any other
    // character), so the fractions work on whatever editable row a digit lands in. No corner symbol
    // (Gboard doesn't show one either). Default is the superscript: first cell for 1-5, last for 6-0.
    private static void addDigitFractionPopup(KeyboardLayoutBuilder kb, char c) {
        switch (c) {
            case '1': kb.withPopupNoCorner(5, "¹", "⅙","⅐","⅛","⅑","⅒", "¹","½","⅓","¼","⅕"); break;
            case '2': kb.withPopupNoCorner(3, "²", "²","⅖","⅔"); break;
            case '3': kb.withPopupNoCorner(4, "³", "³","⅗","¾","⅜"); break;
            case '4': kb.withPopupNoCorner(2, "⁴", "⁴","⅘"); break;
            case '5': kb.withPopupNoCorner(3, "⁵", "⁵","⅝","⅚"); break;
            case '6': kb.withPopupNoCorner(1, "⁶", "⁶"); break;
            case '7': kb.withPopupNoCorner(2, "⁷", "⅞","⁷"); break;
            case '8': kb.withPopupNoCorner(1, "⁸", "⁸"); break;
            case '9': kb.withPopupNoCorner(1, "⁹", "⁹"); break;
            case '0': kb.withPopupNoCorner(3, "⁰", "ⁿ","∅","⁰"); break;
            default: break;
        }
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
    public static void addQwertzRows(KeyboardLayoutBuilder keyboard, float splitGap) {
        boolean split = splitGap > 0;
        float shiftSize = split ? 1.0f : 1.5f;
        keyboard.newRow();
        addLetterKey(keyboard, 'q'); addLetterKey(keyboard, 'w'); addLetterKey(keyboard, 'e');
        addLetterKey(keyboard, 'r'); addLetterKey(keyboard, 't');
        if (split) keyboard.addSplitGap(splitGap);
        addLetterKey(keyboard, 'z'); addLetterKey(keyboard, 'u'); addLetterKey(keyboard, 'i');
        addLetterKey(keyboard, 'o'); addLetterKey(keyboard, 'p');

        keyboard.newRow();
        if (!split) keyboard.addGap(0.5f);
        addLetterKey(keyboard, 'a'); addLetterKey(keyboard, 's'); addLetterKey(keyboard, 'd');
        addLetterKey(keyboard, 'f'); addLetterKey(keyboard, 'g');
        if (split) { keyboard.addSplitGap(splitGap); addLetterKey(keyboard, 'g'); }
        addLetterKey(keyboard, 'h'); addLetterKey(keyboard, 'j'); addLetterKey(keyboard, 'k');
        addLetterKey(keyboard, 'l');
        if (!split) keyboard.addGap(0.5f);

        keyboard.newRow().addShiftKey().withSize(shiftSize);
        addLetterKey(keyboard, 'y'); addLetterKey(keyboard, 'x'); addLetterKey(keyboard, 'c');
        addLetterKey(keyboard, 'v');
        if (split) { keyboard.addSplitGap(splitGap); addLetterKey(keyboard, 'v'); }
        addLetterKey(keyboard, 'b'); addLetterKey(keyboard, 'n'); addLetterKey(keyboard, 'm');
        keyboard.addBackspaceKey().withSize(shiftSize);
    }

    // AZERTY with the Gboard treatment (corner symbols + popups). Splits each row down the middle
    // (generic midpoint, no key duplication). The old !/?/Tab tail of the last row is dropped: !
    // and ? live on the period popup, Tab on the top action row.
    public static void addAzertyRows(KeyboardLayoutBuilder keyboard, float splitGap) {
        boolean split = splitGap > 0;
        float shiftSize = split ? 1.0f : 1.5f;
        keyboard.newRow();
        addLetterKey(keyboard, 'a'); addLetterKey(keyboard, 'z'); addLetterKey(keyboard, 'e');
        addLetterKey(keyboard, 'r'); addLetterKey(keyboard, 't'); addLetterKey(keyboard, 'y');
        addLetterKey(keyboard, 'u'); addLetterKey(keyboard, 'i'); addLetterKey(keyboard, 'o');
        addLetterKey(keyboard, 'p');
        if (split) keyboard.splitCurrentRow(splitGap, 12);

        keyboard.newRow();
        addLetterKey(keyboard, 'q'); addLetterKey(keyboard, 's'); addLetterKey(keyboard, 'd');
        addLetterKey(keyboard, 'f'); addLetterKey(keyboard, 'g'); addLetterKey(keyboard, 'h');
        addLetterKey(keyboard, 'j'); addLetterKey(keyboard, 'k'); addLetterKey(keyboard, 'l');
        addLetterKey(keyboard, 'm');
        if (split) keyboard.splitCurrentRow(splitGap, 12);

        keyboard.newRow().addShiftKey().withSize(shiftSize);
        addLetterKey(keyboard, 'w'); addLetterKey(keyboard, 'x'); addLetterKey(keyboard, 'c');
        addLetterKey(keyboard, 'v'); addLetterKey(keyboard, 'b'); addLetterKey(keyboard, 'n');
        keyboard.addBackspaceKey().withSize(shiftSize);
        if (split) keyboard.splitCurrentRow(splitGap, 12);
    }

    // Dvorak with the Gboard treatment (corner symbols + popups), generic midpoint split. The
    // leading "!" stays a plain key; the row-1 Enter is dropped (the shared bottom row has Enter).
    public static void addDvorakRows(KeyboardLayoutBuilder keyboard, float splitGap) {
        boolean split = splitGap > 0;
        float shiftSize = split ? 1.0f : 1.5f;
        keyboard.newRow().addKey('!');
        addLetterKey(keyboard, 'p'); addLetterKey(keyboard, 'y'); addLetterKey(keyboard, 'f');
        addLetterKey(keyboard, 'g'); addLetterKey(keyboard, 'c'); addLetterKey(keyboard, 'r');
        addLetterKey(keyboard, 'l');
        if (split) keyboard.splitCurrentRow(splitGap, 12);

        keyboard.newRow();
        addLetterKey(keyboard, 'a'); addLetterKey(keyboard, 'o'); addLetterKey(keyboard, 'e');
        addLetterKey(keyboard, 'u'); addLetterKey(keyboard, 'i'); addLetterKey(keyboard, 'd');
        addLetterKey(keyboard, 'h'); addLetterKey(keyboard, 't'); addLetterKey(keyboard, 'n');
        addLetterKey(keyboard, 's');
        keyboard.addBackspaceKey().withSize(shiftSize);
        if (split) keyboard.splitCurrentRow(splitGap, 12);

        keyboard.newRow().addShiftKey().withSize(shiftSize);
        addLetterKey(keyboard, 'q'); addLetterKey(keyboard, 'j'); addLetterKey(keyboard, 'k');
        addLetterKey(keyboard, 'x'); addLetterKey(keyboard, 'b'); addLetterKey(keyboard, 'm');
        addLetterKey(keyboard, 'w'); addLetterKey(keyboard, 'v'); addLetterKey(keyboard, 'z');
        if (split) keyboard.splitCurrentRow(splitGap, 12);
    }

    public void addSymbolRows(KeyboardLayoutBuilder keyboard, float splitGap) {
        keyboard.newRow()
                .addKey("Home", -18)
                .addKey("End", -19)
                .addKey("Del", -21)
                .addKey("PgUp", -22)
                .addKey("PgDn", -23);
        if (splitGap > 0) keyboard.splitCurrentRow(splitGap, 12);
        // Row 2 mirrors the regular bottom letter row so Shift, F1-F4 and Backspace line up with
        // Shift, z-v and Backspace when you switch pages. In split, Shift/Backspace take the stagger
        // width and F5-F7 widen (there are only three of them between the gap and Backspace) so the
        // right half matches and Backspace stays letter-row width.
        boolean split = splitGap > 0;
        float symEndKey = split ? (1.0f + SPLIT_END_SPACER) : 1.5f;
        float symFKeyRight = split ? (4f / 3f) : 1.0f; // (5 + spacer - (1 + spacer)) / 3; the spacer cancels
        keyboard.newRow()
                .addShiftKey().withSize(symEndKey)
                .addKey("F1", -6)
                .addKey("F2", -7)
                .addKey("F3", -8)
                .addKey("F4", -9)
                .addKey("F5", -10).withSize(symFKeyRight)
                .addKey("F6", -11).withSize(symFKeyRight)
                .addKey("F7", -12).withSize(symFKeyRight)
                .addBackspaceKey().withSize(symEndKey);
        if (splitGap > 0) keyboard.splitCurrentRow(splitGap, 12);
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
        List<String> units = parseUnits(symbols); // same {token} support as the other custom rows

        keyboard.newRow().addKey("Ctrl", 17).asModifier().onCtrlShow("CTRL");

        for (int i = 0; i < (units.size() + 1) / 2; i++) {
            addUnitKey(keyboard, units.get(i));
            keyboard.withSize(.7f);
        }
        keyboard.addKey(context.getDrawable(R.drawable.ic_space_bar_24dp), 32).withSize(2f);
        for (int i = (units.size() + 1) / 2; i < units.size(); i++) {
            addUnitKey(keyboard, units.get(i));
            keyboard.withSize(.7f);
        }
        keyboard.addEnterKey();

    }

    // Gboard-style QWERTY: every letter carries a corner symbol and a long-press popup.
    // Popup chars are listed in on-screen order (top-left to bottom-right); the second
    // argument of withPopup is the default (typed on a hold-and-lift without sliding).
    public static void addGboardQwertyRows(KeyboardLayoutBuilder keyboard) {
        addGboardQwertyRows(keyboard, 0f);
    }

    // splitGap > 0 means split: a central gap of that width (in key-widths) pushes each row into two
    // halves (for thumb typing on wide screens) and the inner letters G and V are duplicated so both
    // halves have one. When not split, small end gaps inset the home row. The bottom row
    // (addGboardBottomRow) is added separately and never splits.
    public static void addGboardQwertyRows(KeyboardLayoutBuilder keyboard, float splitGap) {
        boolean split = splitGap > 0;
        float centerGap = splitGap; // central split gap as a FRACTION of the row width (see addSplitGap)
        // Gboard split stagger: the home (a..l) and bottom (z..m) rows are inset at each outer end by
        // a small spacer, and Shift/Backspace grow by that SAME amount. Both rows then have an
        // identical half built from "5 + inset" units, so s/d/f/g sit exactly above z/x/c/v and
        // g/h/j/k above v/b/n/m. The top row (q..p) has no inset, so its keys read a touch wider than
        // the home row, the natural "asdfg slightly narrower than qwert" Gboard look. One constant
        // drives both the inset and the Shift/Backspace gain so they always match.
        float splitEndSpacer = SPLIT_END_SPACER; // tune via the SPLIT_END_SPACER class constant
        float shiftSize = split ? (1.0f + splitEndSpacer) : 1.5f;

        // Row 1: q w e r t | y u i o p
        keyboard.newRow()
                .addKey('q').onShiftUppercase().withPopup(2, "%", "%", "ʔ")
                .addKey('w').onShiftUppercase().withPopup(1, "\\", "\\")
                .addKey('e').onShiftUppercase().withPopup(6, "|", "ę","ë","ē","ė","ə","ɛ̃", "è","|","é","ê","ɜ","ɛ")
                .addKey('r').onShiftUppercase().withPopup(5, "=", "=","ʁ","ɹ","ɾ","ʀ")
                .addKey('t').onShiftUppercase().withPopup(2, "[", "[", "θ");
        if (split) keyboard.addSplitGap(centerGap);
        keyboard.addKey('y').onShiftUppercase().withPopup(5, "]", "ʏ","ij","]","ÿ","ý")
                .addKey('u').onShiftUppercase().withPopup(4, "<", "ũ","ù","ū","ʊ", "û","<","ú","ü")
                .addKey('i').onShiftUppercase().withPopup(5, ">", "ɪ","ij","į","ì","ĩ", "ī","ï","î",">","í")
                .addKey('o').onShiftUppercase().withPopup(6, "{", "ɔ̃","œ̃","õ","ō","ø","ò", "ɔ","œ","ö","ô","ó","{")
                .addKey('p').onShiftUppercase().withPopup(1, "}", "}");

        // Row 2: a s d f g | g h j k l   (G duplicated when split). Outer end spacers inset the row:
        // 0.5 in normal mode (classic stagger), the split inset when split (so a..l read narrower
        // than q..p and line up over z..m).
        keyboard.newRow();
        keyboard.addGap(split ? splitEndSpacer : 0.5f);
        keyboard.addKey('a').onShiftUppercase().withPopup(6, "@", "æ","ã","å","ā","ɒ","ɑ", "@","à","á","â","ä","ɑ̃")
                .addKey('s').onShiftUppercase().withPopup(3, "#", "#","ß","ʃ")
                .addKey('d').onShiftUppercase().withPopup(6, "€", "€","$","£","¥","¢","ð")
                .addKey('f').onShiftUppercase().withPopup(1, "-", "-")
                .addKey('g').onShiftUppercase().withPopup(2, "&", "&","ɣ");
        if (split) keyboard.addSplitGap(centerGap).addKey('g').onShiftUppercase().withPopup(2, "&", "&","ɣ");
        keyboard.addKey('h').onShiftUppercase().withPopup(2, "-", "-","ɦ")
                .addKey('j').onShiftUppercase().withPopup(2, "+", "+","j́")
                .addKey('k').onShiftUppercase().withPopup(1, "(", "(")
                .addKey('l').onShiftUppercase().withPopup(1, ")", ")");
        keyboard.addGap(split ? splitEndSpacer : 0.5f);

        // Row 3: shift z x c v | v b n m backspace   (V duplicated when split)
        keyboard.newRow()
                .addShiftKey().withSize(shiftSize)
                .addKey('z').onShiftUppercase().withPopup(2, "*", "*","ʒ")
                .addKey('x').onShiftUppercase().withPopup(1, "\"", "\"")
                .addKey('c').onShiftUppercase().withPopup(4, "'", "ć","'","ç","č")
                .addKey('v').onShiftUppercase().withPopup(2, ":", ":","ʌ");
        if (split) keyboard.addSplitGap(centerGap).addKey('v').onShiftUppercase().withPopup(2, ":", ":","ʌ");
        keyboard.addKey('b').onShiftUppercase().withPopup(1, ";", ";")
                .addKey('n').onShiftUppercase().withPopup(5, "!", "ŋ","ɲ","ń","!","ñ")
                .addKey('m').onShiftUppercase().withPopup(1, "?", "?")
                .addBackspaceKey().withSize(shiftSize);
    }

    // Bottom row for the Gboard QWERTY: Ctrl, comma, space, period, enter.
    // Period carries the punctuation popup; comma carries the IPA stress/length marks.
    public void addGboardBottomRow(KeyboardLayoutBuilder keyboard, float splitGap) {
        // Bottom row laid out to line up under the bottom letter row: Ctrl sits under Shift, comma
        // under z, period under m, Enter under Backspace. So Ctrl/Enter take the Shift/Backspace
        // width (1 + SPLIT_END_SPACER in split, 1.5 normally) and comma/period stay letter-width
        // (1.0); the spacebar takes the rest. The row is NOT split, so to keep those widths matching
        // the (split) letter rows we scale to their total: the letter rows hold 2*(5 + SPLIT_END_SPACER)
        // key-units across (1 - gap) of the width, so rowTotal = that / (1 - gap); normally it is 10.
        // Comma's corner/hold-default is the backtick (rehomed from the old number row), IPA marks as
        // slide alternates; period shows its comma default.
        boolean split = splitGap > 0;
        float ctrlEnterSize = split ? (1.0f + SPLIT_END_SPACER) : 1.5f;
        float rowTotal = split ? (2f * (5f + SPLIT_END_SPACER) / (1f - splitGap)) : 10f;
        float spaceSize = rowTotal - 2f * ctrlEnterSize - 2f; // minus Ctrl+Enter and comma+period
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
