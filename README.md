# Finsight — Personal Finance Companion 💰

A full-featured Android personal finance app built with **Kotlin**, **Jetpack Compose**, **Room**, and **Hilt**. Designed for clarity, performance, and a polished mobile-first experience.

---

## 📸 Feature Overview

| Screen | What it does |
|---|---|
| **Home Dashboard** | Balance card, income/expense summary, category donut chart, recent transactions, active goals preview |
| **Transactions** | Full CRUD with grouped date list, search, filter by type, summary strip |
| **Goals** | Savings goals, no-spend challenges, budget limits, streak tracking with visual progress |
| **Insights** | Donut chart, horizontal bar chart, line chart for daily spend, 6-month trend, week comparison |
| **Settings** | Dark mode toggle, user name editor, currency preference |

---

## 🏗️ Architecture

```
com.example.finsight/
├── data/
│   ├── local/
│   │   ├── dao/            # Room DAOs (TransactionDao, GoalDao)
│   │   ├── entity/         # Room entities
│   │   └── FinsightDatabase.kt
│   └── repository/         # Repository layer (TransactionRepository, GoalRepository)
│
├── domain/
│   └── model/              # Pure Kotlin domain models (Transaction, Goal, Category)
│
├── presentation/
│   ├── components/         # Reusable UI: Charts, TransactionItem, BottomBar, EmptyState
│   ├── navigation/         # NavGraph, Screen sealed class, BottomNavItem
│   ├── screens/
│   │   ├── home/           # HomeScreen + HomeViewModel
│   │   ├── transactions/   # TransactionsScreen, AddEditTransactionScreen + ViewModels
│   │   ├── goals/          # GoalsScreen, AddEditGoalScreen + ViewModels
│   │   ├── insights/       # InsightsScreen + InsightsViewModel
│   │   └── settings/       # SettingsScreen + SettingsViewModel + SettingsDataStore
│   └── theme/              # Color.kt, Type.kt, Theme.kt
│
├── di/                     # Hilt DI modules (AppModule, SettingsModule)
├── utils/                  # CurrencyFormatter, DateUtils, SeedDataUtil, extensions
├── FinsightApplication.kt
└── MainActivity.kt
```

**Pattern:** MVVM + Repository + Clean Architecture (UI → ViewModel → Repository → Room)

---

## 🛠️ Tech Stack

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 1.9.22 | Language |
| Jetpack Compose BOM | 2024.02.00 | Declarative UI |
| Material 3 | Latest | Design system |
| Room | 2.6.1 | Local SQLite ORM |
| Hilt | 2.51 | Dependency injection |
| Navigation Compose | 2.7.7 | In-app navigation |
| DataStore Preferences | 1.0.0 | Settings persistence |
| Lifecycle / ViewModel | 2.7.0 | State management |
| Coroutines + Flow | 1.7.3 | Async + reactive data |
| SplashScreen API | 1.0.1 | Branded splash screen |
| KSP | 1.9.22-1.0.17 | Annotation processing |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Gradle 8.4

### Clone & Run

```bash
git clone https://github.com/your-username/finsight.git
cd finsight
```

Open in Android Studio → wait for Gradle sync → Run on emulator or device (API 26+).

### First Launch
On first launch, the app **automatically seeds demo data**: ~25 transactions spanning 3 months and 3 sample goals, so all charts and insights are immediately populated.

---

## 🎯 Core Feature Details

### 1. Home Dashboard
- **Balance card** with gradient showing total balance + this month's in/out
- **Summary cards** for lifetime income and expenses
- **Donut chart** for monthly category spending breakdown
- **Goal previews** (up to 2 active goals)
- **Recent transactions** list (latest 5)
- Contextual greeting based on time of day

### 2. Transaction Tracking
- Add/Edit/Delete transactions with full validation
- Fields: amount, type (income/expense), category (17 options with emoji), description, date picker, note
- **Search** with 300ms debounce across description, note, category
- **Filter** by All / Income / Expense
- **Grouped by date** with per-day net summary
- Swipe-safe delete with confirmation dialog

