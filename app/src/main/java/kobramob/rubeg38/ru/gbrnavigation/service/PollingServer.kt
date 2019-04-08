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
import android.system.Os.socket
import android.R.attr.port

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

    val request: Request = Request()
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
        // timer.scheduleAtFixedRate(MainTask(), 0, 10000)
        DataPacketReceiver().start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()

        Log.d(LOG_TAG, "OnDestroy")
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    inner class DataPacketReceiver : Thread() {
        override fun run() {
            super.run()
            // println(request.registerCar())
            val socket: DatagramSocket = DatagramSocket()
            println(request.register(socket))
            try {

                while (true) {
                    println("Сосить бибу")
                    val receiverBuffer: ByteArray = kotlin.ByteArray(1041)
                    val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
                    socket.receive(receiverPacket)

                    if (receiverPacket.data[0].toInt() != 0) {
                        System.out.println(Arrays.toString(receiverPacket.data))
                    }
                    println(receiverPacket.socketAddress)
                    val sendData = ByteArray(0)
                    val nullPacket = DatagramPacket(sendData, sendData.size, receiverPacket.address, receiverPacket.port)
                    socket.send(nullPacket)
                    Thread.sleep(10000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Сосить хуй")
            }
        }
    }

    /*val buffer = ByteArray(512)
    val response = DatagramPacket(buffer, buffer.size)
    socket.receive(response)

    val quote = String(buffer, 0, response.length)*/

    /*   println("Цикл")
    val ipAddr = byteArrayOf(192.toByte(), 168.toByte(), 2, 110)
    val request = DatagramPacket(ByteArray(1), 1, InetAddress.getByAddress(ipAddr), 8301)
    socket.send(request)*/

    /*val ipAddr = byteArrayOf(192.toByte(), 168.toByte(), 2, 110)
    val receiverBuffer:ByteArray = kotlin.ByteArray(41)
    val packet = DatagramPacket(receiverBuffer,receiverBuffer.size,InetAddress.getByAddress(ipAddr),8301)
    socket.receive(packet)
    if(packet.data[0].toInt()!=0)
    {
        System.out.println(Arrays.toString(packet.data))
        socket.send(packet)
    }*/

    inner class MainTask : TimerTask(), Runnable {
        override fun run() {
            val socket = DatagramSocket()
            try {

                /*val intent = Intent(StartActivity.BROADCAST_ACTION)
                intent.putExtra("test", "Информация пошла")
                sendBroadcast(intent)
                println("Lat " + currentLocation!!.latitude + " Lon " + currentLocation!!.longitude)*/
            } catch (e: Exception) {}
        }
    }
}
