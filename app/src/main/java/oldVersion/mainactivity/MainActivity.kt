package oldVersion.mainactivity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arellomobile.mvp.MvpAppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.iid.FirebaseInstanceId
import oldVersion.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.R
import oldVersion.loginactivity.LoginActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import oldVersion.resource.ControlLifeCycleService
import oldVersion.workservice.LocationService
import oldVersion.workservice.ProtocolNetworkService
import oldVersion.workservice.RegistrationEvent
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : MvpAppCompatActivity() {

    companion object {
        var isAlive = false
    }

    private var registration = false
    private var authorization = false

    private var permissionGranted = PackageManager.PERMISSION_GRANTED

    private var exit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)


        if (savedInstanceState != null)
        {
            main_progressBar.visibility = View.VISIBLE
            textView.visibility = View.VISIBLE
            if (!registration && !authorization) {
                checkData()
            }
        } else
        {
            YoYo.with(Techniques.FadeIn)
                .duration(2500)
                .playOn(main_icon)
            thread {
                sleep(2000)
                runOnUiThread {
                     main_progressBar.visibility = View.VISIBLE
                    textView.visibility = View.VISIBLE

                    checkData()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        isAlive = true
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()

        isAlive = false

        println("MainActivity onStop")
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    @SuppressLint("HardwareIds")
    private fun checkData() {
        if (getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("ip") &&
            getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("port") &&
            getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("imei")
        ) {
            authorization()
        } else {
            registration()
        }
    }

    @SuppressLint("HardwareIds", "SetTextI18n")
    private fun registration() {
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) == permissionGranted
        ) {
            registration = true
            thread {
                checkGPS()

                sleep(2000)

                runOnUiThread {

                    val telephonyMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                    val deviceID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        telephonyMgr.imei
                    } else {
                        telephonyMgr.deviceId
                    }

                    val loginActivity = Intent(this,newVersion.login.LoginActivity::class.java)
                    loginActivity.putExtra("imei", deviceID)

                    startActivity(loginActivity)
                    finish()
                }
            }
        } else {

            checkPermission()

        }
    }

    private fun authorization() {
        println("Authorization")
        authorization = true
        val myLocation = LocationService()
        myLocation.initLocation(applicationContext)

        thread {

            checkGPS()

            ControlLifeCycleService.startService(applicationContext)

            thread {
                sleep(2000)

                val token = if (getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("fcmtoken", "") == "") {
                    var token = ""
                    FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                        token = it.token
                    }
                    token
                } else {
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("fcmtoken", "")
                }

                val authorizationMessage = JSONObject()
                authorizationMessage.put("\$c$", "reg")
                authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                authorizationMessage.put(
                    "password",
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
                )
                authorizationMessage.put(
                    "token",
                    token
                )
                authorizationMessage.put("keepalive","10")

                Log.d("Authorization", authorizationMessage.toString())
                runOnUiThread {
                    ProtocolNetworkService.protocol?.send(authorizationMessage.toString()) { success: Boolean ->
                        if (success) {
                            ProtocolNetworkService.connectServer = true
                            ProtocolNetworkService.connectInternet = true

                        } else {
                            runOnUiThread {

                                ControlLifeCycleService.stopService(applicationContext)

                                Toast.makeText(this, "Приложение не смогло авторизироваться на сервере", Toast.LENGTH_LONG).show()

                                val loginActivity = Intent(this, LoginActivity::class.java)
                                loginActivity.putExtra("imei", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                                startActivity(loginActivity)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkGPS() {

        val myLocation = LocationService()
        var longitude = 0.0

        runOnUiThread {

            myLocation.initLocation(applicationContext)
            textView.text = getString(R.string.waitConnectToGPS)

            if(!LocationService.Enable){
                myLocation.initLocation(applicationContext)
            }

            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val buildAlertMessageNoGps = {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("GPS отключен, хотите ли вы его включить? (Приложение не работает без GPS)")
                    .setCancelable(false)
                    .setPositiveButton("Да"){_,_ ->
                        startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))

                    }
                    .setNegativeButton("No") {
                            dialog,_->
                        dialog.cancel()

                    }
                val alert = builder.create()
                alert.show()
            }

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
            }
        }

        var tryReconnect = 0

        while (longitude == 0.0) {
            if(tryReconnect>3){
                longitude = 1.0
                runOnUiThread {
                    Toast.makeText(this,"Ваше месторасположение не определено",Toast.LENGTH_SHORT).show()
                }
            }
            try {
                println("Longitude $longitude")
                longitude = LocationService.imHere?.longitude!!
                tryReconnect++
            } catch (e: Exception) {
                tryReconnect++
            }
            sleep(3000)
        }

        runOnUiThread {
            if(authorization)
                textView.text = getString(R.string.authorization)

            if(registration)
                textView.text = getString(R.string.registration)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onRegistrationEvent(event: RegistrationEvent) {

        when(event.command){
            "regok"->{

                Toast.makeText(this, "Авторизация прошла успешно", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@MainActivity, CommonActivity::class.java)
                startActivity(intent)
                finish()
                
            }
            "accessdenied"->{

                Toast.makeText(this,"Данного пользователя не существует в базе",Toast.LENGTH_SHORT).show()

                ControlLifeCycleService.stopService(applicationContext)

                val loginActivity = Intent(this, LoginActivity::class.java)
                loginActivity.putExtra("imei", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                startActivity(loginActivity)
                finish()
            }
        }

    }

    @SuppressLint("HardwareIds")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when {
            grantResults.isNotEmpty() && grantResults[1] == permissionGranted  -> {
                registration()
            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_NETWORK_STATE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.INTERNET) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.VIBRATE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CALL_PHONE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WAKE_LOCK) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.FOREGROUND_SERVICE) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.SYSTEM_ALERT_WINDOW) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.SYSTEM_ALERT_WINDOW) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.SYSTEM_ALERT_WINDOW) != permissionGranted
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CALL_PHONE,
                    android.Manifest.permission.WAKE_LOCK,
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.SYSTEM_ALERT_WINDOW,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_NETWORK_STATE,
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.VIBRATE
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


    override fun onBackPressed() {
        if (exit) {

            ControlLifeCycleService.stopService(applicationContext)

            android.os.Process.killProcess(android.os.Process.myPid())
        } else {
            Toast.makeText(this, "Вы точно хотите выйти? Для того чтобы закрыть приложение нажмите еще раз", Toast.LENGTH_LONG).show()
            exit = true
        }
    }
}
