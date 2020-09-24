package newVersion.servicess

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import newVersion.utils.NewCredentials
import newVersion.login.OldLoginActivity
import newVersion.main.OldMainActivity
import newVersion.models.Auth
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.network.auth.AuthAPI
import newVersion.network.auth.OnAuthListener
import newVersion.network.auth.RPAuthAPI
import newVersion.servicess.NotificationService.createNotification
import newVersion.utils.LocationInfo
import newVersion.utils.Location
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.util.GeoPoint
import ru.rubeg38.rubegprotocol.ConnectionWatcher
import rubegprotocol.RubegProtocol
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class NetworkService : Service(), ConnectionWatcher, OnAuthListener {

    private lateinit var unsubscribe: () -> Unit
    companion object {
        var isServiceStarted = false
    }
    private var connectionLost = false
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var protocol: RubegProtocol
    private lateinit var authAPI: AuthAPI

    private var coordinateQueue:ArrayList<LocationInfo> = ArrayList()
    var oldLocation:GeoPoint? = null
    var oldSpeed:Int? = null
    lateinit var hostPool: HostPool
    var credentials:Credentials? = null

    private fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    private fun isConnected(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected
    }

    override fun onConnectionLost() {
        Log.d("Service", "Connection lost")

        if (!connectionLost && protocol.isStarted && !OldLoginActivity.isAlive && !OldMainActivity.isAlive) {
            connectionLost = true
            thread{
            when {
                isConnected(applicationContext) -> {

                        val remoteMessage1 = RemoteMessage.Builder("Status")
                            .addData("command", "disconnectServer")
                            .build()
                        createNotification(remoteMessage1, applicationContext)

                }
                else -> {
                        val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                            .addData("command", "disconnectInternet")
                            .build()
                        createNotification(remoteMessage, applicationContext)

                        sleep(1_000)

                        val remoteMessage1 = RemoteMessage.Builder("Status")
                            .addData("command", "disconnectServer")
                            .build()
                        createNotification(remoteMessage1, applicationContext)
                }
            }
            }
        }

        thread{
            while (!isConnected(applicationContext)) {
                sleep(1000)
            }

            authAPI.sendAuthRequest {
            }
        }
    }

    override fun onConnectionEstablished() {
        Log.d("Service", "Connection establish")

        if (connectionLost) {
            val authorization: RemoteMessage = RemoteMessage.Builder("Status")
                .addData("command", "reconnectServer")
                .build()
            createNotification(authorization, applicationContext)
        }
        connectionLost = false
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onAuthDataReceived(auth: Auth) {

        println("Service Auth")
        if (auth.authorized) {
            protocol.token = auth.authInfo?.token
        } else {
          /*  val protocol = RubegProtocol.sharedInstance
            if (protocol.isStarted)
                protocol.stop()*/
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCredentialsChanged(event: NewCredentials) {

        //TODO NewCredential удалить

        Log.d("Service", "credentials received ${event.credentials}")

        authAPI.onDestroy()
        authAPI = RPAuthAPI(protocol, event.credentials)

        authAPI.onAuthListener = this
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onLocationLoop(event:Location){

        if(GeoPoint(event.lat,event.lon) == oldLocation) return

        if(oldSpeed == 0 && (event.speed * 3.6).toInt() == 0) return

        oldLocation = GeoPoint(event.lat,event.lon)
        oldSpeed = (event.speed * 3.6).toInt()

        val df = DecimalFormat("#.######")

        if(credentials == null) return

        if(!protocol.isConnected || connectionLost )
        {
            coordinateQueue.add(LocationInfo(event.lat,event.lon,event.accuracy,(event.speed * 3.6).toInt(),event.satelliteCount))
            return
        }

        while(coordinateQueue.isNotEmpty()){
            val jsonMessage = JsonObject()
            jsonMessage.addProperty("\$c$", "gbrkobra")
            jsonMessage.addProperty("command", "location")
            jsonMessage.addProperty("id",credentials?.imei )
            jsonMessage.addProperty("lon", df.format(coordinateQueue[0].lon))
            jsonMessage.addProperty("lat", df.format(coordinateQueue[0].lat))
            jsonMessage.addProperty("speed", coordinateQueue[0].speed)
            jsonMessage.addProperty("accuracy",coordinateQueue[0].accuracy)
            jsonMessage.addProperty("gpsCount",coordinateQueue[0].satelliteCount)
            val request = jsonMessage.toString()

            protocol.send(request){}

            coordinateQueue.removeAt(0)
        }

        val jsonMessage = JsonObject()
        jsonMessage.addProperty("\$c$", "gbrkobra")
        jsonMessage.addProperty("command", "location")
        jsonMessage.addProperty("id",credentials?.imei )
        jsonMessage.addProperty("lon", df.format(event.lon))
        jsonMessage.addProperty("lat", df.format(event.lat))
        jsonMessage.addProperty("speed", (event.speed * 3.6).toInt())
        jsonMessage.addProperty("accuracy",event.accuracy)
        jsonMessage.addProperty("gpsCount",event.satelliteCount)
        val request = jsonMessage.toString()

        protocol.send(request){}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.getStringExtra("command")) {
            "start" -> {

                val notification = createNotification(context = applicationContext)
                startForeground(1, notification)

                val credentials = intent.getSerializableExtra("credentials") as Credentials
                hostPool = intent?.getSerializableExtra("hostPool") as HostPool

                startService(credentials, hostPool)
            }
            "stop" -> {
                stopService()
            }
        }
        return START_NOT_STICKY
    }

    private fun startService(credentials: Credentials, hostPool: HostPool) {
        if (isServiceStarted) return

        EventBus.getDefault().register(this)
        isServiceStarted = true


        protocol = RubegProtocol.sharedInstance
        unsubscribe = protocol.subscribe(this as ConnectionWatcher)

        authAPI = RPAuthAPI(protocol, credentials)

        authAPI.onAuthListener = this

        this.credentials = credentials

        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService:lock").apply {
                acquire()
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    Log.d("Service", "process")
                    pingFakeServer()
                }
                delay(1 * 60 * 1000)
            }
        }
    }

    private fun stopService() {
        try {

            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }

            protocol.stop()

            authAPI.onDestroy()

            unsubscribe()

            EventBus.getDefault().unregister(this)
            stopForeground(true)
            stopSelf()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.d("Service", "Service stopped without being starter: ${e.message}")
        }
        isServiceStarted = false
    }

    @SuppressLint("HardwareIds", "SimpleDateFormat")
    private fun pingFakeServer() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmZ")
        val gmtTime = df.format(Date())

        val deviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

        val json =
            """
                {
                    "deviceId": "$deviceId",
                    "createdAt": "$gmtTime"
                }
            """
        try {
            Log.d("PinkFakeService", "true")
            Fuel.post("https://jsonplaceholder.typicode.com/posts")
                .jsonBody(json)
                .response { _, _, result ->

                    val (bytes, error) = result
                    if (bytes != null) {
                        // faik
                    } else {
                        // faik
                    }
                }
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        Log.d("NetworkService", "Destroy")
        stopService()
        super.onDestroy()
    }
}