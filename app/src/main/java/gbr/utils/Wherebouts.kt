package gbr.utils

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import gbr.ui.start.StartActivity
import gbr.utils.data.GPSPoint
import gbr.utils.models.Workable
import gbr.utils.servicess.ProtocolService


/**
 * Uses Google Play API for obtaining device locations
 * Created by alejandro.tkachuk
 * alejandro@calculistik.com
 * www.calculistik.com Mobile Development
 */
@SuppressLint("MissingPermission")
class Wherebouts private constructor() {
    private val mFusedLocationClient: FusedLocationProviderClient
    private val locationCallback: LocationCallback
    private val locationRequest: LocationRequest = LocationRequest()
    val locationSettingsRequest: LocationSettingsRequest
    private var workable: Workable<GPSPoint>? = null

    fun onChange(workable: Workable<GPSPoint>) {
        this.workable = workable
    }

    fun stop() {
        Log.i(TAG, "stop() Stopping location tracking")
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private val instance = Wherebouts()
        private val TAG = Wherebouts::class.java.simpleName
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
        fun instance(): Wherebouts {
            return instance
        }
    }

    init {
        locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        locationSettingsRequest = builder.build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult) // why? this. is. retarded. Android.
                val currentLocation = locationResult.lastLocation
                val gpsPoint = GPSPoint(currentLocation)
                Log.i(TAG, "Location Callback results: $gpsPoint")
                if (null != workable) workable!!.work(gpsPoint)
            }
        }
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(ProtocolService.context)
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback, Looper.myLooper()
        )
    }

}

