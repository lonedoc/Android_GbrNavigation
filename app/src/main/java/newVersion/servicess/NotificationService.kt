package newVersion.servicess

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import java.lang.Thread.sleep
import kobramob.rubeg38.ru.gbrnavigation.R
import newVersion.alarm.AlarmActivity
import newVersion.common.CommonActivity
import newVersion.login.LoginActivity
import kotlin.concurrent.thread

object NotificationService {

    fun createNotification(remoteMessage: RemoteMessage, context: Context) {
        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
            context,
            channelID(context)
        ) else NotificationCompat.Builder(context)

        val alarmBuilder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
            context,
            alarmChannelID(context)
        ) else NotificationCompat.Builder(context)

        val serverBuilder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
            context,
            serverChannelID(context)
        ) else NotificationCompat.Builder(context)

        val pendingIntent = when {
            LoginActivity.isAlive -> {
                val i = Intent(context, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }
           CommonActivity.isAlive -> {
                val i = Intent(context, CommonActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            else -> {
                null
            }
        }
        when (remoteMessage.data["command"]) {
            "gbrstatus" -> {
                val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setContentTitle("Смена статуса")
                    .setContentIntent(pendingIntent)
                    .setContentText("Смена статуса на: ${remoteMessage.data["status"]}")
                    .setSound(statusSound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_unknown_status).color = ContextCompat.getColor(context, R.color.colorPrimary)
                val notification = builder.build()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(6, notification)
            }
            "alarm" -> {
                val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.alarm_sound)
                alarmBuilder.setContentTitle("Тревога")
                    .setContentIntent(pendingIntent)
                    .setContentText("Тревога на : ${remoteMessage.data["name"]}")
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setSmallIcon(R.drawable.ic_alarm).color = ContextCompat.getColor(context, R.color.colorPrimary)
                val notification = alarmBuilder.build()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(7, notification)
            }
            "disconnectServer" -> {
                    sleep(3000)
                    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    serverBuilder.setContentTitle("Соединение с сервером")
                        .setContentText("Соединение с сервером потеряно")
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setAutoCancel(true)
                        .setSound(alarmSound)
                        .setSmallIcon(R.drawable.ic_disconnect).color = ContextCompat.getColor(context, R.color.colorPrimary)
                    builder.build().flags = Notification.FLAG_AUTO_CANCEL
                    val notification = serverBuilder.build()

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(2, notification)
            }
            "reconnectServer" -> {
                    sleep(3000)
                    val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    serverBuilder.setContentTitle("Соединение с сервером")
                        .setContentIntent(pendingIntent)
                        .setContentText("Соединение с сервером восстановлено")
                        .setSound(statusSound)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setSmallIcon(R.drawable.ic_connect).color = ContextCompat.getColor(context, R.color.colorPrimary)
                    val notification = serverBuilder.build()
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(2, notification)
            }
            "connectServer" -> {
                val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                serverBuilder.setContentTitle("Соединение с сервером")
                    .setContentIntent(pendingIntent)
                    .setContentText("Соединение с сервером установлено")
                    .setSound(statusSound)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setSmallIcon(R.drawable.ic_connect).color = ContextCompat.getColor(context, R.color.colorPrimary)
                val notification = serverBuilder.build()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2, notification)
            }
            "serverNotResponse" -> {
                val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                serverBuilder.setContentTitle("Соединение с сервером")
                    .setContentIntent(pendingIntent)
                    .setContentText("Соединение с сервером не было установлено")
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setSound(statusSound)
                    .setSmallIcon(R.drawable.ic_disconnect).color = ContextCompat.getColor(context, R.color.colorPrimary)
                builder.build().flags = Notification.FLAG_AUTO_CANCEL
                val notification = serverBuilder.build()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2, notification)
            }
            "disconnectInternet" -> {
                    sleep(3000)
                    val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    serverBuilder.setContentTitle("Интернет")
                        .setContentIntent(pendingIntent)
                        .setContentText("Проблемы с сетью интернет")
                        .setSound(statusSound)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setSmallIcon(R.drawable.ic_disconnect).color = ContextCompat.getColor(context, R.color.colorPrimary)
                    val notification = serverBuilder.build()
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(3, notification)
            }
            "reconnectInternet" -> {
                    sleep(3000)
                    val statusSound =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    serverBuilder.setContentTitle("Интернет")
                        .setContentIntent(pendingIntent)
                        .setContentText("Работа сети интернет восстановлена")
                        .setSound(statusSound)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                        .setAutoCancel(true)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setSmallIcon(R.drawable.ic_connect).color =
                        ContextCompat.getColor(context, R.color.colorPrimary)
                    val notification = serverBuilder.build()
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(3, notification)
            }
        }
    }

    private fun alarmChannelID(context: Context): String {
        val notificationChannelId = "Alarm channel"

        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.alarm_sound)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Alarm channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Alarm channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it.setSound(soundUri, audioAttributes)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
        return notificationChannelId
    }

    private fun serverChannelID(context:Context):String{
        val notificationChannelId = "Server channel"

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Serveer channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Server channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.setSound(statusSound, audioAttributes)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
        return notificationChannelId
    }
    private fun channelID(context: Context): String {
        val notificationChannelId = "Status channel"

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val statusSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Status channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Status channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.setSound(statusSound, audioAttributes)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
        return notificationChannelId
    }

    fun createNotification(context: Context): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
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
        }

        /*val pendingIntent: PendingIntent = Intent(context, CommonActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(context, 0, notificationIntent, 0)
        }*/

        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
            context,
            notificationChannelId
        ) else NotificationCompat.Builder(context)

        return builder
            .setContentTitle("Service")
            .setContentText("Service notifications")
            /*.setContentIntent(pendingIntent)*/
            .setSmallIcon(R.drawable.ic_service)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }
}