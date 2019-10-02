package newVersion

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class LocationListener @SuppressLint("MissingPermission") constructor(locationManager: LocationManager) : LocationListener {

    companion object {
        var imHere: Location? = null
    }

    init {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            50.toFloat(),
            this
        )
        imHere = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }
    override fun onLocationChanged(myLocation: Location?) {
        imHere = myLocation
        println("${imHere?.latitude}")
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        // Изменение статуса GPS
    }

    override fun onProviderEnabled(p0: String?) {
        // Провайдер включен
    }

    override fun onProviderDisabled(p0: String?) {
        // Провайдер выключен
    }
}