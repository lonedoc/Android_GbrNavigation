package newVersion.common.serverSetting

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import newVersion.login.LoginActivity
import newVersion.servicess.NetworkService

class ServerSettingFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        isCancelable = false
        return builder
            .setMessage("Вы хотите сменить настройки сервера?")
            .setPositiveButton("Да") { dialog, s ->
                stopService()
                openLoginScreen()
                dismiss()
            }
            .setNegativeButton("Нет") { dialog, s ->
                dismiss()
            }
            .create()
    }

    private fun openLoginScreen() {
        val loginActivity = Intent(context, LoginActivity::class.java)
        startActivity(loginActivity)
    }

    private fun stopService() {
        if (!NetworkService.isServiceStarted) return

        val stopServiceIntent = Intent(context, NetworkService::class.java)
        context?.stopService(stopServiceIntent)
    }
}