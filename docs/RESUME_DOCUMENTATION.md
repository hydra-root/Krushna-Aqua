# Krushna Aqua — Offline-First Water Distribution & Logistics Platform
> **Lead Developer & Architect:** **Pavan Pawar**  
> **Resume & Portfolio Documentation Guide**  
> *Prepared for Software Engineer / Android Developer profiles (Freshers, Mid-Level, and Senior)*

---

## 1. Project At A Glance

| Attribute | Details |
| :--- | :--- |
| **Project Name** | **Krushna Aqua** (Water Distribution ERP & Logistics Management) |
| **Developer** | **Pavan Pawar** (Android Engineer & Mobile Solutions Architect) |
| **Platform** | Android (Min SDK 29 / Android 10+ to Latest Android) |
| **Language & Core** | **Kotlin** (100%), Kotlin Coroutines, Kotlinx Serialization |
| **UI Framework** | **Jetpack Compose**, **Material 3 Expressive Design System** |
| **Architecture** | **Multi-Module Clean Architecture** (`:admin`, `:employee`, `:core`), MVVM, Repository Pattern, UDF |
| **Local Database** | **Room (SQLite)** with reactive `StateFlow` and `Flow` observable queries |
| **Networking & Sync** | **Embedded Ktor HTTP Server** (on Admin device) + Ktor Client, DHCP Gateway Auto-Discovery |
| **Dependency Injection** | **Hilt (Dagger)** |
| **Key Functionalities**| Zero-cloud P2P Wi-Fi Hotspot synchronization, Jar debt & return ledger, Cash vs Postpaid customer billing, on-device PDF invoice generation, SQLite database backup/restore |

---

## 2. Resume Ready Bullet Points

Select the format that best matches your target role and resume space constraints.

### Option A: Senior / Experienced Android Engineer (High Impact, Metrics-Driven)
*Use this for experienced roles where system design, architecture, and autonomy are evaluated.*

> **Krushna Aqua — Offline-First Water Distribution ERP** | *Kotlin, Jetpack Compose, Room, Ktor, Hilt*
> - Engineered an **offline-first multi-module Android ERP** (`:admin`, `:employee`, `:core`) eliminating cloud hosting costs through a **zero-cloud peer-to-peer Wi-Fi Hotspot synchronization protocol**.
> - Architected an embedded **Ktor HTTP sync server** on the master plant device paired with automated **DHCP Gateway discovery (`DhcpInfo`)**, allowing field drivers to sync records in under 3 seconds without manual network configuration.
> - Implemented an **idempotent transactional sync engine** using client-generated UUIDs and Room `@Transaction` isolation (`OnConflictStrategy.IGNORE`), achieving **100% data integrity** and zero record duplication across concurrent syncs.
> - Designed a dual-track **real-time inventory & financial ledger** tracking jar circulation (`Delivered - Returned = Balance`), yesterday’s delivery carryover, and payment tracking for Cash/Postpaid customer tiers.
> - Built a modern, accessible UI using **Jetpack Compose and Material 3 Expressive**, optimizing field operations for **one-handed thumb ergonomics** and reducing delivery entry time to **<10 seconds**.
> - Integrated an on-device **PDF invoice generation engine** and SQLite automated backup/restore pipeline, providing instant commercial billing without external dependencies.

---

### Option B: Mid-Level Android Engineer (Architecture & Implementation Focused)
*Use this to highlight clean architecture, reactive programming, and end-to-end feature delivery.*

> **Krushna Aqua — Water Distribution Management System** | *Kotlin, Jetpack Compose, Room, Hilt*
> - Developed a full-fledged enterprise Android solution consisting of an **Admin Plant application** and an **Employee Delivery application** sharing a modularized `:core` library.
> - Established **MVVM architecture** with Unidirectional Data Flow (UDF), managing UI state via `StateFlow` and asynchronous Room `Flow` streams for reactive UI updates.
> - Built a **local Wi-Fi Hotspot sync engine** with embedded Ktor server, enabling daily batch synchronization of delivery records and customer registries without internet connectivity.
> - Devised business logic to calculate **live customer jar balances**, morning collection targets, and multi-tier pricing models (Cash vs Postpaid).
> - Styled complete application workflows using **Material 3 Expressive design tokens**, featuring dark/light theming, ergonomic bottom sheets, and responsive search filtering.
> - Implemented native Android **PDF generation** for customer billing statements and local database export/import for disaster recovery.

