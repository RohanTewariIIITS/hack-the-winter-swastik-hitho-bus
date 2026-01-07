package com.example.celltowertrackingforbus.BusTracking

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder

/**
 * Helper object to start/stop/bind to BusTrackingService
 * Use this from your Activity or ViewModel
 */
object BusServiceController {

    /**
     * Start the tracking service.
     * On Android 8+, this starts a foreground service.
     */
    fun startTracking(context: Context) {
        val intent = Intent(context, BusTrackingService::class.java).apply {
            action = BusTrackingService.ACTION_START
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8+ requires startForegroundService for background
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Stop the tracking service.
     */
    fun stopTracking(context: Context) {
        val intent = Intent(context, BusTrackingService::class.java).apply {
            action = BusTrackingService.ACTION_STOP
        }
        context.startService(intent)
    }

    /**
     * Bind to the service to get direct access.
     * Returns a ServiceConnection that you must unbind when done.
     *
     * Usage:
     * val connection = BusServiceController.bindService(context) { service ->
     *     // Collect from service.busLocation
     * }
     *
     * // Later, when done:
     * context.unbindService(connection)
     */
    fun bindService(
        context: Context,
        onConnected: (BusTrackingService) -> Unit
    ): ServiceConnection {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as BusTrackingService.LocalBinder).getService()
                onConnected(service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // Service crashed or was killed
            }
        }

        val intent = Intent(context, BusTrackingService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        return connection
    }
}
