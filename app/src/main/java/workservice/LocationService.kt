package workservice

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import java.lang.Exception

class LocationService : LocationListener {
    companion object {
        var imHere: Location? = null
        var Enable:Boolean = false
        var oldTime:Long? = null
        var newTime:Long? = null
    }

    @SuppressLint("MissingPermission")
    fun initLocation(context: Context?) {

        val locationManager: LocationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500,
                10.toFloat(),
                this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                500,
                10.toFloat(),
                this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                500,
                10.toFloat(),
                this)
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
        if(newTime!=null){
            oldTime = newTime

        }
        imHere = location
        newTime = System.currentTimeMillis()
        if(oldTime!=null){
            Log.d("LocationService","Time ${(newTime!! - oldTime!!)/1000}")
        }
        Enable = true
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }
}