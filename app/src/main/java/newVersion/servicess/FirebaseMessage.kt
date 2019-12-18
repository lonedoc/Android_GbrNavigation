package newVersion.servicess

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kobramob.rubeg38.ru.gbrnavigation.R
import newVersion.alarm.AlarmActivity
import newVersion.common.CommonActivity
import newVersion.common.alarm.AlarmDialogFragment

class FirebaseMessage : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) {
        when {
            p0.data["message"] == "Связь с сервером потеряна" -> {
                Log.d("Push - message", "Связь с сервером потеряна")
            }
            CommonActivity.isAlive || AlarmActivity.isAlive || AlarmDialogFragment.isAlive->{
                val alarmBuilder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
                    this,
                    alarmChannelID(this)
                ) else NotificationCompat.Builder(this)

                alarmBuilder.setContentTitle("Тревога")
                    .setContentText("${p0.data["message"]}")
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setSmallIcon(R.drawable.ic_alarm).color = ContextCompat.getColor(this, R.color.colorPrimary)
                val notification = alarmBuilder.build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(7, notification)
            }
            else -> {
                val alarmBuilder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
                    this,
                    alarmChannelID(this)
                ) else NotificationCompat.Builder(this)

                val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.alarm_sound)
                alarmBuilder.setContentTitle("Тревога")
                    .setContentText("${p0.data["message"]}")
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setSmallIcon(R.drawable.ic_alarm).color = ContextCompat.getColor(this, R.color.colorPrimary)
                val notification = alarmBuilder.build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(7, notification)
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
            when{
                AlarmActivity.isAlive || AlarmActivity.isAlive || AlarmDialogFragment.isAlive->{
                    val channel = NotificationChannel(
                        notificationChannelId,
                        "Alarm channel",
                        NotificationManager.IMPORTANCE_HIGH
                    ).let {
                        it.description = "Alarm channel"
                        it.enableLights(true)
                        it.lightColor = Color.RED
                        it.enableVibration(true)
                        it.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
                        it
                    }
                    notificationManager.createNotificationChannel(channel)
                }
                else->{
                    val channel = NotificationChannel(
                        notificationChannelId,
                        "Alarm channel",
                        NotificationManager.IMPORTANCE_HIGH
                    ).let {
                        it.description = "Alarm channel"
                        it.enableLights(true)
                        it.lightColor = Color.RED
                        it.enableVibration(true)
                        it.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
                        it.setSound(soundUri, audioAttributes)
                        it
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }

        }
        return notificationChannelId
    }
}