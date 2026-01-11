<p align=center><img src="https://i.postimg.cc/yY9yzDW8/hithobus.jpg" alt="Logo" width="150"></p>

## Introduction
**HithoBus** is a centralized, user-friendly bus tracking platform that integrates all major Indian government road
transport services such as:

UTC (Uttarakhand Transport Corportation)

UPSRTC (Uttar Pradesh State Road Transport Corporation)

PBSRTC (Punjab State Road Transport Corporation)

HRSRTC (Haryana State Road Transport Corporation)

RJSRTC (Rajasthan State Road Transport Corporation)

and more…

The application allows passengers to track government buses in real time, even when the internet connection is weak or unavailable.

The most important feature of our app is its **offline tracking capability**. The app stores previously synced bus routes, schedules, and last-known bus locations, enabling users to continue tracking without active internet access — making it ideal for rural and low-network areas.


## Tech Stack

**Language & Framework:** 

- Kotlin 
- JetPack Compose

**Modern UI:**
- Google's Material 3 ->*Provides optimized, modern UI components.*
- Navigation 3 ->*Ensures smooth screen transitions and clean navigation flow.*

**Backend:** 
- NodeJs
- ExpressJS

**Database:** 
- MongoDB

**Python Library:** Pandas,GeoPandas 


 
## Features

- Simple UI
- Live Govt Roadways Buses Tracking 
- Offline tracking capability
- Automatic stores all new routes in the database
- Speed and arrived estimated time calculation 
- Bus timing schedules 
## Why are we choosing this problem ?

There are some challenges that we face & notice -

- Unlike trains, buses operate in a highly decentralized ecosystem.
- Many passengers face long and uncertain waiting times for govt buses.
- No uniform mandate for GPS or data sharing.



## Demo 

<p align=center><img src="https://i.postimg.cc/NF0JGB9G/Screenshot-2026-01-11-223404.png
" alt="demoImage"></p>

<p align=center><img src="https://i.postimg.cc/hhWdy0qZ/Screenshot-2026-01-11-223325.png" alt="demoImage" width=200></p>


Click here to download the APK - [download](https://drive.google.com/file/d/11J7U6uBo3CVTBF8ThX2rB4mm1T9HtNis/view)


## Contributing

We all worked as a team on this project. However, each team member made contributions in specific areas of the app, such as:-

1. Rohan Tewari (Team Leader) -
- Designed the mobile UI.
- Implemented continuous GPS & cell tracking.
- Built route-aware tower tracking.

2. Abhi Mishra - 
- Assisted in UI/UX design.
- Handled research, documentation and solution planning.

3. Nayan - 
- Handled backend development.
- Managed server-side logic.
- Created backend APIs for tracking and routes.

4. Ravi Yadav -
- Managed the database and ensured data integrity.
- Contributed to backend APIs and server logic.
- Handled backend deployment.
## Working Architecutre

### How the offline tracking is done using cell tower ?
1. **Route & Tower Data Fetching**

- Fetch route data from *route.geojson* file via REST API (Node backend)

- Create towers.json containing nearby cell towers along the highway

- Database: MongoDB with REST API endpoints

2. **Cell Tower Detection**

- Use TelephonyManager class (Android API) to fetch current connected cell tower

- Support both newer Android versions and legacy PhoneStateListener

*code snippet of fetching current cell tower -* 

```
fun startMonitoring() {
    telephonyCallback?.let {
        try {
            telephonyManager.registerTelephonyCallback(context.mainExecutor, callback = it)
            Log.d(TAG, "✓ TelephonyCallback registered (API 31+)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register TelephonyCallback", tr = e)
        }
    } else {
        @Suppress("DEPRECATION")
        phoneStateListener?.let {
            try {
                telephonyManager.listen(
                    listener = it,
                    events = PhoneStateListener.LISTEN_CELL_INFO
                )
                Log.d(TAG, "✓ PhoneStateListener registered (Legacy)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register PhoneStateListener", tr = e)
            }
        }
    }
}

```

3. **Tower Comparison & Storage**

- Compare fetched tower with database entries

- Store tower data in Room Database for local querying

- Run search query to match connected tower against database

*code snippet of foreground service logic*
```
class BusTrackingService : Service() {

    private fun startTracking() {

        // Start as foreground service with notification
        startForeground(
            id = NOTIFICATION_ID,
            notification = createNotification(status = "Starting bus tracking...")
        )

        // Cancel any existing tracking job
        trackingJob?.cancel()

        // Start new tracking loop
        trackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Call your existing BusTracker logic
                    val location = busTracker.getCurrentBusLocation()
                    _busLocation.value = location

                    // Update notification with current status
                    updateNotification(location)

                    Log.d(TAG, "Location updated: $location")

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update location", e)
                }

                // Wait for some interval before next update
                delay(TRACKING_INTERVAL_MS)
            }
        }
    }
}

```

4. **Location Resolution**

- If tower exists in DB → *Show approximate location*

- If tower not found → *Prompt user to contribute with live GPS data*

### How the online tracking is done using ( GPS + Crowdsourcing ) ?
**User Roles:**

Outside Bus - *Fetch live location from crowdsourced GPS data (bus occupants)*

Inside Bus - *Can choose between cell tracking or GPS crowdsourcing*

**GPS Crowdsourcing Process:**

1.  GPS-enabled users inside bus contribute their real-time location

2. Fetch location every 15 seconds (optimized for battery efficiency)

3. Use Fused Location Provider from Google Play Services (GMS Location API)

4. Implement Flow<Location> with LocationCallback for location updates

**Server Communication:**

1. Send GPS location to server via REST calls with polling mechanism

2. Polling Interval - Every 15 seconds (prevents system overload)

3. Server processes location data and calculates:

    - Nearest bus stop

    - Relevant information for tracking

4. Server sends response back to app via polling

<br>

**Android Services Implementation**

**Bus Tracking Service**

- Run as Foreground Service with persistent notification

- Cancel existing tracking job before starting new one

- Continuous location update loop while service active

- Update notification with current location status

**Location Crowdsourcing Service**

-  Collect GPS location at 15-second intervals

-  Extract latitude, longitude, speed (convert m/s → km/h)

-  Update foreground notification with location

-  Post location data to server

