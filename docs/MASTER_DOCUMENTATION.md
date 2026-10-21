# Krushna Aqua: Water Plant Distribution System - Master Documentation
> **Author & System Architect:** **Pavan Pawar**  
> **Platform:** Android (Multi-Module: `:admin`, `:employee`, `:core`)

## 1. Overview
An **offline-first Android application** designed for water jar distribution businesses. It operates completely offline, eliminating the need for cloud hosting or internet access, by syncing locally over Wi-Fi.

---

## 2. System Architecture

The project is structured into three distinct Gradle modules to enforce separation of concerns:

1. **`:core` (Android Library)**
   - **Database**: Room setup, DAOs, and Models (`AppDatabase`, `Customer`, `Employee`, `Delivery`).
   - **Networking**: Ktor embedded server (`SyncServer`) and Ktor/HttpURLConnection client (`SyncClient`).
   - **Utilities**: Network discovery, PDF Generation, CSV Export, Date/Time formatting.
   - **Theme**: Shared Jetpack Compose Material 3 theme.

2. **`:admin` (Plant Phone App)**
   - The master application running on the owner's device.
   - Acts as the central source of truth (Master Database).
   - Runs the embedded HTTP server to accept sync requests.
   - Handles report generation, employee/customer management, and database backups.

3. **`:employee` (Delivery Phone App)**
   - The client application used by drivers.
   - Operates 100% offline during the day to record jar deliveries, returns, and cash collected.
   - Pushes data to the Admin app at the end of the day over a local hotspot.

---

## 3. Technology Stack & Patterns

- **UI Framework**: Jetpack Compose (Material 3).
- **Architecture**: MVVM paired with the Repository Pattern.
- **Dependency Injection**: Hilt (Dagger).
- **Asynchronous Flow**: Kotlin Coroutines and `StateFlow` observe database changes reactively. Room returns `Flow<List<T>>` to auto-update the UI.
- **JSON Serialization**: `kotlinx.serialization` for reflection-free JSON processing.

---

## 4. Offline-First Database Design (Room)

| Table | Primary Key | Key Fields |
|-------|-------------|------------|
| `employees` | `id` | name, pin, active |
| `customers` | `id` | name, company, phone, address, jarValue, customerGroup |
| `deliveries` | `uuid` | customerId, employeeId, deliveredJars, returnedJars, cashPaid, totalAmount, createdAt, modifiedAt, syncStatus |

### UUIDs for Conflict Resolution
Instead of auto-incrementing integers, **UUIDs** are generated on the client device. This guarantees that delivery records from different employee devices never collide.

### Jar Balance Formula
```
Customer Balance = SUM(deliveredJars) - SUM(returnedJars)
```

---

## 5. Synchronization Protocol

Because the system runs offline, syncing happens over a **Local Area Network (LAN)** (a Wi-Fi Hotspot created by the Admin device).

### Auto-Discovery Mechanism
Manual IP entry is bypassed using Android's `WifiManager`:
1. Admin device activates Wi-Fi Hotspot.
2. Employee device connects to the Hotspot.
3. Employee app uses `DhcpInfo` to read the `gateway` IP address (which is always the Admin device).
4. Employee app pings `http://<Gateway_IP>:8080/ping` to verify connection.

### Idempotency and Conflict Resolution
To handle network drops gracefully:
1. **Client-side UUIDs**: Every delivery has a unique UUID.
2. **Server-side Ignore**: The Admin's `DeliveryDao` uses `@Insert(onConflict = OnConflictStrategy.IGNORE)`.
3. If the server receives a UUID it already has, it silently ignores the insert but returns it in the `acknowledgedDeliveryIds` response.
4. The client updates those specific records to `SYNCED`, locking them from further edits.

### Bidirectional Sync
To keep employees updated, the Admin app attaches a full list of active `Customer` records in every `SyncResponse`. The Employee app receives this list, wipes its local `Customer` table, and inserts the fresh list, ensuring updated pricing and balances.

---

## 6. Daily Workflow

1. **Morning**: Admin adds/updates employees and customers (categorized as Cash or Postpaid).
2. **During the day**: Employees use their offline devices to log deliveries, returns, and cash collected.
3. **Evening**: Employees connect to the Plant Phone's Wi-Fi hotspot and tap **Sync**.
4. **Result**: All delivery data is transferred to the Admin database instantly, and updated customer lists are pulled down to the Employee device.

---

## 7. Security & Access
- The apps rely on local PIN codes for authentication (default Admin PIN: `1234`).
- Network security is implicitly handled by the requirement to be physically connected to the Admin's Wi-Fi Hotspot.
- Synced records are permanently locked (`SYNCED` status) on employee devices to prevent tampering.

---

## 8. Building & Exporting
1. Open the root folder in Android Studio (Hedgehog 2023.1.1+).
2. Wait for Gradle sync (Requires JDK 17).
3. Generate APKs using the Gradle wrapper:
   ```bash
   ./gradlew :admin:assembleDebug
   ./gradlew :employee:assembleDebug
   ```
4. APKs are generated in `admin/build/outputs/apk/debug/` and `employee/build/outputs/apk/debug/`.