---

### Option C: Compact / 1-Page Resume (3 High-Impact Bullet Points)
*Perfect when space on your resume is limited.*

> **Krushna Aqua — Offline-First Logistics Android App** | *Kotlin, Jetpack Compose, Room, Ktor, Hilt*
> - Designed and deployed a multi-module (`:admin`, `:employee`, `:core`) offline-first Android ERP using **Jetpack Compose (Material 3)**, **Hilt**, and **Room SQLite**.
> - Implemented a **zero-cloud P2P sync system** using an embedded **Ktor HTTP server** and DHCP auto-discovery, enabling field workers to synchronize offline delivery logs over Wi-Fi hotspot.
> - Built an **idempotent transactional data engine** with client-side UUIDs to ensure 0% data collisions, real-time jar debt tracking, and automated on-device PDF invoice generation.

---

## 3. Elevator Pitch (30-Second Summary for Recruiter Calls)

> *"I designed and built **Krushna Aqua**, an enterprise-grade, offline-first Android logistics application for water jar distribution plants. In this business, delivery drivers operate in areas with poor or zero cellular connectivity, and plant owners lose thousands in unaccounted 20-liter jars and cash leaks.*  
> *To solve this without recurring cloud costs, I built a multi-module Android system using **Jetpack Compose**, **Room SQLite**, and an embedded **Ktor HTTP server**. Delivery drivers log entries completely offline with sub-10-second one-handed flows. In the evening, when they connect to the plant's Wi-Fi hotspot, the app auto-discovers the admin device via DHCP gateway inspection and executes a bidirectional, idempotent synchronization in seconds. It also handles customer pricing tiers, real-time jar balances, and on-device PDF billing."*

---

## 4. Key Engineering Challenges & How You Solved Them

Interviewers love hearing about real technical hurdles. Here are the top 4 stories from this project:

### 1. Challenge: Zero-Cloud, Zero-Internet Synchronization
- **Problem**: Water distribution drivers travel through basements, rural zones, and industrial sectors with unreliable internet. A standard cloud API (Firebase/AWS) would fail or cause high data bills.
- **Solution**: Developed a peer-to-peer LAN sync mechanism. The Admin device acts as a portable Wi-Fi Hotspot running an embedded **Ktor Netty/CIO HTTP server**. When the employee's phone connects to the hotspot, Android’s `DhcpInfo` automatically resolves the gateway IP. The employee app issues a single authenticated `POST /sync` request containing all pending records.

### 2. Challenge: Distributed Data Collisions & Network Drops
- **Problem**: If multiple employees sync simultaneously or a Wi-Fi connection drops mid-sync, duplicate entries or inconsistent jar balances could corrupt the master ledger.
- **Solution**: 
  1. Primary keys are generated as client-side **UUID strings** instead of auto-incrementing integers.
  2. The Admin database utilizes Room `@Transaction` with `OnConflictStrategy.IGNORE`. Duplicate transmissions are silently de-duplicated.
  3. The server responds with acknowledged UUIDs. Only then does the employee app atomically mark local records as `SYNCED` and lock them from tampering.

### 3. Challenge: Physical Inventory & Ledger Tracking (The "Jar Balance" Problem)
- **Problem**: 20L water jars are valuable returnable assets. Customers frequently take 10 jars yesterday and return 8 today. Keeping track of "who owes how many empty jars" across months was historically done on lost paper cards.
- **Solution**: Engineered a reactive mathematical formula:
  $$\text{Balance} = \sum(\text{Delivered Jars}) - \sum(\text{Returned Jars})$$
  Added specialized tracking for "Yesterday Delivered" vs "Today Collection Target", giving delivery drivers immediate operational clarity right on their customer card.

### 4. Challenge: Driver Ergonomics & Speed
- **Problem**: Delivery drivers carry heavy 20-kilogram water jars. They cannot navigate complicated multi-screen forms or tiny buttons with two hands.
- **Solution**: Re-engineered the UI with **Material 3 Expressive**. Placed primary actions within thumb-reach (bottom-anchored controls, large touch targets, automatic calculation previews). Drivers can search a customer, enter delivered/returned jars, collect cash, and submit the entry in under 10 seconds.

---

## 5. System Architecture & Tech Stack Summary

