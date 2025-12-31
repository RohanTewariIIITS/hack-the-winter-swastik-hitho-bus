package com.example.celltowertrackingforbus.BusTracking

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.celltowertrackingforbus.MainActivity
import com.example.celltowertrackingforbus.R
import com.example.celltowertrackingforbus.RoomDatabase.TowersDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * BusTrackingService - A Foreground Service that wraps BusTracker
 *
 * This service:
 * 1. Keeps BusTracker running in background
 * 2. Shows a persistent notification with current status
 * 3. Exposes location updates via StateFlow for UI binding
 * 4. Handles its own lifecycle (start/stop)
 *
 * Your BusTracker class remains unchanged and reusable!
 */
class BusTrackingService : Service() {

    companion object {
        private const val TAG = "BusTrackingService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "bus_tracking_channel"

        // Actions for controlling the service
        const val ACTION_START = "com.example.celltowertrackingforbus.START_TRACKING"
        const val ACTION_STOP = "com.example.celltowertrackingforbus.STOP_TRACKING"
    }

    // Service scope for coroutines - cancelled when service is destroyed
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Job for the tracking loop - can be cancelled independently
    private var trackingJob: Job? = null

    // Your existing business logic classes
    lateinit var busTracker: BusTracker
    lateinit var db: TowersDatabase
    private lateinit var stopRepository: StopRepository

    // Expose location updates to UI via StateFlow
    private val _busLocation = MutableStateFlow<BusLocation>(BusLocation.Unknown)
    val busLocation: StateFlow<BusLocation> = _busLocation.asStateFlow()

    // Binder for Activity binding (optional - for direct communication)
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BusTrackingService = this@BusTrackingService
    }

    // ==================== LIFECYCLE METHODS ====================

    /**
     * Called once when service is first created.
     * Initialize your dependencies here.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

        // Initialize database
        db = Room.databaseBuilder(
            applicationContext,
            TowersDatabase::class.java,
            "towers.db"
        ).fallbackToDestructiveMigration().build()

        // Initialize repositories
        stopRepository = StopRepository(applicationContext)

        // Initialize BusTracker - your business logic stays the same!
        busTracker = BusTracker(
            context = applicationContext,
            towerDao = db.dao,
            stopRepository = stopRepository
        )
    }

    /**
     * Called each time startService() or startForegroundService() is called.
     * Handle start/stop commands here.
     *
     * @param intent The intent used to start the service
     * @param flags Additional data about start request
     * @param startId Unique ID for this start request
     * @return How system should handle service if killed
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }

        // START_STICKY: System will restart service if killed, but intent will be null
        // START_REDELIVER_INTENT: System will restart with last intent
        // START_NOT_STICKY: Don't restart if killed
        return START_STICKY
    }

    /**
     * Called when an Activity binds to this service.
     * Return the binder for communication.
     */
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service onBind")
        return binder
    }

    /**
     * Called when service is being destroyed.
     * Clean up all resources here.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")

        // Cancel all coroutines
        serviceScope.cancel()
        trackingJob?.cancel()
    }

    // ==================== TRACKING LOGIC ====================

    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    private fun startTracking() {
        Log.d(TAG, "Starting tracking")

        // Start as foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification("Starting bus tracking..."))

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
                    Log.e(TAG, "Error getting location", e)
                }

                delay(5000) // Update every 5 seconds
            }
        }
    }

    private fun stopTracking() {
        Log.d(TAG, "Stopping tracking")

        trackingJob?.cancel()
        trackingJob = null

        // Stop foreground and remove notification
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ==================== NOTIFICATION ====================

    private fun createNotification(status: String): Notification {
        // Intent to open app when notification is tapped
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent for stop action button
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, BusTrackingService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bus Tracking Active")
            .setContentText(status)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your own icon
            .setOngoing(true) // Can't be swiped away
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop Trackinf", stopIntent)
            .build()
    }

    private fun updateNotification(location: BusLocation) {
        val status = when (location) {
            is BusLocation.Active -> {
                val distance = location.distanceToStop
                when (location.status) {
                    StopStatus.AT_STOP -> "At ${location.nearestStop.name}"
                    StopStatus.APPROACHING -> when {
                        distance < 500 -> "Arriving at ${location.nearestStop.name}"
                        distance < 2000 -> "Approaching ${location.nearestStop.name}"
                        else -> "${(distance / 1000).toInt()}km to ${location.nearestStop.name}"
                    }
                    StopStatus.DEPARTED -> "Departed ${location.nearestStop.name}"
                }
            }
            is BusLocation.Unknown -> "Searching for signal..."
        }

        val notification = createNotification(status)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
}
