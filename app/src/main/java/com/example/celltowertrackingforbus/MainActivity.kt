package com.example.celltowertrackingforbus

import android.Manifest
import android.content.ServiceConnection
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.celltowertrackingforbus.BusTracking.BusServiceController
import com.example.celltowertrackingforbus.BusTracking.BusTrackingService
import com.example.celltowertrackingforbus.Screens.OfflineBusTracker
import com.example.celltowertrackingforbus.Screens.OfflineBusTrackerLoading
import com.example.celltowertrackingforbus.ui.theme.CellTowerTrackingForBusTheme
import com.example.celltowertrackingforbus.NewScreens.AppNavigation
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    // Service connection for binding
    private var serviceConnection: ServiceConnection? = null

    // Mutable state to trigger recomposition when service connects
    private var busTrackingService by mutableStateOf<BusTrackingService?>(null)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val phoneStateGranted = permissions[Manifest.permission.READ_PHONE_STATE] == true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
            if (!notificationPermissionGranted) {
                Toast.makeText(this, "Notification permission is required", Toast.LENGTH_LONG).show()
                openAppSettings()
                return@registerForActivityResult
            }
        }

        when {
            !locationGranted -> {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
            !phoneStateGranted -> {
                Toast.makeText(this, "Phone State permission is required", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
            else -> {
                Toast.makeText(this, "Location and State Permissions granted!", Toast.LENGTH_SHORT).show()
                // Request background location separately (Android 10+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocation()
                }
                // Service will be started when user clicks "I am inside bus"
            }
        }
    }

    // Separate launcher for background location
    private val backgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Background location granted - service will start when user clicks "I am inside bus"
            Toast.makeText(this, "Background location granted!", Toast.LENGTH_SHORT).show()
        } else {
            // Background location is optional - service can still work in foreground
            Toast.makeText(this, "Background location denied, tracking may stop when app is closed", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Check if already granted
            if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Background location already granted
                return
            }

            // On Android 11+, must direct user to settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Toast.makeText(
                    this,
                    "Please select 'Allow all the time' for location",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            } else {
                // Android 10 can still use the launcher
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }


    private fun requestPermissions() {
        val permissionsToRequest = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.READ_PHONE_STATE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            // DON'T include ACCESS_BACKGROUND_LOCATION here
        }

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }





    @RequiresApi(Build.VERSION_CODES.R)
    @androidx.annotation.RequiresPermission(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)



        requestPermissions()

        enableEdgeToEdge()
        setContent {
            val navController: NavHostController = rememberNavController()
            CellTowerTrackingForBusTheme {
                Scaffold(modifier = Modifier.fillMaxSize())  { innerPadding ->
//                  HomeScreen(innerPadding, db, uploadingViewModel)
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
//                        // Optional: Add a minimum delay before showing tracker
//                        var isReady by remember { mutableStateOf(false) }
//
//                        LaunchedEffect(Unit) {
//                            delay(3000) // Wait 3 seconds to observe loading UI
//                            isReady = true
//                        }
//
//                        // When service is bound AND ready, use its StateFlow
//                        val service = busTrackingService
//                        if (service != null && isReady) {
//                            OfflineBusTracker(service.busLocation, service)
//                        } else {
//                            // Show loading state while waiting for service to bind
//                            OfflineBusTrackerLoading()
//                        }
                         AppNavigation(
                             navController = navController,
                             busLocationFlow = busTrackingService?.busLocation,
                             onStartTracking = {
                                 BusServiceController.startTracking(this@MainActivity)
                             }
                         )
                    }
                }
            }
        }
    }

    private fun bindToService() {
        serviceConnection = BusServiceController.bindService(this) { service ->
            busTrackingService = service
            // The UI will now automatically observe the service's StateFlow
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to service when activity becomes visible
        bindToService()
    }

    override fun onStop() {
        super.onStop()
        // Unbind when activity is no longer visible (service continues in background)
        serviceConnection?.let {
            unbindService(it)
            serviceConnection = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BusServiceController.stopTracking(this)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
