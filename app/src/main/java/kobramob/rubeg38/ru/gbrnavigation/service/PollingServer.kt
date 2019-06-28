package kobramob.rubeg38.ru.gbrnavigation.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.PRIORITY_MIN
import android.util.Log
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import org.json.JSONObject
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.DatagramChannel
import java.util.*

class PollingServer : Service(), LocationListener {

    val request: Request = Request()
    private val coder: Coder = Coder()

    private val timer: Timer = Timer()
    private val LOG_TAG = "PollingService"

    private val typePacket: Byte = 0

    companion object {
        lateinit var socket: DatagramSocket
        var countReceiver: Long = 0
        var countSender: Long = 1
        var latitude: Double = 0.toDouble()
        var longtitude: Double = 0.toDouble()
        var currentLocation: Location? = null
        var serverAlive: Boolean = true
        var tryCount = 0
        var register: Boolean = false
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

    private fun startForeground() {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification =
            notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSubText("Кобра ГБР")
                .setContentTitle("Поддерживаем соединение с сервером")
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(channelId.toInt(), notification)

        startService()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "101"
        val channelName = "My Background Service"
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onCreate() {
        super.onCreate()
        getLocation()

        Log.d(LOG_TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "OnStartCommand")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            startForeground()
        }
        else
        {
            startService()
        }

        return START_STICKY
    }

    fun initSocket(ip: String?, port: Int) {
        try{
            socket = DatagramSocket(null)
            socket.reuseAddress = true
            socket.bind(InetSocketAddress(socket.localAddress, socket.localPort))
            socket.connect(InetSocketAddress(ip, port))
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun startService() {

        serverReceiver()

        val timerTask: TimerTask = RequestTask()
        timer.schedule(timerTask, 0, 9500)

    }

    inner class RequestTask : TimerTask() {
        override fun run() {
            if (register) {
                if (serverAlive) {
                    var speed: Float = 0.toFloat()
                    var lat: Double = 0.toDouble()
                    var lon: Double = 0.toDouble()

                    try {
                        speed = currentLocation!!.speed
                        lat = currentLocation!!.latitude
                        latitude = currentLocation!!.latitude
                        longtitude = currentLocation!!.longitude
                        lon = currentLocation!!.longitude
                    } catch (e: Exception) {
                    }

                    serverAlive = false
                    if (speed> 0.toFloat()) {
                        request.sendLocation(
                            socket,
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                            getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010),
                            countSender,
                            typePacket,
                            lat,
                            lon,
                            speed,
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", ""),
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", "")
                        )
                    } else {
                        Log.d("254", "sent")
                        request.packet254(
                            socket,
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", ""),
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                            getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010)
                        )
                    }
                } else {
                    countSender = 1
                    countReceiver = 1
                    if (tryCount <2) {
                        request.register(
                            socket, countSender, typePacket,
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", ""),
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                            getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010),
                            ""
                        )
                        tryCount++
                    } else {
                        tryCount = 0

                        try {
                            val intentLoginActivity = Intent(LoginActivity.BROADCAST_ACTION)
                            intentLoginActivity.putExtra("accessDenied", true)
                            sendBroadcast(intentLoginActivity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        try {
                            val intentStartActivity = Intent(LoginActivity.BROADCAST_ACTION)
                            intentStartActivity.putExtra("accessDenied", true)
                            sendBroadcast(intentStartActivity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        try {
                            val intentObjectActivity = Intent(LoginActivity.BROADCAST_ACTION)
                            intentObjectActivity.putExtra("accessDenied", true)
                            sendBroadcast(intentObjectActivity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun serverReceiver() {
        val receiverServer = Runnable {
            while (true) {
                socket.soTimeout = 0
                try {
                    // Создаем буффер
                    val receiverBuffer = ByteArray(1500)
                    // Создаем датаграмму для приема
                    val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
                    // Принимаем то что лежит в сокете
                    // Теперь могу даже перехватить если упал сокет,каеф
                    socket.receive(receiverPacket)

                    // Принимаем пакет
                    Log.d("NumberOfPackages", coder.numberOfPackages(receiverPacket.data).toString())
                    if (coder.numberOfPackages(receiverPacket.data) <= 1) {
                        onePacket(receiverPacket.data)
                    } else {
                        countReceiver++
                        request.packetType255(socket, receiverPacket.data)
                        morePacket(coder.decoder(receiverPacket.data).second, coder.numberOfPackages(receiverPacket.data), coder.decoder(receiverPacket.data).first.messageSize)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }; Thread(receiverServer).start()
    }

    private fun morePacket(data: ByteArray, numberOfPackages: Int, packetSize: Int) {
        try {
            val intentStartActivity = Intent(StartActivity.BROADCAST_ACTION)
            val intentLoginActivity = Intent(LoginActivity.BROADCAST_ACTION)
            val intentObjectActivity = Intent(ObjectActivity.BROADCAST_ACTION)

            var count = 1
            val byteBuffer: ByteBuffer = ByteBuffer.allocate(packetSize)
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
            byteBuffer.put(data)

            Log.d("MessageCount", "NumberOfPackages $numberOfPackages")
            while (count < numberOfPackages) {
                socket.soTimeout = 0
                try {
                    // Создаем буффер
                    val receiverBuffer = ByteArray(1500)
                    // Создаем датаграмму для приема
                    val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)
                    // Принимаем то что лежит в сокете
                    // Теперь могу даже перехватить если упал сокет,каеф
                    socket.receive(receiverPacket)

                    when (coder.typePacket(receiverPacket.data)) {
                        255 -> {
                            serverAlive = true
                        }
                        254 -> {
                            serverAlive = true
                        }
                        0 -> {
                            serverAlive = true
                            request.packetType255(socket, receiverPacket.data)
                            Log.d("CountReceiver", "wait $countReceiver")
                            Log.d("CountReceiver", "came $countReceiver")
                            if (coder.countReceiver(receiverPacket.data) >= countReceiver) {
                                Log.d("TwoPacket", coder.decoderPacketOne(data))
                                if (coder.numberOfPackages(receiverPacket.data)> count) {
                                    byteBuffer.put(coder.decoder(receiverPacket.data).second)
                                    countReceiver++
                                    count++
                                    Log.d("MessageCount", "Count $count.toString()")
                                    if (count == numberOfPackages) {
                                        val dataPacket: ByteArray = byteBuffer.array()
                                        val serverResponse = coder.packetString(dataPacket)
                                        val jsonObject = JSONObject(serverResponse)
                                        try {
                                            when (jsonObject.getString("command")) {
                                                "gbrstatus" -> {
                                                    try {
                                                        intentStartActivity.putExtra("status", jsonObject.getString("status"))
                                                        sendBroadcast(intentStartActivity)
                                                    } catch (e: Exception) {
                                                        SharedPreferencesState.init(this@PollingServer)
                                                        SharedPreferencesState.addPropertyString(
                                                            "status",
                                                            jsonObject.getString("status")
                                                        )
                                                        e.printStackTrace()
                                                    }
                                                    try {
                                                        intentObjectActivity.putExtra("status", jsonObject.getString("status"))
                                                        sendBroadcast(intentObjectActivity)
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        SharedPreferencesState.init(this@PollingServer)
                                                        SharedPreferencesState.addPropertyString(
                                                            "status",
                                                            jsonObject.getString("status")
                                                        )
                                                        e.printStackTrace()
                                                    }
                                                }
                                                "alarm" -> {
                                                    try {
                                                        intentStartActivity.putExtra("alarm", jsonObject.getString("command"))
                                                        intentStartActivity.putExtra("info", serverResponse)
                                                        sendBroadcast(intentStartActivity)
                                                    } catch (e: Exception) { e.printStackTrace() }
                                                }
                                                "regok" -> {
                                                    tryCount = 0
                                                    try {
                                                        intentLoginActivity.putExtra("info", serverResponse)
                                                        sendBroadcast(intentLoginActivity)
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        val jsonObject = JSONObject(serverResponse)
                                                        SharedPreferencesState.init(this@PollingServer)
                                                        SharedPreferencesState.addPropertyString(
                                                            "tid",
                                                            jsonObject.getString("tid")
                                                        )
                                                        if (jsonObject.getString("namegbr") !=
                                                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "")
                                                        ) {
                                                            SharedPreferencesState.addPropertyString(
                                                                "namegbr",
                                                                jsonObject.getString("namegbr")
                                                            )
                                                        }
                                                        if (jsonObject.getString("call") !=
                                                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("call", "")
                                                        ) {
                                                            SharedPreferencesState.addPropertyString(
                                                                "call",
                                                                jsonObject.getString("call")
                                                            )
                                                        }
                                                    }
                                                }
                                                "alarmprok" -> {
                                                    try {
                                                        intentObjectActivity.putExtra("info", jsonObject.getString("command"))
                                                        sendBroadcast(intentObjectActivity)
                                                    } catch (e: Exception) { e.printStackTrace() }
                                                }
                                                "notalarm" -> {

                                                    try {
                                                        SharedPreferencesState.init(this@PollingServer)
                                                        SharedPreferencesState.addPropertyString(
                                                            "status",
                                                            jsonObject.getString("status")
                                                        )
                                                        intentObjectActivity.putExtra("info", jsonObject.getString("command"))
                                                        sendBroadcast(intentObjectActivity)
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        startActivity(Intent(this@PollingServer, StartActivity::class.java))
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            } else {
                                request.packetType255(socket, data)
                                Log.d("FailTwoPacket", coder.decoderPacketOne(data))
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onePacket(data: ByteArray) {
        val intentStartActivity = Intent(StartActivity.BROADCAST_ACTION)
        val intentLoginActivity = Intent(LoginActivity.BROADCAST_ACTION)
        val intentObjectActivity = Intent(ObjectActivity.BROADCAST_ACTION)
        Log.d("OnePacket", coder.typePacket(data).toString())
        when (coder.typePacket(data)) {
            254 -> {
                // ServerAlive
                serverAlive = true
            }
            255 -> {
                // Подтверждение
                serverAlive = true
            }
            0 -> {
                Log.d("CountReceiver", "wait $countReceiver")
                Log.d("CountReceiver", "came $countReceiver")
                if (coder.countReceiver(data) >= countReceiver) {
                    Log.d("OnePacket", coder.decoderPacketOne(data))
                    serverAlive = true
                    // изменить размер пакета

                    request.packetType255(socket, data)

                    val serverResponse = coder.decoderPacketOne(data)
                    val jsonObject = JSONObject(serverResponse)

                    try {
                        when (jsonObject.getString("command")) {
                            "gbrstatus" -> {
                                try {
                                    intentStartActivity.putExtra("status", jsonObject.getString("status"))
                                    sendBroadcast(intentStartActivity)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    SharedPreferencesState.init(this@PollingServer)
                                    SharedPreferencesState.addPropertyString(
                                        "status",
                                        jsonObject.getString("status")
                                    )
                                }
                                try {
                                    intentObjectActivity.putExtra("status", jsonObject.getString("status"))
                                    sendBroadcast(intentObjectActivity)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    SharedPreferencesState.init(this@PollingServer)
                                    SharedPreferencesState.addPropertyString(
                                        "status",
                                        jsonObject.getString("status")
                                    )
                                }
                            }
                            "alarm" -> {
                                try {
                                    intentStartActivity.putExtra("alarm", jsonObject.getString("command"))
                                    intentStartActivity.putExtra("info", serverResponse)
                                    sendBroadcast(intentStartActivity)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                            "regok" -> {
                                tryCount = 0
                                try {
                                    intentLoginActivity.putExtra("info", serverResponse)
                                    sendBroadcast(intentLoginActivity)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    val jsonObject = JSONObject(serverResponse)
                                    SharedPreferencesState.init(this@PollingServer)
                                    SharedPreferencesState.addPropertyString(
                                        "tid",
                                        jsonObject.getString("tid")
                                    )
                                    if (jsonObject.getString("namegbr") !=
                                        getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "")
                                    ) {
                                        SharedPreferencesState.addPropertyString(
                                            "namegbr",
                                            jsonObject.getString("namegbr")
                                        )
                                    }
                                    if (jsonObject.getString("call") !=
                                        getSharedPreferences("state", Context.MODE_PRIVATE).getString("call", "")
                                    ) {
                                        SharedPreferencesState.addPropertyString(
                                            "call",
                                            jsonObject.getString("call")
                                        )
                                    }
                                }
                            }
                            "alarmprok" -> {
                                try {
                                    intentObjectActivity.putExtra("info", jsonObject.getString("command"))
                                    sendBroadcast(intentObjectActivity)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                            "notalarm" -> {

                                try {
                                    SharedPreferencesState.init(this@PollingServer)
                                    SharedPreferencesState.addPropertyString(
                                        "status",
                                        jsonObject.getString("status")
                                    )
                                    intentObjectActivity.putExtra("info", jsonObject.getString("command"))
                                    sendBroadcast(intentObjectActivity)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    startActivity(Intent(this@PollingServer, StartActivity::class.java))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    countReceiver++
                } else {
                    request.packetType255(socket, data)
                    Log.d("FailPacket", coder.decoderPacketOne(data))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        stopForeground(true)
        Log.d(LOG_TAG, "OnDestroy")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d(LOG_TAG, "LowMemory")
    }
}