```
+-----------------------------------------------------------------+
|                       KRUSHNA AQUA ERP                          |
+-----------------------------------------------------------------+
           |                                             |
           v                                             v
  [:admin] Plant Phone App                      [:employee] Delivery Phone App
  * Master Database (Room)                      * Local Cache Database (Room)
  * Embedded Ktor Sync Server (:8080)           * Ktor Sync Client
  * PDF Reports & CSV Export                    * 100% Offline Delivery Logging
  * Admin Dashboard & Analytics                 * Customer Jar Balances & Cash Ledger
           \                                             /
            \                                           /
             v                                         v
   +-----------------------------------------------------------+
   |                       [:core] Module                      |
   |  * Shared Entities (Customer, Employee, Delivery)         |
   |  * Room DAOs & TypeConverters                             |
   |  * Material 3 Design System & Shared Theme                |
   |  * Serialization DTOs & Sync Protocols                    |
   |  * Network discovery utilities (DHCP Gateway Resolver)   |
   +-----------------------------------------------------------+
```

### Detailed Tech Stack Breakdown
- **Language**: Kotlin 1.9+ (Coroutines, Flow, StateFlow, Serialization)
- **UI Toolkit**: Jetpack Compose, Material 3 Expressive, Accompanist, Compose Navigation
- **Architecture**: Multi-Module (`:admin`, `:employee`, `:core`), MVVM, Repository Pattern, Unidirectional Data Flow (UDF)
- **Database**: Room Persistence Library, SQLite, `@Transaction`, `@Insert(onConflict = IGNORE)`
- **Networking**: Ktor Server (CIO/Embedded Engine), Ktor Client / HttpURLConnection, `WifiManager` DHCP resolution
- **Dependency Injection**: Hilt (Dagger 2)
- **Document Engine**: Android native `PdfDocument` Canvas API, CSV streams
- **Tools & Build**: Gradle Kotlin DSL (`build.gradle.kts`), Version Catalogs (`libs.versions.toml`), Android Studio

---

## 6. Technical Interview Q&A Cheatsheet

### Q1: Why did you choose a multi-module architecture instead of a single app module?
> *"Separating the project into `:admin`, `:employee`, and `:core` achieved three major benefits:*  
> *1. **Security & Role Isolation**: The Employee APK doesn't package admin screens, master revenue dashboards, or backup credentials.*  
> *2. **Build Time Optimization**: Gradle caches the `:core` module, accelerating incremental builds when working on UI features.*  
> *3. **Code Reusability**: All Room schemas, DTOs, network models, and Material 3 design tokens are maintained in `:core` in a single source of truth without code duplication."*

### Q2: How does the app discover the Admin phone without the user typing an IP address?
> *"When the employee connects to the Admin device's Wi-Fi hotspot, the Admin device acts as the local DHCP server and default gateway. I used Android's `WifiManager.dhcpInfo.gateway` to programmatically extract the gateway IP address, convert the raw integer IP to an IPv4 string format, and verify server readiness with a lightweight `GET /ping` call before executing data sync."*

### Q3: What happens if an employee loses connection halfway through syncing 50 delivery records?
> *"The sync protocol is completely idempotent and transactional. Each delivery record has a unique UUID generated when created. When the Admin receives a batch, it inserts records using Room's `OnConflictStrategy.IGNORE` within a single database transaction. If the connection drops before the employee receives an acknowledgment, the employee app keeps the records in `PENDING` state. On the next sync attempt, the records are re-sent; the Admin ignores existing UUIDs and returns acknowledgments for all of them. The employee marks acknowledged records as `SYNCED`, preventing both duplicate records and data loss."*

### Q4: How is state managed in Jetpack Compose in this project?
> *"I follow Unidirectional Data Flow (UDF). The ViewModel exposes a single, immutable `StateFlow<ScreenUiState>` representing loading states, error dialogs, and domain lists. The UI composables are stateless functions that observe this state via `collectAsStateWithLifecycle()` and dispatch user actions as typed events or ViewModel method calls. Database changes from Room are collected as continuous Kotlin `Flow` streams, ensuring immediate UI reactivity upon database writes."*

---

## 7. Suggested Portfolio / GitHub README Badges & Title

```markdown
# Krushna Aqua — Offline-First Water Distribution ERP
![Platform](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue?logo=jetpackcompose)
![Architecture](https://img.shields.io/badge/Architecture-Multi--Module%20MVVM-orange)
![Database](https://img.shields.io/badge/Database-Room%20SQLite-red)
![Sync](https://img.shields.io/badge/Sync-Embedded%20Ktor%20P2P-cyan)
```
