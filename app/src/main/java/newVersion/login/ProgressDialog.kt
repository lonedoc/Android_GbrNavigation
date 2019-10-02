package newVersion.login

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import kobramob.rubeg38.ru.gbrnavigation.R
import ru.rubeg38.rubegprotocol.RubegProtocol

class ProgressDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val layoutInflater: LayoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView = layoutInflater.inflate(R.layout.progress_bar, null)
        val protocol = RubegProtocol.sharedInstance
        return builder
            .setView(rootView)
            .setPositiveButton("Отмена") { dialog, s ->
                protocol.stop()
            }
            .setCancelable(false)
            .create()
    }
}