package gbr.utils.servicess

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.gson.JsonObject
import gbr.utils.api.auth.AuthAPI
import gbr.utils.api.auth.OnAuthListener
import gbr.utils.data.AuthInfo
import gbr.utils.data.Location
import gbr.utils.data.ProtocolServiceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import newVersion.servicess.NetworkService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.util.GeoPoint
import ru.rubeg38.rubegprotocol.ConnectionWatcher
import rubegprotocol.RubegProtocol
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ProtocolService: Service(),ConnectionWatcher,OnAuthListener {

    private lateinit var protocol: RubegProtocol
    private lateinit var unsubscribe: () -> Unit

    private var authAPI:AuthAPI? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object{
        var isStarted = false
    }

    private var oldLocation:GeoPoint? = null
    private var oldSpeed:Int? = null
    private var coordinateQueue:ArrayList<Location> = ArrayList()
    private var credentials:newVersion.models.Credentials? = null

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onLocationSend(location: Location){
        val speed = (location.speed * 3.6).toInt()
        val myLocation = GeoPoint(location.lat,location.lon)

        if(oldSpeed == 0 && speed == 0) return

        if(myLocation == oldLocation) return

        if(credentials == null) return

        oldLocation = myLocation
        oldSpeed = speed

        if(!protocol.isConnected || connectionLost)
        {
            coordinateQueue.add(location)
            return
        }

        if(coordinateQueue.isNotEmpty()){
            for(i in 0 until coordinateQueue.size)
            {
                sendCoordinate(coordinateQueue[i])
            }
            coordinateQueue.clear()
        }

        sendCoordinate(location)
    }

    private fun sendCoordinate(location: Location){
        val df = DecimalFormat("#.######")
        val jsonMessage = JsonObject()
        jsonMessage.addProperty("\$c$", "gbrkobra")
        jsonMessage.addProperty("command", "location")
        jsonMessage.addProperty("id",credentials?.imei )
        jsonMessage.addProperty("lon", df.format(location.lon))
        jsonMessage.addProperty("lat", df.format(location.lat))
        jsonMessage.addProperty("speed", (location.speed * 3.6).toInt())
        jsonMessage.addProperty("accuracy",location.accuracy)
        jsonMessage.addProperty("gpsCount",location.satelliteCount)
        val request = jsonMessage.toString()
        protocol.send(request){}
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND,sticky = true)
    fun onStartService(info: ProtocolServiceInfo) {
        isStarted = true

        if(protocol.isStarted)
            protocol.stop()

        protocol.configure(info.hostPool.addresses,info.hostPool.port)

        protocol.start()

        if(authAPI != null)
            authAPI!!.onDestroy()

        authAPI = gbr.utils.api.auth.RPAuthAPI(
            protocol = protocol,
            credentials = info.credentials
        )

        authAPI!!.onAuthListener = this

        credentials = info.credentials

        EventBus.getDefault().removeStickyEvent(info)
    }

    override fun onAuthDataReceived(auth: AuthInfo) {
        protocol.token = auth.token
    }

    @androidx.annotation.IntRange(from = 0, to =  1)
    fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 1
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = 1
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = 1
                    }
                }
            }
        }
        return result
    }

    private var connectionLost = false
    private var internetLost = false

    override fun onConnectionLost() {

        if(getConnectionType(this) == 1)
        authAPI?.sendAuthRequest {}

        if(connectionLost) return

        connectionLost = true

        if(getConnectionType(this) == 0)
        {
            NotificationService().createInternetNotification(false,this)
            internetLost = true
        }


        NotificationService().createConnectNotification(false,this)

    }

    override fun onConnectionEstablished() {
        if(connectionLost)
        {
            if(internetLost)
            {
                NotificationService().createInternetNotification(true,this)
                internetLost = false
            }
            NotificationService().createConnectNotification(true,this)
            connectionLost = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        val notification = NotificationService().createServerNotification(this)
        startForeground(1,notification)

        protocol = RubegProtocol.sharedInstance
        unsubscribe = protocol.subscribe(this as ConnectionWatcher)

        wakeLock()

        return START_NOT_STICKY
    }

    private fun wakeLock() {
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService:lock").apply {
                acquire()
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            while (NetworkService.isServiceStarted) {
                launch(Dispatchers.IO) {
                    Log.d("Service", "process")
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
        //TODO отправить сообщение что сервис обвалился

        Log.d("Protocol","onDestroy")
        isStarted = false

        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }

        protocol.stop()

        authAPI?.onDestroy()

        unsubscribe()

        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        stopForeground(true)

        stopSelf()

        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


}