# CourierRelay (Android)

CourierRelay listens to selected delivery-app updates and forwards parsed offer details to Telegram in near real time.

## Features
- Notification capture via `NotificationListenerService` (backup path)
- On-screen offer capture via `AccessibilityService` for richer job-card details
- Parser with confidence score + optional auto-action (`Accept`) when confidence threshold is met
- Telegram Bot API forwarding over HTTPS (OkHttp)
- Local event log with statuses (`PENDING`, `SENT`, `FAILED`) via Room
- Settings persistence (bot token, chat id, package filters, auto-action toggle/threshold) via DataStore
- Retry path for failed sends via WorkManager + manual retry button
- Mock event sender for quick validation

## Setup
1. Open `CourierRelayAndroid/` in Android Studio (Ladybug+).
2. Let Gradle sync and install Android SDK for API 35.
3. In app settings, paste your **newly rotated** Telegram bot token and chat ID.
4. Add package filters as comma-separated package names (e.g. `com.doordash.driverapp,com.ubercab`).
5. (Optional) Enable **auto-action** and set confidence threshold (recommended 0.85+).
6. Tap **Save Settings**.
7. In Onboarding, enable both:
   - Notification Access for CourierRelay
   - Accessibility Service for CourierRelay
8. Open target delivery app and wait for a real offer card.
9. Verify Telegram message + in-app event log.

## How optional auto-action works
- When an offer card appears, CourierRelay parses visible UI text into structured fields.
- If `autoActionEnabled=true` and `confidence >= minConfidence`, it attempts to click the visible `Accept` control.
- Keep this disabled until you validate parser quality for your app version.

> Note: This repository intentionally excludes `gradle-wrapper.jar` due binary-file PR tooling limitations in this environment.
> If missing locally, regenerate it with `gradle wrapper` from `CourierRelayAndroid/` before first `./gradlew` run.

## Run checks
```bash
cd CourierRelayAndroid
./gradlew :app:assembleDebug
```

## Security
- Never commit real bot tokens/chat IDs.
- A token used in prior iteration should be treated as compromised and rotated.
