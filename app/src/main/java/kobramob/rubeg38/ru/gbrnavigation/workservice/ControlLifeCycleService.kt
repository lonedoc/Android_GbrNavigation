package kobramob.rubeg38.ru.gbrnavigation.workservice

import android.content.Context
import android.content.Intent
import android.os.Build


object ControlLifeCycleService {
    fun startService(context:Context){
        val ip: ArrayList<String> = ArrayList()
        ip.add(context.getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!)

        val service = Intent(context, RubegNetworkService::class.java)
        service.putExtra("command", "start")
        service.putStringArrayListExtra("ip", ip)
        service.putExtra("port", context.getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }

    fun startService(context:Context,ip:String,port:Int){

        val service = Intent(context, RubegNetworkService::class.java)

        service.putExtra("command", "start")
        service.putStringArrayListExtra("ip", arrayListOf(ip))
        service.putExtra("port", port)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }

    fun stopService(context:Context){
        val service = Intent(context, RubegNetworkService::class.java)
        service.putExtra("command", "stop")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service)
        } else {
            context.startService(service)
        }
    }
}