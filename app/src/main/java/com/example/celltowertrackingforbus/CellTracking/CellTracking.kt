package com.example.celltowertrackingforbus.CellTracking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CellTowerInfo(
    val mcc: Int,
    val mnc: Int,
    val lac: Int,
    val cid: Long,
    val type: String,
    val lat: Double? = null,
    val long: Double? = null
)

class CellTowerMonitor(private val context: Context) {

    companion object {
        private const val TAG = "CellTowerMonitor"
    }

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val _currentCellTower = MutableStateFlow<CellTowerInfo?>(null)
    val currentCellTower: StateFlow<CellTowerInfo?> = _currentCellTower

    private val telephonyCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        object : TelephonyCallback(), TelephonyCallback.CellInfoListener {
            override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
                Log.d(TAG, "Cell info changed (API 31+) - ${cellInfo.size} cells")
                updateCellTowerInfo()
            }
        }
    } else {
        null
    }

    private val phoneStateListener = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        object : PhoneStateListener() {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE])
            @RequiresApi(Build.VERSION_CODES.R)
            @Deprecated("Deprecated in Java")
            override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>?) {
                super.onCellInfoChanged(cellInfo)
                Log.d(TAG, "Cell info changed (Legacy) - ${cellInfo?.size} cells")
                updateCellTowerInfo()
            }
        }
    } else {
        null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun startMonitoring() {
        Log.d(TAG, "=== STARTING CELL TOWER MONITORING ===")
        Log.d(TAG, "Android Version: ${Build.VERSION.SDK_INT}")
        Log.d(TAG, "Device: ${Build.MANUFACTURER} ${Build.MODEL}")

        if (!hasPermissions()) {
            Log.e(TAG, "CRITICAL: Missing required permissions!")
            return
        }

        Log.d(TAG, "Permissions granted, registering listener...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let {
                try {
                    telephonyManager.registerTelephonyCallback(context.mainExecutor, it)
                    Log.d(TAG, "✓ TelephonyCallback registered (API 31+)")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to register TelephonyCallback", e)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            phoneStateListener?.let {
                try {
                    telephonyManager.listen(it, PhoneStateListener.LISTEN_CELL_INFO)
                    Log.d(TAG, "✓ PhoneStateListener registered (Legacy)")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to register PhoneStateListener", e)
                }
            }
        }

        Log.d(TAG, "Performing initial cell info update...")
        updateCellTowerInfo()
    }

    fun stopMonitoring() {
        Log.d(TAG, "Stopping cell tower monitoring")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let {
                try {
                    telephonyManager.unregisterTelephonyCallback(it)
                    Log.d(TAG, "✓ TelephonyCallback unregistered")
                } catch (e: Exception) {
                    Log.e(TAG, "Error unregistering TelephonyCallback", e)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            phoneStateListener?.let {
                try {
                    telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
                    Log.d(TAG, "✓ PhoneStateListener unregistered")
                } catch (e: Exception) {
                    Log.e(TAG, "Error unregistering PhoneStateListener", e)
                }
            }
        }
    }

    private fun hasPermissions(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val phoneState = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "Permission Check:")
        Log.d(TAG, "  - FINE_LOCATION: $fineLocation")
        Log.d(TAG, "  - COARSE_LOCATION: $coarseLocation")
        Log.d(TAG, "  - READ_PHONE_STATE: $phoneState")

        return fineLocation && phoneState
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun updateCellTowerInfo() {
        Log.d(TAG, "--- updateCellTowerInfo() called ---")

        if (!hasPermissions()) {
            Log.e(TAG, "Cannot update - permissions missing")
            return
        }

        try {
            val cellInfoList = telephonyManager.allCellInfo

            if (cellInfoList == null) {
                Log.w(TAG, "⚠ cellInfoList is NULL")
                return
            }

            if (cellInfoList.isEmpty()) {
                Log.w(TAG, "⚠ cellInfoList is EMPTY")
                return
            }

            Log.d(TAG, "Found ${cellInfoList.size} cell tower(s)")

            // Log all cells (registered and not)
            cellInfoList.forEachIndexed { index, cellInfo ->
                Log.d(TAG, "Cell[$index]: " +
                        "type=${cellInfo.javaClass.simpleName}, " +
                        "registered=${cellInfo.isRegistered}, " +
                        "strength=${cellInfo.cellSignalStrength}"
                )
            }

            // First pass: try to find registered cell
            for (cellInfo in cellInfoList) {
                if (!cellInfo.isRegistered) continue

                val towerInfo = extractCellInfo(cellInfo)
                if (towerInfo != null) {
                    _currentCellTower.value = towerInfo
                    Log.d(TAG, "✓ Found REGISTERED tower: $towerInfo")
                    return
                }
            }

            // Second pass: if no registered cell, take any valid cell
            Log.w(TAG, "No registered cell found, checking all cells...")
            for (cellInfo in cellInfoList) {
                val towerInfo = extractCellInfo(cellInfo)
                if (towerInfo != null) {
                    _currentCellTower.value = towerInfo
                    Log.d(TAG, "✓ Found NON-REGISTERED tower: $towerInfo")
                    return
                }
            }

            Log.w(TAG, "⚠ No valid cell tower found in ${cellInfoList.size} cells")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cell tower info", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun extractCellInfo(cellInfo: CellInfo): CellTowerInfo? {
        return try {
            when (cellInfo) {
                is CellInfoGsm -> {
                    val identity = cellInfo.cellIdentity
                    Log.d(TAG, "  GSM - MCC:${identity.mcc}, MNC:${identity.mnc}, LAC:${identity.lac}, CID:${identity.cid}")

                    if (identity.mcc == Int.MAX_VALUE || identity.mcc < 200 || identity.mcc > 799) {
                        Log.w(TAG, "  ✗ Invalid GSM MCC: ${identity.mcc}")
                        return null
                    }

                    CellTowerInfo(
                        mcc = identity.mcc,
                        mnc = identity.mnc,
                        lac = identity.lac,
                        cid = identity.cid.toLong(),
                        type = "GSM"
                    )
                }

                is CellInfoLte -> {
                    val identity = cellInfo.cellIdentity
                    Log.d(TAG, "  LTE - MCC:${identity.mcc}, MNC:${identity.mnc}, TAC:${identity.tac}, CI:${identity.ci}")

                    if (identity.mcc == Int.MAX_VALUE || identity.mcc < 200 || identity.mcc > 799) {
                        Log.w(TAG, "  ✗ Invalid LTE MCC: ${identity.mcc}")
                        return null
                    }

                    CellTowerInfo(
                        mcc = identity.mcc,
                        mnc = identity.mnc,
                        lac = identity.tac,
                        cid = identity.ci.toLong(),
                        type = "LTE"
                    )
                }

                is CellInfoWcdma -> {
                    val identity = cellInfo.cellIdentity
                    Log.d(TAG, "  WCDMA - MCC:${identity.mcc}, MNC:${identity.mnc}, LAC:${identity.lac}, CID:${identity.cid}")

                    if (identity.mcc == Int.MAX_VALUE || identity.mcc < 200 || identity.mcc > 799) {
                        Log.w(TAG, "  ✗ Invalid WCDMA MCC: ${identity.mcc}")
                        return null
                    }

                    CellTowerInfo(
                        mcc = identity.mcc,
                        mnc = identity.mnc,
                        lac = identity.lac,
                        cid = identity.cid.toLong(),
                        type = "WCDMA"
                    )
                }

                is CellInfoNr -> {
                    val identity = cellInfo.cellIdentity as CellIdentityNr
                    val mccString = identity.mccString
                    val mncString = identity.mncString

                    Log.d(TAG, "  NR/5G - MCC:$mccString, MNC:$mncString, NCI:${identity.nci}, TAC:${identity.tac}")

                    val mcc = mccString?.toIntOrNull()
                    val mnc = mncString?.toIntOrNull()

                    if (mcc == null || mnc == null) {
                        Log.w(TAG, "  ✗ Invalid NR MCC/MNC - null values")
                        return null
                    }

                    if (mcc < 200 || mcc > 799) {
                        Log.w(TAG, "  ✗ Invalid NR MCC: $mcc")
                        return null
                    }

                    CellTowerInfo(
                        mcc = mcc,
                        mnc = mnc,
                        lac = identity.tac,
                        cid = identity.nci,
                        type = "NR/5G"
                    )
                }

                else -> {
                    Log.d(TAG, "  Unsupported: ${cellInfo.javaClass.simpleName}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting cell info from ${cellInfo.javaClass.simpleName}", e)
            null
        }
    }
}
