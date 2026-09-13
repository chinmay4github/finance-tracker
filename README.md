# Finance Tracker (Android)

A personal finance management Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, local **Room Database**, and real-time bidirectional synchronization with **Google Sheets**.

---

## Features

- **Offline-First Architecture**: All income, expense, and budget data is stored locally in an encrypted Room SQLite database for fast access without internet.
- **Real-Time Google Sheets Cloud Sync**: Automatically pushes additions, updates, and deletions to your personal Google Sheet via a lightweight Google Apps Script Web App.
- **Visual Analytics & Category Breakdowns**: Real-time spending progress bars, monthly summary cards, and category distributions.
- **Customizable Budgets & Alerts**: Track monthly spending limits per category with threshold warnings.
- **Filter, Search & Export**: Filter transactions by type, date range, or category, with fast search and CSV/Sheet sync.

---

## Tech Stack

- **UI Framework**: Jetpack Compose with Material 3 (Dynamic Color, Edge-to-Edge)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture repository pattern
- **Local Persistence**: Room Database (SQLite) with KSP
- **Networking**: OkHttp 4 + Kotlin Coroutines & Flow
- **Cloud Backend**: Google Sheets API via Google Apps Script Web App
- **Language**: Kotlin 2.0+

---

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2+) or newer
- JDK 17 or JDK 21
- Android SDK 34+

### Build & Run

1. Clone or export the repository:
   ```bash
   git clone <your-repo-url>
   cd finance-tracker
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync the dependencies.
4. Run the app on an Android Emulator or physical device:
   ```bash
   gradle assembleDebug
   ```

---

## Environment Variables & Secret Security

This project uses the **Secrets Gradle Plugin** with `.env` files to keep sensitive URLs, private keys, and API tokens safe from public repositories:

- **`.env.example`** is checked into version control as a template documenting all required keys.
- **`.env`** contains your actual private keys and endpoints and is **strictly ignored** by `.gitignore`.
- **Private keys, Keystores & Certificates** (`*.jks`, `*.keystore`, `*.pem`, `*.key`, `*.p12`, `service-account*.json`) are **strictly excluded** from Git in `.gitignore`.

### Setting up `.env` for local builds:

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Open `.env` and add your Google Apps Script endpoint or other keys:
   ```properties
   GOOGLE_SHEET_WEB_APP_URL=https://script.google.com/macros/s/YOUR_SCRIPT_ID/exec
   ```
3. The plugin generates type-safe accessors via `BuildConfig.GOOGLE_SHEET_WEB_APP_URL`.

---

## CI/CD & Automated APK Generation

This repository includes a pre-configured GitHub Actions workflow in [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml):

- **Triggers**: Automatically runs on every push to `main` / `master`, pull requests, or manually via the **Actions** tab ("Run workflow").
- **Build Steps**: Configures Java 21, sets up Gradle, prepares `.env` safely, provisions the debug keystore, and compiles the debug APK (`app-debug.apk`).
- **Download APK**: After the workflow finishes, the APK is available under the **Artifacts** section of the workflow run as `finance-tracker-debug-apk`.

---

## Google Sheets Real-Time Sync Setup

1. Open [Google Sheets](https://sheets.new) and create a new spreadsheet.
2. In the top menu, click **Extensions** > **Apps Script**.
3. Copy the script from `GoogleSheetScriptTemplate.kt` (or tap **"Copy Ready-to-Run Apps Script"** in the app's Sync modal) into `Code.gs`.
4. Click **Deploy** > **New deployment** (or Manage deployments):
   - **Type**: *Web app*
   - **Execute as**: *Me*
   - **Who has access**: *Anyone*
5. Authorize the permissions and copy the Web App URL (`https://script.google.com/macros/s/.../exec`).
6. Paste the URL into the app's **Google Sheet Sync** modal.
