package kobramob.rubeg38.ru.gbrnavigation.workservice

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import org.osmdroid.util.GeoPoint
import java.lang.Exception

class MyLocation : LocationListener {
    companion object {
        var imHere: Location? = null
        var Enable:Boolean = false
    }

    @SuppressLint("MissingPermission")
    fun initLocation(context: Context?) {

        val locationManager: LocationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                0.toFloat(),
                this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                1000,
                0.toFloat(),
                this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                0.toFloat(),
                this
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        when{
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)->{
                imHere = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)->{
                imHere = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)->{
                imHere = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }
        }
        return
    }

    override fun onLocationChanged(location: Location?) {
        imHere = location
        Enable = true
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }
}