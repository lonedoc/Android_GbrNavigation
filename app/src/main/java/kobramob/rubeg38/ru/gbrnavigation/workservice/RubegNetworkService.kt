package kobramob.rubeg38.ru.gbrnavigation.workservice

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings.Secure
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.firebase.messaging.RemoteMessage
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
        if (!isServiceStarted) {
            Log.d("ConnectionLost", "StartService")
            startService()
        }
        sessionId = null
        when {
            !isConnected(this) -> {
                connectInternet = false
                if (!connectionLost) {
                    val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                        .addData("command", "disconnectInternet")
                        .build()
                    createNotification(remoteMessage)
                    sleep(1000)
                    val remoteMessage1 = RemoteMessage.Builder("Status")
                        .addData("command", "disconnectServer")
                        .build()
                    createNotification(remoteMessage1)
                    connectionLost = true
                }

                while (!isConnected(this)) {
                }

                val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                    .addData("command", "reconnectInternet")
                    .build()
                createNotification(remoteMessage)

                sleep(1000)
                val authorizationMessage = JSONObject()
                authorizationMessage.put("\$c$", "reg")
                authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                authorizationMessage.put(
                    "password",
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
                )
                authorizationMessage.put(
                    "token",
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("fcmtoken", "")
                )
                RubegNetworkService.protocol.request(authorizationMessage.toString()) { success: Boolean, data: ByteArray? ->
                    if (success && data != null) {
                        val regGson = Gson()
                        val registration = regGson.fromJson(String(data), RegistrationGson::class.java)
                        this.sessionId = registration.tid

                        sleep(1000)
                        val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                            .addData("command", "reconnectServer")
                            .build()
                        createNotification(remoteMessage)

                        coordinateLoop()
                        connectInternet = true
                        connectionLost = false
                    } else {
                        Log.d("InternetReconnected", "false")
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
                if (!connectionLost) {
                    val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                        .addData("command", "disconnectServer")
                        .build()
                    createNotification(remoteMessage)
                    /*EventBus.getDefault().post(
                        MessageEvent(
                            command = "disconnect",
                            message = "lost"
                        )
                    )*/
                    connectionLost = true
                }
                val authorizationMessage = JSONObject()
                authorizationMessage.put("\$c$", "reg")
                authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                authorizationMessage.put(
                    "password",
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
                )
                authorizationMessage.put(
                    "token",
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("fcmtoken", "")
                )

                RubegNetworkService.protocol.request(authorizationMessage.toString()) { success: Boolean, data: ByteArray? ->
                    if (success && data != null) {
                        val regGson = Gson()
                        val registration = regGson.fromJson(String(data), RegistrationGson::class.java)
                        this.sessionId = registration.tid

                        val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                            .addData("command", "reconnectServer")
                            .build()
                        createNotification(remoteMessage)

                        /*EventBus.getDefault().post(
                            MessageEvent(
                                "reconnectServer",
                                "true"
                            )
                        )*/
                        connectServer = true
                        coordinateLoop()
                    } else {
                        Log.d("ServerReconnected", "false")
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
            ImageEvent(
                command = "getfile",
                byteArray = message
            )
        )
    }

    override fun messageReceived(message: String) {
        Log.d("StringMessage", message)
        try{
        when {
            JSONObject(message).has("command") -> {
                when (JSONObject(message).getString("command")) {
                    "regok" -> {

                        val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                            .addData("command", "connectServer")
                            .build()
                        createNotification(remoteMessage)

                        val regGson = Gson()
                        val registration = regGson.fromJson(message, RegistrationGson::class.java)

                        this.sessionId = registration.tid
                        DataStore.reports = registration.reports
                        DataStore.namegbr = registration.namegbr
                        if (MainActivity.isAlive || LoginActivity.isAlive) {
                            EventBus.getDefault().post(
                                RegistrationEvent(
                                    command = registration.command,
                                    routeServer = registration.routeserver,
                                    call = registration.call,
                                    status = registration.status,
                                    gbrStatus = registration.gpsstatus
                                )
                            )
                        }
                    }
                    "gbrstatus" -> {

                        val statusGson = Gson()
                        val status = statusGson.fromJson(message, StatusGson::class.java)

                        val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                            .addData("command", status.command)
                            .addData("status", status.status)
                            .build()
                        createNotification(remoteMessage)

                        EventBus.getDefault().postSticky(
                            MessageEvent(
                                command = status.command,
                                message = status.status
                            )
                        )
                    }
                    "alarm" -> {

                        val alarmGson = Gson()
                        val alarm = alarmGson.fromJson(message, AlarmGson::class.java)

                        if (!CommonActivity.isAlive && !ObjectActivity.isAlive) {
                            val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                                .addData("command", alarm.command)
                                .addData("name", alarm.name)
                                .build()
                            createNotification(remoteMessage)

                            val ptk = Intent(this, CommonActivity::class.java)
                            ptk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(ptk)
                        }

                        if(alarm.inn=="")
                            alarm.inn = "0"

                        if(alarm.lon == "")
                            alarm.lon = "0.0"

                        if(alarm.lat  == "")
                            alarm.lat = "0.0"

                        if(alarm.zakaz == null){
                            alarm.zakaz = " "
                        }
                        EventBus.getDefault().postSticky(
                            AlarmEvent(
                                command = alarm.command,
                                name = alarm.name,
                                number = alarm.number,
                                lon = alarm.lon.toDouble(),
                                lat = alarm.lat.toDouble(),
                                inn = alarm.inn.toLong(),
                                zakaz = alarm.zakaz!!,
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
                Log.d("StringMessage", "UnknownMessage")
            }
        }

        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
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

    private fun createNotification(remoteMessage: RemoteMessage) {
        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
            this,
            channelID()
        ) else NotificationCompat.Builder(this)

        val pendingIntent = when {
            LoginActivity.isAlive -> {
                val i = Intent(this, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            CommonActivity.isAlive -> {
                val i = Intent(this, CommonActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            ObjectActivity.isAlive || ObjectActivity.saveAlarm != null -> {
                val i = Intent(this, ObjectActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                i.putExtra("objectInfo", ObjectActivity.saveAlarm)
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            else -> {
                null
            }
        }
        when (remoteMessage.data["command"]) {
            "gbrstatus" -> {
                val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setContentTitle("Смена статуса")
                    .setContentIntent(pendingIntent)
                    .setContentText("Смена статуса на: ${remoteMessage.data["status"]}")
                    .setSound(statusSound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_unknown_status).color = ContextCompat.getColor(this, R.color.colorPrimary)
                val notification = builder.build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(6, notification)
            }
            "alarm"->{
                val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ applicationContext.packageName + "/" + R.raw.alarm_sound)
                builder.setContentTitle("Тревога")
                    .setContentIntent(pendingIntent)
                    .setContentText("Тревога на : ${remoteMessage.data["name"]}")
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setSmallIcon(R.drawable.ic_alarm).color = ContextCompat.getColor(this, R.color.colorPrimary)
                val notification = builder.build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(7, notification)
            }
            "disconnectServer" -> {
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setContentTitle("Соединение с сервером")
                    .setContentText("Соединение с сервером потеряно")
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .setSmallIcon(R.drawable.ic_disconnect).color = ContextCompat.getColor(this, R.color.colorPrimary)
                builder.build().flags = Notification.FLAG_AUTO_CANCEL
                val notification = builder.build()

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2, notification)
            }
            "reconnectServer" -> {
                val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setContentTitle("Соединение с сервером")
                    .setContentIntent(pendingIntent)
                    .setContentText("Соединение с сервером восстановлено")
                    .setSound(statusSound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_connect).color = ContextCompat.getColor(this, R.color.colorPrimary)
                val notification = builder.build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2, notification)
            }
            "connectServer" -> {
                val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setContentTitle("Соединение с сервером")
                    .setContentIntent(pendingIntent)
                    .setContentText("Соединение с сервером установлено")
                    .setSound(statusSound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_connect).color = ContextCompat.getColor(this, R.color.colorPrimary)
                val notification = builder.build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2, notification)
            }
            "disconnectInternet" -> {
                Log.d("disconnectInternet", "Yes")
                val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setContentTitle("Интернет")
                    .setContentIntent(pendingIntent)
                    .setContentText("Проблемы с сетью интернет")
                    .setSound(statusSound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_disconnect).color = ContextCompat.getColor(this, R.color.colorPrimary)
                val notification = builder.build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(3, notification)
            }
            "reconnectInternet" -> {
                val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setContentTitle("Интернет")
                    .setContentIntent(pendingIntent)
                    .setContentText("Работа сети интернет восстановлена")
                    .setSound(statusSound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_connect).color = ContextCompat.getColor(this, R.color.colorPrimary)
                val notification = builder.build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(3, notification)
            }
        }
    }
    private fun channelID(): String {
        val notificationChannelId = "Notifications channel"

        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ applicationContext.packageName + "/" + R.raw.alarm_sound)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Notifications channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it.setSound(soundUri,audioAttributes)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
        return notificationChannelId
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
            .setSmallIcon(R.drawable.ic_service)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
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
        thread {
            while (!protocol.connected) {
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
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun coordinateLoop() {
        thread {
            var oldSpeed = 0
            while (protocol.connected) {
                while (sessionId == null) {
                }
                var coordinateSend = false
                if (MyLocation.imHere!!.speed > 0 && sessionId != null || MyLocation.imHere!!.speed >= oldSpeed && sessionId != null) {
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
                    while (!coordinateSend) {
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