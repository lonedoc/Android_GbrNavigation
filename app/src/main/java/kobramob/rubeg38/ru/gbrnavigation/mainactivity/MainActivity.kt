package kobramob.rubeg38.ru.gbrnavigation.mainactivity

import android.annotation.SuppressLint
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
import java.lang.Exception
import java.lang.Thread.sleep
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.service.MyLocation
import kobramob.rubeg38.ru.gbrnavigation.service.NetworkServiceDelegate
import kobramob.rubeg38.ru.gbrnavigation.service.RubegNetworkService
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val networkService:RubegNetworkService = RubegNetworkService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView: ImageView = findViewById(R.id.main_icon)
        val progressBar: ProgressBar = findViewById(R.id.main_progressBar)
        val textView: TextView = findViewById(R.id.main_text)

        if (savedInstanceState != null) {
            progressBar.visibility = View.VISIBLE
            textView.visibility = View.VISIBLE
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
        val myLocation = MyLocation()
        myLocation.initLocation(this)

        thread {
            var longitude = 0.0

            while (longitude == 0.0) {
                try {
                    longitude = MyLocation.imHere!!.longitude
                } catch (e: Exception) {}
            }

            val ip:ArrayList<String> = ArrayList()
            ip.add(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip","")!!)

            val service = Intent(this,RubegNetworkService::class.java)
            service.putStringArrayListExtra("ip",ip)
            service.putExtra("port",getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port",9010))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service)
            }
            else
            {
                startService(service)
            }
            val authorizationMessage = JSONObject()
            authorizationMessage.put("\$c$","reg")
            authorizationMessage.put("id","0D82F04B-5C16-405B-A75A-E820D62DF911")
            authorizationMessage.put("password",getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei",""))
            RubegNetworkService.protocol.request(authorizationMessage.toString()) { success: Boolean, response: ByteArray? ->
                if(success && response !=null)
                {
                    Log.d("Authorization",String(response))
                    val commonActivity = Intent(this, CommonActivity::class.java)
                    startActivity(commonActivity)
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun registration() {
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) == permissionGranted
        ) {

            val myLocation = MyLocation()
            myLocation.initLocation(this)
            thread {
                var longitude = 0.0

                while (longitude == 0.0) {
                    try {
                        longitude = MyLocation.imHere!!.longitude
                    } catch (e: Exception) {}
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
    }

    override fun onBackPressed() {
    }
}
