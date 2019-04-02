package kobramob.rubeg38.ru.gbrnavigation.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import java.lang.Exception
import java.util.*

class PollingServer : Service(), LocationListener {

    private val timer: Timer = Timer()
    val LOG_TAG = "PollingService"
    private var currentLocation: Location? = null
    @SuppressLint("MissingPermission")

    private fun getLocation() {
        try {
            val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
            } catch (e: Exception) {}
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
            } catch (e: Exception) {}
        } catch (e: Exception) { e.printStackTrace() }
    }
    override fun onLocationChanged(location: Location?) {
        currentLocation = location
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//
    }

    override fun onProviderEnabled(provider: String?) {
//
    }

    override fun onProviderDisabled(provider: String?) {
//
    }

    override fun onCreate() {
        super.onCreate()
        getLocation()
        Log.d(LOG_TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "OnStartCommand")
        startService()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startService() {
        timer.scheduleAtFixedRate(MainTask(), 0, 10000)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()

        Log.d(LOG_TAG, "OnDestroy")
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    inner class MainTask : TimerTask() {
        override fun run() {
            try {
            val intent = Intent(StartActivity.BROADCAST_ACTION)
            intent.putExtra("test", "Информация пошла")
            sendBroadcast(intent)
                println("Lat " + currentLocation!!.latitude + " Lon " + currentLocation!!.latitude)
            } catch (e: Exception) {}
        }
    }
}
