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

            }

            StartActivity.isAlive->{

            }
            !StartActivity.isAlive->{

            }
        }
    }
}