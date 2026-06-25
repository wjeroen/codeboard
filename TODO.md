This TODO file still needs to be properly set up. All the context below is simply for the next Claude Code session to work on, after which a proper TODO.md file and improved README.md can be set up and maintained.

## Codeboard — Session Handoff Briefing

This is a fork/continuation of the [gazlaws/codeboard](https://github.com/gazlaws/codeboard) Android IME app, currently maintained at **wjeroen/codeboard**.

---

### Already Done

1. **README** — changed _"would like to thank me"_ → _"would like to support the original author"_. (though it should mention the original author by name with a link to the original repo, please fix this).
2. **Settings export/import** — new "Backup" section in settings. Serialises all `SharedPreferences` to JSON via `KeyboardPreferences.exportToJson()` / `importFromJson()`. Uses Android Storage Access Framework (`ACTION_CREATE_DOCUMENT` / `ACTION_OPEN_DOCUMENT`) — no new permissions. `SettingsFragment` wires two `ActivityResultLauncher` instances for the file picker flows.
3. **Third SYM row F-key fix** — in `CodeBoardIME.java` around line 540, the condition was `if (sym3.isEmpty() && sym4.isEmpty()) addSymbolRows()`. Changed to `if (sym4.isEmpty()) addSymbolRows()` — so a third row augments the F-key/PgUp/PgDn block, while a fourth row keeps the original space-row swap behavior.

> ⚠️ A Gradle build could NOT be run in the environment (`dl.google.com` blocked by egress policy). All changes are code-reviewed but need a local build before merging.

---

### Architecture Notes (important context)

The app is a **fully custom IME** — it does **not** use Android's `KeyboardView`/`Keyboard` XML system. Key facts:

- Each key is its own `KeyboardButtonView extends View`. Touch is handled in `onTouchEvent` (currently only `ACTION_DOWN` and `ACTION_UP` — **`ACTION_MOVE` is not handled**).
- Keys are positioned using normalized `Box` coordinates (0.0–1.0 fractions of keyboard width/height), converted to pixels in `KeyboardButtonView.layout()` (lines 58–68). This means geometry is purely relative — very amenable to transforms like split layout.
- The "preview" on key press (`enablePreview` setting) is **not a separate popup** — it's the same key view being lifted via `setTranslationY(-200)` + `setScaleX/Y(1.2)` + `setElevation(21)` in `animatePress()` (line 189). It reuses the exact same `buttonBodyPaint` color, which is why it looks just as dark as the unpressed key.
- Long-press is driven by a `Timer` in `CodeBoardIME.onPress()` (line 330), which fires `onKeyLongPress()` after `ViewConfiguration.getLongPressTimeout()`. Currently `onKeyLongPress` only handles: shift-lock (code 16), ctrl-lock (code 17), space (code 32 → shows IME picker).
- Space suppression: `longPressedSpaceButton` flag in `CodeBoardIME` suppresses the normal space character on release (line 387).

---

### Planned Features (not yet implemented)

---

#### Feature A: Space-bar cursor navigation (Gboard-style)

**Difficulty: moderate.**

**How it works in Gboard:** long-press + horizontal drag moves the cursor left/right character by character. Some keyboards also support vertical drag for up/down line navigation.

**Implementation plan:**

1. **`KeyboardButtonView.java`** — add `ACTION_MOVE` to `onTouchEvent`. Track cumulative horizontal delta (`accumulatedDx`) since the long-press fired. Every ~N pixels (tunable, e.g. the width of one key), emit a left or right event and reset the accumulator. Gate this on whether a long-press has already fired — reuse the existing `longPressedSpaceButton` flag from `CodeBoardIME` or add a local `isInSpaceDrag` boolean.

2. **Axis locking** — on first meaningful movement, lock to horizontal OR vertical, ignore the other axis for the rest of that gesture (prevents diagonal cursor jumping).

3. **Dispatching cursor movement** — use `InputConnection.sendKeyEvent(new KeyEvent(ACTION_DOWN, KEYCODE_DPAD_LEFT/RIGHT))` for L/R and `KEYCODE_DPAD_UP/DOWN` for up/down. The app already dispatches key events this way in `onKeyLongPress` for shift/ctrl.

4. **Suppress space on release** — the existing `longPressedSpaceButton` flag already does this for the IME picker case; the same flag covers the drag case since once long-press fires, the flag is set.

5. **`CodeBoardIME.onKeyLongPress`** — the IME picker (currently triggered on space long-press at line 426) needs to be demoted. Options: (a) remove the IME picker from space long-press entirely since the drag is more useful, (b) only show it if no drag occurred (check a "did we drag?" flag on release), or (c) move the IME picker to a different gesture.

**Files to touch:**
- `app/src/main/java/com/gazlaws/codeboard/layout/ui/KeyboardButtonView.java` — `onTouchEvent`, possibly new `startSpaceDrag()` / `handleSpaceMove(float dx)` methods.
- `app/src/main/java/com/gazlaws/codeboard/CodeBoardIME.java` — `onKeyLongPress` (adjust or remove the space → IME picker logic), possibly expose a method the view can call to emit cursor key events.

---

#### Feature B: Brighter preview (fix existing key-lift popup)

**Difficulty: small.**

**Problem:** `animatePress()` lifts the key view up/scaled, but it draws with the same `buttonBodyPaint` as unpressed keys, so it looks identical in color.

**Plan:**

1. **`UiTheme.java`** — add a `previewBodyPaint` field. In `buildFromInfo()` (line 41), compute it as a lightened version of the background: `ColorUtils.blendARGB(info.backgroundColor, Color.WHITE, 0.25f)`. `ColorUtils` is already imported in the project. This is theme-aware — dark themes lighten, light themes stay readable.

2. **`KeyboardButtonView.java`** — track an `isPreviewActive` boolean, set it in `animatePress()`, clear it in `animateRelease()`, call `invalidate()` on change. In `drawButtonBody()` (line 118), select `uiTheme.previewBodyPaint` when `isPreviewActive`, otherwise `uiTheme.buttonBodyPaint`.

**Files to touch:**
- `app/src/main/java/com/gazlaws/codeboard/theme/UiTheme.java`
- `app/src/main/java/com/gazlaws/codeboard/layout/ui/KeyboardButtonView.java`

---

#### Feature C: Accent / alternative characters on long press

**Difficulty: medium-large** (biggest of the four remaining).

**Current state:** there is no accent popup at all. `KeyInfo` has no field for alternative characters. Long-press on a letter does nothing.

**Plan:**

**Data model:**
- `KeyInfo.java` — add `public String[] popupCharacters;`
- `KeyboardLayoutBuilder.java` — add fluent method `withPopup(String chars)` that splits the string into individual chars and assigns them (follow the pattern of existing `withOutputText()` / `onShiftShow()` at lines 138–151).
- `Definitions.java` — in `addQwertyRows()` etc., attach accent sets per letter, e.g.:
  ```java
  .addKey('e').onShiftUppercase().withPopup("éèêëē")
  .addKey('a').onShiftUppercase().withPopup("àâäáã")
  ```
  A static `Map<Character, String>` shared across all four layouts (QWERTY/AZERTY/Dvorak/QWERTZ) keeps it maintainable.

**Triggering:**
- In `KeyboardButtonView`, add a long-press `postDelayed` (or extend the existing timer that delegates to `CodeBoardIME.onPress`). When long-press fires and `key.info.popupCharacters != null`, show the popup and set a flag to suppress the normal keydown output on release.

**Popup UI — reuse the preview lift mechanism:**
Per the user's request, the accent popup should use the same visual style as the existing preview-lift. The cleanest approach: show the alternative characters as a row of small `KeyboardButtonView`-like items in a `PopupWindow`, anchored above the pressed key (using `getLocationInWindow()` to get absolute coords). Each item uses the same `previewBodyPaint` introduced in Feature B (so it's automatically brighter than normal keys). Tapping any item commits that character via `inputService.onText(String.valueOf(chosenChar))` (same path as `CodeBoardIME.onText` → `ic.commitText(...)`).

