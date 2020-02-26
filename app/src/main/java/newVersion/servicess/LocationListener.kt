package newVersion.servicess

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import newVersion.utils.ProviderStatus
import org.greenrobot.eventbus.EventBus


class LocationListener @SuppressLint("MissingPermission") constructor(locationManager: LocationManager) : LocationListener {
    var mGnssStatusCallback: GnssStatus.Callback? = null
    var satelliteCount:Int? = null

    companion object {
        var imHere: Location? = null

    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mGnssStatusCallback = object : GnssStatus.Callback() {
                override fun onSatelliteStatusChanged(status: GnssStatus?) {
                    satelliteCount = status?.satelliteCount
                }
            }
            locationManager.registerGnssStatusCallback(mGnssStatusCallback!!)
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            0F,
            this
        )

        if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
            EventBus.getDefault().postSticky(ProviderStatus("enable"))
        }
        else
        {
            EventBus.getDefault().postSticky(ProviderStatus("notInitialized"))
        }

    }
    override fun onLocationChanged(myLocation: Location?) {
        if(myLocation!=null)
        {
            EventBus.getDefault().post(
                newVersion.utils.Location(
                    myLocation.latitude,
                    myLocation.longitude,
                    myLocation.accuracy,
                    myLocation.speed,
                    satelliteCount
                )
            )
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        // Изменение статуса GPS
    }

    override fun onProviderEnabled(p0: String?) {
        // Провайдер включен
        Log.d("ProviderEnable", "true")
        EventBus.getDefault().postSticky(ProviderStatus("enable"))
    }

    override fun onProviderDisabled(p0: String?) {
        // Провайдер выключен
        Log.d("ProviderEnable", "false")
        EventBus.getDefault().postSticky(ProviderStatus("disable"))
    }
}