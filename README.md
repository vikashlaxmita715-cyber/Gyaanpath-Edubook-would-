# Play Store Browser

A lightweight Android app for browsing and searching Google Play Store listings in a WebView.

## Build

1. Open this repository in Android Studio (Ladybug or newer) and allow Gradle sync.
2. Run the `app` configuration on an Android device or emulator.
3. To create a Play-ready bundle, select **Build > Generate Signed Bundle / APK** in Android Studio and choose **Android App Bundle**.

The app opens `https://play.google.com/store`. Searches are sent to Google Play's web search. Links outside `play.google.com` are handed to the device browser. Google Play may limit some functionality inside embedded browsers, and publishing requires your own Play Console account, app signing, privacy disclosures, and compliance with Google's current policies. This is an independent browser app and is not affiliated with Google.
