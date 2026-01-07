package com.example.celltowertrackingforbus

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.celltowertrackingforbus.CellTracking.CellTowerInfo
import com.example.celltowertrackingforbus.CellTracking.CellTowerMonitor
import com.example.celltowertrackingforbus.RoomDatabase.Tower
import com.example.celltowertrackingforbus.RoomDatabase.TowersDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


class UploadingViewModel: ViewModel() {
    var isLoading by mutableStateOf(false)
    var isNotFound by mutableStateOf(false)
    var currentTowerInfo by mutableStateOf<CellTowerInfo?>(null)
    var currentLocation by mutableStateOf<Tower?>(null)
    var locationStatus by mutableStateOf("Not tracking")
    private var cellTowerMonitor: CellTowerMonitor? = null

    fun uploadMissingTowerSuspend(db: TowersDatabase){
        viewModelScope.launch(Dispatchers.IO) {
            isNotFound = false
            uploadMissingTower(db)
        }
    }
    suspend fun uploadMissingTower(db: TowersDatabase){
        try {

            val towerInfo = currentTowerInfo
            if (towerInfo != null) {
                val newTower = Tower(
                    mcc = towerInfo.mcc,
                    mnc = towerInfo.mnc,
                    lac = towerInfo.lac,
                    cid = towerInfo.cid,
                    lat = towerInfo.lat?:0.0,
                    long = towerInfo.lat?:0.0,
                )
                db.dao.insertTower(newTower)
                Log.d("TowerRepo", "Inserted missing tower into database: $newTower")
            }


        }catch (e: Exception){
            Log.e("TowerRepo", "Error pushing tower", e)
            isNotFound = true
        }
    }
    fun uploadTowersSuspend(db: TowersDatabase,context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            uploadTowers(db,context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun startTracking(db: TowersDatabase, context: Context) {
        cellTowerMonitor = CellTowerMonitor(context)
        cellTowerMonitor?.startMonitoring()

        viewModelScope.launch(Dispatchers.IO) {
            cellTowerMonitor?.currentCellTower?.collect { towerInfo ->
                if (towerInfo != null) {
                    currentTowerInfo = towerInfo

                    val foundTower = db.dao.findTower(
                        towerInfo.mcc,
                        towerInfo.mnc,
                        towerInfo.lac,
                        towerInfo.cid
                    )

                    if (foundTower != null) {
                        currentLocation = foundTower
                        locationStatus = "Location found"
                        isNotFound = false
                    } else {
                        currentLocation = null
                        locationStatus = "Tower not in database"
                        isNotFound = true
                    }
                }
            }
        }
    }

    fun stopTracking() {
        cellTowerMonitor?.stopMonitoring()
        cellTowerMonitor = null
        locationStatus = "Not tracking"
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }

    fun emptyTowersTableSuspend(db: TowersDatabase){
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            emptyTowersTable(db)
        }
    }
    suspend fun emptyTowersTable(db: TowersDatabase){
        try {
            db.dao.deleteEntries()
            Log.d("TowerRepo", "Towers removed successfully!!")
        }catch (e: Exception){
            Log.e("TowerRepo", "Error removing towers", e)
        }
        isLoading = false
    }

    suspend fun uploadTowers(db: TowersDatabase,context: Context){
        try {
            val towersGeoJson = context.assets.open("towers.geojson")
                .bufferedReader()
                .use { it.readText() }
            val towers = parseGeoJsonToTowers(towersGeoJson)
            db.dao.insertTowers(towers)
            Log.d("TowerRepo", "Loaded ${towers.size} towers into database")
        }catch (e: Exception){
            Log.e("TowerRepo", "Error loading towers", e)
        }
        isLoading = false
    }
    private fun parseGeoJsonToTowers(geojsonString: String): List<Tower>{
        val towers = mutableListOf<Tower>()
        val json = JSONObject(geojsonString)
        val features = json.getJSONArray("features")
        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val properties = feature.getJSONObject("properties")
            val geometry = feature.getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")

            towers.add(
                Tower(
                    mcc = properties.getInt("mcc"),
                    mnc = properties.getInt("mnc"),
                    lac = properties.getInt("lac"),
                    cid = properties.getLong("cid"),
                    lat = coordinates.getDouble(1),  // GeoJSON is [lon, lat]
                    long = coordinates.getDouble(0),
                )
            )
        }

        return towers
    }
}