# Sahayak — Native Android Kotlin Project

## 1. How to Get the Installable `.apk` via GitHub Actions
1. Create a new GitHub repository and push this folder (`sahayak-android/`) to `main`.
2. Open the **Actions** tab in your GitHub repo.
3. Click **Build Sahayak Debug APK** (runs automatically on push, or click **Run workflow**).
4. Once finished (~2 minutes), download the **`sahayak-debug-apk`** artifact, unzip `app-debug.apk`, and install it on your Android phone.

## 2. Where to Plug In Your API Keys (In-App: **Evals & APIs** Tab)
- **Sarvam AI (`api.sarvam.ai`)**:
  - **Parent Voice Input (`/speech-to-text-translate`)**: Transcribes Hindi, Kannada, Tamil, Marathi, etc.
  - **Educator Dubbing (`/translate`)**: Translates recorded step transcripts into the parent's language.
  - **Parent Voice Guidance (`/text-to-speech`)**: Speaks each step aloud using natural Indian voices (`bulbul:v1`).
- **Divyam.ai**:
  - **Educator Recording Summarizer**: Turns screen frames + audio into structured step-by-step navigation cards.
  - **Parent Live Agent**: Takes `SahayakAccessibilityService.latestUiTreeJson` + parent's question to return the target button coordinates and explanation.

## 3. Included Demo Use Cases & 1-Click Share
- **WhatsApp:** Add & Delete a Story (`sahayak://guide/whatsapp_story`)
- **Instagram:** Add a Song to a Post (`sahayak://guide/instagram_song`)
- **PhonePe:** Find & Share Your QR Code (`sahayak://guide/phonepe_qr`) — with `AccessibilityService` UI-tree fallback for `FLAG_SECURE`.
