# Synchronization Protocol

## Overview
Because the system is designed to run entirely without the internet, synchronization happens over a **Local Area Network (LAN)** — specifically, a Wi-Fi Hotspot created by the Admin device.

## The Architecture
- **Server**: The Admin app embeds a lightweight HTTP server using `Ktor-Server-Netty`. It binds to `0.0.0.0:8080` and listens for incoming connections.
- **Client**: The Employee app uses `HttpURLConnection` (or `Ktor-Client`) to make standard REST requests to the Admin app.

## Auto-Discovery Mechanism
Manual IP entry is tedious and error-prone. The Employee app bypasses this by leveraging Android's `WifiManager`.
1. The Admin device turns on its Wi-Fi Hotspot.
2. The Employee device connects to this Hotspot.
3. The Employee app uses `DhcpInfo` to read the `gateway` IP address. In an Android hotspot scenario, the gateway IP is *always* the device hosting the hotspot (the Admin device).
4. The Employee app pings `http://<Gateway_IP>:8080/ping` to verify the Admin app is active.

## Synchronization Endpoints

### 1. `GET /ping`
- **Purpose**: Health check and discovery verification.
- **Response**: `200 OK` with body `"WaterPlant Sync Server Online"`.

### 2. `POST /sync`
- **Purpose**: Bidirectional data exchange.
- **Request Body**: JSON representing the `SyncRequest` object.
  - `employeeId`: The ID of the employee syncing.
  - `deliveries`: A list of `Delivery` objects where `syncStatus == PENDING`.
- **Response Body**: JSON representing the `SyncResponse` object.
  - `acknowledgedDeliveryIds`: A list of UUIDs that the server successfully saved.
  - `customers`: A list of all active `Customer` objects in the Admin database.

## Idempotency and Conflict Resolution
A critical requirement of offline sync is handling connection drops gracefully. If an employee taps "Sync", the data is sent, but the Wi-Fi disconnects before the response is received, the employee device will still think the data is `PENDING`. They will try to sync again later.

To handle this **Idempotently**:
1. **Client-side UUIDs**: Every delivery has a unique UUID string.
2. **Server-side Ignore**: The `DeliveryDao` in the Admin app uses `@Insert(onConflict = OnConflictStrategy.IGNORE)`.
3. If the server receives a UUID it already has, it silently ignores the insert, but *still includes that UUID in the `acknowledgedDeliveryIds` response*.
4. When the client receives the `acknowledgedDeliveryIds`, it updates those specific local records to `SYNCED`.
5. Once marked `SYNCED`, the record is locked from further editing on the client device and will not be sent in future sync requests.

## Bidirectional Customer Sync
To ensure employees always have the latest customer list and pricing (`jarValue`), the Admin app attaches a full list of active `Customer` records in every `SyncResponse`.
The Employee app receives this list, wipes its local `Customer` table, and inserts the fresh list. Because deliveries reference customers by `id`, referential integrity is maintained.
