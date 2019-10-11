package oldVersion.workservice

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import java.util.*
import kobramob.rubeg38.ru.networkprotocol.RubegProtocol
import kobramob.rubeg38.ru.networkprotocol.RubegProtocolDelegate
import oldVersion.resource.ControlLifeCycleService.startService
import oldVersion.resource.DataStore
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class ProtocolDelegate : RubegProtocolDelegate {

    override var token: String? = null

    lateinit var context: Context

    constructor(protocol: RubegProtocol?, applicationContext: Context) {
        protocol?.delegate = this
        context = applicationContext
    }
    constructor(sessionID: String?, protocol: RubegProtocol?) {
        protocol?.delegate = null
        this.token = sessionID
    }

    private var connectionLost: Boolean = false

    override fun connectionLost() {

        Log.d("ConnectionLost", "Yes")

        if (!ProtocolNetworkService.isServiceStarted) {
            Log.d("ConnectionLost", "StartService")
            startService(context)
        }

        when {
            !isConnected(context) -> {
                ProtocolNetworkService.connectInternet = false

                if (!connectionLost) {
                    val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                        .addData("command", "disconnectInternet")
                        .build()
                    NotificationService.createNotification(remoteMessage, context)
                    Thread.sleep(2000)
                    val remoteMessage1 = RemoteMessage.Builder("Status")
                        .addData("command", "disconnectServer")
                        .build()
                    NotificationService.createNotification(remoteMessage1, context)
                    connectionLost = true
                }

                while (!isConnected(context)) {
                    // wait connect internet
                }

                val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                    .addData("command", "reconnectInternet")
                    .build()
                NotificationService.createNotification(remoteMessage, context)

                Thread.sleep(3000)

                reconnect()
            }
            else -> {

                ProtocolNetworkService.connectServer = false

                if (!connectionLost) {
                    val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                        .addData("command", "disconnectServer")
                        .build()
                    NotificationService.createNotification(remoteMessage, context)
                    connectionLost = true
                }

                Thread.sleep(3000)

                reconnect()
            }
        }
    }

    private fun reconnect() {
        val authorizationMessage = JSONObject()
        authorizationMessage.put("\$c$", "reg")
        authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        authorizationMessage.put(
            "password",
            context.getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
        )
        authorizationMessage.put(
            "token",
            context.getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("fcmtoken", "")
        )
        authorizationMessage.put("keepalive", "10")
        ProtocolNetworkService.protocol?.request(authorizationMessage.toString()) { success: Boolean, data: ByteArray? ->
            if (success && data != null) {
                val regGson = Gson()
                val registration = regGson.fromJson(String(data), RegistrationGson::class.java)
                this.token = registration.tid

                val authorization: RemoteMessage = RemoteMessage.Builder("Status")
                    .addData("command", "reconnectServer")
                    .build()
                NotificationService.createNotification(authorization, context)

                ProtocolNetworkService.connectInternet = true
                ProtocolNetworkService.connectServer = true
                connectionLost = false
            } else {
                Log.d("InternetReconnected", "false")
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
        try {
            val gson = Gson()
            when {
                JSONObject(message).has("command") -> {
                    when (JSONObject(message).getString("command")) {

                        "regok" -> {
                            val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                                .addData("command", "connectServer")
                                .build()
                            NotificationService.createNotification(remoteMessage, context)

                            val registration = gson.fromJson(message, RegistrationGson::class.java)

                            this.token = registration.tid

                            DataStore.initRegistrationData(
                                namegbr = registration.namegbr,
                                call = registration.call,
                                status = registration.status,
                                statusList = registration.gpsstatus,
                                routeServer = registration.routeserver,
                                reports = registration.reports,
                                cityCard = registration.citycard
                            )
                        }

                        "gbrstatus" -> {

                            val status = gson.fromJson(message, StatusGson::class.java)

                            if (status.status != "") {
                                /*if (CommonActivity.isAlive && status.status != "На тревоге") {
                                    val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                                        .addData("command", status.command)
                                        .addData("status", status.status)
                                        .build()
                                    NotificationService.createNotification(remoteMessage, context)
                                }*/
                                EventBus.getDefault().postSticky(
                                    MessageEvent(
                                        command = status.command,
                                        message = status.status
                                    )
                                )
                            }
                        }

                        "alarm" -> {
                            try {
                                val alarm = gson.fromJson(message, Alarm::class.java)

                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }

                        "notalarm" -> {
                            val notalarm = gson.fromJson(message, NotAlarmGson::class.java)
                            EventBus.getDefault().postSticky(
                                MessageEvent(
                                    command = "notalarm",
                                    message = "Свободен",
                                    name = notalarm.name
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
                            ProtocolNetworkService.protocol?.send(startReceive.toString()) {
                                if (it) {
                                    Log.d("startrecive", "start")
                                }
                            }
                        }

                        "accessdenied" -> {
                         /*   if (MainActivity.isAlive || LoginActivity.isAlive) {
                                EventBus.getDefault().post(
                                    RegistrationEvent(
                                        command = "accessdenied"
                                    )
                                )
                            }*/
                        }
                    }
                }
                else -> {
                    Log.d("StringMessage", "UnknownMessage")
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}