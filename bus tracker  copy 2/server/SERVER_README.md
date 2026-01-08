# Bus Tracker Backend API

This documentation details the API endpoints available for the **Bus Tracker Client App **.
The server provides real-time tracking, route information, and accepts GPS/Cell Tower updates from the bus hardware/app.

## Base URL
- **Local Emulator (Android)**: `http://10.0.2.2:3000`
- **Physical Device**: `http://<YOUR_LOCAL_IP>:3000` (Ensure phone and laptop are on the same Wi-Fi)
- **Development**: `http://localhost:3000`

---

## 1. Search Buses
Returns a list of active buses. Currently returns a mocked list for MVP.

- **Endpoint**: `GET /api/buses/search`
- **Query Params** (Optional): `from`, `to`, `date`
- **Response**:
```json
{
  "buses": [
    {
      "bus_id": "UK-07-PA-1234",
      "route_id": "R_UK_DEL",
      "departure_time": "08:00 AM",
      "arrival_time": "02:00 PM",
      "tracking_state": "LIVE" // "LIVE" | "LAST_KNOWN" | "OFFLINE"
    }
  ]
}
```

---

## 2. Get Route Details
Fetches static details about a specific route, including the full polyline and stops.
**Cache this response** in your client app; do not fetch it repeatedly.

- **Endpoint**: `GET /api/routes/:routeId`
- **Params**: `routeId` (e.g., `R_UK_DEL`)
- **Response**:
```json
{
  "_id": "659d... (Mongo ObjectID)",
  "routeId": "R_UK_DEL",
  "name": "Haldwani - Delhi",
  "distanceKm": 280.5,
  "polyline": [
    { "lat": 29.2183, "lng": 79.5130 },
    { "lat": 29.2150, "lng": 79.5100 }
  ],
  "stops": [
    {
      "id": "S1",
      "name": "Haldwani Bus Station",
      "lat": 29.2183,
      "lng": 79.5130
    }
  ]
}
```

---

## 3. Live Bus Tracking
Poll this endpoint to get the real-time location and status of a bus.

- **Endpoint**: `GET /api/bus/:busId/live`
- **Params**: `busId` (e.g., `UK-07-PA-1234`)
- **Poll Interval**: Recommended 3-5 seconds when active.
- **Response**:
```json
{
  "route": {
    "name": "Haldwani → Anand Vihar",
    "current_stop_index": 2, // 1-based index (Next Stop)
    "total_stops": 10
  },
  "stops": {
    "available": true,
    "current_stop": "Rampur", // Next stop name
    "distance_to_stop_m": 4500, // Meters to next stop
    "stop_status": "MOVING" // "MOVING" | "AT_STOP" (if distance < 100m)
  },
  "trip_progress": {
    "near": "Rampur",
    "time_remaining": "2h 15m", // String formatted or "--"
    "distance_left_km": "120.5", // String formatted
    "speed_kmph": 45,
    "progress_percent": 35.5
  },
  "tracking": {
    "state": "LIVE", // "LIVE" | "LAST_KNOWN" | "OFFLINE"
    "source": "GPS", // "GPS" | "CELL_TOWER"
    "lat": 28.8100,
    "lng": 79.0200,
    "last_updated_ts": "2024-01-20T10:30:00.000Z"
  }
}
```

---

## 4. GPS Update (Driver App)
- **Endpoint**: `POST /api/gps/update`
- **Body**:
```json
{
  "bus_id": "UK-07-PA-1234",
  "route_id": "R_UK_DEL",
  "lat": 28.8100,
  "lng": 79.0200,
  "speed_kmph": 45
}
```

## 5. Cell Tower Update (Driver App - Fallback)
- **Endpoint**: `POST /api/cell/update`
- **Body**:
```json
{
  "bus_id": "UK-07-PA-1234",
  "route_id": "R_UK_DEL",
  "cell": {
    "mcc": 404, "mnc": 10, "lac": 12345, "cid": 67890, "signal_dbm": -85
  }
}
```

---

## 🚀 Critical Takeaways for Kotlin Client Integration

### 1. Polling Strategy
Since this backend uses Short Polling (HTTP GET) instead of WebSockets for MVP simplicity, follow these rules:
*   **Active Tracking**: Poll `GET /api/bus/:id/live` every **3 to 5 seconds**.
*   **Background/Idle**: Reduce polling to **30 seconds** or stop completely to save battery.
*   **Offline Handling**: If `tracking.state` is `OFFLINE`, assume the bus is not running. Disable live animations.

### 2. Map & Animations
*   **Polyline**: Fetch the route polyline **once** (`GET /api/routes/:id`) when the activity starts. Do NOT fetch it inside the polling loop.
*   **Smooth Movement**: The API updates location every few seconds. On the client, **interpolate** the marker position between the old `lat/lng` and new `lat/lng` to create a smooth moving animation. Do not just "jump" the marker.
*   **Rotation**: Use the bearing between the last two points to rotate the bus icon correctly.

### 3. Handling Nulls & Data Types
*   **Null Safety**: Fields like `nextStopName` or `time_remaining` might be null or "--" if the bus is finishing its trip or data is insufficient. Handle these gracefully in UI (e.g., show "Arriving..." or hide the fields).
*   **Data Types**: `lat` and `lng` are always `Double`. `speed_kmph` is `Int` (rounded). ensure your Kotlin data classes match these types.

### 4. Connection to Localhost
*   **Android Emulator**: You cannot use `localhost` inside the emulator. You **MUST** use `http://10.0.2.2:3000`.
*   **Physical Device**: Connect your phone via USB or same Wi-Fi. Find your Mac's local IP (e.g., `192.168.1.5`) and use `http://192.168.1.5:3000`.

### 5. Production Readiness
*   This server uses in-memory caching for bus states (`BusStateManager.js`). If the server restarts, live bus locations might reset to "OFFLINE" until the next GPS ping is received. This is expected behavior for this MVP architecture.