### 3. Goal & Challenge System ⭐
The creative feature. Supports four distinct goal types:

| Type | Behaviour |
|---|---|
| **Savings Goal** | Progress bar toward target amount; sync button auto-calculates from this month's income − expenses |
| **No-Spend Challenge** | Streak tracker with daily check-in; 7-day visual streak bar; streak resets if a day is missed |
| **Budget Limit** | Track spending against a cap |
| **Debt Payoff** | Track debt reduction progress |

Goals support: deadlines, custom colors (8 options), manual progress updates, completion detection, overdue alerts, and streak history.

### 4. Insights
Fully canvas-drawn charts — no third-party chart library:
- **Animated donut chart** for category breakdown
- **Line chart** with gradient fill for 7-day daily spending
- **Horizontal bar chart** for category ranking
- **Week-over-week comparison** with percentage delta
- **6-month trend** dual bar (income vs expense)
- Key metrics: savings rate, avg daily spend, transaction count

### 5. Dark Mode
Fully themed dark/light system. Toggle in Settings → persisted in DataStore. Status bar adapts automatically.

---

## 📐 Design Decisions & Assumptions

1. **Currency**: Indian Rupee (₹) is the default. The `CurrencyFormatter` uses `Locale("en","IN")`. Multi-currency can be added via DataStore preference + a currency conversion layer.

2. **No backend required**: All data is stored in Room (SQLite) on device. The app works fully offline.

3. **Seed data on first launch**: To make the app immediately useful for evaluation/demo, `SeedDataUtil` inserts realistic sample transactions on first open. Cleared automatically if user data exists.

4. **Goal "auto-sync"**: The sync 🔄 button on the Goals screen auto-calculates savings progress for SAVINGS-type goals using `income − expenses` for the current month. This bridges goal tracking with actual transaction data.

5. **Canvas-only charts**: All charts (donut, line, bar) are drawn with Compose `Canvas` + animated with `Animatable`. No third-party chart library dependency.

6. **State management**: `StateFlow` + `collectAsStateWithLifecycle()` throughout. Repository layer exposes `Flow` from Room, ViewModels `combine()` flows for derived state.

7. **Navigation**: Single-activity with Compose Navigation. Bottom bar hidden on detail screens (Add/Edit Transaction, Add/Edit Goal, Settings).

8. **Category system**: 17 categories split into income/expense sets. Each has an emoji, display name, and unique color for consistent visual identity across all screens.

---

## 🧪 Testing Notes

Unit tests and instrumented tests are scaffolded but not fully implemented in this submission (per the assignment note: "not expected to build a production-ready application"). Key areas to test:
- `TransactionRepository` ↔ `TransactionDao` integration
- `InsightsViewModel` aggregation logic
- Date utility functions

---

## 🔮 Optional Enhancements (Not Yet Implemented)

- [ ] Biometric app lock (`BiometricPrompt`)
- [ ] Push notification reminders via `WorkManager`
- [ ] Data export to CSV
- [ ] Multi-currency with live exchange rates
- [ ] Recurring transactions
- [ ] Cloud sync / backup

---

## 📁 Project Structure Summary

```
Finsight/
├── app/
│   ├── build.gradle.kts          # All dependencies
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/finsight/
│       │   └── [all source files]
│       └── res/
│           ├── drawable/          # Vector icons, splash logo
│           ├── values/            # strings, themes
│           ├── values-night/      # Dark mode themes
│           ├── mipmap-anydpi-v26/ # Adaptive launcher icons
│           └── xml/               # Backup rules
├── build.gradle.kts               # Root build (plugin versions)
├── settings.gradle.kts
└── gradle/wrapper/
    └── gradle-wrapper.properties
```

---

## 👤 Author

Built for the Personal Finance Companion mobile app assignment.  
Stack: Kotlin · Jetpack Compose · Room · Hilt · Material 3 · Coroutines/Flow
