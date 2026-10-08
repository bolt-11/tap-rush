# Tap Rush

> An innocent-looking Android tap game that secretly abuses Accessibility Service to log user activity in the background.

## Overview

This project demonstrates how a seemingly harmless mobile game can exploit Android's Accessibility Service to silently capture sensitive user data across **all apps** on the device — including passwords, messages, and clipboard content.

Built for **security education and awareness** purposes.

## How It Works

```
┌─────────────────────────────────────────────────────┐
│                     Target Device                   │
│                                                     │
│   ┌───────────────┐       ┌──────────────────────┐  │
│   │   Tap Rush    │       │  Accessibility API   │  │
│   │  (Game UI)    │──────▶│  (Background Spy)    │  │
│   │               │ asks  │                      │  │
│   │  "Enable Game │ perm  │  Captures:           │  │
│   │   Booster"    │       │  • Keystrokes        │  │
│   └───────────────┘       │  • Passwords         │  │
│                           │  • Button clicks     │  │
│                           │  • App switches      │  │
│                           │  • Notifications     │  │
│                           │  • Clipboard         │  │
│                           └──────────┬───────────┘  │
│                                      │              │
│                           ┌──────────▼───────────┐  │
│                           │   system_cache.dat   │  │
│                           │  (Internal Storage)  │  │
│                           └──────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

## Attack Flow

1. **Target installs** the app — it looks like a normal tap game
2. **App prompts** "Enable Game Booster" — a social engineering dialog that leads to Accessibility Settings
3. **Target enables** the service — thinking it optimizes touch response
4. **Game works normally** — the target plays the game without suspicion
5. **In the background** — the Accessibility Service silently captures all user activity across every app

## What Gets Captured

| Label | Description |
|-------|-------------|
| `KEYSTROKE` | Text typed in any input field |
| `PASSWORD` | Text from password fields (auto-detected) |
| `CLICK` | Buttons and links tapped by the user |
| `APP_SWITCH` | App/activity transitions |
| `NOTIFICATION` | Notification content from all apps |
| `CLIPBOARD` | Clipboard content on focus events |

System packages (launcher, keyboard, settings, etc.) are automatically filtered out to reduce noise.

## Reading the Logs

Captured data is stored in the app's internal storage. To read it:

```bash
adb shell run-as com.boltz.maliciousapp cat files/system_cache.dat
```

Live monitoring via Logcat:

```bash
adb logcat -s MalService
```

### Sample Output from Log

```
[2026-10-08 14:23:05] 18162 18162 D MalService: KEYSTROKE    | com.aaaa.bbb          | how to be a millionaire?
[2026-10-08 14:23:12] 18162 18162 D MalService: APP_SWITCH   | com.kkkk.lll          | com.kkkk.lll.LoginActivity
[2026-10-08 14:23:15] 18162 18162 D MalService: KEYSTROKE    | com.ssss.ttt          | johndoe@gmail.com
[2026-10-08 14:23:30] 18162 18162 D MalService: PASSWORD     | com.ssss.ttt          | j
[2026-10-08 14:23:31] 18162 18162 D MalService: PASSWORD     | com.ssss.ttt          | •0
[2026-10-08 14:23:31] 18162 18162 D MalService: PASSWORD     | com.ssss.ttt          | ••h
[2026-10-08 14:23:33] 18162 18162 D MalService: PASSWORD     | com.ssss.ttt          | •••n
[2026-10-08 14:23:35] 18162 18162 D MalService: PASSWORD     | com.ssss.ttt          | ••••1
[2026-10-08 14:23:35] 18162 18162 D MalService: PASSWORD     | com.ssss.ttt          | •••••9
[2026-10-08 14:23:36] 18162 18162 D MalService: PASSWORD     | com.ssss.ttt          | ••••••9
[2026-10-08 14:23:37] 18162 18162 D MalService: PASSWORD     | com.ssss.ttt          | •••••••3
[2026-10-08 14:25:02] 18162 18162 D MalService: NOTIFICATION | com.google.android.gm | New email from boss@company.com
[2026-10-08 14:26:01] 18162 18162 D MalService: CLIPBOARD    | com.android.chrome    | 4532-XXXX-XXXX-1234
```

## Project Structure

```
app/src/main/java/com/boltz/maliciousapp/
├── MainActivity.java            # Game UI + social engineering dialog
├── GameView.java                # Tap game custom view
├── MyAccessibilityService.java  # The malicious accessibility service
└── KeylogStore.java             # Persistent log storage
```

## Build & Install

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Alternatively, grab a prebuilt APK from the Releases page.

## Disclaimer

> **This project is for educational and authorized security testing purposes only.**
>
> It demonstrates a real-world attack vector used by Android malware to steal credentials and spy on users through Accessibility Service abuse. Use it to:
> - Understand how malicious apps exploit legitimate Android APIs
> - Train users to recognize social engineering tactics
> - Test security awareness in controlled environments
>
> **Do NOT use this on devices without explicit consent from the owner.**
