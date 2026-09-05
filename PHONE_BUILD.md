# Build the APK using only your phone

This project includes a GitHub Actions workflow that builds a debug APK in the cloud.

1. Create a GitHub repository from your phone.
2. Upload the **contents of this project** to the repository so that `settings.gradle.kts`, `build.gradle.kts`, `app/`, and `.github/` are at the repository root.
3. Open the repository's **Actions** tab.
4. Select **Build APK** and tap **Run workflow**.
5. Wait for the workflow to finish successfully.
6. Open the completed workflow run and download the artifact named **VideoDownloader-debug-apk**.
7. The downloaded artifact is a ZIP containing `app-debug.apk`. Extract it and install the APK on your Android phone.

The workflow uses Java 17, Android SDK 34, and Gradle 8.7, matching this project's build configuration. GitHub's hosted runner performs the compilation, so Android Studio is not required on your phone.
