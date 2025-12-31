package com.example.celltowertrackingforbus.BusTracking

import android.content.Context
import android.util.Log
import org.json.JSONObject

data class Stop(
    val id: String,
    val name: String,
    val lat: Double,
    val long: Double,
    val sequence: Int
)

class StopRepository(private val context: Context) {

    private var stops: List<Stop>? = null

    fun loadStops(): List<Stop> {
        if (stops != null) return stops!!

        try {
            val json = context.assets
                .open("stops.geojson")
                .bufferedReader()
                .use { it.readText() }

            Log.d("StopRepo", "Loaded JSON: $json")

            val jsonObject = JSONObject(json)

            // Check if it's GeoJSON format (has "features") or custom format (has "stops")
            stops = if (jsonObject.has("features")) {
                parseGeoJsonStops(jsonObject)
            } else if (jsonObject.has("stops")) {
                parseCustomStops(jsonObject)
            } else {
                Log.e("StopRepo", "Unknown JSON format")
                emptyList()
            }

            Log.d("StopRepo", "Parsed ${stops?.size ?: 0} stops")
            return stops!!

        } catch (e: Exception) {
            Log.e("StopRepo", "Error loading stops", e)
            return emptyList()
        }
    }

    private fun parseGeoJsonStops(jsonObject: JSONObject): List<Stop> {
        val features = jsonObject.getJSONArray("features")

        return List(features.length()) { i ->
            val feature = features.getJSONObject(i)
            val properties = feature.getJSONObject("properties")
            val geometry = feature.getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")

            Stop(
                id = properties.optString("id", i.toString()),
                name = properties.getString("name"),
                lat = coordinates.getDouble(1),  // GeoJSON is [lon, lat]
                long = coordinates.getDouble(0),
                sequence = properties.optInt("sequence", i + 1)
            )
        }
    }

    private fun parseCustomStops(jsonObject: JSONObject): List<Stop> {
        val stopsArray = jsonObject.getJSONArray("stops")

        return List(stopsArray.length()) { i ->
            val stop = stopsArray.getJSONObject(i)
            val location = stop.getJSONArray("location")

            Stop(
                id = stop.getString("id"),
                name = stop.getString("name"),
                lat = location.getDouble(1),
                long = location.getDouble(0),
                sequence = stop.getInt("sequence")
            )
        }
    }
}
