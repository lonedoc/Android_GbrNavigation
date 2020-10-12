package gbr.ui.status

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import gbr.presentation.presenter.status.StatusPresenter
import gbr.presentation.view.status.StatusView
import gbr.utils.data.Info
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatDialogFragment
import moxy.presenter.InjectPresenter

class StatusFragment : MvpAppCompatDialogFragment(), StatusView {

    override fun showToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @InjectPresenter
    lateinit var presenter: StatusPresenter


    var rootView: View? = null
    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        val builder = AlertDialog.Builder(context)
        val layoutInflater: LayoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = layoutInflater.inflate(R.layout.fragment_timer_dialog, null)

        var time:Long = 0
        for (i in 0 until Info.statusList!!.count())
        {
            if(Info.statusList!![i].status == Info.status)
            {
                time = Info.statusList!![i].time.toLong()
            }
        }
        presenter.setTimer(time)

        isCancelable = false
        return builder
            .setView(rootView)
            .setPositiveButton("Завершить") { dialog, s ->
                presenter.sendRequest()
                presenter.onDestroy()
                dismiss()
            }
            .create()
    }

    override fun updateTimer(time: Int) {
        val hours = time / 3600
        val minute = (time % 3600) / 60
        val seconds = time % 60

        val statusTimer: TextView = rootView?.findViewById(R.id.status_timer)!!
        val timeRemains = "$hours:$minute:$seconds"
        statusTimer.text = timeRemains
    }

    override fun onDismiss() {
        dismiss()
    }
}