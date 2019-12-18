package newVersion.common.alarm

import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kobramob.rubeg38.ru.gbrnavigation.R
import newVersion.callback.AlarmCallback
import newVersion.utils.Alarm

class AlarmDialogFragment : DialogFragment() {

    var alertSound: MediaPlayer? = null
    var rootView: View? = null

    companion object {
        var isAlive = false
        var callback: AlarmCallback? = null
        var activeSound = true

        fun newInstance(alarm: Alarm, callback: AlarmCallback, activeSound: Boolean): AlarmDialogFragment {
            this.callback = callback
            this.activeSound = activeSound
            val frag = AlarmDialogFragment()
            val args = Bundle()
            args.putSerializable("info", alarm)
            frag.arguments = args
            return frag
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft: FragmentTransaction = manager.beginTransaction()
            ft.add(this, tag)
            ft.commit()
        } catch (e: IllegalStateException) {
            Log.d("ABSDIALOGFRAG", "Exception", e)
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val layoutInflater: LayoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = layoutInflater.inflate(R.layout.fragment_alarm_dialog, null)

        val alarm = arguments?.getSerializable("info") as Alarm
        val applyAlarm: Button = rootView?.findViewById(R.id.apply_alarm) as Button
        val objectName = rootView?.findViewById(R.id.object_name) as TextView
        val objectAddress = rootView?.findViewById(R.id.object_address) as TextView
        objectName.text = alarm.name
        objectAddress.text = alarm.address
        isCancelable = false
        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context?.applicationContext?.packageName + "/" + R.raw.alarm_sound)
        if (activeSound) {
            alertSound = MediaPlayer.create(context, soundUri)
            alertSound?.start()
        }

        applyAlarm.setOnClickListener {
            callback?.applyAlarm(alarm)
            if (activeSound)
                alertSound?.stop()

            val notificationManager: NotificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()

            dismiss()
        }

        return builder
            .setView(rootView)
            .create()
    }

    override fun onResume() {
        super.onResume()
        isAlive = true
    }

    override fun onDestroy() {
        isAlive = false
        isCancelable = true
        if (activeSound)
            alertSound?.stop()
        super.onDestroy()
    }
}