**Files to touch:**
- `app/src/main/java/com/gazlaws/codeboard/layout/builder/KeyInfo.java`
- `app/src/main/java/com/gazlaws/codeboard/layout/builder/KeyboardLayoutBuilder.java`
- `app/src/main/java/com/gazlaws/codeboard/layout/Definitions.java`
- `app/src/main/java/com/gazlaws/codeboard/layout/ui/KeyboardButtonView.java`
- `app/src/main/java/com/gazlaws/codeboard/theme/UiTheme.java` (for the brighter paint, if B is done first)

---

#### Feature D: Split-screen keyboard for foldables/tablets

**Difficulty: medium.** (Lowest urgency — research only so far.)

**Plan:** Because keys are positioned with normalized `Box` coordinates, a split can be implemented as a post-processing geometry transform in `KeyboardLayoutBuilder.build()`: compress left-half keys into `[0, 0.5−gap]` and right-half keys into `[0.5+gap, 1.0]`. The gap in the middle gets no child views so touches there are naturally ignored. No changes to touch, draw, or input dispatch code.

Gate via a preference toggle (Off/Auto/On) + Auto detects wide screens via `getResources().getConfiguration().screenWidthDp >= 600`.

**Files to touch:**
- `app/src/main/java/com/gazlaws/codeboard/layout/builder/KeyboardLayoutBuilder.java`
- `app/src/main/java/com/gazlaws/codeboard/CodeBoardIME.java`
- `app/src/main/java/com/gazlaws/codeboard/KeyboardPreferences.java`
- `app/src/main/res/xml/preferences.xml`

---

### Suggested Implementation Order

1. **Feature B (brighter preview)** — tiny, self-contained, prerequisite for C's popup styling.
2. **Feature A (space-bar cursor nav)** — standalone, no dependencies.
3. **Feature C (accent long-press)** — builds on B's `previewBodyPaint` for popup brightness.
4. **Feature D (split layout)** — lowest urgency, most self-contained.

---

### Key File Map

| File | Role |
|---|---|
| `CodeBoardIME.java` | IME service, long-press timer, `onKeyLongPress`, keyboard assembly |
| `layout/ui/KeyboardButtonView.java` | Per-key view, touch, press animation |
| `layout/ui/KeyboardLayoutView.java` | ViewGroup container, layout from Box |
| `layout/builder/KeyboardLayoutBuilder.java` | Normalised row/key assembly |
| `layout/builder/KeyboardLayoutRowBuilder.java` | Per-row key width distribution |
| `layout/builder/KeyInfo.java` | Key data model (add `popupCharacters` here) |
| `layout/Definitions.java` | Concrete key/row definitions, accent data goes here |
| `theme/UiTheme.java` | Paints/colors used by views (add `previewBodyPaint` here) |
| `theme/ThemeDefinitions.java` | Theme presets |
| `KeyboardPreferences.java` | All SharedPreferences access + export/import (done) |
| `SettingsFragment.java` | Settings UI + SAF launchers (done) |
| `res/xml/preferences.xml` | Settings screen XML |
