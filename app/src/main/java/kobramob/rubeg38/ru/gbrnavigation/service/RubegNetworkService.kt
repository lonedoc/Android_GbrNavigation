package kobramob.rubeg38.ru.gbrnavigation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.rubegnetworkprotocol.RubegProtocol
import kobramob.rubeg38.ru.rubegnetworkprotocol.RubegProtocolDelegate
import java.util.*

class RubegNetworkService: Service(),RubegProtocolDelegate {
    override var sessionId: String? = null

    override fun connectionLost() {
        Log.d("ConnectionLost", true.toString())
    }

    override fun messageReceived(message: ByteArray) {
        Log.d("ByteMessage", Arrays.toString(message))
    }

    override fun messageReceived(message: String) {
        Log.d("StringMessage",message)
    }

    private fun startForeground() {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelID = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            createNotificationChannel()
        }
        else
        {
            "101"
        }
        val notificationBuilder = NotificationCompat.Builder(this,channelID)
        val notification =
            notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSubText("Кобра ГБР")
                .setContentTitle("Сервис фоновой работы приложения")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(channelID.toInt(),notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelID = "101"
        val channelName = "MyBackgroundService"
        val chan = NotificationChannel(
            channelID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )

        chan.lightColor = Color.GRAY
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelID
    }



    companion object{
        lateinit var protocol: RubegProtocol
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        protocol = RubegProtocol(
            intent!!.getStringArrayListExtra("ip")!!,
            intent.getIntExtra("port",9010))

        protocol.delegate = this
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            startForeground()
            protocol.start()

        }
        else
        {

            protocol.start()

        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(false)
        stopSelf()

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}