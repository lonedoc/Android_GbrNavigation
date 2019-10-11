package newVersion.common.status

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatDialogFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kobramob.rubeg38.ru.gbrnavigation.R

class StatusFragment : MvpAppCompatDialogFragment(), StatusView {

    override fun showToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @InjectPresenter
    lateinit var presenter: StatusPresenter

    companion object {
        fun newInstance(time: Long): StatusFragment {
            val frag = StatusFragment()
            val args = Bundle()
            args.putLong("time", time)
            frag.arguments = args
            return frag
        }
    }
    var rootView: View? = null
    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        val builder = AlertDialog.Builder(context)
        val layoutInflater: LayoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = layoutInflater.inflate(R.layout.fragment_timer_dialog, null)

        presenter.setTimer(arguments?.getLong("time")!!)

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