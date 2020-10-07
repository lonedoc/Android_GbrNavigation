package gbr.utils.servicess

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import gbr.utils.api.auth.AuthAPI
import gbr.utils.api.auth.OnAuthListener
import gbr.utils.data.AuthInfo
import gbr.utils.data.ProtocolServiceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ru.rubeg38.rubegprotocol.ConnectionWatcher
import rubeg38.myalarmbutton.utils.api.coordinate.CoordinateAPI
import gbr.utils.api.coordinate.RPCoordinateAPI
import gbr.utils.data.Credentials
import gbr.utils.data.ProviderStatus
import org.osmdroid.util.GeoPoint
import rubegprotocol.RubegProtocol
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ProtocolService: Service(),LocationListener,ConnectionWatcher,OnAuthListener {

    private var oldSpeed:Float? = null
    private val coordinateBuffer:ArrayList<Coordinate> = ArrayList()

    private var lat:String? = null
    private var lon:String? = null
    private var oldCoordinate:GeoPoint? = null
    private var speed:Int? = null
    private var satelliteCount:Int? = null
    private var accuracy:Float? = null

    data class Coordinate(
        val lat:String,
        val lon:String,
        val speed:Int,
        val satelliteCount:Int,
        val accuracy:Float
    )

    data class MyLocation(
        val lat:Double,
        val lon:Double,
        val speed: Int
    )

    private lateinit var protocol: RubegProtocol
    private lateinit var unsubscribe: () -> Unit

    private var credentials: Credentials? = null

    private var authAPI:AuthAPI? = null
    private var coordinateAPI: CoordinateAPI? = null

    private var wakeLock: PowerManager.WakeLock? = null

    companion object{

        var currentLocation: Location? = null
        var isStarted = false
        var isInternetLocationEnable = false
        var isGPSLocationEnable = false
        lateinit var context: Context
        var coordinate:MyLocation? = null
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
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
            return
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

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        context = this

        val notification =NotificationService().createServerNotification(applicationContext)
        startForeground(1,notification)

        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        protocol = RubegProtocol.sharedInstance
        unsubscribe = protocol.subscribe(this as ConnectionWatcher)

        if(coordinateAPI!= null) coordinateAPI?.onDestroy()
        coordinateAPI = RPCoordinateAPI(protocol)

        isStarted = true

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            isInternetLocationEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        try {
            isGPSLocationEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

        if(isInternetLocationEnable)
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0F,this)
        }
        else
            if (isGPSLocationEnable)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0F,this)
            }

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
            while (isStarted) {
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

    private fun stopService()
    {

        Log.d("Service","onDestroy")

        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }

        protocol.stop()

        authAPI?.onDestroy()
        coordinateAPI?.onDestroy()
        unsubscribe()

        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        stopForeground(true)

        stopSelf()

        isStarted = false
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }


    var oldLocation:GeoPoint = GeoPoint(0.toDouble(),0.toDouble())
    override fun onLocationChanged(location: Location?) {
        if(location == null) return
        if(coordinateAPI == null) return

        satelliteCount = 10
        val df = DecimalFormat("#.######")

        lat = df.format(location.latitude)
        lon = df.format(location.longitude)

        speed = (location.speed * 3.6).toInt()

        accuracy = location.accuracy

        coordinate = MyLocation(location.latitude,location.longitude, speed!!)

        currentLocation = location

        if(protocol.token==null) return

        while (coordinateBuffer.isNotEmpty() && protocol.isConnected)
        {
            val lastIndex = coordinateBuffer.lastIndex
            val coordinate = coordinateBuffer.removeAt(lastIndex)
            coordinateAPI?.sendCoordinateRequest(
                coordinate.lat,
                coordinate.lon,
                credentials!!.imei,
                coordinate.speed,
                satelliteCount!!,
                coordinate.accuracy
            )
        }

        if(oldSpeed == location.speed && oldLocation == GeoPoint(location)) return

        oldSpeed = location.speed
        oldLocation = GeoPoint(location)

        if(!protocol.isConnected)
        {
            coordinateBuffer.add(Coordinate(lat!!,lon!!,speed!!,satelliteCount!!,accuracy!!))
            return
        }

        coordinateAPI?.sendCoordinateRequest(lat!!,lon!!,credentials!!.imei,speed!!,satelliteCount!!,accuracy!!)
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
        EventBus.getDefault().postSticky(ProviderStatus("enable"))
    }

    override fun onProviderDisabled(provider: String?) {
        EventBus.getDefault().postSticky(ProviderStatus("disable"))
    }

}