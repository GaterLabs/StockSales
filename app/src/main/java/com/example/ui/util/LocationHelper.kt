package com.example.ui.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        // 1. Try fastest: Best last known location
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.time > bestLocation.time) {
                            bestLocation = loc
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // If last known location is fresh enough (within 10 minutes), use it
        val tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000)
        if (bestLocation != null && bestLocation.time > tenMinutesAgo) {
            return bestLocation
        }

        // 2. Otherwise, request a quick single location update with a 6-second timeout
        val freshLocation = withTimeoutOrNull(6000L) {
            suspendCancellableCoroutine<Location?> { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        try {
                            locationManager.removeUpdates(this)
                        } catch (_: Exception) {}
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                try {
                    val chosenProvider = when {
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                        else -> null
                    }

                    if (chosenProvider != null) {
                        locationManager.requestSingleUpdate(chosenProvider, listener, Looper.getMainLooper())
                        continuation.invokeOnCancellation {
                            try {
                                locationManager.removeUpdates(listener)
                            } catch (_: Exception) {}
                        }
                    } else {
                        continuation.resume(bestLocation)
                    }
                } catch (e: Exception) {
                    continuation.resume(bestLocation)
                }
            }
        }

        return freshLocation ?: bestLocation
    }

    suspend fun getAddressFromLocation(
        context: Context,
        location: Location,
        fallbackRouteArea: String = ""
    ): String = withContext(Dispatchers.IO) {
        val lat = location.latitude
        val lon = location.longitude

        val fallbackGpsText = if (fallbackRouteArea.isNotBlank()) {
            "GPS: ${String.format(Locale.US, "%.5f, %.5f", lat, lon)} ($fallbackRouteArea)"
        } else {
            "GPS: ${String.format(Locale.US, "%.5f, %.5f", lat, lon)}"
        }

        try {
            if (!Geocoder.isPresent()) {
                return@withContext fallbackGpsText
            }

            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = try {
                geocoder.getFromLocation(lat, lon, 1)
            } catch (e: Exception) {
                null
            }

            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val fullAddressLines = mutableListOf<String>()

                val line0 = addr.getAddressLine(0)
                if (!line0.isNullOrBlank()) {
                    return@withContext line0
                }

                val street = listOfNotNull(addr.thoroughfare, addr.subThoroughfare).joinToString(" ")
                if (street.isNotBlank()) fullAddressLines.add(street)

                val subLoc = addr.subLocality ?: addr.locality
                if (!subLoc.isNullOrBlank()) fullAddressLines.add(subLoc)

                val city = addr.subAdminArea ?: addr.adminArea
                if (!city.isNullOrBlank()) fullAddressLines.add(city)

                if (fullAddressLines.isNotEmpty()) {
                    return@withContext fullAddressLines.joinToString(", ")
                }
            }

            return@withContext fallbackGpsText
        } catch (e: Exception) {
            return@withContext fallbackGpsText
        }
    }

    /**
     * 100% Offline Haversine formula calculation in meters.
     * Requires zero internet or external map API.
     */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Radius of earth in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    /**
     * Formats distance cleanly (e.g. "85 m", "350 m", "1.2 km", "4.8 km")
     */
    fun formatDistance(meters: Double): String {
        return if (meters < 1000.0) {
            "${meters.toInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", meters / 1000.0)
        }
    }

    /**
     * Extracts coordinates from StoreEntity either from latitude/longitude columns
     * or fallback parsing "GPS: -6.12345, 106.12345" in address text.
     */
    fun getStoreCoordinates(store: com.example.data.model.StoreEntity): Pair<Double, Double>? {
        if (store.latitude != null && store.longitude != null) {
            return Pair(store.latitude, store.longitude)
        }
        val gpsRegex = Regex("""(-?\d+\.\d+)\s*,\s*(-?\d+\.\d+)""")
        val match = gpsRegex.find(store.address)
        if (match != null) {
            val lat = match.groupValues[1].toDoubleOrNull()
            val lon = match.groupValues[2].toDoubleOrNull()
            if (lat != null && lon != null) {
                return Pair(lat, lon)
            }
        }
        return null
    }

    /**
     * Calculates distance from current user location to a specific store.
     */
    fun getDistanceToStoreMeters(userLocation: Location?, store: com.example.data.model.StoreEntity): Double? {
        if (userLocation == null) return null
        val storeCoords = getStoreCoordinates(store) ?: return null
        return calculateDistanceMeters(
            lat1 = userLocation.latitude,
            lon1 = userLocation.longitude,
            lat2 = storeCoords.first,
            lon2 = storeCoords.second
        )
    }

    /**
     * Starts continuous offline GPS updates (low latency, on-device).
     * Returns a cancellation lambda.
     */
    @SuppressLint("MissingPermission")
    fun startOfflineLocationTracking(
        context: Context,
        onLocationChanged: (Location) -> Unit
    ): () -> Unit {
        if (!hasLocationPermission(context)) return {}

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return {}

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onLocationChanged(location)
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            // Provide immediate last known location if available
            val lastGps = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else null
            val lastNet = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null
            val initial = when {
                lastGps != null && lastNet != null -> if (lastGps.time >= lastNet.time) lastGps else lastNet
                lastGps != null -> lastGps
                lastNet != null -> lastNet
                else -> null
            }
            if (initial != null) {
                onLocationChanged(initial)
            }

            // Register continuous updates: every 3 seconds or 5 meters
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L,
                    5f,
                    listener,
                    Looper.getMainLooper()
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L,
                    5f,
                    listener,
                    Looper.getMainLooper()
                )
            }
        } catch (_: Exception) {}

        return {
            try {
                locationManager.removeUpdates(listener)
            } catch (_: Exception) {}
        }
    }
}
