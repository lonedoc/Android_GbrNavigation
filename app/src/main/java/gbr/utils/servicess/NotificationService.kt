package gbr.utils.servicess

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kobramob.rubeg38.ru.gbrnavigation.R

class NotificationService {

    fun createConnectNotification(connection:Boolean,context:Context) {
        val connectionBuilder = NotificationCompat.Builder(context,connectionChannelID(context))

        if(connection)
        {
            connectionBuilder
                .setContentTitle("Соединение с сервером")
                .setContentText("Соединение с серверов восстановлено")
                .setSmallIcon(R.drawable.ic_connect)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
        }
        else
        {
            connectionBuilder
                .setContentTitle("Соединение с сервером")
                .setContentText("Соединение с сервером потеряно")
                .setSmallIcon(R.drawable.ic_disconnect)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                .setCategory(NotificationCompat.CATEGORY_ALARM)
        }
        val notification = connectionBuilder.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)
    }

    private fun connectionChannelID(context: Context): String {
        val connectionChannelID = "Connection channel"

        val audio =  AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                connectionChannelID,
                connectionChannelID,
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Connection channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.setSound(statusSound, audio)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
        return connectionChannelID
    }

    fun createServerNotification(context: Context): Notification? {
        val notificationChannelID = "ENDLESS SERVICE CHANNEL"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context,notificationChannelID)
            builder
                .setContentTitle("Сервис")
                .setContentText("Сервис для общения с сервером")
                .setSmallIcon(R.drawable.ic_service)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelID,
                "Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it
            }
            notificationManager.createNotificationChannel(channel)
            return builder.build()
        }
        else
        {
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context,notificationChannelID)

            return builder
                .setContentTitle("Сервис")
                .setContentText("Сервис для общения с сервером")
                .setSmallIcon(R.drawable.ic_service)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build()
        }
    }

    fun createInternetNotification(connection: Boolean, context: Context) {
        val internetBuilder = NotificationCompat.Builder(context,internetChannelID(context))

        if(connection)
        {
            internetBuilder
                .setContentTitle("Интернет")
                .setContentText("Работа сети интернет восстановлена")
                .setSmallIcon(R.drawable.ic_connect)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
        }
        else
        {
            internetBuilder
                .setContentTitle("Интернет")
                .setContentText("Проблемы с сетью интернет")
                .setSmallIcon(R.drawable.ic_disconnect)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                .setCategory(NotificationCompat.CATEGORY_ALARM)
        }
        val notification = internetBuilder.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(3, notification)
    }

    private fun internetChannelID(context:Context):String{
        val internet = "Internet channel"

        val audio =  AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                internet,
                internet,
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Internet channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.setSound(statusSound, audio)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
        return internet
    }
}