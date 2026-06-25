# Codeboard

[![Build APK](https://github.com/wjeroen/codeboard/actions/workflows/android.yml/badge.svg)](https://github.com/wjeroen/codeboard/actions/workflows/android.yml)

**A coding keyboard (IME) for Android.** No more switching between letters,
numbers and symbols just to type `{`, `}`, `;`, `|` or `&`. Codeboard puts the
characters programmers actually use one tap away, with arrow keys, Tab, Esc,
and copy/cut/paste/select-all built right in.

This repository (`wjeroen/codeboard`) is a **maintained fork** of the original
[**gazlaws-dev/codeboard**](https://github.com/gazlaws-dev/codeboard) by
**gazlaws**. All credit for the original app goes to them. If you find Codeboard
useful, please consider supporting the original author (see [Credits](#credits--license)).

---

## Table of contents

- [Features](#features)
- [Install](#install)
- [Build from source](#build-from-source)
- [Continuous integration (building APKs on GitHub)](#continuous-integration-building-apks-on-github)
- [Using the keyboard](#using-the-keyboard)
- [Settings reference](#settings-reference)
- [Backup and restore](#backup-and-restore)
- [Architecture and codebase map](#architecture-and-codebase-map)
- [Roadmap (planned features)](#roadmap-planned-features)
- [Project facts](#project-facts)
- [Credits and license](#credits--license)

---

## Features

- **Programmer symbols front and center:** braces `{ [ < (`, plus `; / \ ! | & $`
  and friends, without flipping to a symbol page.
- **Editing keys built in:** cut, copy, paste, select-all, undo/redo, Tab, Esc.
- **Arrow keys** (left / down / up / right), all auto-repeating on hold.
- **Four base layouts:** QWERTY, AZERTY, Dvorak, QWERTZ.
- **Seven themes** (System Default, Material Dark, Material Light, Pure Black,
  White, Blue, Purple) plus a fully custom foreground/background color option.
- **Fully customisable symbol rows.** Every symbol row (main and SYM pages) is a
  plain text field in settings, so you can type your own row of characters.
- **Clipboard pins (Ctrl+SYM):** up to 7 saved snippets you can paste instantly.
- **Backup and restore:** export every setting and custom symbol row to a JSON
  file, then import it on another device or after a reinstall.
- **Quality-of-life:** adjustable key size (portrait and landscape separately),
  font size, optional key-press sound, vibration (with tunable length), a
  key-press preview, key borders, and an optional notification shortcut to pop
  the keyboard open.
- **No ads, no in-app purchases.** Made for students.

The keyboard ships two IME subtypes out of the box: English (`en_US`) and
French (`fr_FR`).

---

## Install

**Option 1: Google Play (original app).** The upstream build is on the
[Play Store](https://play.google.com/store/apps/details?id=com.gazlaws.codeboard).

**Option 2: Build it yourself.** See [Build from source](#build-from-source)
below, or grab a freshly built debug APK from
[GitHub Actions](#continuous-integration-building-apks-on-github).

After installing, you have to **enable** the keyboard (Android hides new IMEs by
default): open the Codeboard app, follow the intro/tutorial, then in
*System Settings → Languages & input → On-screen keyboards*, switch Codeboard on
and select it as your active keyboard.

---

## Build from source

Codeboard is a standard Gradle Android project. You need **JDK 17** and the
Android SDK (Android Studio is the easiest way to get both).

```bash
# Debug APK (installable, signed with the local debug key)
./gradlew assembleDebug

# Output:
#   app/build/outputs/apk/debug/app-debug.apk

# Release APK (unsigned unless you add a signing config)
./gradlew assembleRelease
```

On Windows use `gradlew.bat` instead of `./gradlew`.

> **Note:** the first build downloads the Android Gradle Plugin and
> dependencies from `dl.google.com` and Maven Central. A sandbox that blocks
> those hosts (some CI/agent environments do) cannot build the app. GitHub's
> hosted runners are not restricted, which is why the CI build below works.

---

## Continuous integration (building APKs on GitHub)

The workflow at [`.github/workflows/android.yml`](.github/workflows/android.yml)
builds a debug APK automatically.

- **Triggers:** every `push`, plus a manual **"Run workflow"** button
  (`workflow_dispatch`) on the Actions tab.
- **Runner:** `ubuntu-latest` with Temurin JDK 17.
- **Command:** `./gradlew assembleDebug --no-daemon --stacktrace`.
- **Result:** the APK is uploaded as a build **artifact** named
  `codeboard-debug` (kept for 7 days). Download it from the run's summary page,
  unzip, and install `app-debug.apk` on your device.

### If no build appears

GitHub Actions only runs when it is enabled and when a push is made with normal
credentials. If you push the workflow and see no run, check, in order:

1. **Actions enabled?** On a forked repo, GitHub disables Actions until you open
   the **Actions** tab once and click *"I understand my workflows, go ahead and
   enable them."* Forks start with Actions off.
2. **Who pushed?** Pushes made by a GitHub App / automation token (for example,
   commits created through an API or bot) deliberately do **not** trigger
   workflows. Push from your own machine, or use the **Run workflow** button on
   the Actions tab to start it by hand.
3. **Right branch?** The workflow runs on the branch you pushed. Merge to
   `master` (or use *Run workflow* and pick the branch) to build the mainline.

This workflow is modelled on the working build in the author's
[TrackyTime](https://github.com/wjeroen/TrackyTime) repo. The main difference:
TrackyTime signs its APK with a debug keystore stored in repo **secrets**;
Codeboard does not need that, because its `build.gradle` has no signing config,
so the runner's default debug key signs `app-debug.apk` and it installs fine.

---

## Using the keyboard

- **SYM key:** switches between the main page and the symbol pages.
- **Ctrl+SYM:** opens the clipboard-pins page (your 7 saved snippets).
- **Top action row:** Esc, Tab, arrows / editing keys, and SYM. The arrow and
  editing rows are defined in `Definitions.java`.
- **Shift / Ctrl:** tap to use once. **Long-press Shift or Ctrl to lock** it on.
- **Long-press Space:** opens the system keyboard picker (switch to another IME).
- **Hold an arrow key** to auto-repeat it.

---

## Settings reference

Settings live in *Codeboard app → Settings*, backed by
[`res/xml/preferences.xml`](app/src/main/res/xml/preferences.xml). Categories:

| Category | What it controls |
|---|---|
| **View Keyboard** | Open the IME picker; a scratch text field to test typing. |
| **Features** | Key-press sound, vibration (+ length in ms), font size, keyboard size (portrait and landscape), key-press preview, notification shortcut. |
| **Colour** | Theme picker, custom theme toggle with background/foreground color pickers, key borders, dynamic navigation-bar coloring. |
| **Layout** | Base layout (QWERTY/AZERTY/Dvorak/QWERTZ), "top row actions" toggle, and editable text for every symbol row (main top/second/bottom, SYM top/second/third/fourth). |
| **Clipboard [Ctrl+SYM]** | The 7 pinned clipboard snippets. |
| **Backup** | Export / import all settings (see below). |
| **Restore** | Reset everything to default, or reset just the symbols to the "Old Codeboard" layout. |
| **About** | Restart the tutorial, rate on Play, open the upstream project on GitHub. |

---

## Backup and restore

The **Backup** category exports every `SharedPreferences` value (including your
custom symbol rows and clipboard pins) to a single JSON file, and imports it
back later.

- It uses Android's **Storage Access Framework** (the system file picker), so it
  needs **no extra permissions** and writes wherever you choose (Drive, local
  storage, etc.).
- Implementation: `KeyboardPreferences.exportToJson()` / `importFromJson()`
  handle the (de)serialisation; `SettingsFragment` wires up two
  `ActivityResultLauncher` instances for the create-document / open-document
  flows.

---

## Architecture and codebase map

**The big idea:** Codeboard is a **fully custom IME**. It does **not** use
Android's built-in `KeyboardView` / `Keyboard` XML system. Every key is its own
small `View`, and the whole keyboard is laid out from normalized coordinates.
Understanding these few facts explains most of the code:

1. **Each key is a `View`.** A key is a `KeyboardButtonView extends View`. Touch
   is handled in its `onTouchEvent`, which currently only handles `ACTION_DOWN`
   and `ACTION_UP` (there is **no** `ACTION_MOVE` handling yet, which is why
   drag-based features like cursor-on-spacebar are not implemented).
2. **Geometry is normalized.** Keys are positioned with `Box` coordinates that
   are fractions from 0.0 to 1.0 of the keyboard's width and height. They are
   converted to pixels in `KeyboardButtonView.layout()`. Because geometry is
   purely relative, layout transforms (like a future split keyboard) are easy.
3. **The "preview" is the key itself, lifted.** When `enablePreview` is on,
   pressing a key does not show a separate popup. The same key `View` is lifted
   with `setTranslationY(-200)` + `setScaleX/Y(1.2)` + `setElevation(21)` inside
   `animatePress()`. It reuses the normal key paint, which is why the preview
   looks the same color as the key (see Roadmap → Feature B).
4. **Long-press is a `Timer`.** `CodeBoardIME.onPress()` starts a `Timer` that
   fires `onKeyLongPress()` after `ViewConfiguration.getLongPressTimeout()`.
   Today `onKeyLongPress` handles shift-lock (code 16), ctrl-lock (code 17), and
   space (code 32, which opens the IME picker). A `longPressedSpaceButton` flag
   suppresses the normal space character when space was long-pressed.

### How a key-press becomes text (high level)

```
finger down on a KeyboardButtonView
        │
        ▼
onTouchEvent(ACTION_DOWN) ──► animatePress()  (lift/scale the key view)
        │                  └► CodeBoardIME.onPress(code)  (start long-press Timer)
        │
        ├─ (held long enough) ─► Timer fires ─► CodeBoardIME.onKeyLongPress(code)
        │                                        (shift-lock / ctrl-lock / IME picker)
        ▼
onTouchEvent(ACTION_UP) ───► animateRelease()  (drop the key view back)
                          └► CodeBoardIME.onKey(code) / onText(text)
                                   │
                                   ▼
                          InputConnection.commitText(...)      (normal characters)
                          InputConnection.sendKeyEvent(...)    (arrows, Tab, editing)
```

### File map

| File | Role |
|---|---|
| `CodeBoardIME.java` | The IME service. Assembles the keyboard, owns the long-press `Timer`, `onPress` / `onKey` / `onText` / `onKeyLongPress`, and dispatches characters and key events to the active text field. |
| `layout/Box.java` | Normalized (0.0 to 1.0) rectangle used to position a key. |
| `layout/Key.java` | Runtime model of a placed key (its `Box` + its `KeyInfo`). |
| `layout/Definitions.java` | Concrete key/row definitions: the four base layouts (QWERTY/AZERTY/Dvorak/QWERTZ), the arrows row, the copy/paste row, and custom symbol rows. **Accent data for Feature C goes here.** |
| `layout/builder/KeyboardLayoutBuilder.java` | Fluent builder that assembles rows/keys and computes the normalized layout. |
| `layout/builder/KeyboardLayoutRowBuilder.java` | Distributes key widths within a single row. |
| `layout/builder/KeyInfo.java` | The per-key data model (label, codes, shift behavior, etc.). **Add `popupCharacters` here for Feature C.** |
| `layout/builder/KeyboardLayoutException.java` | Thrown on malformed layout definitions. |
| `layout/ui/KeyboardButtonView.java` | The per-key `View`: drawing, touch (`onTouchEvent`), and the press animation (`animatePress` / `animateRelease`). |
| `layout/ui/KeyboardLayoutView.java` | The container `ViewGroup` that lays out all the key views from their `Box` coordinates. |
| `layout/ui/KeyboardUiFactory.java` | Builds the view hierarchy for a given layout + theme. |
| `theme/UiTheme.java` | The `Paint`s and colors the views draw with. **Add `previewBodyPaint` here for Feature B.** |
| `theme/ThemeDefinitions.java` | The seven built-in theme presets. |
| `theme/ThemeInfo.java` | Data holder for a single theme's colors. |
| `theme/IOnFocusListenable.java` | Small focus-callback interface. |
| `KeyboardPreferences.java` | Every `SharedPreferences` read/write in one place, plus `exportToJson()` / `importFromJson()` for Backup. |
| `SettingsFragment.java` | The settings screen logic, including the Storage Access Framework launchers for Backup. |
| `MainActivity.java` | Launcher activity and the IME's settings host. |
| `IntroActivity.java` / `IntroFragment.java` | First-run tutorial (built on the AppIntro library). |
| `NotificationReceiver.java` | Backs the "open keyboard using notification" feature. |
| `res/xml/preferences.xml` | The settings screen definition. |
| `res/xml/method.xml` | IME metadata: declares the `en_US` and `fr_FR` subtypes. |
| `res/values/array.xml` | The Layouts and Themes lists shown in settings. |

### Tests

| File | Covers |
|---|---|
| `test/.../KeyboardLayoutRowBuilderTest.java` | Pure-JVM unit test of row width distribution. |
| `androidTest/.../DefinitionsTest.java` | Layout definitions (instrumented). |
| `androidTest/.../KeyboardLayoutBuilderTest.java` | Layout builder (instrumented). |
| `androidTest/.../MainActivityTest.java` | Main activity (instrumented). |

---

## Roadmap (planned features)

These are designed but **not yet implemented**. They are listed as checkboxes in
[`TODO.md`](./TODO.md); the implementation notes live here. Suggested order:
**B → A → C → D** (B is a prerequisite for C's popup styling).

### Feature B: Brighter key-press preview *(small)*

**Problem:** `animatePress()` lifts the key view but draws it with the same
`buttonBodyPaint` as an unpressed key, so the preview looks identical in color.

**Plan:**
1. In `UiTheme.java`, add a `previewBodyPaint` field, computed as a lightened
   version of the background, for example
   `ColorUtils.blendARGB(info.backgroundColor, Color.WHITE, 0.25f)`. This is
   theme-aware: dark themes lighten, light themes stay readable.
2. In `KeyboardButtonView.java`, track an `isPreviewActive` boolean (set in
   `animatePress()`, cleared in `animateRelease()`, `invalidate()` on change).
   In the body-draw step, pick `previewBodyPaint` when active.

### Feature A: Space-bar cursor navigation, Gboard-style *(moderate)*

**Goal:** long-press Space, then drag horizontally to move the cursor character
by character (optionally vertical drag for line up/down).

**Plan:**
1. Add `ACTION_MOVE` handling to `KeyboardButtonView.onTouchEvent`. Once a
   long-press has fired, accumulate horizontal delta; every ~1 key-width, emit a
   left/right cursor move and reset the accumulator.
2. **Axis-lock** on first meaningful movement (horizontal *or* vertical) to avoid
   diagonal jumping.
3. Dispatch movement with `InputConnection.sendKeyEvent(KEYCODE_DPAD_LEFT/RIGHT`
   (and `UP/DOWN`). The app already sends key events this way.
4. Reuse the existing `longPressedSpaceButton` flag to suppress the space
   character on release.
5. Decide what happens to the current space-long-press IME picker: keep it only
   when no drag occurred, or move it to another gesture.

**Touches:** `KeyboardButtonView.java`, `CodeBoardIME.java`.

### Feature C: Accent / alternate characters on long-press *(medium-large)*

**Goal:** hold a letter to pick an accented variant (for example hold `e` to get
`é è ê ë ē`).

**Plan:**
- **Data model:** add `String[] popupCharacters` to `KeyInfo.java`; add a
  `withPopup(String chars)` builder method to `KeyboardLayoutBuilder.java`;
  attach accent sets per letter in `Definitions.java` (a shared
  `Map<Character, String>` keeps all four layouts consistent).
- **Trigger:** when long-press fires and `popupCharacters != null`, show a popup
  and suppress the normal key output on release.
- **Popup UI:** reuse the preview-lift look. Show the alternates as a small row
  in a `PopupWindow` anchored above the pressed key, drawn with Feature B's
  `previewBodyPaint`. Tapping one commits it via the existing `onText` path.

**Touches:** `KeyInfo.java`, `KeyboardLayoutBuilder.java`, `Definitions.java`,
`KeyboardButtonView.java`, `UiTheme.java` (do Feature B first).

### Feature D: Split keyboard for tablets / foldables *(medium, low urgency)*

**Goal:** a centered gap that pushes the left half left and the right half right,
for thumb typing on wide screens.

**Plan:** because keys use normalized `Box` coordinates, implement the split as a
post-processing geometry transform in `KeyboardLayoutBuilder.build()`: compress
left-half keys into `[0, 0.5 - gap]` and right-half keys into `[0.5 + gap, 1.0]`.
The middle gap has no child views, so touches there are naturally ignored. No
changes to touch, drawing, or input dispatch are needed. Gate it behind an
Off/Auto/On preference, where Auto detects wide screens via
`getResources().getConfiguration().screenWidthDp >= 600`.

**Touches:** `KeyboardLayoutBuilder.java`, `CodeBoardIME.java`,
`KeyboardPreferences.java`, `res/xml/preferences.xml`.

---

## Project facts

| | |
|---|---|
| Application ID | `com.gazlaws.codeboard` |
| Version | 6.0.3 (versionCode 23) |
| Min SDK | 23 (Android 6.0) |
| Target SDK | 35 |
| Compile SDK | 34 |
| Language | Java |
| Build | Gradle 8.10.2, Android Gradle Plugin 8.8.0, JDK 17 |
| Permissions | `VIBRATE`, `POST_NOTIFICATIONS` |
| Notable libraries | AndroidX AppCompat / Preference, Material Components, AppIntro, a Material color-picker dialog |

---

## Credits & license

- **Original app and all original code:** [**gazlaws**](https://github.com/gazlaws-dev)
  via [gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard). Please
  support the original author:

  <a href="https://www.buymeacoffee.com/gazlaws" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy the original author a coffee" style="height: 41px !important;width: 164px !important;"></a>

- **This fork:** maintained at [wjeroen/codeboard](https://github.com/wjeroen/codeboard).

See [`LICENSE.txt`](./LICENSE.txt) for license terms.
