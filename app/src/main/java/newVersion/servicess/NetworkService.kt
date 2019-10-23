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
import newVersion.utils.newCredetials
import newVersion.login.LoginActivity
import newVersion.main.MainActivity
import newVersion.models.Auth
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.network.auth.AuthAPI
import newVersion.network.auth.OnAuthListener
import newVersion.network.auth.RPAuthAPI
import newVersion.servicess.LocationListener.Companion.imHere
import newVersion.servicess.NotificationService.createNotification
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.util.GeoPoint
import ru.rubeg38.rubegprotocol.ConnectionWatcher
import rubegprotocol.RubegProtocol

class NetworkService : Service(), ConnectionWatcher, OnAuthListener {

    private lateinit var unsubscribe: () -> Unit
    companion object {
        var isServiceStarted = false
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var protocol: RubegProtocol
    private lateinit var authAPI: AuthAPI

    private fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    private fun isConnected(context: Context): Boolean {
        val info = getNetworkInfo(context)
        return info != null && info.isConnected
    }

    private var connectionLost = false

    override fun onConnectionLost() {
        Log.d("Service", "Connection lost")

        if (!connectionLost && protocol.isStarted && !LoginActivity.isAlive || !connectionLost && protocol.isStarted && !MainActivity.isAlive) {
            connectionLost = true
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

        while (!isConnected(applicationContext)) {
        }

        authAPI.sendAuthRequest {
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
    fun onCredentialsChanged(event: newCredetials) {
        Log.d("Service", "credentials received ${event.credetials}")

        authAPI.onDestroy()
        authAPI = RPAuthAPI(protocol, event.credetials)

        authAPI.onAuthListener = this
    }

    lateinit var hostPool: HostPool
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        assert(intent != null)
        assert(intent!!.hasExtra("credentials"))
        assert(intent.hasExtra("hostPool"))
        assert(intent.hasExtra("command"))

        when (intent.getStringExtra("command")) {
            "start" -> {

                val notification = createNotification(context = applicationContext)
                startForeground(1, notification)

                val credentials = intent.getSerializableExtra("credentials") as Credentials
                hostPool = intent.getSerializableExtra("hostPool") as HostPool

                startService(credentials, hostPool)
            }
            "stop" -> {
                stopService()
            }
        }
        return START_STICKY
    }

    private fun startService(credentials: Credentials, hostPool: HostPool) {
        if (isServiceStarted) return




        EventBus.getDefault().register(this)
        isServiceStarted = true


        protocol = RubegProtocol.sharedInstance
        unsubscribe = protocol.subscribe(this as ConnectionWatcher)

        authAPI = RPAuthAPI(protocol, credentials)

        authAPI.onAuthListener = this


        coordinateLoop(credentials)

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


    private fun coordinateLoop(credentials: Credentials) {
        var oldCoordinate:GeoPoint? = GeoPoint(0.0,0.0)
        thread {
            while(isServiceStarted)
            {
                try{

                    if(!protocol.isConnected) continue

                    if(imHere == null) continue

                    if(oldCoordinate == GeoPoint(imHere)) continue

                    oldCoordinate = GeoPoint(imHere)

                    Log.d("Speed","${imHere?.speed?.times(3.6)}")
                    val jsonMessage = JsonObject()
                    jsonMessage.addProperty("\$c$", "gbrkobra")
                    jsonMessage.addProperty("command", "location")
                    jsonMessage.addProperty("id",credentials.imei )
                    jsonMessage.addProperty("lon", imHere?.longitude)
                    jsonMessage.addProperty("lat", imHere?.latitude)
                    jsonMessage.addProperty("speed", (imHere?.speed!! * 3.6).toInt())

                    val request = jsonMessage.toString()

                    protocol.send(request){
                        if(it){
                            Log.d("Coordinate","Send")
                        }
                        else
                        {
                            Log.d("Coordinate","NotSend")
                        }
                    }

                }catch (e:java.lang.Exception){
                    e.printStackTrace()
                }
                sleep(2000)

            }
        }
    }
    override fun onDestroy() {
        Log.d("NetworkService", "Destroy")
        stopService()
        super.onDestroy()
    }
}