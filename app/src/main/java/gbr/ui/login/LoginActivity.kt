package gbr.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import gbr.presentation.presenter.login.LoginPresenter
import gbr.presentation.view.login.LoginView
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_new_login.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import ru.rubeg38.technicianmobile.utils.setOnTextChanged

class LoginActivity:MvpAppCompatActivity(),LoginView {

    @InjectPresenter
    lateinit var presenter:LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_login)
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun getImei():String{
        val telephonyMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return when{
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ->{
                ""
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->{
                telephonyMgr.imei
            }
            else ->{
                telephonyMgr.deviceId
            }
        }
    }

    override fun setImei() {
        new_login_imei.setText(getImei())
    }

    override fun setPort(port:String) {
        new_login_port.setText(port)
    }

}