# Codeboard TODO

Task list for **wjeroen/codeboard**, a maintained fork of
[gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard).

The checklist is up top. The detailed implementation plans for the planned
features are in **Feature Plans** at the bottom of this file. How the app
actually works (architecture, codebase map, build and install) is in the
[README](./README.md).

---

## Current Sprint

### High Priority
- [ ] Install the CI-built `codeboard-debug` APK on a real device and smoke-test
      the two never-run app changes (Backup export/import, third SYM row fix).
      The CI build is green; this is the on-device check.

### Features to Implement
- [ ] **Feature B: brighter key-press preview** (small). The preview reuses the
      normal key color, so it looks identical. Add a lightened `previewBodyPaint`.
      Do this first; Feature C depends on it. (Plan below.)
- [ ] **Feature A: space-bar cursor navigation** (moderate). Long-press Space and
      drag to move the cursor, Gboard-style. (Plan below.)
- [ ] **Feature C: accent / long-press alternate characters** (medium-large).
      Hold a letter to pick `é`, `à`, etc. Builds on Feature B. (Plan below.)
- [ ] **Feature D: split keyboard for tablets/foldables** (medium, low urgency).
      Geometry transform on the normalized key boxes. (Plan below.)

### Bug Fixes
- [ ] **Ctrl+Enter does not insert a newline** in "Enter = send" fields (chat apps).
      It currently sends an Enter key event with a Ctrl modifier, which apps ignore
      or treat as send. Proposed fix: make Ctrl+Enter commit a literal newline
      (`CodeBoardIME.onKey`, the Enter case, primaryCode -4). Needs on-device check.
- [ ] **First two SYM rows show swapped default symbols** on a fresh install (before
      you customise them). `getCustomSymbolsSym()` and `getCustomSymbolsSym2()` in
      `KeyboardPreferences.java` fall back to each other's default string. Fix: point
      each getter at its own matching default.

### Testing
- [x] Build the debug APK in CI: run is green, `codeboard-debug` artifact (~5.4 MB) produced (2026-06-25)
- [ ] Install that APK on a device and smoke-test the two never-run changes (tracked under High Priority)

### Documentation
- [x] Rewrite README: full feature list, build/CI steps, settings, and a codebase map (2026-06-25)
- [x] Convert this TODO into a proper checklist; keep the feature plans in this file (2026-06-25)
- [x] Drop the Google Play references, this fork will diverge from the original (2026-06-25)

---

## Completed Recently
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
- [ ] Credit the original author / upstream in the in-app About screen.
- [ ] Pick a permanent name and app ID for the fork (currently the placeholder
      "CodeBoard Fork" and `com.gazlaws.codeboard.fork`).
- [ ] Per-layout accent maps (extends Feature C) for AZERTY / QWERTZ users.
- [ ] Optional vertical drag for line navigation in Feature A.

---

## Feature Plans

Detailed plans for the checklist items above. Suggested order: **B, A, C, D**
(B is a prerequisite for C's popup styling). Paths are under
`app/src/main/java/com/gazlaws/codeboard/`; see the README codebase map for where
each file lives.

### Feature B: Brighter key-press preview (small)

**Problem:** `animatePress()` lifts the key view but draws it with the same
`buttonBodyPaint` as an unpressed key, so the preview looks identical in color.

**Plan:**
1. In `theme/UiTheme.java`, add a `previewBodyPaint` field, computed as a
   lightened version of the background, for example
   `ColorUtils.blendARGB(info.backgroundColor, Color.WHITE, 0.25f)`. Theme-aware:
   dark themes lighten, light themes stay readable.
2. In `layout/ui/KeyboardButtonView.java`, track an `isPreviewActive` boolean (set
   in `animatePress()`, cleared in `animateRelease()`, `invalidate()` on change).
   In the body-draw step, pick `previewBodyPaint` when active.

**Touches:** `theme/UiTheme.java`, `layout/ui/KeyboardButtonView.java`.

### Feature A: Space-bar cursor navigation, Gboard-style (moderate)

**Goal:** long-press Space, then drag horizontally to move the cursor character by
character (optionally vertical drag for line up/down).

**Plan:**
1. Add `ACTION_MOVE` handling to `KeyboardButtonView.onTouchEvent`. Once a
   long-press has fired, accumulate horizontal delta; every ~1 key-width, emit a
   left/right cursor move and reset the accumulator.
2. **Axis-lock** on the first meaningful movement (horizontal or vertical) to
   avoid diagonal jumping.
3. Dispatch movement with `InputConnection.sendKeyEvent(KEYCODE_DPAD_LEFT/RIGHT)`
   (and `UP/DOWN`). The app already sends key events this way.
4. Reuse the existing `longPressedSpaceButton` flag to suppress the space character
   on release.
5. Decide what happens to the current space-long-press IME picker: keep it only
   when no drag occurred, or move it to another gesture.

**Touches:** `layout/ui/KeyboardButtonView.java`, `CodeBoardIME.java`.

### Feature C: Accent / alternate characters on long-press (medium-large)

**Goal:** hold a letter to pick an accented variant (for example hold `e` to get
`é è ê ë ē`).

**Plan:**
- **Data model:** add `String[] popupCharacters` to `layout/builder/KeyInfo.java`;
  add a `withPopup(String chars)` builder method to
  `layout/builder/KeyboardLayoutBuilder.java`; attach accent sets per letter in
  `layout/Definitions.java` (a shared `Map<Character, String>` keeps all four
  layouts consistent).
- **Trigger:** when long-press fires and `popupCharacters != null`, show a popup
  and suppress the normal key output on release.
- **Popup UI:** reuse the preview-lift look. Show the alternates as a small row in
  a `PopupWindow` anchored above the pressed key, drawn with Feature B's
  `previewBodyPaint`. Tapping one commits it via the existing `onText` path.

**Touches:** `layout/builder/KeyInfo.java`,
`layout/builder/KeyboardLayoutBuilder.java`, `layout/Definitions.java`,
`layout/ui/KeyboardButtonView.java`, `theme/UiTheme.java` (do Feature B first).

### Feature D: Split keyboard for tablets / foldables (medium, low urgency)

**Goal:** a centered gap that pushes the left half left and the right half right,
for thumb typing on wide screens.

**Plan:** because keys use normalized `Box` coordinates, implement the split as a
post-processing geometry transform in `KeyboardLayoutBuilder.build()`: compress
left-half keys into `[0, 0.5 - gap]` and right-half keys into `[0.5 + gap, 1.0]`.
The middle gap has no child views, so touches there are naturally ignored. No
changes to touch, drawing, or input dispatch are needed. Gate it behind an
Off/Auto/On preference, where Auto detects wide screens via
`getResources().getConfiguration().screenWidthDp >= 600`.

**Touches:** `layout/builder/KeyboardLayoutBuilder.java`, `CodeBoardIME.java`,
`KeyboardPreferences.java`, `res/xml/preferences.xml`.

---

## Reference
- [README.md](./README.md): features, build and install, settings, and the
  architecture and codebase map.
- Upstream project: [gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard).
