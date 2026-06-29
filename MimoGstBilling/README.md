# Mimo GST Billing

A comprehensive Android application for Indian GST billing, inspired by Vyapar.

## Features

- GST Invoice creation with automatic tax calculation
- Customer & Supplier management
- Product/Service catalog with HSN codes
- Sales & Purchase tracking
- GST Reports (GSTR-1, GSTR-3B summary)
- Multi-company support
- Google Drive backup & restore
- PDF generation & sharing
- Local SQLite database (Room)

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM + Clean Architecture
- **Database**: Room (SQLite)
- **DI**: Hilt
- **PDF**: iText / Android PdfDocument
- **Cloud**: Google Drive API

## Project Structure

```
com.mimo.gstbilling/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   ├── entity/       # Room Entities
│   │   └── AppDatabase.kt
│   └── repository/       # Repositories
├── domain/
│   └── model/            # Domain models
├── di/
│   └── AppModule.kt      # Hilt modules
├── ui/
│   ├── components/       # Reusable UI components
│   ├── navigation/       # Navigation graph
│   ├── screens/          # Compose screens
│   │   ├── DashboardScreen.kt
│   │   ├── CreateInvoiceScreen.kt
│   │   ├── PartiesScreen.kt
│   │   ├── ItemsScreen.kt
│   │   ├── ReportsScreen.kt
│   │   └── SettingsScreen.kt
│   └── theme/            # Colors, Theme, Typography
└── utils/
    ├── GoogleDriveBackupHelper.kt
    └── PdfGenerator.kt
```

## Build Instructions

### Option 1: Local Build

1. **Prerequisites**:
   - Android Studio Hedgehog (2023.1.1) or later
   - JDK 17
   - Android SDK 35

2. **Steps**:
   ```bash
   # 1. Open project in Android Studio
   # File -> Open -> Select MimoGstBilling folder

   # 2. Sync Gradle
   # Click "Sync Now" in the notification bar

   # 3. Build APK
   Build -> Build Bundle(s) / APK(s) -> Build APK(s)

   # Or via command line:
   ./gradlew assembleDebug
   ```

3. **Output**: `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: GitHub Push + CI/CD Build

1. **Push to GitHub**:
   ```bash
   # Initialize git repo
   git init
   git add .
   git commit -m "Initial commit: Mimo GST Billing"

   # Add remote and push
   git remote add origin https://github.com/YOUR_USERNAME/mimo-gst-billing.git
   git branch -M main
   git push -u origin main
   ```

2. **Enable GitHub Actions** (Optional):
   - GitHub will auto-detect the Android project
   - Go to Actions tab → New workflow → Android CI
   - This enables automatic APK builds on every push

## Configuration

### Google Drive Backup Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable Google Drive API
4. Create OAuth 2.0 credentials (Android type)
5. Add your SHA-1 fingerprint
6. Download `google-services.json` and place in `app/` directory

### GST Rates Configuration

Default GST rates are pre-configured:
- 0% (Exempt)
- 5%
- 12%
- 18%
- 28%

## Screenshots

| Dashboard | Create Invoice | Parties | Reports |
|-----------|--------------|---------|---------|
| ![Dashboard](screenshots/dashboard.png) | ![Invoice](screenshots/invoice.png) | ![Parties](screenshots/parties.png) | ![Reports](screenshots/reports.png) |

## License

MIT License
