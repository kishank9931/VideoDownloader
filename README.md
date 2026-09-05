# Video Downloader (Android)

A modern Kotlin + Jetpack Compose app for downloading video files with
pause/resume, progress, history, a built-in file manager, and quality
selection — all through legitimate, ToS-safe sources.

## Why YouTube / Instagram / TeraBox / Diskwala / Flezen aren't wired in

None of these platforms publish a public API for downloading arbitrary
videos. The way most "video downloader" apps support them is by scraping
or reverse-engineering private, unofficial endpoints — which violates each
platform's Terms of Service, is a common reason such apps get pulled from
the Play Store, and can infringe the copyright of the people who made the
videos. This project deliberately leaves that layer out.

What it supports instead, out of the box:

- **Direct video links** — any `http(s)://.../file.mp4` (or `.mkv`,
  `.webm`, `.mov`, `.m4v`, `.avi`, `.3gp`) style URL. Covers your own
  hosted files, CDN links, or anything you have explicit permission to
  fetch.
- **A simple JSON manifest format** for multi-quality sources you or a
  partner control:

  ```json
  {
    "title": "My Clip",
    "qualities": [
      { "label": "1080p", "url": "https://cdn.example.com/clip-1080.mp4", "sizeBytes": 104857600 },
      { "label": "720p",  "url": "https://cdn.example.com/clip-720.mp4" }
    ]
  }
  ```

  Point the app at the `.json` URL and it shows a quality picker.

### Adding a new source later

The whole app talks to sources through one interface,
`VideoExtractor` (`extractor/VideoExtractor.kt`). If you obtain a licensed
API key or a partner agreement with a platform, implement `VideoExtractor`
for it and register the instance in `ExtractorRegistry` — nothing else in
the app (download engine, progress UI, history, file manager) needs to
change.

## Features included

- Paste-a-link home screen with clipboard paste button and share-sheet
  intake (share a link from Chrome straight into the app)
- Quality picker when a manifest offers more than one rendition
- Pause / resume via HTTP `Range` requests — a paused download keeps its
  partial bytes on disk and resumes from that offset
- Live progress (bytes downloaded / total, percentage bar) backed by
  WorkManager + Room, so downloads survive process death
- History tab for completed/failed/cancelled downloads
- Files tab: play, share, or delete anything you've downloaded
- Material 3 UI with dynamic color (Android 12+) and light/dark support

## Project structure

```
app/src/main/java/com/videodownloader/app/
├── data/         Room entities, DAO, database, repository (WorkManager glue)
├── download/     DownloadEngine (range-request core) + DownloadWorker
├── extractor/    Pluggable source resolvers (see above)
├── ui/
│   ├── theme/    Material3 color scheme, typography
│   ├── nav/      Bottom-nav + NavHost
│   ├── screens/  Home, History, Files
│   └── components/ Progress card, quality-picker dialog
├── util/         File-size formatting, share/open intents
├── MainActivity.kt
├── MainViewModel.kt
└── VideoDownloaderApp.kt
```

## Building the APK

You'll need [Android Studio](https://developer.android.com/studio)
(Koala or newer) — it bundles everything else required.

1. Unzip the project and choose **Open** in Android Studio, pointing at
   the `VideoDownloader` folder.
2. Let Gradle sync (it will download the Android Gradle Plugin, Kotlin,
   and the dependencies listed in `app/build.gradle.kts` — this needs
   an internet connection the first time).
3. **Build > Build App Bundle(s) / APK(s) > Build APK(s)**, or just press
   **Run ▶** with a device/emulator connected.
4. The debug APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

No signing config is set up for a release build yet — Android Studio's
**Build > Generate Signed Bundle / APK** wizard will walk you through
creating a keystore when you're ready to publish or sideload a release
build.

### If Gradle sync complains about the KSP version

The KSP plugin version in the root `build.gradle.kts`
(`1.9.24-1.0.20`) must match whatever Kotlin version you use. If Android
Studio suggests a newer Kotlin/AGP version, bump `org.jetbrains.kotlin.android`
and the KSP version together — check
[github.com/google/ksp/releases](https://github.com/google/ksp/releases)
for the matching pair.

## Permissions used

- `INTERNET` / `ACCESS_NETWORK_STATE` — to fetch the file/manifest you
  paste in
- `POST_NOTIFICATIONS` — Android 13+ requires this to show download
  progress notifications
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_DATA_SYNC` — lets a download
  keep running briefly if you leave the app

No storage permission is requested: files are written to the app's own
scoped storage (`getExternalFilesDir`), which needs no runtime permission
on modern Android and is automatically cleaned up if the app is
uninstalled.
