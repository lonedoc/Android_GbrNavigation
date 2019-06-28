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
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import kobramob.rubeg38.ru.gbrnavigation.R

class NetworkServiceTest: Service() {


    private fun startForeground() {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification =
            notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSubText("Кобра ГБР")
                .setContentTitle("Поддерживаем соединение с сервером")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(channelId.toInt(), notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "101"
        val channelName = "My Background Service"
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.importance = NotificationManager.IMPORTANCE_NONE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onCreate() {
        super.onCreate()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            startForeground()
        }
        else
        {

        }

        return START_STICKY
    }

    fun putPacketOnQueue(data:ByteArray,sessionID:String?,isWaitingForResponse:Boolean){

        val messageNumber = PollingServer.countSender + 1
        var packetCount = data.size / 962
        if(data.size % 962 != 0){
            packetCount += 1
        }

        val booleanArray = BooleanArray(packetCount)
        for(i in 0 until packetCount){
            booleanArray[i]=false
        }

        var subpacketNumber = 1
        var leftBound = 0
        while(leftBound<data.count())
        {
            val rightBound = if(leftBound+962<data.count()){
                leftBound+962
            }
            else
                data.count()

            val chunk = data.slice(IntRange(leftBound, rightBound - 1))

            val packet = DataPacket(
                data = chunk.toByteArray(),
                sessionID = sessionID,
                contentType = ContentType.string,
                messageNumber = messageNumber,
                messageSize = data.count(),
                shift = leftBound,
                packetCount = packetCount,
                packetNumber = subpacketNumber
            )
            subpacketNumber++
            leftBound += 962
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}