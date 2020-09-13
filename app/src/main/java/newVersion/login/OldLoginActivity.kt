package newVersion.login

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
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlin.concurrent.thread
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_new_login.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import newVersion.common.CommonActivity
import newVersion.login.resource.AdapterIpAddress
import newVersion.login.resource.ProgressDialog
import newVersion.models.HostPool
import newVersion.models.Preferences
import newVersion.servicess.NetworkService
import newVersion.servicess.NotificationService
import newVersion.utils.PrefsUtil
import ru.rubeg38.technicianmobile.utils.setOnTextChanged

class OldLoginActivity : MvpAppCompatActivity(), OldLoginView {

    @InjectPresenter
    lateinit var presenter: OldLoginPresenter

    private val progressDialog = ProgressDialog()
    lateinit var adapter: AdapterIpAddress

    companion object {
        var isAlive = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        imeiTextView.setText(getImei())

        imeiTextView.setOnTextChanged { str -> presenter.validateImei(str?.toString()) }

        portTextView.setOnTextChanged { str -> presenter.validatePort(str?.toString()) }

        portTextView.setOnFocusChangeListener { _, hasFocus ->
            val port = portTextView.text.toString()

            if (hasFocus) presenter.validatePort(port)
        }

        login_button.setOnClickListener { this.onClickLoginButton() }

        add?.setOnClickListener {
            adapter.addItem()
        }

        remove?.setOnClickListener {
            adapter.removeItem()
        }

        if (!presenter.isInit()) {
            val preferences: Preferences = PrefsUtil(this)
            presenter.init(preferences)
        }
    }

    private fun onClickLoginButton() {
        thread {
            if (adapter.getAddresses() != null) {
                val address = adapter.getAddresses()
                val port = portTextView.text.toString()
                val imei = imeiTextView.text.toString()
                presenter.submit(address, port, imei)
            } else {
                Log.d("ActivityLogin", "Enter return")
                return@thread
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

    override fun onResume() {
        super.onResume()
        isAlive = true
    }

    override fun addItem(address: java.util.ArrayList<String>) {
        address.add("")
        adapter.notifyItemInserted(address.size - 1)
    }

    override fun setImeiTextViewError(error: String?) {
        imeiTextView.error = error
    }

    override fun removeItem(indexItem: Int, size: Int) {
        try {
            recyclerView?.removeViewAt(indexItem)
            adapter.notifyItemRemoved(indexItem)
            adapter.notifyItemRangeRemoved(indexItem, size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
    }

    override fun setAddress(address: ArrayList<String>?) {
        if (address?.count()!! <1) {
            address.add("")
        }

        if (address.count() <2) {
            visibilityRemoveButton(View.GONE)
        }

        adapter = AdapterIpAddress(presenter, address)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = adapter
    }

    override fun setPort(port: String?) {
        portTextView.setText(port)
    }

    override fun visibilityAddButton(View: Int) {
        add?.visibility = View
    }

    override fun visibilityRemoveButton(View: Int) {
        remove?.visibility = View
    }

    override fun setPortTextViewError(message: String?) {
        runOnUiThread {
            portInputLayout.error = message
        }
    }

    override fun showToastMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun startService(credentials: newVersion.models.Credentials, hostPool: HostPool) {
        runOnUiThread {
            if (NetworkService.isServiceStarted) return@runOnUiThread

            val intent = Intent(this, NetworkService::class.java)

            val bundle = Bundle()
            bundle.putSerializable("credentials", credentials)
            bundle.putSerializable("hostPool", hostPool)
            bundle.putString("command", "start")
            intent.putExtras(bundle)

            startService(intent)
        }
    }

    override fun disconnectServer() {
        val remoteMessage1 = RemoteMessage.Builder("Status")
            .addData("command", "serverNotResponse")
            .build()
        NotificationService.createNotification(remoteMessage1, applicationContext)
    }

    override fun showDialog() {

        runOnUiThread {
            progressDialog.show(supportFragmentManager, "ProgressDialog")
        }
    }

    override fun closeDialog() {
        runOnUiThread {
            progressDialog.dismiss()
        }
    }

    override fun openCommonScreen() {

        val remoteMessage1 = RemoteMessage.Builder("Status")
            .addData("command", "connectServer")
            .build()
        NotificationService.createNotification(remoteMessage1, applicationContext)

        runOnUiThread {
            val intent = Intent(this, CommonActivity::class.java)
            presenter.onDestroy()
            startActivity(intent)

            Log.d("LoginActivity", "RegisterSuccess")
        }
    }

    override fun onBackPressed() {
    }
}