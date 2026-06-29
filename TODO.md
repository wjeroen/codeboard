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
- [x] Stage 3 + preview refinements: (1) press preview keeps its original brightness
      (`previewBodyPaint`); only its shape/size changed, not the colour. (2) Spacebar
      cursor now moves the caret with `InputConnection.setSelection` (stays inside the
      field, stops at the ends) instead of arrow-key events that jumped focus out;
      slightly faster (`CURSOR_STEP_DP` 11) with a per-character haptic tick. (3) Bottom
      row rebalanced: comma/period are now the same width as the letters, the spacebar is
      wider, and Enter is about Esc/Tab/SYM width. (4) Period popup reordered to the
      agreed layout (2026-06-29)
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
- [ ] Credit the original author / upstream in the in-app About screen.
- [ ] Pick a permanent name and app ID for the fork (currently the placeholder
      "CodeBoard Fork" and `com.gazlaws.codeboard.fork`).
- [ ] Per-layout accent maps (extends the long-press alternates) for AZERTY / QWERTZ users.
- [ ] Optional vertical spacebar drag for line navigation (Stage 3 added horizontal only).

---

## Feature Plans

No detailed plans are pending. The long-press keyboard work (Stages 1-3: corner
symbols, alternates grid, press preview, split keyboard, spacebar cursor) is all
done, see Completed Recently and the README "Long-press reference" / architecture
section for how it works. The only open feature, **clipboard history (Feature E)**,
still needs a design pass before it gets a plan here.

Tunables worth revisiting after on-device testing (all in code, easy to change):
- Split central gap width: `centerGap` in `Definitions.addGboardQwertyRows` (1.5).
- Spacebar cursor sensitivity: `CURSOR_STEP_DP` in `KeyboardButtonView` (11dp/char).
- Cursor haptic tick reuses the keypress vibration length (`vibrateLength`).
- Auto-split screen threshold: 600dp in `CodeBoardIME.onCreateInputView`.

---

## Reference
- [README.md](./README.md): features, build and install, settings, and the
  architecture and codebase map.
- Upstream project: [gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard).
