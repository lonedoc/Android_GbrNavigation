package oldVersion.resource

import android.content.Context
import android.content.Intent
import android.os.Build
import java.lang.Exception
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import oldVersion.workservice.ProtocolNetworkService
import org.json.JSONObject

object ControlLifeCycleService {
    fun startService(context: Context) {
        println("Service start")
        val ip: ArrayList<String> = ArrayList()
        ip.add(context.getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!)

        val service = Intent(context, ProtocolNetworkService::class.java)
        service.putExtra("command", "start")
        service.putStringArrayListExtra("ip", ip)
        service.putExtra("port", context.getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }

    fun startService(context: Context, ip: String, port: Int) {

        println("ReconnectService")
        val service = Intent(context, ProtocolNetworkService::class.java)

        service.putExtra("command", "start")
        service.putStringArrayListExtra("ip", arrayListOf(ip))
        service.putExtra("port", port)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }

    fun stopService(context: Context) {
        if (ProtocolNetworkService.isServiceStarted) {
            val service = Intent(context, ProtocolNetworkService::class.java)
            service.putExtra("command", "stop")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                println("${Build.VERSION.SDK_INT}")
                try {
                    context.startForegroundService(service)
                } catch (e: Exception) {
                    e.printStackTrace()
                    context.startService(service)
                }
            } else {
                println("THis")
                context.startService(service)
            }
        }
        return
    }

    fun reconnectToServer(context: Context) {
        startService(
            context,
            context.getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!,
            9010
        )
        thread {
            sleep(500)
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

            ProtocolNetworkService.protocol?.send(authorizationMessage.toString()) { success: Boolean ->
                if (success) {
                    // reconnect
                } else {
                    // disconnect
                    stopService(context)
                    reconnectToServer(context)
                }
            }
        }
    }
}