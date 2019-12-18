package newVersion.common.alarm

import android.content.ContentResolver
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.fragment_alarm_dialog.*
import newVersion.alarm.AlarmActivity
import newVersion.utils.Alarm

class AlarmDialogActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_alarm_dialog)

        this.setFinishOnTouchOutside(false)

        val alarm = intent.getSerializableExtra("info") as Alarm
        object_name.text = alarm.name
        object_address.text = alarm.address

        val alertSound: MediaPlayer? = MediaPlayer.create(this, Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext?.packageName + "/" + R.raw.alarm_sound))
        alertSound?.start()

        apply_alarm.setOnClickListener {
            if(alertSound != null && alertSound.isPlaying)
            {
                alertSound.stop()
                alertSound.release()
            }

            finish()

            val alarmActivity = Intent(this, AlarmActivity::class.java)

            alarmActivity.putExtra("info", alarm)

            startActivity(alarmActivity)
        }
    }

}