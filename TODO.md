# Codeboard TODO

Task list for **wjeroen/codeboard**, a maintained fork of
[gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard).

How the app works, how to build it, and the detailed plans for the features
below all live in the [README](./README.md). This file is just the checklist.

---

## Current Sprint

### High Priority
- [ ] Confirm GitHub Actions actually builds an APK. The `android.yml` workflow
      shows **0 runs** so far. Make a run go green and download the
      `codeboard-debug` artifact (see README → "If no build appears" for why a
      run may not start: Actions disabled on a fork, or a push made by an
      automation token).

### Features to Implement
- [ ] **Feature B: brighter key-press preview** (small). The preview reuses the
      normal key color, so it looks identical. Add a lightened `previewBodyPaint`.
      Do this first; Feature C depends on it. (README → Roadmap)
- [ ] **Feature A: space-bar cursor navigation** (moderate). Long-press Space and
      drag to move the cursor, Gboard-style. (README → Roadmap)
- [ ] **Feature C: accent / long-press alternate characters** (medium-large).
      Hold a letter to pick `é`, `à`, etc. Builds on Feature B. (README → Roadmap)
- [ ] **Feature D: split keyboard for tablets/foldables** (medium, low urgency).
      Geometry transform on the normalized key boxes. (README → Roadmap)

### Bug Fixes
- [ ] (none open)

### Testing
- [ ] Build the debug APK in CI, install it on a device, and smoke-test the three
      already-merged changes below. None of them have ever been compiled (see note).

### Documentation
- [x] Rewrite README: full feature list, build/CI steps, settings, and a codebase map (2026-06-25)
- [x] Convert this TODO into a proper checklist (2026-06-25)

---

## Completed Recently
- [x] Rewrite the CI workflow to build on `ubuntu-latest` with `assembleDebug`,
      modelled on the working TrackyTime workflow (was `windows-latest` +
      `gradlew.bat assemble`); added a manual "Run workflow" trigger (2026-06-25)
- [x] Fix attribution: upstream is `gazlaws-dev/codeboard`, not `gazlaws/codeboard`
      (the old link 404s); credit the original author by name with a link (2026-06-25)
- [x] Settings **export/import** ("Backup" section) via the Storage Access
      Framework; serialises all prefs to JSON (2026-06-25)
- [x] Third SYM row fix: a 3rd symbol row now augments the F-key/PgUp/PgDn block
      instead of being ignored (`CodeBoardIME.java`) (2026-06-25)

> ⚠️ The three app-code items (backup, SYM-row fix) were code-reviewed but
> **never compiled** (the previous sandbox blocked `dl.google.com`, so Gradle
> could not run). They need a real CI build and an on-device smoke test before
> being trusted.

---

## Future Ideas
- [ ] Credit the original author / upstream in the in-app About screen too
      (currently only the README does).
- [ ] Per-layout accent maps (extends Feature C) for AZERTY / QWERTZ users.
- [ ] Optional vertical drag for line navigation in Feature A.
- [ ] Optional signed debug APK in CI (TrackyTime-style keystore in repo
      secrets) so CI builds install over each other without uninstalling.

---

## Reference
- [README.md](./README.md): features, build/install, settings, architecture and
  codebase map, and the full implementation plans for Features A to D.
- Upstream project: [gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard).
