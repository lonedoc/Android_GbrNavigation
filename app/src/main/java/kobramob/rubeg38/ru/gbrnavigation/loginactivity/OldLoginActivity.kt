package kobramob.rubeg38.ru.gbrnavigation.loginactivity

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.telephony.TelephonyManager
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import java.lang.Exception
import java.lang.Thread.sleep
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.service.*
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import kotlin.concurrent.thread
import org.json.JSONObject

class OldLoginActivity : AppCompatActivity() {

    lateinit var imei: String
    lateinit var ipInput: TextInputEditText
    lateinit var portInput: TextInputEditText

    companion object {
        var Alive = false
    }
    private var progressBarOpen: String = "close"

    private lateinit var cancelDialog: AlertDialog

    private var permissionGranted = PackageManager.PERMISSION_GRANTED

    @SuppressLint("InflateParams")
    private fun showProgressBar() {
        if (progressBarOpen == "close") {
            val dialog = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.progress_bar, null)
            dialog.setView(dialogView)
            dialog.setCancelable(false)
            dialog.setPositiveButton("Отмена") { dialog, which ->
                dialog.cancel()
                progressBarOpen = "close"
                Alive = false
                val startService = Intent(this, NetworkService::class.java)
                stopService(startService)

                Toast.makeText(this, "Возможно произошел обрыв соединения с сервером, попробуйте войти снова", Toast.LENGTH_LONG).show()
            }
            cancelDialog = dialog.create()
            cancelDialog.show()
            progressBarOpen = "open"
        }
    }

    private fun closeProgressBar() {
        if (progressBarOpen == "open") {
            cancelDialog.cancel()
            progressBarOpen = "close"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oldlogin)
    }

    /*@SuppressLint("HardwareIds")
    override fun onStart() {
        super.onStart()
        Alive = true

        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CALL_PHONE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WAKE_LOCK) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.FOREGROUND_SERVICE) != permissionGranted
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CALL_PHONE,
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.WAKE_LOCK
                ),
                permissionGranted
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(

                        android.Manifest.permission.FOREGROUND_SERVICE
                    ),
                    permissionGranted
                )
            }
        } else {
            val telephonyMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            imei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyMgr.imei
            } else {
                telephonyMgr.deviceId
            }

            enterToApp()
        }
    }

    @SuppressLint("HardwareIds")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when {
            grantResults.isNotEmpty() && grantResults[3] == permissionGranted -> {
                checkPermission()

                val telephonyMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                imei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    telephonyMgr.imei
                } else {
                    telephonyMgr.deviceId
                }

                enterToApp()
            }
        }
    }

    private fun enterToApp() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= 21) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        if (getSharedPreferences("state", Context.MODE_PRIVATE).contains("ip") &&
            getSharedPreferences("state", Context.MODE_PRIVATE).contains("port") &&
            getSharedPreferences("state", Context.MODE_PRIVATE).contains("imei")
        ) {
            showProgressBar()

            ipInput = findViewById(R.id.ipInputText)
            portInput = findViewById(R.id.portInputText)

            val clean = getSharedPreferences("state", Context.MODE_PRIVATE).edit()
            clean.remove("tid")
            clean.apply()

            authorization()
        }

        ipInput = findViewById(R.id.ipInputText)
        portInput = findViewById(R.id.portInputText)

        val buttonEnterToApp: Button = findViewById(R.id.enterToApp)

        setOnClickListenerEnterToApp(buttonEnterToApp, imei)
    }

    private fun setOnClickListenerEnterToApp(buttonEnterToApp: Button, imei: String) {

        buttonEnterToApp.setOnClickListener {
            showProgressBar()

            if (ipInput.text.toString() == "" || portInput.text.toString() == "") {
                closeProgressBar()
                val dataNotEntered = AlertDialog.Builder(this)
                dataNotEntered.setTitle("Данные не введены")
                    .setMessage("Необходимо заполнить все поля")
                    .setCancelable(false)
                    .setPositiveButton("Ок") { _, _ ->
                        ipInput.setText("")
                        portInput.setText("")
                    }.show()
            } else {
                registration(ipInput.text.toString(), portInput.text.toString().toInt(), imei)
            }
        }
    }

    private fun registration(ip: String, port: Int, imei: String) {

        val startService = Intent(this, NetworkService::class.java)

        networkService.initSocket(
            ip,
            port
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startService)
        } else {
            startService(startService)
        }

        val myLocation = MyLocation()
        myLocation.initLocation(this)

        val message = JSONObject()
        message.put("\$c$", "reg")
        message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        message.put("password", imei)

        Alive = true

        receiver(ip, port, imei)

        networkService.request(message.toString(), sessionID = null) {
            success: Boolean, data: ByteArray? ->
                if (success) {
                    Log.d("Registration", "success")
                    if (data != null) {
                        val jsonObject = JSONObject(String(data))

                        SharedPreferencesState.init(this@OldLoginActivity)

                        SharedPreferencesState.addPropertyString(
                            "ip",
                            ip
                        )

                        SharedPreferencesState.addPropertyInt(
                            "port",
                            port
                        )

                        SharedPreferencesState.addPropertyString(
                            "imei",
                            imei
                        )

                        SharedPreferencesState.addPropertyString(
                            "tid",
                            jsonObject.getString("tid")
                        )

                        networkService.initData(jsonObject.getString("tid"), imei, this)

                        try {
                            SharedPreferencesState.addPropertyString(
                                "routeserver",
                                jsonObject.getJSONArray("routeserver").getString(0)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            SharedPreferencesState.addPropertyString(
                                "namegbr",
                                jsonObject.getString("namegbr")
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            SharedPreferencesState.addPropertyString(
                                "call",
                                jsonObject.getString("call")
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                SharedPreferencesState.addPropertyString(
                                    "call",
                                    ""
                                )
                                Toast.makeText(this, "Данная группа не была поставлена на дежурство в дежурном операторе", Toast.LENGTH_LONG).show()
                            }
                        }

                        closeProgressBar()
                        startActivity(Intent(this@OldLoginActivity, StartActivity::class.java))
                    }
                } else {
                    runOnUiThread {
                        Log.d("Registration", "failed")
                        closeProgressBar()

                        val startService = Intent(this, NetworkService::class.java)
                        stopService(startService)

                        Toast.makeText(this@OldLoginActivity, "Нет соединения с сервером", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun authorization() {

        val startService = Intent(this, NetworkService::class.java)

        networkService.initSocket(
            getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", "")!!,
            getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startService)
        } else {
            startService(startService)
        }

        val myLocation = MyLocation()
        myLocation.initLocation(this)

        ipInput.setText(getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", "")!!)
        portInput.setText(getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010).toString())

        val message = JSONObject()
        message.put("\$c$", "reg")
        message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        message.put("password", imei)

        Alive = true
        receiver(
            getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", "")!!,
            getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010), imei
        )

        networkService.send(message = message.toString(), sessionID = null) { success: Boolean ->
            if (success) {
                Log.d("Authorization", "success")
            }
        }
    }

    private fun receiver(ip: String, port: Int, imei: String) {
        thread {
            while (Alive) {
                if (NetworkService.stringMessageBroker.count()> 0) {
                    for (i in 0 until NetworkService.stringMessageBroker.count()) {
                        SharedPreferencesState.init(this@OldLoginActivity)
                        Log.d("LoginReceiver", NetworkService.stringMessageBroker[i])
                        val length = NetworkService.stringMessageBroker.count()
                        val jsonMessage = JSONObject(NetworkService.stringMessageBroker[i])

                        when (jsonMessage.getString("command")) {
                            "regok" -> {
                                SharedPreferencesState.init(this@OldLoginActivity)

                                SharedPreferencesState.addPropertyString(
                                    "ip",
                                    ip
                                )

                                SharedPreferencesState.addPropertyInt(
                                    "port",
                                    port
                                )

                                SharedPreferencesState.addPropertyString(
                                    "imei",
                                    imei
                                )

                                SharedPreferencesState.addPropertyString(
                                    "tid",
                                    jsonMessage.getString("tid")
                                )

                                networkService.initData(jsonMessage.getString("tid"), imei, this)

                                try {
                                    SharedPreferencesState.addPropertyString(
                                        "routeserver",
                                        jsonMessage.getJSONArray("routeserver").getString(0)
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                try {
                                    SharedPreferencesState.addPropertyString(
                                        "namegbr",
                                        jsonMessage.getString("namegbr")
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                try {
                                    SharedPreferencesState.addPropertyString(
                                        "call",
                                        jsonMessage.getString("call")
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    runOnUiThread {
                                        SharedPreferencesState.addPropertyString(
                                            "call",
                                            ""
                                        )
                                        Toast.makeText(this, "Данная группа не была поставлена на дежурство в дежурном операторе", Toast.LENGTH_LONG).show()
                                    }
                                }
                                runOnUiThread {
                                    closeProgressBar()

                                    try {
                                        if (NetworkService.stringMessageBroker.count()> 0) {
                                            NetworkService.stringMessageBroker.removeAt(i)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                    startActivity(Intent(this@OldLoginActivity, StartActivity::class.java))
                                }
                            }
                            "disconnected" -> {
                                runOnUiThread {
                                    closeProgressBar()
                                    Alive = false
                                    val startService = Intent(this, NetworkService::class.java)
                                    stopService(startService)
                                    Toast.makeText(this, "Соединение с сервером не найдено", Toast.LENGTH_LONG).show()
                                    if (NetworkService.stringMessageBroker.count()> 0)
                                        NetworkService.stringMessageBroker.removeAt(i)
                                }
                            }
                        }
                        if (length> NetworkService.stringMessageBroker.count())
                            break
                    }
                }
                sleep(100)
            }
        }
    }

    override fun onBackPressed() {
    }

    override fun onStop() {
        super.onStop()
        Alive = false

        try {
            closeProgressBar()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CALL_PHONE) != permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WAKE_LOCK) != permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) != permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.FOREGROUND_SERVICE) != permissionGranted

        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CALL_PHONE,
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.WAKE_LOCK
                ),
                permissionGranted
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(

                        android.Manifest.permission.FOREGROUND_SERVICE
                    ),
                    permissionGranted
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val startService = Intent(this, NetworkService::class.java)
        stopService(startService)
    }*/
}
