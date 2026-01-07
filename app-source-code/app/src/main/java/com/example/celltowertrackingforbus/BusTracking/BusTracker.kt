package com.example.celltowertrackingforbus.BusTracking

import android.Manifest
import android.content.Context
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.celltowertrackingforbus.RoomDatabase.Tower
import com.example.celltowertrackingforbus.RoomDatabase.TowersDao
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject


fun loadRouteGeometry(context: Context): List<LatLng> {
    return try {
        val json = context.assets
            .open("route.geojson")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(json)
        val features = root.getJSONArray("features")
        val geometry = features
            .getJSONObject(0)
            .getJSONObject("geometry")

        val coordinates = geometry.getJSONArray("coordinates")

        val routePoints = ArrayList<LatLng>(coordinates.length())

        for (i in 0 until coordinates.length()) {
            val coord = coordinates.getJSONArray(i)

            val long = coord.getDouble(0)
            val lat = coord.getDouble(1)

            routePoints.add(LatLng(lat, long))
        }

        routePoints
    } catch (e: Exception) {
        Log.e("RouteLoader", "Failed to load route geometry", e)
        emptyList()
    }
}


class BusTracker(
    private val context: Context,
    private val towerDao: TowersDao,
    val stopRepository: StopRepository
) {
    private val stops = stopRepository.loadStops().sortedBy { it.sequence }
    private val routeGeometry = loadRouteGeometry(context)

    // Track previous position to determine direction of travel
    private var previousRouteIndex: Int = -1
    private var isMovingForward: Boolean = true

    // Pre-compute each stop's closest route index for efficiency
    private val stopRouteIndices: Map<String, Int> by lazy {
        stops.associate { stop ->
            val stopPos = LatLng(stop.lat, stop.long)
            val routeIdx = routeGeometry.indices.minByOrNull {
                distanceBetween(stopPos, routeGeometry[it])
            } ?: 0
            stop.id to routeIdx
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    suspend fun getCurrentBusLocation(): BusLocation {
        // 1. Get current cell info (OFFLINE)
        val cellInfo = readCellTower() ?: return BusLocation.Unknown

        val tower = towerDao.findTower(
            cellInfo.mcc,
            cellInfo.mnc,
            cellInfo.lac,
            cellInfo.cid
        ) ?: return BusLocation.Unknown



        val towerPos = LatLng(tower.lat, tower.long)


        //         404
//MNC: 56
//LAC / TAC: 365
//Radio Type: GSM

        // 2. Lookup tower in local DB (OFFLINE)
//        Log.d("BusTracker", "Found tower in DB: $tower")

        // 3. Snap to route (OFFLINE)
//        val towerPos = LatLng(28.72594, 77.89264)


        Log.d("BusTracker", "Tower position: $towerPos")

        // Snap to route and get route index
        val (routePos, currentRouteIndex) = snapToRouteWithIndex(towerPos, routeGeometry)

        // Determine direction of travel based on route index change
        if (previousRouteIndex >= 0) {
            // Only update direction if we've moved significantly (at least 2 indices)
            val indexDelta = currentRouteIndex - previousRouteIndex
            if (kotlin.math.abs(indexDelta) >= 2) {
                isMovingForward = indexDelta > 0
            }
        }
        previousRouteIndex = currentRouteIndex

        // Find the relevant stop with direction-aware status
        val stopInfo = findStopWithStatus(routePos, currentRouteIndex)

        // Derive network name from MNC (common Indian operators)
        val networkName = getNetworkName(cellInfo.mnc)

        return BusLocation.Active(
            estimatedPosition = routePos,
            nearestStop = stopInfo.stop,
            distanceToStop = stopInfo.distance,
            status = stopInfo.status,
            cellInfo = cellInfo,
            towerName = networkName
        )
    }

    private fun getNetworkName(mnc: Int): String {
        return when (mnc) {
            // Jio
            6, 86, 89, 857, 863 -> "Jio 4G"
            // Airtel
            10, 31, 40, 45, 49, 70, 92, 93, 94, 95, 96, 97, 98 -> "Airtel"
            // Vi (Vodafone-Idea)
            11, 20, 84, 88 -> "Vi (Vodafone-Idea)"
            // BSNL
            53, 54, 55, 56, 57, 58, 59, 64, 66, 71, 72, 73, 74, 75, 76, 80 -> "BSNL"
            // MTNL
            1, 3 -> "MTNL"
            else -> "Tower $mnc"
        }
    }

    private fun findStopWithStatus(position: LatLng, currentRouteIndex: Int): StopInfo {
        // Find the nearest stop
        val nearestStop = stops.minByOrNull { stop ->
            distanceBetween(position, LatLng(stop.lat, stop.long))
        }!!

        val distance = distanceBetween(position, LatLng(nearestStop.lat, nearestStop.long))
        val stopRouteIndex = stopRouteIndices[nearestStop.id] ?: 0

        // Determine status based on position relative to stop and direction
        val status = when {
            // Very close to stop - at the stop
            distance < 100 -> StopStatus.AT_STOP

            // Moving forward: if bus route index < stop route index, we're approaching
            isMovingForward && currentRouteIndex < stopRouteIndex -> StopStatus.APPROACHING

            // Moving forward: if bus route index > stop route index, we've passed it
            isMovingForward && currentRouteIndex > stopRouteIndex -> StopStatus.DEPARTED

            // Moving backward (reverse route): if bus route index > stop route index, approaching
            !isMovingForward && currentRouteIndex > stopRouteIndex -> StopStatus.APPROACHING

            // Moving backward: if bus route index < stop route index, we've passed it
            !isMovingForward && currentRouteIndex < stopRouteIndex -> StopStatus.DEPARTED

            // At the same route index as the stop
            else -> if (distance < 200) StopStatus.AT_STOP else StopStatus.APPROACHING
        }

        // If we've departed from the nearest stop, find the next upcoming stop
        if (status == StopStatus.DEPARTED) {
            val nextStop = findNextUpcomingStop(currentRouteIndex)
            if (nextStop != null) {
                val nextDistance = distanceBetween(position, LatLng(nextStop.lat, nextStop.long))
                return StopInfo(nextStop, nextDistance, StopStatus.APPROACHING)
            }
        }

        return StopInfo(nearestStop, distance, status)
    }

    private fun findNextUpcomingStop(currentRouteIndex: Int): Stop? {
        return if (isMovingForward) {
            // Find next stop ahead on the route
            stops.filter { stop ->
                val stopIdx = stopRouteIndices[stop.id] ?: 0
                stopIdx > currentRouteIndex
            }.minByOrNull { stop ->
                stopRouteIndices[stop.id] ?: Int.MAX_VALUE
            }
        } else {
            // Find next stop behind on the route (when going in reverse)
            stops.filter { stop ->
                val stopIdx = stopRouteIndices[stop.id] ?: 0
                stopIdx < currentRouteIndex
            }.maxByOrNull { stop ->
                stopRouteIndices[stop.id] ?: Int.MIN_VALUE
            }
        }
    }

    private fun snapToRouteWithIndex(
        towerPos: LatLng,
        route: List<LatLng>
    ): Pair<LatLng, Int> {
        var minDist = Float.MAX_VALUE
        var bestIndex = 0
        route.forEachIndexed { index, point ->
            val dist = distanceBetween(towerPos, point)
            if (dist < minDist) {
                minDist = dist
                bestIndex = index
            }
        }
        Log.d("BusTracker", "Snapped to route index: $bestIndex, position: ${route[bestIndex]}")
        return Pair(route[bestIndex], bestIndex)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun readCellTower(): CellInfo? {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE)
                as TelephonyManager

        val cells = tm.allCellInfo ?: return null
        val cell = cells.firstOrNull { it.isRegistered } ?: return null

        return when (cell) {
            is CellInfoLte -> {
                val id = cell.cellIdentity
                CellInfo(id.mcc, id.mnc, id.tac, id.ci.toLong())
            }
            is CellInfoGsm -> {
                val id = cell.cellIdentity
                CellInfo(id.mcc, id.mnc, id.lac, id.cid.toLong())
            }
            else -> null
        }
    }


    private fun distanceBetween(p1: LatLng, p2: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return results[0]
    }
}

data class CellInfo(
    val mcc: Int,
    val mnc: Int,
    val lac: Int,
    val cid: Long
)

enum class StopStatus {
    APPROACHING,
    AT_STOP,
    DEPARTED
}

data class StopInfo(
    val stop: Stop,
    val distance: Float,
    val status: StopStatus
)

sealed class BusLocation {
    data class Active(
        val estimatedPosition: LatLng,
        val nearestStop: Stop,
        val distanceToStop: Float,
        val status: StopStatus,
        val cellInfo: CellInfo? = null,
        val towerName: String? = null
    ) : BusLocation()

    object Unknown : BusLocation()
}