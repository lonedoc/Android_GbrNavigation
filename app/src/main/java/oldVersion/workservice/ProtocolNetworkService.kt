package oldVersion.workservice

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings.Secure
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kobramob.rubeg38.ru.networkprotocol.RubegProtocol
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import oldVersion.commonactivity.CommonActivity
import oldVersion.workservice.NotificationService.createNotification
import org.json.JSONObject
import org.osmdroid.util.GeoPoint

class ProtocolNetworkService : Service() {

    private val coordinateList: ArrayList<Pair<GeoPoint, Int>> = ArrayList()
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        var protocol: RubegProtocol? = null
        var connectServer: Boolean = true
        var connectInternet: Boolean = true
        var isServiceStarted = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.getStringExtra("command")) {
                "start" -> {

                    val notification = createNotification(context = applicationContext)
                    startForeground(1, notification)

                    protocol = RubegProtocol(
                        intent.getStringArrayListExtra("ip")!!,
                        intent.getIntExtra("port", 9010)
                    )

                    startService()
                }
                "stop" -> {
                    stopService()
                }
            }
        }

        return START_STICKY
    }

    private fun coordinateLoop() {
        thread {
            var rewritePosition = 0
            var oldLocation: GeoPoint? = null

            var oldSpeed = 0
            var newSpeed = 0

            var firstSend = true

            while (isServiceStarted) {
                if (protocol?.isConnected!! && connectServer && connectInternet) {
                    if (coordinateList.count()> 0) {
                        while (coordinateList.count()> 0) {
                            val data = coordinateList.removeAt(0)

                            val coordinateMessage = JSONObject()
                            coordinateMessage.put("\$c$", "gbrkobra")
                            coordinateMessage.put("command", "location")
                            coordinateMessage.put("id", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                            coordinateMessage.put("lon", data.first.longitude)
                            coordinateMessage.put("lat", data.first.latitude)
                            coordinateMessage.put("speed", data.second)

                            protocol?.send(coordinateMessage.toString()) {
                                if (it) {
                                    Log.d("Coordinate", "Server receiver")
                                } else {
                                    Log.d("Coordinate", "Server not receiver")
                                }
                            }
                        }
                    }

                    if (LocationService.Enable) {
                        try {
                            val newLocation = GeoPoint(
                                LocationService.imHere?.latitude!!,
                                LocationService.imHere?.longitude!!
                            )

                            if (oldLocation != null) {
                                oldSpeed = newSpeed
                                newSpeed = ((newLocation.distanceToAsDouble(oldLocation) / 2)*3.6).toInt()
                                Log.d("CoordinateLoop", "oldSpeed $oldSpeed")
                                Log.d("CoordinateLoop", "newSpeed $newSpeed")
                            }

                            if (newSpeed in 1..249 && abs(newSpeed - oldSpeed) < 40 && newLocation.distanceToAsDouble(oldLocation)> 5 && newLocation.distanceToAsDouble(oldLocation) <120 || firstSend) {
                                oldLocation = newLocation

                                if (firstSend) {
                                    oldSpeed = newSpeed
                                    oldLocation = newLocation
                                }

                                Log.d("CoordinateLoop", "SendMyLocation")

                                firstSend = false

                                val coordinateMessage = JSONObject()
                                coordinateMessage.put("\$c$", "gbrkobra")
                                coordinateMessage.put("command", "location")
                                coordinateMessage.put("id", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                                coordinateMessage.put("lon", newLocation.longitude)
                                coordinateMessage.put("lat", newLocation.latitude)
                                coordinateMessage.put("speed", newSpeed)

                                protocol?.send(coordinateMessage.toString()) {
                                    if (it) {
                                        Log.d("Coordinate", "Server receiver")
                                    } else {
                                        Log.d("Coordinate", "Server not receiver")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        connectServer = true
                        connectInternet = true

                        sleep(2000)
                    }
                } else {
                    if (LocationService.Enable) {
                        if (coordinateList.count() <1000) {
                            rewritePosition = 0
                            if (coordinateList.count()> 0) {
                                val oldPosition = coordinateList[coordinateList.lastIndex].first
                                val newPosition = GeoPoint(
                                    LocationService.imHere?.latitude!!,
                                    LocationService.imHere?.longitude!!
                                )
                                val speed = ((newPosition.distanceToAsDouble(oldPosition) / 2)*3.6).toInt()
                                if (speed> 0)
                                    coordinateList.add(Pair(newPosition, speed))
                            } else {
                                coordinateList.add(
                                    Pair(
                                        GeoPoint(
                                            LocationService.imHere?.latitude!!,
                                            LocationService.imHere?.longitude!!
                                        ),
                                        0
                                    )
                                )
                            }
                        } else {
                            if (rewritePosition> 0) {
                                val oldPosition = coordinateList[rewritePosition - 1].first
                                val newPosition = GeoPoint(
                                    LocationService.imHere?.latitude!!,
                                    LocationService.imHere?.longitude!!
                                )
                                val speed = ((newPosition.distanceToAsDouble(oldPosition) / 2)*3.6).toInt()
                                if (speed> 0)
                                    coordinateList.add(Pair(newPosition, speed))
                            } else {
                                val oldPosition = coordinateList[coordinateList.lastIndex].first
                                val newPosition = GeoPoint(
                                    LocationService.imHere?.latitude!!,
                                    LocationService.imHere?.longitude!!
                                )
                                val speed = ((newPosition.distanceToAsDouble(oldPosition) / 2)*3.6).toInt()
                                if (speed> 0)
                                    coordinateList.add(Pair(newPosition, speed))
                            }
                            rewritePosition++
                            if (rewritePosition > 1000) {
                                rewritePosition = 0
                            }
                        }
                        sleep(3000)
                    }
                }
            }
        }
    }

    private fun startService() {
        if (isServiceStarted) return

        Log.d("Service", "Starting the foreground service task")
        isServiceStarted = true
        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        ProtocolDelegate(protocol, applicationContext)

        protocol?.start()

        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    Log.d("Service", "process")
                    pingFakeServer()
                }
                delay(1 * 60 * 1000)
            }
        }

        thread {

            while (!protocol?.isConnected!!) {
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

    private fun stopService() {
        try {

            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }

            stopForeground(true)
            stopSelf()

            ProtocolDelegate(null, protocol)

            if (protocol != null)
                protocol?.stop()
        } catch (e: Exception) {
            Log.d("Service", "Service stopped without being started: ${e.message}")
        }

        isServiceStarted = false
    }

    override fun onDestroy() {
        // TODO Закрывает приложение когда это не надо!!!
        if (CommonActivity.exit) {
            Log.d("Service", "Destroy")
            System.exit(0)
        }
        isServiceStarted = false
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}