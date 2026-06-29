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

- [Differences from the original](#differences-from-the-original)
- [Features](#features)
- [Install](#install)
- [Build from source](#build-from-source)
- [Continuous integration (building APKs on GitHub)](#continuous-integration-building-apks-on-github)
- [Using the keyboard](#using-the-keyboard)
- [Long-press reference](#long-press-reference)
- [Settings reference](#settings-reference)
- [Backup and restore](#backup-and-restore)
- [Architecture and codebase map](#architecture-and-codebase-map)
- [Project facts](#project-facts)
- [Credits and license](#credits--license)

---

## Differences from the original

High-level changes in this fork versus
[gazlaws-dev/codeboard](https://github.com/gazlaws-dev/codeboard). If you are
deciding whether to switch, this is what to expect:

- **Separate, coexisting app.** Its own app ID (`com.gazlaws.codeboard.fork`),
  name ("CodeBoard Fork"), and a purple theme, so it installs alongside the
  original instead of replacing it.
- **Working signed APK builds** from GitHub Actions, so you can download and
  install a build without a local toolchain.
- **Shift+Enter inserts a real newline** in apps where Enter sends (chat apps).
- **Settings backup:** export and import all settings (custom rows, themes, pins)
  to a JSON file.
- **One keyboard entry** in the system switcher (the redundant French one is gone).
- **Gboard-style long-press keyboard on all four layouts** (QWERTY, AZERTY, Dvorak,
  QWERTZ): corner symbols on every letter, a bright press preview, and a hold-to-open
  grid of accents/symbols you slide to pick (see
  [Long-press reference](#long-press-reference)).
- **Fixed number row** (`1`–`0`) with long-press fraction popups (e.g. hold `1` for
  `½ … ⅒`), replacing the old editable top row.
- **Spacebar cursor:** drag the spacebar left/right to move the caret (haptic tick per
  character).
- **Split keyboard** for wide screens (tablets/foldables), Off/Auto/On in settings;
  splits the letters, number row, and symbol rows, and caps each half's width so the
  keys stay near your thumbs instead of stretching on very wide screens.
- **Up-arrow Shift key** (matches the backspace width) with a clear **caps-lock**
  indicator (underlined arrow; double-tap or long-press to lock), and a symmetric
  bottom row (equal-width Ctrl and Enter, letter-width comma/period).
- Plus fixes: symbol-row defaults, a third symbol row, the settings banner overlap.
- **Planned:** an advanced clipboard history (see [`TODO.md`](./TODO.md)).

## Features

- **Programmer symbols front and center:** braces `{ [ < (`, plus `; / \ ! | & $`
  and friends, without flipping to a symbol page.
- **Editing keys built in:** cut, copy, paste, select-all, undo/redo, Tab, Esc.
- **Arrow keys** (left / down / up / right), all auto-repeating on hold.
- **Four base layouts:** QWERTY, AZERTY, Dvorak, QWERTZ, all Gboard-style (corner
  symbols + long-press accent/symbol popups).
- **Fixed number row** (`1 2 3 4 5 6 7 8 9 0`) with long-press fraction popups, shared
  by every layout.
- **Seven themes** (System Default, Material Dark, Material Light, Pure Black,
  White, Blue, Purple) plus a fully custom foreground/background color option.
- **Customisable symbol rows.** The SYM-page rows and the optional extra main rows are
  plain text fields in settings, so you can type your own row of characters. (The number
  row and the letter rows are fixed.)
- **Clipboard pins (Ctrl+SYM):** up to 7 saved snippets you can paste instantly.
- **Backup and restore:** export every setting and custom symbol row to a JSON
  file, then import it on another device or after a reinstall.
- **Quality-of-life:** adjustable key size (portrait and landscape separately),
  font size, optional key-press sound, vibration (with tunable length), a
  key-press preview, key borders, and an optional notification shortcut to pop
  the keyboard open.
- **No ads, no in-app purchases.** Made for students.

The keyboard registers a single English (`en_US`) IME subtype, so it shows up
once in the system keyboard switcher.

---

## Install

Two ways to get the app:

1. **Download a build (easiest).** Grab the latest debug APK from
   [GitHub Actions](#continuous-integration-building-apks-on-github): open the
   newest **Build APK** run, download the `codeboard-debug` artifact, unzip it,
   and install `app-debug.apk`.
2. **Build it yourself.** See [Build from source](#build-from-source).

After installing, you have to **enable** the keyboard (Android hides new IMEs by
default): open the Codeboard app, follow the intro/tutorial, then in
*System Settings, Languages & input, On-screen keyboards*, switch Codeboard on
and select it as your active keyboard.

---

## Build from source

Codeboard is a standard Gradle Android project. You need **JDK 17** and the
Android SDK (Android Studio is the easiest way to get both).

```bash
# Debug APK (installable, signed with the repo's committed debug key)
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
[TrackyTime](https://github.com/wjeroen/TrackyTime) repo.

### Signing (why builds install over each other)

Every build is signed with the **same** debug key, so a new APK installs straight
over an older one with no uninstall. The key comes from a `debug.keystore`
committed at the repo root. That is safe: a *debug* keystore is not a secret (it
uses Android's well-known debug credentials), and it is never used for store
releases.

If you would rather keep the keystore out of the repo, add these four repository
secrets and delete `debug.keystore`; the build picks them up automatically when
they are present:

- `DEBUG_KEYSTORE_BASE64` (the keystore file, base64-encoded)
- `DEBUG_KEYSTORE_PASSWORD`
- `DEBUG_KEY_ALIAS`
- `DEBUG_KEY_PASSWORD`

This is **debug** signing only. A real **release** signing key must never be
committed, always keep that in secrets.

---

## Using the keyboard

- **SYM key:** switches between the main page and the symbol pages.
- **Ctrl+SYM:** opens the clipboard-pins page (your 7 saved snippets).
- **Top action row:** Esc, Tab, arrows / editing keys, and SYM. The arrow and
  editing rows are defined in `Definitions.java`.
- **Shift / Ctrl:** tap to use once. **Long-press Ctrl to lock** it on. Shift is an
  up-arrow icon (Gboard-style), the same width as backspace.
- **Caps lock:** **double-tap Shift** (or long-press it) to lock; the arrow gains an
  underline while it is on. A single tap turns it back off.
- **Drag the Spacebar** left/right to move the cursor character by character, with a
  haptic tick per character (a plain tap still types a space). This replaced the old
  space long-press IME picker.
- **Split keyboard:** Settings has an Off / Auto / On switch. Auto splits the keyboard
  into two halves on wide screens (>= 600dp, tablets/foldables). The letter rows
  duplicate their inner key (e.g. G and V on QWERTY) so each thumb has one; the number,
  symbol, and custom rows split down the middle. On very wide screens each half stops
  growing once it reaches 5 key-heights wide and the central gap takes the extra space,
  so the keys stay near your thumbs instead of stretching across the screen.
- **Hold an arrow key** to auto-repeat it.

---

## Long-press reference

All four layouts are Gboard-style. Each letter shows a small **corner symbol**
(top-right). **Pressing** a key instantly shows a bright popup cell above it with just
that character (same square shape as a grid cell). **Holding** for 300ms (Gboard's
default long-press delay) expands that cell into a popup grid of alternates: the
**[default]** (shown in brackets, which is also the corner symbol) plus every option.
Lifting without sliding types the default; sliding the finger onto another cell
highlights it and types that one instead. Single-option keys work the same way, they
just show one cell.

Every key uses this same floating cell now (Ctrl, Backspace, Enter, the F-keys, and so
on, not just letters), each with a soft drop shadow, floating just above the key. The
spacebar is the one exception: it stays put and brightens, because dragging it is the
cursor control. While Shift is held, the alternates are shown capitalised, matching what
gets typed.

> The tables below are the **QWERTY** reference. AZERTY, Dvorak, and QWERTZ share the
> same per-letter accents (a letter's accents follow the letter, not its position), but
> their popup *arrangement* (which cell is the default, column count) is generic for now
> and may be tuned per layout later. See [`TODO.md`](./TODO.md).

**Number row** (fixed `1`–`0`, no corner symbol; hold for the superscript default plus
fractions):

```
1 → ⅙ ⅐ ⅛ ⅑ ⅒  /  [¹] ½ ⅓ ¼ ⅕
2 → [²] ⅖ ⅔
3 → [³] ⅗ ¾ ⅜
4 → [⁴] ⅘
5 → [⁵] ⅝ ⅚
6 → [⁶]
7 → ⅞ [⁷]
8 → [⁸]
9 → [⁹]
0 → ⁿ ∅ [⁰]
```

**Corner symbols** (letters)

```
Row 1:  Q %   W \   E |   R =   T [   Y ]   U <   I >   O {   P }
Row 2:  A @   S #   D €   F -   G &   H -   J +   K (   L )
Row 3:  Z *   X "   C '   V :   B ;   N !   M ?
```

**Multi-character popups** (default in `[ ]`):

```
A → æ ã å ā ɒ ɑ  /  [@] à á â ä ɑ̃
E → ę ë ē ė ə ɛ̃  /  è [|] é ê ɜ ɛ
I → ɪ ij į ì ĩ  /  ī ï î [>] í
O → ɔ̃ œ̃ õ ō ø ò  /  ɔ œ ö ô ó [{]
U → ũ ù ū ʊ  /  û [<] ú ü
Y → ʏ ij [ ] ] ÿ ý
C → ć ['] ç č
N → ŋ ɲ ń [!] ñ
S → [#] ß ʃ
J → [+] j́
D → [€] $ £ ¥ ¢ ð
```

**Symbol keys** (corner symbol is the default; some add IPA):

```
Q [%] ʔ   W [\]   R [=] ʁ ɹ ɾ ʀ   T [[] θ   P [}]
F [-]   G [&] ɣ   H [-] ɦ   K [(]   L [)]
Z [*] ʒ   X ["]   V [:] ʌ   B [;]   M [?]
```

**Bottom row** (`Ctrl  ,  space  .  enter`; Ctrl and Enter are equal width, comma and
period are letter width):

```
,  (comma,  corner `) → [`] ː ˈ ˌ
.  (period, corner ,) → · _ & % " +
                        - : @ ' / ;
                        ( ) # ! [,] ?
```

The backtick `` ` `` lives here now (it used to be on the old editable number row); the
comma key's IPA stress/length marks are kept as slide alternates.

---

## Settings reference

Settings live in *Codeboard app → Settings*, backed by
[`res/xml/preferences.xml`](app/src/main/res/xml/preferences.xml). Categories:

| Category | What it controls |
|---|---|
| **View Keyboard** | Open the IME picker; a scratch text field to test typing. |
| **Features** | Key-press sound, vibration (+ length in ms), font size, keyboard size (portrait and landscape), key-press preview, notification shortcut. |
| **Colour** | Theme picker, custom theme toggle with background/foreground color pickers, key borders, dynamic navigation-bar coloring. |
| **Layout** | Base layout (QWERTY/AZERTY/Dvorak/QWERTZ), **Split keyboard (Off/Auto/On)**, "top row actions" toggle, and editable text for the customisable symbol rows (main second/bottom, SYM top/second/third/fourth). The number row and the letter rows are fixed. |
| **Clipboard [Ctrl+SYM]** | The 7 pinned clipboard snippets. |
| **Backup** | Export / import all settings (see below). |
| **Restore** | Reset everything to default, or reset just the symbols to the "Old Codeboard" layout. |
| **About** | Restart the tutorial, and an "Open on GitHub" link to this fork. |

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
   is handled in its `onTouchEvent` (`ACTION_DOWN` / `MOVE` / `UP` / `CANCEL`).
   `ACTION_MOVE` drives the long-press alternates popup (sliding to pick a character)
   and the spacebar cursor drag (`handleSpaceCursorDrag`).
2. **Geometry is normalized.** Keys are positioned with `Box` coordinates that
   are fractions from 0.0 to 1.0 of the keyboard's width and height. They are
   converted to pixels in `KeyboardButtonView.layout()`. Because geometry is purely
   relative, the split keyboard is just extra keys plus an `addGap` spacer in the
   middle of each row (`Definitions.addGboardQwertyRows(builder, split)`); the gap is
   a real "key" with no view (`isSpacer`, skipped by `KeyboardUiFactory`).
3. **Press preview, then long-press alternates.** Both are the same `PopupKeyboardView`
   in a non-touchable `PopupWindow` above the key. (a) The instant *preview* (character
   keys, `hasCharPreview()`): on press, a single bright cell showing just the pressed
   character (`configurePreview`). (b) The *alternates*: holding for `POPUP_DELAY_MS`
   (300ms) calls `showPopup()`, which resizes that same popup into the full grid
   (`configure(KeyInfo)`) with the default cell bright. The popup is anchored so the
   default/selected cell sits above the key. Icon, modifier, and multi-char keys have
   no text popup, so they instead lift in place (`setTranslationY(-200)` + scale) in
   `animatePress()`.
4. **Long-press has two paths.** For modifier keys, `CodeBoardIME.onPress()` starts a
   `Timer` that fires `onKeyLongPress()` after `ViewConfiguration.getLongPressTimeout()`;
   it handles shift-lock (code 16) and ctrl-lock (code 17). For keys that have an
   alternates popup, `KeyboardButtonView` runs its **own** `Handler`-based long-press
   that opens the popup. The **spacebar** no longer opens the IME picker on long-press;
   instead, dragging it horizontally moves the caret (`onSpaceCursorMove` uses
   `InputConnection.setSelection` so the caret stays inside the field and stops at the
   ends, with a per-character haptic tick; it sets `longPressedSpaceButton` so no space
   is typed). A boundary-guarded arrow-key fallback covers editors without
   `getExtractedText`.

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
| `layout/Definitions.java` | Concrete key/row definitions: the four base layouts (QWERTY/AZERTY/Dvorak/QWERTZ), the fixed number row (`addGboardNumberRow`), the Gboard-style long-press rows (corner symbols + alternates) and bottom row, the arrows row, the copy/paste row, and custom symbol rows. Per-letter long-press is split into shared accent **data** (`letterSymbol` / `letterAccents`) and per-layout **arrangement** (`addLetterKey`, the seam for future per-layout tuning; QWERTY keeps a hand-tuned arrangement inline). `splitCurrentRow` adds the central split gap to rows that have no manual split. |
| `layout/builder/KeyboardLayoutBuilder.java` | Fluent builder that assembles rows/keys and computes the normalized layout. |
| `layout/builder/KeyboardLayoutRowBuilder.java` | Distributes key widths within a single row. |
| `layout/builder/KeyInfo.java` | The per-key data model (label, codes, shift behavior, the long-press fields `cornerLabel` / `popupChars` / `popupDefaultIndex` / `popupColumns`, and `isSpacer` for empty gap "keys"). |
| `layout/builder/KeyboardLayoutException.java` | Thrown on malformed layout definitions. |
| `layout/ui/KeyboardButtonView.java` | The per-key `View`: drawing, touch (`onTouchEvent`), the press animation (`animatePress` / `animateRelease`), and the long-press alternates popup (`showPopup` / `PopupWindow`). |
| `layout/ui/PopupKeyboardView.java` | The grid of long-press alternates drawn above a held key. Display-only: the key view forwards absolute finger coordinates so this view can highlight the cell under the finger; the key view reads `getSelectedChar()` on release. |
| `layout/ui/KeyboardLayoutView.java` | The container `ViewGroup` that lays out all the key views from their `Box` coordinates. |
| `layout/ui/KeyboardUiFactory.java` | Builds the view hierarchy for a given layout + theme. |
| `theme/UiTheme.java` | The `Paint`s and colors the views draw with, including `previewBodyPaint` (brighter pressed key / popup cell), `popupSelectedPaint` (the highlighted alternate), and `cornerPaint` (the small corner symbol). |
| `theme/ThemeDefinitions.java` | The seven built-in theme presets. |
| `theme/ThemeInfo.java` | Data holder for a single theme's colors. |
| `theme/IOnFocusListenable.java` | Small focus-callback interface. |
| `KeyboardPreferences.java` | Every `SharedPreferences` read/write in one place, plus `exportToJson()` / `importFromJson()` for Backup. |
| `SettingsFragment.java` | The settings screen logic, including the Storage Access Framework launchers for Backup. |
| `MainActivity.java` | Launcher activity and the IME's settings host. |
| `IntroActivity.java` / `IntroFragment.java` | First-run tutorial (built on the AppIntro library). |
| `NotificationReceiver.java` | Backs the "open keyboard using notification" feature. |
| `res/xml/preferences.xml` | The settings screen definition. |
| `res/xml/method.xml` | IME metadata: declares the single `en_US` subtype. |
| `res/values/array.xml` | The Layouts and Themes lists shown in settings. |

### Tests

| File | Covers |
|---|---|
| `test/.../KeyboardLayoutRowBuilderTest.java` | Pure-JVM unit test of row width distribution. |
| `androidTest/.../DefinitionsTest.java` | Layout definitions (instrumented). |
| `androidTest/.../KeyboardLayoutBuilderTest.java` | Layout builder (instrumented). |
| `androidTest/.../MainActivityTest.java` | Main activity (instrumented). |

---

## Project facts

| | |
|---|---|
| Application ID | `com.gazlaws.codeboard.fork` (distinct from the original `com.gazlaws.codeboard`, so both can be installed side by side) |
| App name | "CodeBoard Fork" (placeholder, rename in `res/values/strings.xml`) |
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
