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
import java.lang.Exception

class AlarmDialogActivity:AppCompatActivity() {

    companion object{
        var isAlive:Boolean = false
    }
    lateinit var alertSound: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_alarm_dialog)

        this.setFinishOnTouchOutside(false)

       val alarm = intent.getSerializableExtra("info") as Alarm
        object_name.text = alarm.name
        object_address.text = alarm.address

        alertSound = MediaPlayer.create(this, Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext?.packageName + "/" + R.raw.alarm_sound))
        if(alertSound.isPlaying)
            alertSound.stop()

        alertSound.start()

        apply_alarm.setOnClickListener {
            try{
                alertSound.stop()
            }catch (e:Exception){
                e.printStackTrace()
            }

            val alarmActivity = Intent(this, AlarmActivity::class.java)

            alarmActivity.putExtra("info", alarm)

            startActivity(alarmActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        isAlive = true
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
    }

}