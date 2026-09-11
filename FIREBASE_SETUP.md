# Cloud Database & Authentication Setup Guide

## Overview
FinTrack now supports cloud storage via Firebase Firestore with user authentication. All transactions and preferences are automatically synced to the cloud, ensuring data persistence across device re-installs and multi-device access.

## Architecture

### Components Added

1. **AuthService** (`auth/AuthService.kt`)
   - Handles Firebase Authentication (sign-up, sign-in, sign-out)
   - Manages user session state
   - Provides reactive auth state via StateFlow

2. **FirestoreRepository** (`repository/FirestoreRepository.kt`)
   - Manages Firestore database operations
   - Implements cloud CRUD operations for transactions
   - Handles real-time data syncing

3. **TransactionRepository** (`repository/TransactionRepository.kt`)
   - Repository pattern implementation
   - Abstracts local (Room) and cloud (Firestore) data sources
   - Combines local cache with cloud data

4. **LoginScreen** (`ui/auth/LoginScreen.kt`)
   - Jetpack Compose UI for sign-up/sign-in
   - Error handling and loading states
   - Account creation flow

5. **Updated Transaction Model** (`data/Transaction.kt`)
   - Added `userId` field for ownership
   - Added `cloudId` for mapping to Firestore documents
   - Added `syncStatus` enum (PENDING, SYNCED, ERROR)
   - Added `syncTimestamp` for conflict resolution

### Updated Components

- **MainActivity** - Auth state handling, login flow integration
- **SettingsScreen** - Added logout button
- **FinTrackApp** - Firebase initialization

## Setup Instructions

### Step 1: Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Create project" or select existing project
3. Enable Google Analytics (optional but recommended)
4. Proceed to project setup

### Step 2: Add Android App to Firebase
1. In Firebase Console, go to Project Settings
2. Click "Add app" → "Android"
3. Enter package name: `com.vmeduri.fintrack`
4. Download `google-services.json`
5. Place it in `app/` directory (replacing the template file)

### Step 3: Enable Authentication
1. In Firebase Console, go to **Authentication**
2. Click **Sign-in method**
3. Enable **Email/Password**
4. (Optional) Add other providers (Google, Facebook, etc.)

### Step 4: Set Up Firestore Database
1. In Firebase Console, go to **Firestore Database**
2. Click **Create database**
3. Choose "Start in production mode"
4. Select a location near your users

### Step 5: Configure Security Rules
Replace the default Firestore security rules with:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
      
      match /transactions/{transactionId} {
        allow read, write: if request.auth.uid == userId;
      }
      
      match /preferences/{preferenceId} {
        allow read, write: if request.auth.uid == userId;
      }
    }
  }
}
```

### Step 6: Build and Run
```bash
./gradlew assembleDebug
```

## Data Flow

### Sign-Up / Sign-In
1. User enters email and password on LoginScreen
2. AuthService calls Firebase Authentication
3. On success, MainActivity navigates to Dashboard
4. TransactionRepository is initialized with authenticated userId

### Create Transaction
1. User enters transaction details
2. Transaction saved to local Room DB immediately (offline support)
3. Background sync pushes to Firestore
4. CloudId assigned and transaction marked as SYNCED
5. Changes reflected in Dashboard

### Data Sync
1. **Local-first**: Transactions saved to Room for offline access
2. **Cloud push**: Pending transactions synced to Firestore
3. **Cloud pull**: Real-time listener updates UI with cloud changes
4. **Conflict resolution**: Latest timestamp wins

### Sign-Out
1. User taps "Sign Out" in Settings
2. AuthService clears Firebase session
3. MainActivity navigates to LoginScreen
4. Local Room DB preserved (can be accessed after re-login)

## Features

### Offline Support
- All changes saved locally first
- Background sync pushes to cloud when online
- Works seamlessly without internet

### Cross-Device Sync
- Sign in on another device with same account
- All transactions appear automatically
- Real-time updates via Firestore listeners

### Data Security
- User data isolated per UID
- Firestore rules prevent cross-user access
- Passwords handled by Firebase (never stored locally)

### Automatic Recovery
- After app reinstall, sign in restores all data
- No data loss between devices
- Cloud as source of truth for persistent data

## Developer Notes

### Repository Pattern
The `TransactionRepository` abstracts the data layer. When Firestore is available, it:
- Combines local Room results with Firestore results
- Deduplicates by cloudId
- Keeps UI simple and testable

### Sync Status Tracking
Transactions track sync status to detect:
- **PENDING**: Not yet synced to cloud
- **SYNCED**: Safe in Firestore
- **ERROR**: Sync failed, will retry

### Testing
- Unit tests in `src/test/` validate Transaction model
- Integration tests in `src/androidTest/` test auth flow
- Test with Firebase Emulator Suite for local development

## Troubleshooting

### "SDK location not found" Error
Create `local.properties` in project root:
```
sdk.dir=/path/to/android/sdk
```

### Firebase Connection Issues
1. Verify `google-services.json` is in `app/` directory
2. Check Firebase Console credentials
3. Ensure security rules allow your app

### Sync Not Working
1. Check user is authenticated (`authService.isUserAuthenticated()`)
2. Verify Firestore rules are correct
3. Check LogCat for Firebase errors
4. Use Firebase Emulator for local testing

### Build Errors
1. Run `./gradlew clean`
2. Sync Gradle files
3. Invalidate IDE cache

## Next Steps

### Optional Enhancements
1. Add cloud backup of GoalsPreferences
2. Implement transaction sharing between users
3. Add offline sync conflict UI
4. Implement data import/export
5. Add Google/Facebook sign-in
6. Implement biometric authentication

### Monitoring
1. Set up Firebase Crashlytics for error tracking
2. Use Firebase Analytics for usage insights
3. Monitor Firestore read/write operations in console

## Environment Configuration

### Local Development
For local testing without Firebase:
1. Transaction model supports offline-only mode
2. Set `firestoreRepository = null` in TransactionRepository
3. App works with local Room DB only

### Production
1. Use real Firebase project
2. Replace `google-services.json` with production credentials
3. Monitor Firestore costs (free tier: 50k reads/writes daily)
4. Set up automated backups

## Support

For issues:
1. Check Firebase Console for errors
2. Review Firestore security rules
3. Test with Firebase Local Emulator Suite
4. Refer to [Firebase Documentation](https://firebase.google.com/docs)
