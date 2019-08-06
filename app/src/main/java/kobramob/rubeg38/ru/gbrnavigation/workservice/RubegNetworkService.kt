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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.rubegnetworkprotocol.RubegProtocol
import kobramob.rubeg38.ru.rubegnetworkprotocol.RubegProtocolDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class RubegNetworkService : Service(), RubegProtocolDelegate {
    override var sessionId: String? = null

    private var connectionLost:Boolean = false


    override fun connectionLost() {
        Log.d("ConnectionLost", "connectionLost")
        if(!connectionLost)
        {
            connectionLost = true
            sessionId = null
            protocol.start()
         when{
             !isConnected(this)->{
                 connectInternet = false
                 EventBus.getDefault().post(MessageEvent(
                     command = "internet",
                     message = "lost"
                 ))
                 while(!isConnected(this))
                 {

                 }
                 EventBus.getDefault().post(MessageEvent
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
                 RubegNetworkService.protocol.request(authorizationMessage.toString()) { success: Boolean,data:ByteArray? ->
                     if (success && data!=null) {
                         this.sessionId = JSONObject(String(data)).getString("tid")

                         EventBus.getDefault().post(MessageEvent(
                             "reconnectInternet",
                             "true"
                         ))
                         connectInternet = true
                         connectionLost = false
                     }
                     else
                     {
                         EventBus.getDefault().post(MessageEvent(
                             "reconnectInternet",
                             "false"
                         ))
                         connectionLost = false
                     }
                 }
             }
             else->{
                 connectServer = false
                 EventBus.getDefault().post(MessageEvent(
                     command = "disconnect",
                     message = "lost"
                 ))
                 val authorizationMessage = JSONObject()
                 authorizationMessage.put("\$c$", "reg")
                 authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                 authorizationMessage.put(
                     "password",
                     getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
                 )

                 RubegNetworkService.protocol.request(authorizationMessage.toString()) { success: Boolean,data:ByteArray? ->
                     if (success && data!=null) {
                         this.sessionId = JSONObject(String(data)).getString("tid")

                         EventBus.getDefault().post(MessageEvent(
                             "reconnectServer",
                             "true"
                         ))
                         connectServer = true
                         connectionLost = false
                     }
                     else
                     {
                         EventBus.getDefault().post(MessageEvent(
                             "reconnectServer",
                             "false"
                         ))
                         connectionLost = false
                     }
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

                            EventBus.getDefault().post(
                                MessageEvent(
                                    command = JSONObject(message).getString("command"),
                                    routeServer = routeServer,
                                    call = call,
                                    status = status,
                                    gbrStatus = gbrStatus
                                )
                            )

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

                        val alarmObject = JSONObject(message)
                        var command = ""
                        var name = ""
                        var number = ""
                        var lon = 0.0
                        var lat = 0.0
                        var inn = 0
                        var zakaz = ""
                        var address = ""
                        var areaName = ""
                        var areaAlarmTime = ""
                        try {
                            command = alarmObject.getString("command")
                        } catch (e: Exception) {
                        }

                        try {
                            name = alarmObject.getString("name")
                        } catch (e: java.lang.Exception) {}

                        try {
                            number = alarmObject.getString("number")
                        } catch (e: java.lang.Exception) {}

                        try {
                            lon = alarmObject.getDouble("lon")
                        } catch (e: java.lang.Exception) {}

                        try {
                            lat = alarmObject.getDouble("lat")
                        } catch (e: java.lang.Exception) {}

                        try {
                            inn = alarmObject.getInt("inn")
                        } catch (e: java.lang.Exception) {}

                        try {
                            zakaz = alarmObject.getString("zakaz")
                        } catch (e: java.lang.Exception) {}

                        try {
                            address = alarmObject.getString("address")
                        } catch (e: java.lang.Exception) {}

                        try {
                            areaName = alarmObject.getJSONObject("area").getString("name")
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }

                        try {
                            areaAlarmTime = alarmObject.getJSONObject("area").getString("alarmtime")
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }

                        val otvl: ArrayList<String> = ArrayList()
                        for (i in 0 until JSONArray(alarmObject.getString("otvl")).length()) {
                            otvl.add(alarmObject.getJSONArray("otvl").getString(i))
                        }

                        val plan: ArrayList<String> = ArrayList()
                        for (i in 0 until JSONArray(alarmObject.getString("plan")).length()) {
                            plan.add(alarmObject.getJSONArray("plan").getString(i))
                        }

                        for(i in 0 until JSONArray(alarmObject.getString("photo")).length()){
                            plan.add(alarmObject.getJSONArray("plan").getString(i))
                        }

                        EventBus.getDefault().postSticky(
                            MessageEvent(
                                command = command,
                                name = name,
                                number = number,
                                lon = lon,
                                lat = lat,
                                inn = inn,
                                zakaz = zakaz,
                                address = address,
                                areaName = areaName,
                                areaAlarmTime = areaAlarmTime,
                                otvl = otvl,
                                plan = plan
                            )
                        )
                    }
                    "notalarm"->{
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
        var connectServer:Boolean = true
        var connectInternet:Boolean = true
    }

    private var serviceWork = false


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

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("Service")
            .setContentText("Service notifications")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        if (intent != null) {
            serviceWork = true
            protocol = RubegProtocol(
                intent.getStringArrayListExtra("ip")!!,
                intent.getIntExtra("port", 9010)
            )
            when (intent.getStringExtra("command")) {
                "start" -> startService()
                "stop" -> stopService()
            }
        } else {

        }


        return START_STICKY
    }


    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private fun startService() {
        if (isServiceStarted) return
        Log.d("Service","Starting the foreground service task")
        isServiceStarted = true

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        protocol.delegate = this
        protocol.start()
        coordinateLoop()

        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    pingFakeServer()
                }
                delay(1 * 60 * 1000)
            }
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
            serviceWork = false
            protocol.stop()
        } catch (e: Exception) {
            Log.d("Service","Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
    }

    private fun coordinateLoop(){
        thread{

            while(serviceWork){
                Log.d("Connection", protocol.connected.toString())
                if(MyLocation.imHere!!.speed>=0 && sessionId!=null){
                    val coordinateMessage = JSONObject()
                    coordinateMessage.put("\$c$", "gbrkobra")
                    coordinateMessage.put("command", "location")
                    coordinateMessage.put("id", getSharedPreferences("gbrStorage",Context.MODE_PRIVATE).getString("imei",""))
                    coordinateMessage.put("lon", MyLocation.imHere?.longitude)
                    coordinateMessage.put("lat", MyLocation.imHere?.latitude)
                    coordinateMessage.put("speed", MyLocation.imHere?.speed)
                    protocol.send(coordinateMessage.toString()){
                        if(it)
                        {
                            Log.d("Coordinate","send")
                        }
                        else{
                            protocol.stop()
                            connectionLost()
                        }
                    }
                }
                sleep(9000)
            }
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}