package kobramob.rubeg38.ru.gbrnavigation.workservice

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings.Secure
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.mainactivity.MainActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.resource.SPGbrNavigation
import kobramob.rubeg38.ru.gbrnavigation.workservice.NotificationService.createNotification
import kobramob.rubeg38.ru.networkprotocol.RubegProtocol
import kobramob.rubeg38.ru.networkprotocol.RubegProtocolDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class RubegNetworkService : Service(), RubegProtocolDelegate {
    override var sessionId: String? = null

    private var connectionLost: Boolean = false

    companion object {
        lateinit var protocol: RubegProtocol
        var connectServer: Boolean = true
        var connectInternet: Boolean = true
        var serviceWork = false
        var isServiceStarted = false
    }

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
                    createNotification(remoteMessage,this)
                    sleep(1000)
                    val remoteMessage1 = RemoteMessage.Builder("Status")
                        .addData("command", "disconnectServer")
                        .build()
                    createNotification(remoteMessage1,this)
                    connectionLost = true
                }

                while (!isConnected(this)) {
                    //wait connect internet
                }

                val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                    .addData("command", "reconnectInternet")
                    .build()
                createNotification(remoteMessage,this)

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
                protocol.request(authorizationMessage.toString()) { success: Boolean, data: ByteArray? ->
                    if (success && data != null) {
                        val regGson = Gson()
                        val registration = regGson.fromJson(String(data), RegistrationGson::class.java)
                        this.sessionId = registration.tid

                        sleep(1000)
                        val authorization: RemoteMessage = RemoteMessage.Builder("Status")
                            .addData("command", "reconnectServer")
                            .build()
                        createNotification(authorization,this)

                        coordinateLoop()
                        connectInternet = true
                        connectionLost = false
                    } else {
                        Log.d("InternetReconnected", "false")

                    }
                }
            }
            else -> {
                connectServer = false
                if (!connectionLost) {
                    val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                        .addData("command", "disconnectServer")
                        .build()
                    createNotification(remoteMessage,this)
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

                protocol.request(authorizationMessage.toString()) { success: Boolean, data: ByteArray? ->
                    if (success && data != null) {
                        val regGson = Gson()
                        val registration = regGson.fromJson(String(data), RegistrationGson::class.java)
                        this.sessionId = registration.tid

                        val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                            .addData("command", "reconnectServer")
                            .build()
                        createNotification(remoteMessage,this)
                        connectServer = true
                        coordinateLoop()
                    } else {
                        Log.d("ServerReconnected", "false")
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
        Log.d("String", "Message: $message")
        try{
            val gson = Gson()
        when {
            JSONObject(message).has("command") -> {
                when (JSONObject(message).getString("command")) {
                    "regok" -> {

                        val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                            .addData("command", "connectServer")
                            .build()
                        createNotification(remoteMessage,this)

                        val registration = gson.fromJson(message, RegistrationGson::class.java)

                        this.sessionId = registration.tid



                        DataStore.initRegistrationData(
                            namegbr = registration.namegbr,
                            call = registration.call,
                            status = registration.status,
                            statusList = registration.gpsstatus,
                            routeServer = registration.routeserver,
                            reports = registration.reports,
                            cityCard = registration.citycard
                        )

                        if (MainActivity.isAlive || LoginActivity.isAlive) {
                            EventBus.getDefault().post(
                                RegistrationEvent(
                                    command = registration.command
                                )
                            )
                        }

                    }
                    "gbrstatus" -> {

                        val status = gson.fromJson(message, StatusGson::class.java)

                        if(status.status != "")
                        {
                            if(CommonActivity.isAlive && status.status!="На тревоге"){
                                val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                                    .addData("command", status.command)
                                    .addData("status", status.status)
                                    .build()
                                createNotification(remoteMessage,this)
                            }
                            EventBus.getDefault().postSticky(
                                MessageEvent(
                                    command = status.command,
                                    message = status.status
                                )
                            )
                        }

                    }
                    "alarm" -> {

                        val alarm = gson.fromJson(message, AlarmGson::class.java)

                        if (!CommonActivity.isAlive && !ObjectActivity.isAlive) {
                            val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                                .addData("command", alarm.command)
                                .addData("name", alarm.name)
                                .build()
                            createNotification(remoteMessage,this)

                            val ptk = Intent(this, CommonActivity::class.java)
                            ptk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(ptk)
                        }

                        SPGbrNavigation.init(this)
                        SPGbrNavigation.addPropertyString("alarm",message)

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
                    "accessdenied"->{
                        if (MainActivity.isAlive || LoginActivity.isAlive) {
                            EventBus.getDefault().post(
                                RegistrationEvent(
                                    command = "accessdenied"
                                )
                            )
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

    override fun onCreate() {
        super.onCreate()
        val notification = createNotification(context = this)
        startForeground(1, notification)
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
                    Log.d("Service","process")
                    pingFakeServer()
                }
                delay(1 * 60 * 1000)
            }
        }
        thread {
            while (!protocol.isConnected) {
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
                        //faik
                    } else {
                        //faik
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
            while (protocol.isConnected) {
                try {
                    if (MyLocation.imHere!!.speed > 0 && sessionId != null && MyLocation.Enable || MyLocation.imHere!!.speed >= oldSpeed && sessionId != null && MyLocation.Enable ) {
                        oldSpeed++

                        val coordinateMessage = JSONObject()
                        coordinateMessage.put("\$c$", "gbrkobra")
                        coordinateMessage.put("command", "location")
                        coordinateMessage.put("id", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                        coordinateMessage.put("lon", MyLocation.imHere?.longitude)
                        coordinateMessage.put("lat", MyLocation.imHere?.latitude)
                        coordinateMessage.put("speed", (MyLocation.imHere?.speed)!!.toInt())

                        protocol.send(coordinateMessage.toString()) {
                            if(it) {
                                Log.d("Coordinate", "Server receiver")
                            }
                            else
                            {
                                Log.d("Coordinate", "Server not receiver")
                            }
                        }
                    }
                }catch (e:Exception)
                {
                    e.printStackTrace()
                }
                connectServer = true
                connectInternet = true



                sleep(2000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Service","destroy")
        isServiceStarted = false
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}