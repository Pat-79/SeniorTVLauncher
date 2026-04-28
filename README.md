# Senior TV Launcher

A clean, simple, and senior-friendly Android TV / Google TV launcher designed for elder users who need a distraction-free experience with large, easy-to-use buttons.

## What is it?

**Senior TV Launcher** is a home screen replacement for Android TV and Google TV devices. It shows a large clock, date, and a customisable grid of app buttons — nothing more. Settings are protected against accidental changes by a press-and-hold gesture followed by a randomly generated PIN code displayed on screen.

**Package name:** `nl.awayfromhome.seniortvlauncher`  
**Minimum SDK:** API 21 (Android 5.0 Lollipop)  
**Target SDK:** API 34

---

## Features

- **Big, clear app buttons** with optional icon-colour glow effect
- **Configurable grid** — set the number of rows and columns
- **Button shapes** — Circle, Rounded Square, or Square
- **Adjustable button size** (80 dp – 200 dp)
- **Optional app name** displayed below each icon
- **Clock and date display** — respects system 12/24 h setting and locale
- **Background image** — optional, loaded from device storage
- **Background blur** — optional blur effect with adjustable level
- **Fully D-pad navigable** — works with a TV remote, no touchscreen required
- **Settings protection** — press-and-hold the gear icon for 3 seconds, then enter the code shown on screen
- **App slot management** — assign, remove, and reorder apps in the grid

---

## Build Instructions

### Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or later
- Android SDK with API 34 installed

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/Pat-79/SeniorTVLauncher.git
   ```
2. Open Android Studio → **File → Open** → select the `SeniorTVLauncher` folder.
3. Let Gradle sync complete (it downloads all dependencies automatically).
4. Connect an Android TV device or start an Android TV emulator.
5. Click **Run → Run 'app'** or press `Shift+F10`.

To build a release APK:
```bash
./gradlew assembleRelease
```

---

## How to Use the Settings

1. On the launcher home screen, focus on the **gear icon** in the bottom-right corner.
2. **Press and hold** the Select/OK button for **3 seconds**.
3. A dialog appears showing a **randomly generated 4-digit code** in large digits.
4. Use the **on-screen number pad** (D-pad navigable) to enter the code.
5. Press the **✓ (confirm)** button.
   - Correct code → Settings screen opens.
   - Wrong code → Dialog dismisses with a "Incorrect code" toast.

### Settings Sections

| Section | Options |
|---|---|
| Display | Rows (1–6), Columns (1–8), Button size, Show app name, Button shape |
| Background | Set image, Remove image, Enable blur, Blur level |
| Clock & Date | Show clock, Show date |
| App Buttons | Assign app to slot, Remove app, Move Up/Down |
| Save | Save Changes button |

---

## Licence

This project is licensed under the **GNU General Public License v3.0**.  
See [LICENSE](LICENSE) for the full licence text.
