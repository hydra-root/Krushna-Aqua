# Krushna Aqua — Offline-First Water Distribution ERP & Logistics System

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Multi--Module%20Clean%20Architecture-FF6F00?style=for-the-badge)](https://developer.android.com/topic/architecture)
[![Database](https://img.shields.io/badge/Database-Room%20(SQLite)-00897B?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Sync](https://img.shields.io/badge/Sync-Embedded%20Ktor%20P2P-black?style=for-the-badge&logo=ktor&logoColor=white)](https://ktor.io/)

---

## 👨‍💻 Author & Lead Engineer
**Pavan Pawar**  
*Full-Stack Android Engineer & Mobile Solutions Architect*  
- **System**: Krushna Aqua Water Jar Distribution Management System
- **Focus**: Zero-Cloud Synchronization, High-Performance Local Databases, Material 3 Ergonomics

---

## 📌 Executive Overview
**Krushna Aqua** is a mission-critical, **offline-first Android Enterprise Resource Planning (ERP)** platform tailored for water purification and jar distribution plants. 

In delivery logistics, drivers regularly navigate subterranean parking complexes, industrial estates, and rural outskirts with spotty or nonexistent mobile internet. Traditional cloud setups cause transaction failures, lost sales logs, and substantial inventory loss of returnable 20-liter water jars. 

Krushna Aqua operates **100% offline without cloud hosting, third-party databases, or cellular data dependencies**. Delivery personnel capture jar drop-offs, empty jar returns, and cash collections locally on their phones. At the close of business, drivers link to the plant master phone's Wi-Fi hotspot to execute an automatic, bidirectional, idempotent synchronization in seconds.

---

## 🏗️ Multi-Module Architecture

The system is partitioned into three decoupled Gradle modules enforcing clean separation of concerns:

| Module | Type | Target Device | Core Responsibilities |
| :--- | :--- | :--- | :--- |
| **`:admin`** | Android Application | Plant Owner Device | Master source of truth (Room database), embedded HTTP sync server (`:8080`), PDF invoice generation, business analytics, database backup/restore |
| **`:employee`** | Android Application | Field Driver Devices | Lightweight offline client, rapid single-hand delivery logging, real-time customer jar ledger, auto-gateway Wi-Fi sync |
| **`:core`** | Android Library | Shared Infrastructure | Room DAOs, Entities, Ktor sync protocols, DTOs, Material 3 shared theme tokens, and PDF/CSV reporting utilities |

---

## 🚀 Key Engineering Highlights

### 1. Zero-Cloud P2P Hotspot Synchronization Protocol
- **No Cloud Servers / No Firebase**: Cuts operational cloud costs to zero.
- **Embedded Ktor HTTP Server**: The Admin device hosts a lightweight server on port 8080.
- **Dynamic DHCP Discovery**: When field drivers connect to the plant's portable Wi-Fi hotspot, the employee application queries Android's `DhcpInfo.gateway` to automatically locate the master device IP.
- **Zero Configuration**: Drivers tap **Sync** with zero manual IP inputs.

### 2. Idempotent Data Engine & Conflict Resolution
- **Client-Side UUIDs**: Every delivery record generates a cryptographic UUID at creation, eliminating auto-increment ID collisions across multi-driver fleets.
- **Database Transaction Isolation**: The Admin device imports batches using Room's `@Transaction` with `OnConflictStrategy.IGNORE`. Repeated sync taps are safely de-duplicated without ledger corruption.
- **Atomic State Locking**: Successfully acknowledged records transition to `SYNCED` and are locked on the driver's device against post-sync tampering.

### 3. Real-Time Jar Inventory & Financial Ledger
- **Returnable Asset Tracking**: Reusable 20L jars are tracked via:
  $$\text{Customer Balance} = \sum(\text{Delivered Jars}) - \sum(\text{Returned Jars})$$
- **Daily Operational Memory**: The app tracks **Yesterday's Delivered Jars** and calculates **Today's Empty Jar Collection Target** per stop.
- **Customer Segmentation**: Automatic ledger tracking for both **Cash** (spot payment) and **Postpaid** (monthly invoice) customers.

### 4. Material 3 Expressive UI/UX
- **Google Workspace / Wallet Inspired**: Modern, clean design using official Material 3 Expressive tokens.
- **One-Handed Driver Ergonomics**: Key action triggers are placed within bottom thumb reach. Drivers complete a full delivery entry in **<10 seconds**.
- **Executive Dashboard**: Plant owners inspect daily jars delivered, returned, cash collected, and monthly projections in **<5 seconds**.

---

## 🛠️ Technology Stack

- **Programming Language**: Kotlin 1.9+ (100% Kotlin codebase)
- **UI Framework**: Jetpack Compose, Material 3 Expressive, Navigation Compose
- **State & Concurrency**: Kotlin Coroutines, `StateFlow`, Room reactive `Flow`
- **Local Persistence**: Room SQLite (with custom TypeConverters & Schema Migrations)
- **Dependency Injection**: Hilt (Dagger)
- **Networking**: Embedded Ktor HTTP Server, Ktor HTTP Client, `WifiManager` DHCP resolution
- **Data Serialization**: `kotlinx.serialization` (reflection-free JSON processing)
- **Document Engine**: Android native `PdfDocument` Canvas API & CSV stream exports

---

## 🔄 Daily Operational Workflow

```
[Morning: Plant Setup]
  • Admin updates customer accounts (Cash vs Postpaid) & pricing on Plant Phone.
  • Delivery drivers launch Krushna Aqua on their delivery phones.

[Daytime: Field Deliveries]
  • Drivers operate 100% offline on assigned delivery routes.
  • Driver logs: Jars Delivered, Empty Jars Returned, and Cash Collected.
  • App updates local customer jar debt and cash summary instantly.

[Evening: End-of-Day Sync]
  • Drivers arrive back at the distribution plant.
  • Drivers connect to the Plant Phone Wi-Fi Hotspot.
  • Drivers tap "Sync" -> Delivery records upload; updated customer accounts pull down.
  • Admin reviews daily dashboard and prints/exports PDF statements.
```

---

## 📂 Project Structure

```
Krushna-Aqua/
├── admin/                           # Master Plant Phone Application
│   └── src/main/java/.../admin/
│       ├── AdminApplication.kt     # App entry point & Hilt setup
│       ├── AdminActivity.kt        # Single activity host
│       ├── navigation/             # Compose Navigation graph
│       └── ui/
│           ├── dashboard/          # Real-time analytics & KPIs
│           ├── customer/           # Customer creation & pricing tiers
│           ├── employee/           # Driver credential management
│           ├── reports/            # Daily/Monthly reports & PDF billing
│           └── backup/             # Local database export/restore
├── employee/                        # Field Driver Delivery Application
│   └── src/main/java/.../employee/
│       ├── EmployeeApplication.kt
│       ├── EmployeeActivity.kt
│       ├── navigation/
│       └── ui/
│           ├── login/              # PIN based driver authentication
│           ├── customers/          # Route list with search & jar balances
│           ├── delivery/           # Sub-10-second fast delivery entry
│           ├── history/            # Today's deliveries (read-only once synced)
│           └── sync/               # Wi-Fi Hotspot LAN sync interface
├── core/                            # Shared Architecture Library
│   └── src/main/java/.../core/
│       ├── data/                   # Room database, Entities & DAOs
│       ├── di/                     # Hilt singleton dependency modules
│       ├── network/                # Embedded Ktor server & client protocol
│       ├── reports/                # On-device PDF & CSV engines
│       ├── ui/theme/               # Material 3 Expressive design tokens
│       └── utils/                  # DHCP resolver & date utilities
├── docs/                            # Architectural specifications & interview guide
│   ├── ARCHITECTURE.md
│   ├── MASTER_DOCUMENTATION.md
│   ├── RESUME_DOCUMENTATION.md
│   └── SYNC_PROTOCOL.md
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📜 License
Developed by **Pavan Pawar**. Proprietary enterprise application.
