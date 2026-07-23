# Odysseus Android AI Client

A native Android client for self-hosted Odysseus backends, crafted in Kotlin and Jetpack Compose. This app closely mimics the aesthetics, typography, animations, and high-fidelity haptic touchpoints of the Anthropic Claude mobile application.

---

## Key Features

1. **MVI (Model-View-Intent) Unidirectional Architecture**
   - Solid, reliable state management powered by Kotlin Coroutines & `StateFlow`.
   - Prevents UI flickering or incomplete states during high-frequency token updates.

2. **Claude-Style Aesthetic Polish**
   - Warm paper color scheme backgrounds (`#FAF6F0`).
   - Generously padded, round user message bubbles (`#F3EFE9`) with distinct anchor shapes.
   - Serif/humanist typography for model responses (`FontFamily.Serif`), making readings feel considered.
   - Smooth token-by-token streaming response append behavior.

3. **High-Fidelity Centralized Haptics (`HapticManager`)**
   - **Send message:** Short, light tactile click.
   - **Response starts streaming:** Subtle initial tick.
   - **Streaming tokens:** Extremely subtle, throttled tactile feedback on word boundaries.
   - **Response completion:** Double-tap finish pattern.
   - **Stop streaming:** Distinct heavy-click stop response.
   - **Copy message / switch models:** Standard action tactile clicks.
   - **Error states:** Two rapid, short alert pulses.

4. **Robust Networking & Caching Layer**
   - Built on top of the **Ktor Client** for direct native Server-Sent Events (SSE) stream processing.
   - Secure server settings storage via Jetpack **DataStore Preferences**.
   - Offline, local conversation backup and caching using **Room Database**.

---

## Tech Stack Details

- **Language:** 100% Kotlin
- **UI:** Jetpack Compose + Material 3 (Dynamic palette overridden with warm hues)
- **Dependency Injection:** Dagger Hilt
- **Network Client:** Ktor (OkHttp engine)
- **Serialization:** kotlinx.serialization (JSON)
- **Database (Local Storage):** Room Database
- **Preference Storage:** Jetpack DataStore
- **Architecture Pattern:** MVI (Model-View-Intent)

---

## Project Structure

```
odysseus-app/
│
├── build.gradle.kts               # Project-level Gradle build configuration
├── settings.gradle.kts            # Project settings (includes :app module)
├── gradle.properties              # Build parameters (AndroidX, JVM settings)
│
└── app/
    ├── build.gradle.kts           # App module-level Gradle build configuration
    └── src/
        └── main/
            ├── AndroidManifest.xml # Permissions (INTERNET, VIBRATE) & MainActivity registration
            └── java/com/odysseus/app/
                ├── OdysseusApplication.kt # @HiltAndroidApp application subclass
                ├── MainActivity.kt       # Activity entry point with Compose & NavHost
                │
                ├── data/                 # Data Layer
                │   ├── ChatRepository.kt # Integrates Room DB caching with Ktor API streaming
                │   ├── SettingsRepository.kt # Preferences data access (API URL / Auth tokens)
                │   │
                │   ├── local/            # Local SQLite storage via Room
                │   │   ├── AppDatabase.kt
                │   │   ├── ChatDao.kt
                │   │   ├── Entities.kt
                │   │   └── DatabaseModule.kt # Hilt providers for Local DB
                │   │
                │   └── remote/           # API and Network connection setup
                │       ├── NetworkModels.kt
                │       └── KtorClientModule.kt # Hilt providers for HttpClient
                │
                ├── haptics/              # Central Haptic Engine
                │   ├── HapticManager.kt  # Fine-tuned vibration effects (one-shots, waveforms)
                │   └── HapticsModule.kt  # Hilt provider for Vibrator system service
                │
                └── ui/                   # Presentation Layer (MVI UI Components)
                    ├── base/             # Base abstractions for MVI pattern
                    │   ├── UiState.kt, UiIntent.kt, UiSideEffect.kt, BaseViewModel.kt
                    │   
                    ├── theme/            # Theme definitions (Warm Claude Palette)
                    │   └── Theme.kt
                    │   
                    ├── chat/             # Main Chat Screen & Side Drawer Layout
                    │   ├── ChatContract.kt
                    │   ├── ChatViewModel.kt
                    │   └── ChatScreen.kt
                    │   
                    └── settings/         # Configuration Screen (Server Address / Token Keys)
                        ├── SettingsContract.kt
                        ├── SettingsViewModel.kt
                        └── SettingsScreen.kt
```

---

## How to Import & Build

1. **Prerequisites:**
   - Install **Android Studio (Hedgehog or newer)**.
   - Use **JDK 17** (standard for modern Android builds).
   - Ensure you have internet access to fetch Gradle libraries.

2. **Steps:**
   - Launch Android Studio.
   - Click `File -> New -> Import Project` (or select `Open` on the startup window).
   - Select the `odysseus-app` folder inside your cloned repo.
   - Let Android Studio index and synchronize Gradle packages.
   - Choose a physical Android device or Emulator (API 26+) and click **Run (Shift+F10)**.

3. **Connecting to Odysseus:**
   - Go to **Settings** (Gear icon in top-right corner).
   - Enter your Odysseus Server Base URL (e.g., `http://10.0.2.2:5000/v1` if running on localhost and connecting via Emulator, or your Tailscale IP/domain like `https://my-odysseus.tailscale.net/v1`).
   - Enter your API/Auth Bearer Token if your server is protected.
   - Click **Save & Apply**.
   - Your model selection menu on the Chat toolbar will automatically populate, allowing you to select and query models running on your self-hosted setup!
