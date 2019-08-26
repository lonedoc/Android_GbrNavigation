package kobramob.rubeg38.ru.gbrnavigation.resource

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.mainactivity.MainActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.workservice.RubegNetworkService

class FCM : FirebaseMessagingService() {

    val tag = "FCM"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
            this,
            channelID()
        ) else NotificationCompat.Builder(this)

        val pendingIntent = when {
            LoginActivity.isAlive -> {
                val i = Intent(this, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            CommonActivity.isAlive -> {
                val i = Intent(this, CommonActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            ObjectActivity.isAlive || ObjectActivity.saveAlarm != null -> {
                val i = Intent(this, ObjectActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                i.putExtra("objectInfo", ObjectActivity.saveAlarm)
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }
            else -> {
                val i = Intent(this, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        if(!RubegNetworkService.isServiceStarted && !MainActivity.isAlive)
        when (remoteMessage!!.notification!!.body!!) {
            "Связь с сервером потеряна" -> {
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setContentTitle("Соединение с сервером")
                    .setContentText(remoteMessage.notification!!.body!!)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .setSmallIcon(R.drawable.ic_disconnect).color = ContextCompat.getColor(this, R.color.colorPrimary)
                builder.build().flags = Notification.FLAG_AUTO_CANCEL
                val notification = builder.build()

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(2, notification)
            }
        }

    }

    private fun channelID(): String {
        val notificationChannelId = "Notifications channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "Notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Notifications channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
        return notificationChannelId
    }
}
