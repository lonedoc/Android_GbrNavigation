package kobramob.rubeg38.ru.gbrnavigation.mainactivity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import java.lang.Thread.sleep
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.resource.DataBase
import kobramob.rubeg38.ru.gbrnavigation.resource.SPGbrNavigation
import kobramob.rubeg38.ru.gbrnavigation.workservice.MessageEvent
import kobramob.rubeg38.ru.gbrnavigation.workservice.MyLocation
import kobramob.rubeg38.ru.gbrnavigation.workservice.RubegNetworkService
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    companion object {
        var isAlive = false
    }

    private var register = false
    private var authorization = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView: ImageView = findViewById(R.id.main_icon)
        val progressBar: ProgressBar = findViewById(R.id.main_progressBar)
        val textView: TextView = findViewById(R.id.main_text)

        if (savedInstanceState != null) {
            progressBar.visibility = View.VISIBLE
            textView.visibility = View.VISIBLE
            if (!register && !authorization) {
                checkData()
            }
        } else {
            YoYo.with(Techniques.FadeIn)
                .duration(2500)
                .playOn(imageView)
            thread {
                sleep(2000)
                runOnUiThread {
                    progressBar.visibility = View.VISIBLE
                    textView.visibility = View.VISIBLE

                    checkData()
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun checkData() {
        if (getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("ip") &&
            getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("port") &&
            getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("imei")
        ) {
            authorization()

            Toast.makeText(this, "Авторизация", Toast.LENGTH_SHORT).show()
        } else {

            registration()

            Toast.makeText(this, "Регистрация", Toast.LENGTH_SHORT).show()
        }
    }

    private fun authorization() {
        authorization = true
        val myLocation = MyLocation()
        myLocation.initLocation(this)

        thread {
            var longitude = 0.0

            while (longitude == 0.0) {
                try {
                    longitude = MyLocation.imHere!!.longitude
                } catch (e: Exception) {
                }
            }

            val ip: ArrayList<String> = ArrayList()
            ip.add(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!)

            val service = Intent(this, RubegNetworkService::class.java)
            service.putExtra("command", "start")
            service.putStringArrayListExtra("ip", ip)
            service.putExtra("port", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service)
            } else {
                startService(service)
            }
            thread {
                sleep(2000)
                val authorizationMessage = JSONObject()
                authorizationMessage.put("\$c$", "reg")
                authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                authorizationMessage.put(
                    "password",
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
                )
                RubegNetworkService.protocol.send(authorizationMessage.toString()) { success: Boolean ->
                    if (success) {
                        runOnUiThread {
                            Toast.makeText(this, "Авторизация прошла успешно", Context.MODE_PRIVATE).show()
                        }
                    } else {
                        runOnUiThread {
                            val ipList: ArrayList<String> = ArrayList()
                            ipList.add(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!)
                            service.putExtra("command", "stop")
                            service.putStringArrayListExtra("ip", ipList)
                            service.putExtra("port", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(service)
                            } else {
                                startService(service)
                            }

                            Toast.makeText(this, "Приложение не смогло авторизироваться на сервере", Toast.LENGTH_LONG).show()
                            val loginActivity = Intent(this, LoginActivity::class.java)
                            loginActivity.putExtra("imei", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                            startActivity(loginActivity)
                        }
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.command) {
            "regok" -> {
                Log.d(
                    "Authorization",
                    "\n " + " " + "\n routeServer ${event.routeServer} \n call ${event.call} \n status ${event.status} \n gbrStatus ${event.gbrStatus}"
                )
                val dbHelper = DataBase(this)
                val db = dbHelper.writableDatabase

                val cursorRoute = db.query("RouteServerList", null, null, null, null, null, null)
                if (cursorRoute.count == 0) {
                    val cv = ContentValues()
                    for (i in 0 until event.routeServer.count()) {
                        cv.put("ip", event.routeServer[i])
                        Log.d("DataBase", "id = " + db.insert("RouteServerList", null, cv))
                    }
                }
                cursorRoute.close()

                val cursorStatus = db.query("StatusList", null, null, null, null, null, null)
                if (cursorStatus.count == 0) {
                    val cv = ContentValues()
                    for (i in 0 until event.gbrStatus.count()) {
                        cv.put("status", event.gbrStatus[i])
                        Log.d("DataBase", "id = " + db.insert("StatusList", null, cv))
                    }
                }
                cursorStatus.close()

                SPGbrNavigation.init(this)
                if(event.call!="")
                    SPGbrNavigation.addPropertyString("call", event.call)
                else
                {
                    SPGbrNavigation.addPropertyString("call", "")
                    Toast.makeText(this,"Группа не была поставлена на дежурство в дежурном операторе",Toast.LENGTH_SHORT).show()
                }
                if(event.status!=""){
                    SPGbrNavigation.addPropertyString("status",event.status)
                }
                else
                {
                    SPGbrNavigation.addPropertyString("status", "")
                }

                if(event.routeServer.count()>0)
                    SPGbrNavigation.addPropertyString("routeserver", event.routeServer[0])
                else
                    SPGbrNavigation.addPropertyString("routeserver", "91.189.160.38:5000")

                if(EventBus.getDefault().isRegistered(this))
                    EventBus.getDefault().unregister(this)

                val intent = Intent(this@MainActivity, CommonActivity::class.java)
                intent.putExtra("status", event.status)
                startActivity(intent)

            }
            "disconnect" -> {
                if (event.message == "lost") {
                    // Dialog
                    val service = Intent(this, RubegNetworkService::class.java)
                    Toast.makeText(this, "Нет соединения с сервером", Toast.LENGTH_LONG).show()
                    stopService(service)
                    val loginActivity = Intent(this, LoginActivity::class.java)
                    loginActivity.putExtra("imei", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                    startActivity(loginActivity)
                }
            }
            "internet" -> {
                if (event.message == "lost") {
                    // Dialog
                    val service = Intent(this, RubegNetworkService::class.java)
                    Toast.makeText(this, "Нет соединения с интернетом", Toast.LENGTH_LONG).show()
                    stopService(service)
                    val loginActivity = Intent(this, LoginActivity::class.java)
                    loginActivity.putExtra("imei", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                    startActivity(loginActivity)
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun registration() {
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) == permissionGranted
        ) {
            register = true
            val myLocation = MyLocation()
            myLocation.initLocation(this)
            thread {
                var longitude = 0.0

                while (longitude == 0.0) {
                    try {
                        longitude = MyLocation.imHere!!.longitude
                    } catch (e: Exception) {
                    }
                }
                sleep(2000)
                runOnUiThread {

                    val telephonyMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    val deviceID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        telephonyMgr.imei
                    } else {
                        telephonyMgr.deviceId
                    }

                    val loginActivity = Intent(this, LoginActivity::class.java)
                    loginActivity.putExtra("imei", deviceID)

                    startActivity(loginActivity)
                }
            }
        } else {
            checkPermission()
        }
    }

    @SuppressLint("HardwareIds")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when {
            grantResults.isNotEmpty() && grantResults[3] == permissionGranted -> {
                registration()
            }
        }
    }

    private var permissionGranted = PackageManager.PERMISSION_GRANTED

    private fun checkPermission() {
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
        }
    }

    override fun onStart() {
        super.onStart()

        isAlive = true
        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()

        isAlive = false
        if(EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().unregister(this)
    }
    private var exit = false
    override fun onBackPressed() {
        if (exit) {
            exitProcess(0)
        } else {
            Toast.makeText(this, "Вы точно хотите выйти? Для того чтобы закрыть приложение нажмите еще раз", Toast.LENGTH_LONG).show()
            exit = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val ip: ArrayList<String> = ArrayList()
        ip.add(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!)

        val service = Intent(this, RubegNetworkService::class.java)
        service.putExtra("command", "stop")
        service.putStringArrayListExtra("ip", ip)
        service.putExtra("port", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))

        startService(service)
    }
}
