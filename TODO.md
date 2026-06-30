# Codeboard TODO

Task list for **wjeroen/codeboard**, a maintained fork of
[gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard).

The checklist is up top. The detailed implementation plans for the planned
features are in **Feature Plans** at the bottom of this file. How the app
actually works (architecture, codebase map, build and install) is in the
[README](./README.md).

> 📌 **When you add a big feature or behaviour change versus the original**, also
> record it (high level) in the README's "Differences from the original" section,
> so people deciding whether to switch know what to expect.

---

## Current Sprint

### High Priority
- [ ] Install the CI-built `codeboard-debug` APK on a real device and smoke-test
      the two never-run app changes (Backup export/import, third SYM row fix).
      The CI build is green; this is the on-device check.
- [ ] **Test the non-QWERTY layouts on a device** (AZERTY, Dvorak, QWERTZ). They got
      the Gboard treatment (corner symbols, accent popups, splitting) but only QWERTY
      has been used in practice. Likely rough spots: which corner symbol sits on which
      key, the default-cell position per key, and how each row splits. The shared accent
      data is fine; only the per-layout *arrangement* (`addLetterKey`) may need tuning.
- [ ] **Per-layout long-press arrangement** (follow-up to the above): right now every
      non-QWERTY layout uses one generic arrangement (symbol first, then accents, ≤6
      columns). The seam to customise per layout is `Definitions.addLetterKey`; the
      shared accent data is in `letterSymbol`/`letterAccents`. Decide per layout where
      the default cell should sit (Gboard puts it nearest the key's screen position).

### Features to Implement

> 🧭 **Roadmap note (2026-06-30):** the items below mostly hang off one foundation. Today the app
> has two kinds of keys: typed-character keys (movable, set in Settings) and special action keys
> (Esc/Tab/arrows/F-keys/SYM, hardcoded in `Definitions`). Making the editable rows able to hold
> special keys (the "foundation" item) turns most of the button/page features into "register one
> more button". Suggested order: foundation -> Settings button -> Emoji page -> AI reword/prompts.

- [ ] **Fully customizable rows incl. special keys (the "foundation")** (medium-large). Today the
      editable rows (`addCustomRow`) only accept plain characters; special keys are hardcoded. Goal:
      let any customizable row hold special keys too, EXCEPT the letter rows, Shift/Ctrl, and the
      bottom row (those stay fixed). MVP: a token syntax in the existing text fields (e.g.
      `{esc} {tab} {<} {sym}`) plus an action-key registry mapping each token to its icon/label/code,
      and handling new codes in `CodeBoardIME.onKey`. Deluxe (later): a visual row-builder UI.
- [ ] **Buttons that ride on the foundation** (do after the foundation lands):
      - **Settings button** that opens the app settings (trivial once tokens exist).
      - **Emoji page** (a new page like SYM; study how DevEmperor/DictateKeyboard builds its emoji grid).
      - **AI reword + an AI-prompts page** (copy DictateKeyboard's functionality but in our own UI:
        send the selected text to an LLM with the user's own API key, paste back the result; plus a
        page of preset prompt buttons). Pull the current Claude API details when building it.
      - **Suggested-word button**: a SINGLE button (not a suggestion strip) showing the correction
        from Android's spellcheck, so a tap fixes "wont"->"won't", "pottoes"->"potatoes".
- [ ] **Apply the split stagger to QWERTZ / AZERTY / Dvorak** once QWERTY is confirmed good on a
      device. QWERTY has the hand-tuned 0.4 inset; the others currently split without it.
- [ ] **Feature E: clipboard history** (medium-large, not yet designed). Replace the
      current fixed setup (7 manually-set pins plus the single current clipboard) with a
      real, growing history of recently copied items you can scroll and paste from on the
      Ctrl+SYM page. Needs a design pass first: where/how history is stored, how many
      items, and how to clear it for privacy.

### Bug Fixes
- [ ] (none open)

### Testing
- [x] Build the debug APK in CI: run is green, `codeboard-debug` artifact (~5.4 MB) produced (2026-06-25)
- [ ] Install that APK on a device and smoke-test the two never-run changes (tracked under High Priority)

### Documentation
- [x] Rewrite README: full feature list, build/CI steps, settings, and a codebase map (2026-06-25)
- [x] Convert this TODO into a proper checklist; keep the feature plans in this file (2026-06-25)
- [x] Drop the Google Play references, this fork will diverge from the original (2026-06-25)

---

## Completed Recently
- [x] **Split-mode Gboard stagger + consistent centre gap** (2026-06-30): reworked how the split
      keyboard reserves its central gap. The gap is now a fixed FRACTION of the keyboard width
      reserved by the row builder (`KeyInfo.isSplitGap` + `KeyboardLayoutRowBuilder.buildSplit`),
      with each side laid out independently. So the centre gap is now identical on every row
      regardless of key count, which also fixes the long-standing annoyance where a custom 12-char
      number row showed a narrower gap than the letter rows. On QWERTY the home (a..l) and bottom
      (z..m) rows are inset by `splitEndSpacer` (0.4 of a key) while Shift/Backspace grow by the
      same amount, so `s d f g` line up exactly over `z x c v` and `a..g` read slightly narrower
      than `q..t`, mimicking Gboard. The gap still grows on wider screens (each half capped at 5
      key-heights). QWERTZ uses the same split markers; AZERTY/Dvorak/custom/SYM rows get the
      consistent gap via the midpoint split. **Needs on-device check** (compiled in CI only).
- [x] **Configurable split side margin** (2026-06-30): the left/right margin in split mode is now a
      "Split side margin (%)" setting (default 5, was a hardcoded 0.05). Normal mode stays
      edge-to-edge. (`KeyboardPreferences.getSplitSideMargin`, `preferences.xml`,
      `default_keyboard_preferences.xml`, `CodeBoardIME.onCreateInputView`.)
- [x] **Purple app icon everywhere** (2026-06-29): applied the same +113 deg hue shift used on
      the purple Play Store icon to every in-APK icon (the 5-density adaptive foreground, the
      legacy `ic_launcher`/`ic_launcher_round` rasters, the notification/intro `icon_large`, and
      the keyboard-switcher `icon_old_1`), plus the adaptive background colour `#3CE2D4` ->
      `#BD3CE2`. The launcher icon was unchanged before because modern Android builds it from the
      adaptive foreground + background colour, not the raster that was edited.
- [x] **Bright-theme popups, shadow layering, wider split margin** (2026-06-29):
  - Popup cells now **darken on bright themes** (luminance > 0.5) instead of lifting toward
    white, which was invisible on a light keyboard. Both levels flip (press/preview and the
    selected option).
  - Popup **shadows are drawn behind every cell** (a first pass), so one option's shadow no
    longer falls on the next option.
  - Split **side padding 0.03 → 0.05** (does not change key width thanks to the max-width cap).
- [x] **Split max-width, caps lock, popup polish** (2026-06-29):
  - **Split halves capped:** each half of a split keyboard is now at most 5 key-heights wide
    (`5/rows` of the keyboard height), so on a wide screen the keys stop stretching and the
    central gap grows to keep each half near the thumb. Keys keep their relative sizes
    (irregular ones like Ctrl/Enter are not forced square). The gap is computed per-build
    (`CodeBoardIME.computeSplitGap`), floored at `MIN_SPLIT_GAP` on narrow screens.
  - **Caps lock is clear now:** the Shift key shows an underlined arrow
    (`ic_caps_lock_24dp`) while caps lock is on. Enable it by **double-tapping Shift**
    (Gboard-style) or long-pressing it; a single tap clears it. The long-press floating cell
    shows the arrow you are switching to.
  - **No more popup slide:** the popup window is now always sized to the full alternates grid,
    so the press preview and the long-press grid share one window. The preview just draws one
    cell and the grid fills the rest, so it never repositions.
  - **Float height** lowered to half a key height (was ~a full key height). **Shadow** deepened
    (radius 30, 10px down). **Shift alternates** show capitalised while Shift is held.
- [x] **Popup unification + split/feedback tweaks** (2026-06-29):
  - **Unified popups:** every key now uses the same floating cell as the letter preview
    (Ctrl, Backspace, Enter, F-keys, etc. no longer jump up in place). Each cell has a
    soft drop shadow and floats `POPUP_LIFT_PX` (100px, half the old lift) above the key.
    Brightness unchanged (preview dimmer than the selected alternate). The spacebar still
    just brightens (it is the cursor control). Pop-in animation disabled (`setAnimationStyle(0)`).
  - **Shift shows capital alternates:** while Shift is held, the long-press grid draws its
    characters uppercased, matching what gets typed (display-only).
  - **Split spacing:** left/right padding only in split mode (0.03, normal mode is now
    edge-to-edge with no padding); central gap widened 1.5 → 2.0.
  - **Number keys:** `0` gains `ⁿ` as its left-most option (`ⁿ ∅ ⁰`). QWERTY `n` popup
    reordered to `ŋ ɲ ń ! ñ` (`!` still the default).
- [x] **Gboard polish pass (one big commit)** (2026-06-29):
  - **Fixed number row** for every layout: `1 2 3 4 5 6 7 8 9 0`, no longer editable
    (the "Main keyboard [Top Row]" setting and its `` `-= `` keys are gone). Each digit
    long-presses to its superscript (the default) plus Gboard's fractions, e.g. `1` →
    `½ … ⅒`, `2` → `⅖ ⅔`. Splits `1 2 3 4 5 | 6 7 8 9 0`. (`addGboardNumberRow`)
    > ⚠️ **Later reverted (2026-06-30):** the top row is the editable "Main keyboard [Top Row]"
    > again (default `` `1234567890-= ``), built by `addCustomRow`; the digit-fraction popups now
    > attach to whatever digit lands in any custom row (`addDigitFractionPopup`). There is no
    > `addGboardNumberRow`. The README's editable-top-row description is the current, correct one.
  - `` ` `` (backtick) rehomed onto the **comma** key as its corner symbol + hold-default,
    keeping the IPA stress marks as slide alternates. The **period** key now shows `,` as
    its corner symbol.
  - **Symmetric bottom row**: Ctrl and Enter are equal width (1.25), comma/period stay
    letter-width (1.0), spacebar takes the rest. Works in normal and split mode.
  - **Shift key** is now an up-arrow icon (was "Shft" text) and matches the backspace
    width; the whole bottom letter row matches the home row's key sizes.
  - **Splitting** now also applies to custom rows and the SYM-page rows (generic
    midpoint split: half the keys each side, the left side gets the odd key, dropped
    above 12 keys). Best-effort: the F8-F12 spacebar row is left whole.
  - **AZERTY / Dvorak / QWERTZ** got the same Gboard treatment as QWERTY (corner
    symbols, long-press accent popups, split). Built on a future-proof seam: the shared
    accent DATA lives in `letterSymbol`/`letterAccents`, the per-layout ARRANGEMENT
    lives in `addLetterKey` (generic for now; QWERTY keeps a hand-tuned arrangement as
    the reference). See "needs testing" below.
  - **Defaults**: font size 25, portrait keyboard size 35, landscape size 40 (landscape
    is genuinely independent now; the landscape field was defaulting to the portrait
    integer). Corner symbols slightly smaller (0.52× font). Small left/right side
    padding (0.015), nothing top/bottom.
- [x] Stage 3 + preview refinements: (1) press preview keeps its original brightness
      (`previewBodyPaint`); only its shape/size changed, not the colour. (2) Spacebar
      cursor now moves the caret with `InputConnection.setSelection` (stays inside the
      field, stops at the ends) instead of arrow-key events that jumped focus out;
      slightly faster (`CURSOR_STEP_DP` 11) with a per-character haptic tick. (3) Bottom
      row rebalanced so comma/period are exactly the letter-key width and Enter is exactly
      the Esc/Tab/SYM width (1/7) in BOTH normal and split modes: the bottom row total
      tracks the letter-row total, and in split mode the central gap's width goes to the
      spacebar (so comma/period shrink with the letters and the spacebar is a bigger
      target). (4) Period popup reordered to the agreed layout (2026-06-29)
- [x] Long-press keyboard Stage 3: **split keyboard** + **spacebar cursor**. A new
      "Split keyboard (QWERTY)" setting (Off / Auto / On; Auto splits at >= 600dp) pushes
      the main QWERTY into two halves with a central gap and duplicates the inner G and V
      (`addGboardQwertyRows(builder, split)`). Dragging the spacebar horizontally moves
      the caret; space long-press no longer opens the IME picker (2026-06-28)
- [x] Long-press polish: pressing a key now instantly shows a bright single popup cell
      with just that character (same square shape as a grid cell); holding for 300ms
      (Gboard's default long-press delay) expands it into the alternates grid. Single-
      option keys show a one-cell popup too. Made `a`/`l` the same width as the other
      letters with half-key gaps on the home-row ends (new `addGap` spacer; dropped
      their 1.5x size) (2026-06-28)
- [x] Long-press keyboard Stage 2: holding a key now shows a real popup grid of
      alternates above it (new `PopupKeyboardView` + a non-touchable `PopupWindow`
      in `KeyboardButtonView`). Slide the finger onto a cell to pick it; resting on
      the default and lifting types the bracketed default. Selected cell is the
      brightest (`popupSelectedPaint`) (2026-06-28)
- [x] Made the corner symbols a bit larger (`cornerPaint` text size 0.62× the key
      font, was smaller) so they're readable (2026-06-28)
- [x] Turned popup/key previews on by default and removed the now-pointless toggle
      from settings (`default_keyboard_preferences.xml`, `preferences.xml`) (2026-06-28)
- [x] Long-press keyboard Stage 1: Gboard-style QWERTY with corner symbols and a
      hold-types-the-default framework; new bottom row (Ctrl, comma, space, period,
      enter). Full character spec recorded in the README (2026-06-26)
- [x] Feature B: brighter key-press preview. Pressed/previewed keys now draw with a
      lightened `previewBodyPaint` so they stand out (`UiTheme`, `KeyboardButtonView`)
      (2026-06-26)
- [x] Recolor the app chrome (action bar in light and dark mode, intro screens) to
      purple (#9C27B0), distinct from the original indigo. The keyboard themes are
      unaffected (2026-06-26)
- [x] Settings banner overlap fixed (confirmed on device) via `fitsSystemWindows` on the
      settings layout root (2026-06-26)
- [x] Remove the duplicate French keyboard entry: dropped the fr_FR IME subtype from
      `method.xml` (the layout is chosen in settings, not by subtype) (2026-06-25)
- [x] Point the in-app "Open on GitHub" link at this fork (wjeroen/codeboard) (2026-06-25)
- [x] Shift+Enter now inserts a real newline in "Enter = send" apps (confirmed working
      on device). Ctrl+Enter left stock. The old behaviour failed because Android does
      not reliably deliver a soft-keyboard Shift+Enter to web editors
      (`CodeBoardIME.onKey`) (2026-06-25)
- [x] Fix swapped SYM-row defaults: `getCustomSymbolsSym()` and `getCustomSymbolsSym2()`
      now fall back to their own default strings, so a fresh install shows the top two
      SYM rows with the intended characters (`KeyboardPreferences.java`) (2026-06-25)
- [x] Give the fork its own app ID `com.gazlaws.codeboard.fork` and name "CodeBoard
      Fork" so it installs next to the original instead of conflicting. Also pointed
      the Restart Tutorial intent at the new ID (2026-06-25)
- [x] Remove the in-app "Rate on Google Play" button (`preferences.xml`) (2026-06-25)
- [x] Fixed debug signing so every CI build uses the SAME key and new APKs install
      over old ones with no uninstall. Committed a `debug.keystore` (a debug key is
      not secret) and wired an optional repo-secrets override into `android.yml`
      and `app/build.gradle` (2026-06-25)
- [x] Rewrite the CI workflow to build on `ubuntu-latest` with `assembleDebug`,
      modelled on the working TrackyTime workflow (was `windows-latest` +
      `gradlew.bat assemble`); added a manual "Run workflow" trigger. Verified
      green: the first run built and uploaded the APK in ~1m45s (2026-06-25)
- [x] Fix attribution: upstream is `gazlaws-dev/codeboard`, not `gazlaws/codeboard`
      (the old link 404s); credit the original author by name with a link (2026-06-25)
- [x] Settings **export/import** ("Backup" section) via the Storage Access
      Framework; serialises all prefs to JSON (2026-06-25)
- [x] Third SYM row fix: a 3rd symbol row now augments the F-key/PgUp/PgDn block
      instead of being ignored (`CodeBoardIME.java`) (2026-06-25)

> ⚠️ The two app-code items (Backup export/import, the SYM-row fix) were
> code-reviewed but **never run on a device** yet. CI now compiles them, but they
> still need an on-device smoke test before being trusted.

---

## Future Ideas
- [ ] **Floating keyboard** (low priority, far future): a movable/floating keyboard window like
      DictateKeyboard. Android IMEs do not float easily (needs special window handling), so this is
      a project on its own, not a quick button.
- [ ] Credit the original author / upstream in the in-app About screen.
- [ ] Pick a permanent name and app ID for the fork (currently the placeholder
      "CodeBoard Fork" and `com.gazlaws.codeboard.fork`).
- [ ] Optional vertical spacebar drag for line navigation (Stage 3 added horizontal only).
- [ ] Code cleanup: `Definitions.addQwertyRows` (the old non-Gboard QWERTY) and the
      single-arg `addGboardQwertyRows(keyboard)` overload are now dead (nothing calls
      them). Left in place on purpose as a reference; delete once the new layouts are
      confirmed good on-device.

---

## Feature Plans

No detailed plans are pending. The long-press keyboard work (Stages 1-3: corner
symbols, alternates grid, press preview, split keyboard, spacebar cursor) is all
done, see Completed Recently and the README "Long-press reference" / architecture
section for how it works. The only open feature, **clipboard history (Feature E)**,
still needs a design pass before it gets a plan here.

Tunables worth revisiting after on-device testing (all in code, easy to change):
- Split central gap: computed per-build by `CodeBoardIME.computeSplitGap` so each half is
  capped at 5 key-heights wide; floored at `MIN_SPLIT_GAP` (2.5) on narrow screens. The
  "10" in that formula is the two halves of 5 keys. Signalled to the row methods as the
  `splitGap` float (<= 0 means "not split").
- Max keys for a row to still split: the `12` passed to `splitCurrentRow` (rows longer
  than that stay full-width).
- Ctrl/Enter width on the bottom row: `ctrlEnterSize` in `addGboardBottomRow` (1.25).
- Side padding: `sidePadding` in `CodeBoardIME.onCreateInputView` (split only: 0.05,
  normal mode 0 / edge-to-edge). With the max-width cap this trades gap for margin, not
  key width.
- Popup cell contrast flip: `BRIGHT_THEME_LUMINANCE` in `UiTheme` (0.5). Above it the
  popup cells darken instead of lighten so they show on bright themes.
- Corner symbol size: `cornerPaint` text size in `UiTheme` (0.52× the key font).
- Floating popup lift: `lift = cellH * 0.5f` in `KeyboardButtonView.showOrUpdatePopup`
  (half a key height above the key). Applies to the preview cell and the alternates grid.
- Popup drop shadow: `SHADOW_PAD` / `SHADOW_RADIUS` / `SHADOW_DY` / `SHADOW_COLOR` in
  `PopupKeyboardView` (44 / 30 / 10 / ~40% black). Drawn on a software layer, in a first
  pass behind all cells so a cell's shadow never lands on another cell.
- Caps-lock double-tap window: `SHIFT_DOUBLE_TAP_MS` in `CodeBoardIME` (300ms).
- Popup pop-in is animation-free (`setAnimationStyle(0)` in `KeyboardButtonView`), and the
  window is grid-sized from the preview on, so it never repositions on long-press.
- Spacebar cursor sensitivity: `CURSOR_STEP_DP` in `KeyboardButtonView` (11dp/char).
- Cursor haptic tick reuses the keypress vibration length (`vibrateLength`).
- Auto-split screen threshold: 600dp in `CodeBoardIME.onCreateInputView`.
- Per-layout long-press arrangement seam: `Definitions.addLetterKey` (shared accent
  data in `letterSymbol`/`letterAccents`).

---

## Reference
- [README.md](./README.md): features, build and install, settings, and the
  architecture and codebase map.
- Upstream project: [gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard).
