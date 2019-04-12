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
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*

class PollingServer : Service(), LocationListener {

    val response: SingleResponse = SingleResponse()
    val request: SingleRequest = SingleRequest()
    private val coder: Coder = Coder()

    private val timer: Timer = Timer()
    private val LOG_TAG = "PollingService"
    private var currentLocation: Location? = null

    private var count: Long = 0
    private val typePacket: Byte = 0
    private val typePacket255: Byte = 255.toByte()

    companion object {
        val socket: DatagramSocket = DatagramSocket()
    }

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
        // DataPacketReceiver().start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()

        Log.d(LOG_TAG, "OnDestroy")
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun clientRegister(): String {
        count++
        return request.register(socket, count, typePacket)
    }

    inner class MainTask : TimerTask(), Runnable {
        override fun run() {
            try {
                val sharedPreferences = getSharedPreferences("state", Context.MODE_PRIVATE)
                val stopService = sharedPreferences.getBoolean("service", false)
                // count++
                if (!stopService) {
                    request.nullPacket(socket)
                    val ipAddress = "192.168.2.110"
                    val receiverBuffer: ByteArray = kotlin.ByteArray(1041)
                    val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
                    socket.receive(receiverPacket)
                    // println(Arrays.toString(receiverPacket.data))
                    if (receiverPacket.data[0].toInt() != 0) {
                        request.packetType255(socket, receiverPacket.data)
                        println(coder.encoderPacketOne(receiverPacket.data))
                    }
                    /*val responseServer = response.serverReceiver(socket)
                    when(responseServer){
                        "255"->{
                            println(255)
                            request.nullPacket(socket)
                        }
                        "Null"->{
                            println("Null")
                            request.nullPacket(socket)
                        }
                        else->{
                            println("Else")
                            println(responseServer)
                        }
                    }*/
                }
                /*val intent = Intent(StartActivity.BROADCAST_ACTION)
                intent.putExtra("test", "Информация пошла")
                sendBroadcast(intent)
                println("Lat " + currentLocation!!.latitude + " Lon " + currentLocation!!.longitude)*/
            } catch (e: Exception) {}
        }
    }
}
