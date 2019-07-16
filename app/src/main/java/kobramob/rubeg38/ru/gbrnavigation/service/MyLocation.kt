package kobramob.rubeg38.ru.gbrnavigation.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import java.lang.Exception

class MyLocation : LocationListener {
    companion object {
        var imHere: Location? = null
    }

    @SuppressLint("MissingPermission")
    fun initLocation(context: Context) {
        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, 100.toFloat(), this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                8000,
                100.toFloat(),
                this
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        imHere = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    override fun onLocationChanged(location: Location?) {
        imHere = location
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }
}