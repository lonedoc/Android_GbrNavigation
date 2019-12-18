package newVersion.common.alarm

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
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
import kotlinx.android.synthetic.main.activity_login.*
import newVersion.callback.AlarmCallback
import newVersion.utils.Alarm
import java.lang.IllegalStateException

class NewAlarmDialogFragment:DialogFragment() {

    var rootView: View? = null
    companion object{
        var callback: AlarmCallback? = null
        fun newInstance(alarm:Alarm,callback:AlarmCallback):NewAlarmDialogFragment{
            this.callback = callback
            val frag = NewAlarmDialogFragment()
            val args = Bundle()
            args.putSerializable("info",alarm)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater: LayoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = layoutInflater.inflate(R.layout.fragment_alarm_dialog, null)

        val alarm = arguments?.getSerializable("info") as Alarm
        val applyAlarm: Button = rootView?.findViewById(R.id.apply_alarm) as Button
        val objectName = rootView?.findViewById(R.id.object_name) as TextView
        val objectAddress = rootView?.findViewById(R.id.object_address) as TextView
        objectName.text = alarm.name
        objectAddress.text = alarm.address
        isCancelable = false

        applyAlarm.setOnClickListener{
            callback?.applyAlarm(alarm)
            dismiss()
        }

        val builder = AlertDialog.Builder(context)
        return builder
            .setView(rootView)
            .create()
    }
}