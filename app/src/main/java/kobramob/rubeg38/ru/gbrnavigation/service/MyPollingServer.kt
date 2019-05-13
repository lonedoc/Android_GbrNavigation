package kobramob.rubeg38.ru.gbrnavigation.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import org.json.JSONObject
import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.channels.DatagramChannel
import java.util.*

class MyPollingServer:LocationListener {

    private val request: Request = Request()
    private val coder: Coder = Coder()

    private val LOG_TAG = "PollingService"

    private val typePacket: Byte = 0
    private val typePacket255: Byte = 255.toByte()

    private val timer: Timer = Timer()

    companion object{
        private val datagramChannel:DatagramChannel = DatagramChannel.open()
        val socket:DatagramSocket = datagramChannel.socket()

        lateinit var currentLocation:Location
        var latitude:Double = 0.toDouble()
        var longtitude:Double = 0.toDouble()

        var tryCount = 0
        var countReceiver:Long = 2
        var countSender:Long = 0

        var serverAlive:Boolean = true

    }

    fun startService(context:Context){
        getLocation(context)
        /*val timerTask:TimerTask = TestTask(context)
        timer.schedule(timerTask,0,9000)*/
        val timerTask:TimerTask = RequestTask(context)
        timer.schedule(timerTask,0,9000)
        serverReceiver(context)
    }

    class TestTask(context: Context) : TimerTask() {
        override fun run() {
            println("Моя машина")
        }

    }

    private fun serverReceiver(context: Context) {
        val serverReceiver = Runnable{
            while(serverAlive) {
                socket.soTimeout = 0
                println("Ресивер")
                val intentStartActivity = Intent(StartActivity.BROADCAST_ACTION)
                val intentLoginActivity = Intent(LoginActivity.BROADCAST_ACTION)
                val intentObjectActivity = Intent(ObjectActivity.BROADCAST_ACTION)

                val receiverBuffer = ByteArray(1057)

                val receiverPacket = DatagramPacket(receiverBuffer, receiverBuffer.size)

                socket.receive(receiverPacket)

                if (receiverPacket.length == 0) {
                    println("Server Alive")
                    serverAlive = true
                } else
                    when (coder.typePacket(receiverPacket.data)) {
                        255 -> {
                            println("Server Alive")
                            serverAlive = true
                        }
                        0 -> {
                            println("Server Alive")
                            serverAlive = true
                            if (coder.countReceiver(receiverPacket.data) >= countReceiver) {
                                request.packetType255(
                                    socket,
                                    receiverPacket.data,
                                    receiverPacket.port,
                                    receiverPacket.address
                                )
                                val serverResponse = coder.decoderPacketOne(receiverPacket.data)
                                val jsonObject = JSONObject(serverResponse)
                                val jsonArray = jsonObject.getJSONArray("d")
                                when (JSONObject(jsonArray.getString(0)).getString("command")) {
                                    "gbrstatus" -> {
                                        try {
                                            intentStartActivity.putExtra(
                                                "status",
                                                JSONObject(jsonArray.getString(0)).getString("status")
                                            )
                                            context.sendBroadcast(intentStartActivity)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        try {
                                            intentObjectActivity.putExtra(
                                                "status",
                                                JSONObject(jsonArray.getString(0)).getString("status")
                                            )
                                            context.sendBroadcast(intentObjectActivity)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    "alarm" -> {
                                    }
                                    "regok" -> {
                                        tryCount = 0
                                        try {
                                            intentLoginActivity.putExtra("info", serverResponse)
                                            context.sendBroadcast(intentLoginActivity)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            val jsonObject = JSONObject(serverResponse)
                                            val jsonArray = jsonObject.getJSONArray("d")
                                            SharedPreferencesState.init(context)
                                            SharedPreferencesState.addPropertyString(
                                                "tid",
                                                JSONObject(jsonArray.getString(0)).getString("tid")
                                            )
                                            if (JSONObject(jsonArray.getString(0)).getString("namegbr") !=
                                                context.getSharedPreferences(
                                                    "state",
                                                    Context.MODE_PRIVATE
                                                ).getString("namegbr", "")
                                            ) {
                                                SharedPreferencesState.addPropertyString(
                                                    "namegbr",
                                                    JSONObject(jsonArray.getString(0)).getString("namegbr")
                                                )
                                            }
                                            if (JSONObject(jsonArray.getString(0)).getString("call") !=
                                                context.getSharedPreferences(
                                                    "state",
                                                    Context.MODE_PRIVATE
                                                ).getString("call", "")
                                            ) {
                                                SharedPreferencesState.addPropertyString(
                                                    "call",
                                                    JSONObject(jsonArray.getString(0)).getString("call")
                                                )
                                            }
                                        }
                                    }
                                    "alarmprok" -> {
                                        try {
                                            intentObjectActivity.putExtra(
                                                "info",
                                                JSONObject(jsonArray.getString(0)).getString("command")
                                            )
                                            context.sendBroadcast(intentObjectActivity)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    "notalarm" -> {
                                        try {
                                            intentObjectActivity.putExtra(
                                                "info",
                                                JSONObject(jsonArray.getString(0)).getString("command")
                                            )
                                            context.sendBroadcast(intentObjectActivity)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                countReceiver++
                            }
                        }
                    }

            }
        };Thread(serverReceiver).start()
    }

    inner class RequestTask(val context: Context) : TimerTask() {
        override fun run() {
            if (serverAlive) {
                var speed: Float = 0.toFloat()
                var lat: Double = 0.toDouble()
                var lon: Double = 0.toDouble()

                try {
                    speed = currentLocation.speed
                    lat = currentLocation.latitude
                    latitude = currentLocation.latitude
                    longtitude = currentLocation.longitude
                    lon = currentLocation.longitude
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                serverAlive = false
                if (speed> 0.toFloat()) {
                    println("Send Location")
                    request.sendLocation(
                        socket,
                        context.getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                        context.getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010),
                        countSender,
                        typePacket,
                        lat,
                        lon,
                        speed,
                        context.getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", ""),
                        context.getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", "")
                    )
                } else {
                    println("Null Packet")
                    request.nullPacket(
                        socket,
                        context.getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                        context.getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010)
                    )
                }
            }
            else
            {
                countSender = 1
                countReceiver = 1
                if(tryCount <2)
                {
                    request.register(
                        socket, countSender, typePacket,
                        context.getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", ""),
                        context.getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                        context.getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010),
                        ""
                    )
                    tryCount++
                }
                else
                {
                    tryCount = 0

                    try{
                        val intentLoginActivity = Intent(LoginActivity.BROADCAST_ACTION)
                        intentLoginActivity.putExtra("accessDenied",true)
                        context.sendBroadcast(intentLoginActivity)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                    try{
                        val intentStartActivity = Intent(LoginActivity.BROADCAST_ACTION)
                        intentStartActivity.putExtra("accessDenied",true)
                        context.sendBroadcast(intentStartActivity)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                    try{
                        val intentObjectActivity = Intent(LoginActivity.BROADCAST_ACTION)
                        intentObjectActivity.putExtra("accessDenied",true)
                        context.sendBroadcast(intentObjectActivity)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(context:Context) {
        try {
            val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
            } catch (e: Exception) {}
            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
            } catch (e: Exception) {}
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onLocationChanged(location: Location?) {
        currentLocation = location!!
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }



}