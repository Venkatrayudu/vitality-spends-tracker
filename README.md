# Spend Tracker

A native Android app (Kotlin + Jetpack Compose) for tracking your weekly and monthly
card spend, with a home-screen widget and Friday/month-end reminders. Everything lives
only on your phone — no account, no server, nothing sent anywhere.

## What it does

- **Add a spend** manually (amount, category, date, optional note). Categories are
  **Healthy Food**, **Healthy Care**, and **Other**.
- **Dashboard** shows, live:
  - This week's total spend vs. your **R4,500** weekly goal (week = Saturday–Friday)
  - This month's total spend vs. your **R29,000** monthly goal (calendar month)
  - This month's Healthy Food total and Healthy Care total (no goal by default — just a
    running total, since you didn't give target numbers for those two; you can set
    optional goals for them in Settings if you want)
- **History** lists every entry, tap one to edit or delete it.
- **Settings** lets you change the weekly/monthly goals, the optional Healthy
  Food/Healthy Care goals, turn reminders on/off, and change the reminder time
  (default 18:00 / 6pm).
- **Home-screen widget**: add it like any other widget (long-press your home screen →
  Widgets → Spend Tracker) to see this week's and this month's spend/remaining without
  opening the app.
- **Reminders**: a notification on **Friday evening** if the weekly goal isn't reached
  yet, and one on the **last day of the month** if the monthly goal isn't reached yet.
  Nothing fires if the goal has already been met.

## Assumptions I made (all easy to change)

- **Week = Saturday to Friday.** The weekly goal and the Friday reminder are based on
  this. If you'd rather it ran Sunday–Saturday, that's a one-line change in
  `util/DateUtils.kt` (`DayOfWeek.SATURDAY` → `DayOfWeek.FRIDAY`).
- **Reminder time defaults to 18:00 (6pm)**, editable in Settings.
- **Healthy Food / Healthy Care have no spend goal by default** — the app just tracks
  and displays your monthly total for each, since no target number was given for them.
  You can set one in Settings any time.
- Reminders use Android's WorkManager, which is battery-friendly but not
  millisecond-exact — the notification typically appears within a few minutes of the
  target time, occasionally later if the phone is in deep sleep (Doze mode). This is
  normal Android behaviour for any non-urgent scheduled notification.

## Getting the APK — two ways

I built the complete, working project (about 30 source files), but I could not compile
it into an APK myself: this sandbox's network is locked down to a handful of package
registries and does not allow reaching Google's Android SDK/Maven servers
(`dl.google.com`, `maven.google.com`) or even Maven Central, which any Android build
needs. So you'll need to do the actual compile step yourself — either option below takes
a few minutes and needs no coding knowledge.

### Option A — GitHub Actions (recommended, no software to install)

This project already includes a GitHub Actions workflow
(`.github/workflows/build-apk.yml`) that builds the APK for you in the cloud.

1. Create a new repository on [github.com](https://github.com) (private is fine — this
   is your personal finance app).
2. Upload this whole project folder into it (drag-and-drop on the GitHub website works,
   or `git push` if you're comfortable with git).
3. Go to the repo's **Actions** tab. A workflow run should start automatically (or click
   **"Build debug APK" → "Run workflow"** if it didn't).
4. Wait for it to finish (~3–5 minutes, shown as a green check).
5. Open the finished run, scroll to **Artifacts**, and download
   **spend-tracker-debug-apk** — that's a zip containing `app-debug.apk`.
6. Transfer that APK to your phone (email it to yourself, use Google Drive/WhatsApp/USB
   cable — whatever's easiest) and install it (see below).

### Option B — Android Studio on your own computer

1. Install [Android Studio](https://developer.android.com/studio) (free).
2. Open this project folder (`File → Open`, pick the `FinTrack` folder).
3. Let it finish syncing (Android Studio downloads everything it needs automatically —
   this step needs internet).
4. Plug your phone in via USB with USB debugging enabled, and click the green **Run**
   button — this installs and opens the app directly. Or, to just get an APK file:
   **Build → Build App Bundle(s) / APK(s) → Build APK(s)**, then **locate** the file
   (it'll be in `app/build/outputs/apk/debug/app-debug.apk`).

### Installing the APK on your phone

1. Copy `app-debug.apk` onto your phone (or download it directly on the phone).
2. Tap the file. Android will ask permission to "install unknown apps" for whichever
   app you opened it with (Files, Gmail, etc.) — allow it, this is normal for any app
   installed outside the Play Store.
3. Tap **Install**. Done — no Play Store involved.

This is a **debug build**, signed with Android's standard debug key. That's perfectly
fine for installing on your own phone; it's just not eligible for the Play Store (which
wasn't the goal here anyway).

## If a build fails

Since I couldn't compile this myself to catch mistakes, if `./gradlew assembleDebug` (or
the GitHub Actions run) reports an error, paste the error text back to me and I'll fix
the specific file — I can't reproduce a live build here, but I can read and correct
Kotlin/Gradle errors from the message.

## Project structure

```
app/src/main/kotlin/com/vmeduri/fintrack/
  data/           Room database: Transaction entity, DAO, and the local settings (DataStore)
  util/           Date-range math and Rand currency formatting/parsing
  reminders/      WorkManager job that checks goals on Fridays / month-end and posts notifications
  widget/         The home-screen widget (Jetpack Glance)
  ui/
    theme/        Colours, typography
    dashboard/    Home screen: progress vs. goals
    history/      List of past entries, tap to edit
    addedit/      Add/edit/delete a single entry
    settings/     Goals + reminder settings
    components/   Shared ProgressCard widget
  MainActivity.kt Navigation between the three tabs + the add/edit screen
  FinTrackApp.kt  App startup: creates the notification channel, schedules reminders
```

All data is stored locally in a Room (SQLite) database on your phone — there is no
cloud sync or account, by design (per your choice of local-only storage).
