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
import android.support.v4.app.NotificationCompat
import android.util.Log
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import org.json.JSONObject
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.channels.DatagramChannel
import java.util.*

class PollingServer : Service(), LocationListener {

    val response: SingleResponse = SingleResponse()
    val request: Request = Request()
    private val coder: Coder = Coder()

    private val timer: Timer = Timer()
    private val LOG_TAG = "PollingService"
    private var currentLocation: Location? = null



    private val typePacket: Byte = 0
    private val typePacket255: Byte = 255.toByte()

    companion object {
        private val datagramChannel: DatagramChannel = DatagramChannel.open()
        val socket: DatagramSocket = datagramChannel.socket()
        var countReceiver: Long = 2
        var countSender: Long = 1
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
        timer.scheduleAtFixedRate(requestTask(), 0, 10000)
        serverReceiver()
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

/*    fun clientRegister(imei: String, ip: String, port: Int, tid: String): String {
        countSender++
        return request.register(socket, countSender, typePacket, imei, ip, port, tid)
    }*/

    inner class requestTask : TimerTask(), Runnable {
        override fun run() {
            // runAsForeground()
            request.nullPacket(
                socket,
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010)
            )
        }
    }

    private val NOTIFICATION_ID = 42
    private fun runAsForeground() {
        // Intent notificationIntent = new Intent(this, RecorderMainActivity.class);
        // PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,
        // notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
        val notification =
            NotificationCompat.Builder(this)
                // /.setSmallIcon(R.drawable.ic_launcher)
                .setContentText("Я вас вижу")
                // .setContentIntent(pendingIntent)
                .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun serverReceiver() {
        val receiverServer = Runnable {
            socket.soTimeout = 0
            while (true) {
                val intent = Intent(StartActivity.BROADCAST_ACTION)
                // Создаем буффер
                val receiverBuffer = ByteArray(1057)
                // Создаем датаграмму для приема
                val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
                // Принимаем то что лежит в сокете
                socket.receive(receiverPacket)
                // Проверяем тип пакета
                when (coder.typePacket(receiverPacket.data)) {
                    255 -> {
                        println("Пакет принят")
                    }
                    0 -> {
                        if (coder.countReceiver(receiverPacket.data) >= countReceiver) {
                            request.packetType255(socket, receiverPacket.data, receiverPacket.port, receiverPacket.address)
                            val serverResponse = coder.decoderPacketOne(receiverPacket.data)
                            val jsonObject = JSONObject(serverResponse)
                            val jsonArray = jsonObject.getJSONArray("d")
                            when (JSONObject(jsonArray.getString(0)).getString("command")) {
                                "gbrstatus" -> {

                                    intent.putExtra("status", JSONObject(jsonArray.getString(0)).getString("status"))
                                    sendBroadcast(intent)
                                }
                                "alarm"->{
                                    intent.putExtra("status", JSONObject(jsonArray.getString(0)).getString("command"))
                                    intent.putExtra("info",serverResponse)
                                    sendBroadcast(intent)
                                }
                            }

                            println(coder.decoderPacketOne(receiverPacket.data))
                            countReceiver++
                        } else {
                            println("Пустой пакет")
                        }
                    }
                }
            }
        }; Thread(receiverServer).start()
    }
}
/*    try {
        val intent = Intent(StartActivity.BROADCAST_ACTION)
        val tid = sharedPreferences.getString("tid","")
        val speed: Float =intent.getFloatExtra("speed",0f)
        println("Lat " + currentLocation!!.latitude + " Lon " + currentLocation!!.longitude)
        request.sendLocation(datagramChannel,count,typePacket,currentLocation!!.latitude,currentLocation!!.longitude,speed)
    }catch (e:Exception){

        request.nullPacket(datagramChannel)
        val ipAddress = "192.168.2.110"
        val receiverBuffer: ByteArray = kotlin.ByteArray(1041)
        val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
        datagramChannel.receive(receiverPacket)
        // println(Arrays.toString(receiverPacket.data))
        if (receiverPacket.data[0].toInt() != 0) {
            request.packetType255(datagramChannel, receiverPacket.data)
            println(coder.decoderPacketOne(receiverPacket.data))
        }

    }*/

/*intent.putExtra("test", "Информация пошла")
sendBroadcast(intent)*/