package kobramob.rubeg38.ru.gbrnavigation.loginactivity

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.telephony.TelephonyManager
import android.widget.Button
import android.os.Build
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import org.json.JSONObject
import android.os.StrictMode
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.service.*
import org.greenrobot.eventbus.Subscribe
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private val request: Request = Request()
    private val networkService: NetworkService = NetworkService()
    private val coder = Coder()

    private lateinit var serviceConnection: ServiceConnection

    lateinit var imei: String
    lateinit var ipInput: TextInputEditText
    lateinit var portInput: TextInputEditText

    companion object {
        var Alive = false
        const val BROADCAST_ACTION = "kobramob.ruber38.ru.gbrnavigation.loginactivity"
    }
    private var progressBarOpen: String = "close"
    private lateinit var cancelDialog: AlertDialog

    @SuppressLint("InflateParams")
    private fun showProgressBar() {
        if (progressBarOpen == "close") {
            val dialog = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.progress_bar, null)
            dialog.setView(dialogView)
            dialog.setCancelable(false)
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
        setContentView(R.layout.activity_login)

    }

    private var permissionGranted = PackageManager.PERMISSION_GRANTED

    @SuppressLint("HardwareIds")
    override fun onStart() {
        super.onStart()
        Alive = true
        // checkPermission()
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED
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

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= 21) {
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
            }
            broadcastReceiver()

            if (getSharedPreferences("state", Context.MODE_PRIVATE).contains("ip") &&
                getSharedPreferences("state", Context.MODE_PRIVATE).contains("port") &&
                getSharedPreferences("state", Context.MODE_PRIVATE).contains("imei")
            ) {
                showProgressBar()

                val clean = getSharedPreferences("state", Context.MODE_PRIVATE).edit()
                clean.remove("tid")
                clean.apply()

                ipInput = findViewById(R.id.ipInputText)
                portInput = findViewById(R.id.portInputText)

                authorization()

            }

            ipInput = findViewById(R.id.ipInputText)
            portInput = findViewById(R.id.portInputText)

            val buttonEnterToApp: Button = findViewById(R.id.enterToApp)

            setOnClickListenerEnterToApp(buttonEnterToApp, imei)
        }
    }

    @SuppressLint("HardwareIds")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when {
            grantResults.isNotEmpty() && grantResults[3] == PackageManager.PERMISSION_GRANTED -> {
                checkPermission()
                val telephonyMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                imei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    telephonyMgr.imei
                } else {
                    telephonyMgr.deviceId
                }

                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                if (Build.VERSION.SDK_INT >= 21) {
                    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                    StrictMode.setThreadPolicy(policy)
                }

                broadcastReceiver()

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
        }
    }

    private fun setOnClickListenerEnterToApp(buttonEnterToApp: Button, imei: String) {

        buttonEnterToApp.setOnClickListener {
            showProgressBar()

            if (ipInput.text.toString() == "" || portInput.text.toString() == "") {
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
        networkService.initSocket(
            ip,
            port
        )

        networkService.startService()

        val message = JSONObject()
        message.put("\$c$", "reg")
        message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        message.put("password", imei)

        networkService.request(message.toString(), sessionID = null) {
            success: Boolean, data: ByteArray? ->
                if (success) {
                    if (data != null) {
                        val jsonObject = JSONObject(String(data))

                        SharedPreferencesState.init(this@LoginActivity)

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

                        networkService.initData(jsonObject.getString("tid"), imei,this)

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
                        }

                        closeProgressBar()
                        startActivity(Intent(this@LoginActivity, StartActivity::class.java))
                    }
                }
            else
                {
                    Log.d("Registration","RegistrationFailed")
                }
            }
    }

    @Subscribe
    private fun authorization() {

        ipInput.setText(getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", "")!!)
        portInput.setText(getSharedPreferences("state",Context.MODE_PRIVATE).getInt("port",0).toString())

        networkService.initSocket(
            getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", "")!!,
            getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 0)
        )

        networkService.startService()

        val message = JSONObject()
        message.put("\$c$", "reg")
        message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        message.put("password", imei)

        networkService.request(message = message.toString(), sessionID = null) { success: Boolean, data: ByteArray? ->
            if (success) {
                Log.d("Authorization", "success")

                if (data != null) {

                    Log.d("Authorization",String(data))
                    val jsonObject = JSONObject(String(data))

                    SharedPreferencesState.init(this@LoginActivity)
                    SharedPreferencesState.addPropertyString(
                        "tid",
                        jsonObject.getString("tid")
                    )
                    networkService.initData(jsonObject.getString("tid"),imei,this)
                    try {
                        if (jsonObject.getString("namegbr") !=
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "")
                        ) {
                            SharedPreferencesState.addPropertyString(
                                "namegbr",
                                jsonObject.getString("namegbr")
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        if (jsonObject.getString("call") !=
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("call", "")
                        ) {
                            SharedPreferencesState.addPropertyString(
                                "call",
                                jsonObject.getString("call")
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    closeProgressBar()
                    startActivity(Intent(this@LoginActivity, StartActivity::class.java))
                }
            } else
                Log.d("Authorization", "failed")
        }
    }

    private lateinit var br: BroadcastReceiver
    private fun broadcastReceiver() {
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val connectionLost = intent?.getBooleanExtra("connectionLost",false)
                if(connectionLost != null){
                    if(connectionLost){
                        closeProgressBar()

                        Toast.makeText(this@LoginActivity,"Не удается установить связь с сервером",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        val intentFilter = IntentFilter(BROADCAST_ACTION)
        registerReceiver(br, intentFilter)
    }

    override fun onBackPressed() {
    }

    override fun onStop() {
        super.onStop()
        try {
            Alive = false
            unregisterReceiver(br)
            cancelDialog.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val clean = getSharedPreferences("state", Context.MODE_PRIVATE).edit()
        clean.remove("tid")
        clean.apply()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED

        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CALL_PHONE,
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.WAKE_LOCK,
                    android.Manifest.permission.FOREGROUND_SERVICE
                ),
                101
            )
        }
    }
}
