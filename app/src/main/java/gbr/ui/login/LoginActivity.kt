package gbr.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import gbr.presentation.presenter.login.LoginPresenter
import gbr.presentation.view.login.LoginView
import gbr.ui.main.MainActivity
import gbr.utils.PrefsUtils
import gbr.utils.adapters.login.AdapterIpAddresses
import gbr.utils.callbacks.ValidateAddress
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_new_login.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import newVersion.common.CommonActivity
import newVersion.login.resource.ProgressDialog
import ru.rubeg38.technicianmobile.utils.setOnTextChanged
import java.util.ArrayList
import kotlin.concurrent.thread

class LoginActivity:MvpAppCompatActivity(),LoginView,ValidateAddress {

    @InjectPresenter
    lateinit var presenter:LoginPresenter

    lateinit var adapter:AdapterIpAddresses

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_login)

        new_login_port.setOnTextChanged { str -> presenter.validatePort(str.toString()) }

        new_login_port.setOnFocusChangeListener { _, hasFocus ->
            val port = new_login_port.text.toString()
            if (hasFocus) presenter.validatePort(port)
        }
        new_login_imei.setOnTextChanged { str -> presenter.validateImei(str.toString()) }

        new_login_imei.setOnFocusChangeListener { _, hasFocus ->
            val imei = new_login_imei.text.toString()
            if (hasFocus) presenter.validateImei(imei)
        }

        new_login_enter.setOnClickListener {
            val port = new_login_port.text.toString()
            val imei = new_login_imei.text.toString()
            val addresses = adapter.getAddresses()

            thread{
                presenter.submit(addresses,port,imei)
            }

        }
    }

    override fun onStart() {
        super.onStart()
        val prefUtils = PrefsUtils(this)
        presenter.init(prefUtils)
    }

    fun onClickListener(view:View){
        when(view.id){
            R.id.new_login_add->{
                val count = adapter.addItem()
                presenter.visibility(count)
                adapter.notifyItemInserted(count)
            }
            R.id.new_login_remove->{
                val count = adapter.removeItem()
                presenter.visibility(count)
                adapter.notifyItemRemoved(count)
                adapter.notifyItemRangeRemoved(count,count + 1)
            }
        }
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

    override fun validateAddress(holder: AdapterIpAddresses.ViewHolder, address: String) {
        presenter.validateAddress(holder,address)
    }

    override fun setAddress(address: ArrayList<String>) {
        adapter = AdapterIpAddresses(address,this)
        new_login_ips.layoutManager = LinearLayoutManager(this)
        new_login_ips.adapter = adapter
    }

    override fun setImei() {
        new_login_imei.setText(getImei())
    }

    override fun setImeiTextViewError(message: String?) {
        runOnUiThread {
            textInputImei.error = message
        }
    }

    override fun setPort(port:String) {
        new_login_port.setText(port)
    }

    override fun setPortTextViewError(message: String?) {
        runOnUiThread {
            textInputPort.error = message
        }
    }

    override fun visibilityAddButton(visibility: Int) {
        new_login_add.visibility = visibility
    }

    override fun visibilityRemoveButton(visibility: Int) {
        new_login_remove.visibility = visibility
    }

    override fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
        }
    }

    private val progressDialog = ProgressDialog()

    override fun mainActivity() {
        val main = Intent(this, MainActivity::class.java)
        startActivity(main)
    }

    override fun showDialog() {
        runOnUiThread {
            progressDialog.show(supportFragmentManager,"ProgressDialog")
        }
    }

    override fun closeDialog() {
        runOnUiThread {
            progressDialog.dismiss()
        }
    }
}