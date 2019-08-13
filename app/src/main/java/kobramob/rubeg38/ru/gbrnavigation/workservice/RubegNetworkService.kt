package kobramob.rubeg38.ru.gbrnavigation.workservice

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings.Secure
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.gson.Gson
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.mainactivity.MainActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.rubegnetworkprotocol.RubegProtocol
import kobramob.rubeg38.ru.rubegnetworkprotocol.RubegProtocolDelegate
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class RubegNetworkService : Service(), RubegProtocolDelegate {
    override var sessionId: String? = null

    private var connectionLost: Boolean = false

    override fun connectionLost() {
        Log.d("ConnectionLost", "Yes")
            if(!isServiceStarted){
                Log.d("ConnectionLost","StartService")
                startService()
            }
            sessionId = null
            when {
                !isConnected(this) -> {
                    connectInternet = false
                    if(!connectionLost)
                    {
                        EventBus.getDefault().post(
                            MessageEvent(
                                command = "internet",
                                message = "lost"
                            )
                        )
                        connectionLost = true
                    }

                    while (!isConnected(this)) {
                    }
                    EventBus.getDefault().post(
                        MessageEvent
                        (
                            "internet",
                            "connected"
                        )
                    )
                    val authorizationMessage = JSONObject()
                    authorizationMessage.put("\$c$", "reg")
                    authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                    authorizationMessage.put(
                        "password",
                        getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
                    )
                    RubegNetworkService.protocol.request(authorizationMessage.toString()) { success: Boolean, data: ByteArray? ->
                        if (success && data != null) {
                            this.sessionId = JSONObject(String(data)).getString("tid")

                            EventBus.getDefault().post(
                                MessageEvent(
                                    "reconnectInternet",
                                    "true"
                                )
                            )
                            coordinateLoop()
                            connectInternet = true
                            connectionLost = false
                        } else {
                            Log.d("Reconnected","false")
                            /*EventBus.getDefault().post(
                                MessageEvent(
                                    "reconnectInternet",
                                    "false"
                                )
                            )*/
                        }
                    }
                }
                else -> {
                    connectServer = false
                    if(!connectionLost){
                        EventBus.getDefault().post(
                            MessageEvent(
                                command = "disconnect",
                                message = "lost"
                            )
                        )
                        connectionLost = true
                    }
                    val authorizationMessage = JSONObject()
                    authorizationMessage.put("\$c$", "reg")
                    authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                    authorizationMessage.put(
                        "password",
                        getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
                    )

                    RubegNetworkService.protocol.request(authorizationMessage.toString()) { success: Boolean, data: ByteArray? ->
                        if (success && data != null) {
                            this.sessionId = JSONObject(String(data)).getString("tid")

                            EventBus.getDefault().post(
                                MessageEvent(
                                    "reconnectServer",
                                    "true"
                                )
                            )
                            connectServer = true
                            coordinateLoop()
                        } else {
                            Log.d("Reconnected","false")
                            /*EventBus.getDefault().post(
                                MessageEvent(
                                    "reconnectServer",
                                    "false"
                                )
                            )*/
                        }
                    }
                }
            }
    }

    private fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    private fun isConnected(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected
    }

    override fun messageReceived(message: ByteArray) {
        Log.d("ByteMessage", Arrays.toString(message))
        EventBus.getDefault().post(
            MessageEvent(
                command = "getfile",
                message = message
            )
        )
    }

    override fun messageReceived(message: String) {
        Log.d("StringMessage", message)
        when {
            JSONObject(message).has("command") -> {
                when (JSONObject(message).getString("command")) {
                    "regok" -> {
                        this.sessionId = JSONObject(message).getString("tid")

                        val routeServer: ArrayList<String> = ArrayList()
                        for (i in 0 until JSONObject(message).getJSONArray("routeserver").length())
                            routeServer.add(JSONObject(message).getJSONArray("routeserver").getString(i))

                        val call: String = JSONObject(message).getString("call")

                        val status: String = JSONObject(message).getString("status")

                        val gbrStatus: ArrayList<String> = ArrayList()
                        for (i in 0 until JSONObject(message).getJSONArray("gpsstatus").length())
                            gbrStatus.add(JSONObject(message).getJSONArray("gpsstatus").getString(i))

                        if(MainActivity.isAlive || LoginActivity.isAlive){
                            EventBus.getDefault().post(
                                RegistrationEvent(
                                    command = JSONObject(message).getString("command"),
                                    routeServer = routeServer,
                                    call = call,
                                    status = status,
                                    gbrStatus = gbrStatus
                                )
                            )
                        }
                    }
                    "gbrstatus" -> {
                        EventBus.getDefault().postSticky(
                            MessageEvent(
                                command = JSONObject(message).getString("command"),
                                message = JSONObject(message).getString("status")
                            )
                        )
                    }
                    "alarm" -> {

                        if(!CommonActivity.isAlive && !ObjectActivity.isAlive){
                            val ptk = Intent(this, CommonActivity::class.java)
                            ptk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(ptk)
                        }


                        val alarmGson = Gson()
                        val alarm = alarmGson.fromJson(message,AlarmGson::class.java)

                        EventBus.getDefault().postSticky(
                            AlarmEvent(
                                command = alarm.command,
                                name = alarm.name,
                                number = alarm.number,
                                lon = alarm.lon.toDouble(),
                                lat = alarm.lat.toDouble(),
                                inn = alarm.inn.toLong(),
                                zakaz = alarm.zakaz,
                                address = alarm.address,
                                area = alarm.area,
                                otvl = alarm.otvl,
                                plan = alarm.plan,
                                photo = alarm.photo
                            )
                        )

                    }
                    "notalarm" -> {
                        EventBus.getDefault().postSticky(
                            MessageEvent(
                                command = "notalarm",
                                message = "close"
                            )
                        )
                    }
                }
            }
            JSONObject(message).has("\$c$") -> {
                when (JSONObject(message).getString("\$c$")) {
                    "sendfile" -> {
                        val startReceive = JSONObject()
                        startReceive.put("\$c$", "startrecivefile")
                        protocol.send(startReceive.toString()) {
                            if (it) {
                                Log.d("startrecive", "start")
                            }
                        }
                    }
                }
            }
            else -> {
                Log.d("StringMessage", JSONObject(message).getString("\$c$"))
            }
        }
    }

    private fun startForeground() {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            "101"
        }
        val notificationBuilder = NotificationCompat.Builder(this, channelID)
        val notification =
            notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSubText("Кобра ГБР")
                .setContentTitle("Сервис фоновой работы приложения")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(channelID.toInt(), notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelID = "101"
        val channelName = "MyBackgroundService"
        val chan = NotificationChannel(
            channelID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        chan.lightColor = Color.GRAY
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelID
    }

    companion object {
        lateinit var protocol: RubegProtocol
        var connectServer: Boolean = true
        var connectInternet: Boolean = true
        var serviceWork = false
        var isServiceStarted = false
    }



    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, CommonActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
            this,
            notificationChannelId
        ) else NotificationCompat.Builder(this)

/*.setStyle(NotificationCompat.MessagingStyle("You")
                .addMessage("Privet",System.currentTimeMillis(),"Mama")
                .addMessage("Poka",System.currentTimeMillis(),"Otec")
            )*/

        return builder
            .setContentTitle("Service")
            .setContentText("Service notifications")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.drawable.ic_arrivedtoobject,"Прибытие",pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH   ) // for under android 26 compatibility
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null) {
            serviceWork = true
            when (intent.getStringExtra("command")) {
                "start" -> {
                    protocol = RubegProtocol(
                        intent.getStringArrayListExtra("ip")!!,
                        intent.getIntExtra("port", 9010)
                    )

                    startService()
                    protocol.delegate = this
                    protocol.start()
                }
                "stop" -> {
                    stopService()
                    isServiceStarted = false
                    serviceWork = false
                }
            }
        }

        return START_STICKY
    }

    private var wakeLock: PowerManager.WakeLock? = null

    private fun startService() {
        if (isServiceStarted) return
        Log.d("Service", "Starting the foreground service task")
        isServiceStarted = true

        serviceWork = true
        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        protocol.delegate = this
        protocol.start()

        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    pingFakeServer()
                }
                delay(1 * 60 * 1000)
            }
        }
        thread{
            while(!protocol.connected){

            }
            coordinateLoop()
        }
    }

    @SuppressLint("HardwareIds", "SimpleDateFormat")
    private fun pingFakeServer() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmZ")
        val gmtTime = df.format(Date())

        val deviceId = Secure.getString(applicationContext.contentResolver, Secure.ANDROID_ID)

        val json =
            """
                {
                    "deviceId": "$deviceId",
                    "createdAt": "$gmtTime"
                }
            """
        try {
            Fuel.post("https://jsonplaceholder.typicode.com/posts")
                .jsonBody(json)
                .response { _, _, result ->
                    val (bytes, error) = result
                    if (bytes != null) {
                    } else {
                    }
                }
        } catch (e: Exception) {
        }
    }

    private fun stopService() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
            Log.d("Service", "Destroy")
            sessionId = null
            protocol.stop()
        } catch (e: Exception) {
            Log.d("Service", "Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
    }

    private fun coordinateLoop() {
        thread {
            var oldSpeed = 0
            while (protocol.connected) {
                while(sessionId==null){

                }
                var coordinateSend = false
                if (MyLocation.imHere!!.speed > 0 && sessionId != null || MyLocation.imHere!!.speed>=oldSpeed && sessionId!=null) {
                    oldSpeed++
                    val coordinateMessage = JSONObject()
                    coordinateMessage.put("\$c$", "gbrkobra")
                    coordinateMessage.put("command", "location")
                    coordinateMessage.put("id", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                    coordinateMessage.put("lon", MyLocation.imHere?.longitude)
                    coordinateMessage.put("lat", MyLocation.imHere?.latitude)
                    coordinateMessage.put("speed", MyLocation.imHere?.speed)
                    protocol.send(coordinateMessage.toString()) {
                        coordinateSend = if (it) {
                            Log.d("Coordinate", "send")
                            true
                        } else {
                            true
                        }
                    }
                    while(!coordinateSend){

                    }
                }

                sleep(8000)
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}