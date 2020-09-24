package gbr.utils.servicess

import android.app.PendingIntent
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import gbr.ui.main.MainActivity
import gbr.ui.start.StartActivity


class FCM: FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        when{
            !MainActivity.isAlive->{
                val intent = Intent(
                    applicationContext,
                    MainActivity::class.java
                ) // Here pass your activity where you want to redirect.


                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                val contentIntent = PendingIntent.getActivity(
                    this,
                    (Math.random() * 100).toInt(), intent, 0
                )
                startActivity(intent)
            }

            StartActivity.isAlive->{

            }
            !StartActivity.isAlive->{

            }
        }
    }
}