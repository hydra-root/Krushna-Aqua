# System Architecture

## Overview

The Water Plant Distribution System is an **offline-first** solution built entirely for Android devices. It eliminates the need for expensive cloud hosting, continuous internet connections, and monthly subscriptions. It operates using a single "Master" device and multiple "Client" devices that sync locally over Wi-Fi.

## Application Modules

The project is structured into three distinct Gradle modules to enforce separation of concerns and maximize code reuse:

1. **`:core` (Android Library)**
   - Contains all the foundational logic and shared infrastructure.
   - **Database**: Room database setup, DAOs, and Models (`AppDatabase`, `Customer`, `Employee`, `Delivery`).
   - **Networking**: The Ktor HTTP embedded server (`SyncServer`) and the Ktor HTTP client (`SyncClient`).
   - **Utilities**: Network discovery (`NetworkUtils`), Date formatting, PDF Generation, and CSV Export.
   - **Theme**: Shared Jetpack Compose Material 3 theme and UI components (`EmptyState`).

2. **`:admin` (Plant Phone App)**
   - The master application installed on the owner's device.
   - Acts as the central source of truth (Master Database).
   - Runs the embedded HTTP server in the background to listen for sync requests.
   - Handles Employee/Customer creation, report generation, and database backups.

3. **`:employee` (Delivery Phone App)**
   - The lightweight client application installed on the delivery drivers' phones.
   - Operates 100% offline during the day to record jar deliveries, returns, and cash collected.
   - Acts as an HTTP client to push data to the Admin app at the end of the day.

## Tech Stack & Patterns

- **UI Framework**: Jetpack Compose (Material 3).
- **Architecture Pattern**: MVVM (Model-View-ViewModel) paired with the Repository Pattern.
- **Dependency Injection**: Hilt (Dagger) is used to inject DAOs, Repositories, and Network clients into ViewModels.
- **Asynchronous Programming**: Kotlin Coroutines and `StateFlow` are used extensively to observe database changes reactively. Room returns `Flow<List<T>>` which automatically updates the UI when the underlying data changes.
- **JSON Serialization**: `kotlinx.serialization` is used for ultra-fast, reflection-free JSON encoding/decoding during sync.

## Offline-First Database Design (Room)

The application relies heavily on SQLite (via Room) for its offline capabilities.

### Key Entities
- `Employee`: Represents delivery drivers. Soft-deletes are handled via an `active` boolean.
- `Customer`: Represents the buyers. Contains `jarValue` (per-jar pricing) and `customerGroup` ("CASH" or "POSTPAID").
- `Delivery`: The core transactional record. Tracks `deliveredJars`, `returnedJars`, `cashPaid`, and `totalAmount`.

### UUIDs for Conflict Resolution
Instead of relying on auto-incrementing integer IDs which would clash between different offline devices, **UUIDs (Universally Unique Identifiers)** are generated on the client device at the moment of creation. This ensures that a delivery record created on Employee A's phone will never collide with a record created on Employee B's phone.

## Security & Access
- The apps do not use traditional usernames and passwords. They rely on local PIN codes for authentication (`1234` default for Admin, and unique PINs per Employee).
- Network security is implicitly handled by the physical requirement to be connected to the same local Wi-Fi Hotspot as the Admin phone.
