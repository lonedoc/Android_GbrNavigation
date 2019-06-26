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
import android.view.WindowManager
import android.widget.Toast
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.service.*
import java.lang.Exception
import java.util.jar.Manifest

class LoginActivity : AppCompatActivity() {

    private val request: Request = Request()
    private val pollingServer: PollingServer = PollingServer()
    private val networkService: NetworkService = NetworkService()
    private val cooder: Coder = Coder()
    lateinit var imei: String
    lateinit var ipInput: TextInputEditText
    lateinit var portInput: TextInputEditText

    companion object {
        var firstStart = false
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

                registerThread()
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

                    val clean = getSharedPreferences("state", Context.MODE_PRIVATE).edit()
                    clean.remove("tid")
                    clean.apply()

                    registerThread()
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
            firstStart = true
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
                SharedPreferencesState.init(this@LoginActivity)
                SharedPreferencesState.addPropertyString(
                    "ip",
                    ipInput.text.toString()
                )

                SharedPreferencesState.addPropertyInt(
                    "port",
                    portInput.text.toString().toInt()
                )

                pollingServer.initSocket(
                    ipInput.text.toString(),
                    portInput.text.toString().toInt()
                )

                val intent = Intent(this@LoginActivity, PollingServer::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(intent)
                } else {
                    this.startService(intent)
                }

                val registerThread = Runnable {
                    request.register(PollingServer.socket, PollingServer.countSender, 0, imei, ipInput.text.toString(), portInput.text.toString().toInt(), "")
                    PollingServer.countSender++
                    PollingServer.serverAlive = true
                }; Thread(registerThread).start()
            }
        }
    }

    private fun registerThread() {

        val registerThread = Runnable {
            val intent = Intent(this@LoginActivity, PollingServer::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            /* val message = JSONObject()
             message.put("\$c$", "reg")
             message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
             message.put("password", "861111033192520")

             val handler: ResultHandler = { success, bytes ->
                 if(success){
                     Log.d("SendLoop","PacketSend")
                 }
             }
             networkService.initSocket(
                 getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", "")!!,
                 getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 9010)
             )
             networkService.send(
                message.toString().toByteArray(),
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", "")!!,
                false,handler)*/

            pollingServer.initSocket(getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", "")!!, getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 0))
            request.register(
                PollingServer.socket,
                PollingServer.countSender,
                0,
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", "")!!,
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", "")!!,
                getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 0),
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", "")!!
            )

            PollingServer.countSender++
        }; Thread(registerThread).start()
    }

    private lateinit var br: BroadcastReceiver
    private fun broadcastReceiver() {
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val info = intent?.getStringExtra("info")
                val accessDenied = intent?.getBooleanExtra("accessDenied", false)
                if (accessDenied!!) {
                    closeProgressBar()
                    Toast.makeText(this@LoginActivity, "Данное устройство не зарегистрировано на этом сервере", Toast.LENGTH_LONG).show()
                } else if (!firstStart) {

                    val jsonObject = JSONObject(info)

                    SharedPreferencesState.init(this@LoginActivity)
                    SharedPreferencesState.addPropertyString(
                        "tid",
                        jsonObject.getString("tid")
                    )
                    PollingServer.register = true
                    try {
                        if (jsonObject.getString("namegbr") !=
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "")
                        ) {
                            SharedPreferencesState.addPropertyString(
                                "namegbr",
                                jsonObject.getString("namegbr")
                            )
                        }
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
                } else {
                    println("FirstStart")
                    val jsonObject = JSONObject(info)

                    SharedPreferencesState.init(this@LoginActivity)

                    SharedPreferencesState.addPropertyString(
                        "imei",
                        imei
                    )

                    SharedPreferencesState.addPropertyString(
                        "tid",
                        jsonObject.getString("tid")
                    )
                    PollingServer.register = true
                    try {
                        SharedPreferencesState.addPropertyString(
                            "routeserver",
                            jsonObject.getJSONArray("routeserver").getString(0)
                        )
                        SharedPreferencesState.addPropertyString(
                            "namegbr",
                            jsonObject.getString("namegbr")
                        )

                        SharedPreferencesState.addPropertyString(
                            "call",
                            jsonObject.getString("call")
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    SharedPreferencesState.addPropertyString(
                        "ip",
                        ipInput.text.toString()
                    )

                    SharedPreferencesState.addPropertyInt(
                        "port",
                        portInput.text.toString().toInt()
                    )

                    closeProgressBar()
                    startActivity(Intent(this@LoginActivity, StartActivity::class.java))
                    firstStart = false
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
            unregisterReceiver(br)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this@LoginActivity, PollingServer::class.java)
        stopService(intent)

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
