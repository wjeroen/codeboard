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

### Features to Implement
- [ ] **Long-press keyboard, Stage 3: split + spacebar cursor.** Auto-split on wide
      screens (>= 600dp, duplicating G and V), and drag the spacebar horizontally to
      move the cursor (no vertical).
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
- [ ] Credit the original author / upstream in the in-app About screen.
- [ ] Pick a permanent name and app ID for the fork (currently the placeholder
      "CodeBoard Fork" and `com.gazlaws.codeboard.fork`).
- [ ] Per-layout accent maps (extends the long-press alternates) for AZERTY / QWERTZ users.
- [ ] Optional vertical drag for line navigation in Stage 3a (spacebar cursor).

---

## Feature Plans

The only remaining planned work is **Stage 3** of the long-press keyboard, which has
two independent parts, planned below: **Spacebar cursor** (was "Feature A") and
**Split keyboard** (was "Feature D"). Paths are under
`app/src/main/java/com/gazlaws/codeboard/`; see the README codebase map for where
each file lives.

> Feature B (brighter preview) and Feature C (long-press alternates, Stages 1-2) are
> done, see Completed Recently and the README "Long-press reference". Their old plans
> were removed from here to keep this list current.

### Stage 3a: Space-bar cursor navigation, Gboard-style (moderate)

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

### Stage 3b: Split keyboard for tablets / foldables (medium, low urgency)

**Goal:** a centered gap that pushes the left half left and the right half right,
for thumb typing on wide screens. Per the agreed spec, the inner letters G and V are
duplicated so each half has its own, and the bottom row (Ctrl, comma, space, period,
enter) does **not** split.

**Plan:** because keys use normalized `Box` coordinates, implement the split as a
post-processing geometry transform in `KeyboardLayoutBuilder.build()`: compress
left-half keys into `[0, 0.5 - gap]` and right-half keys into `[0.5 + gap, 1.0]`,
leaving the bottom row full-width. The middle gap has no child views, so touches
there are naturally ignored. No changes to touch, drawing, or input dispatch are
needed. Gate it behind an Off/Auto/On preference, where Auto detects wide screens via
`getResources().getConfiguration().screenWidthDp >= 600`.

**Touches:** `layout/builder/KeyboardLayoutBuilder.java`, `CodeBoardIME.java`,
`KeyboardPreferences.java`, `res/xml/preferences.xml`.

---

## Reference
- [README.md](./README.md): features, build and install, settings, and the
  architecture and codebase map.
- Upstream project: [gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard).
