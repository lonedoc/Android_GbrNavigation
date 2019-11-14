package newVersion.main

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.messaging.RemoteMessage
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlin.concurrent.thread
import kotlinx.android.synthetic.main.activity_main.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import newVersion.utils.PrefsUtil
import newVersion.common.CommonActivity
import newVersion.login.LoginActivity
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.models.Preferences
import newVersion.servicess.LocationListener
import newVersion.servicess.NetworkService
import newVersion.servicess.NotificationService

class MainActivity : MvpAppCompatActivity(), MainView {
    @InjectPresenter
    lateinit var presenter: MainPresenter

    private val permissionGranted = PackageManager.PERMISSION_GRANTED

    companion object{
        var isAlive = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) != permissionGranted
        ) {
            checkPermission()
        } else {
            run()
        }
    }

    override fun onStart() {
        super.onStart()
        isAlive = true
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
    }

    private fun run() {
        if (!presenter.isInit()) {
            Log.d("MainActivity", "Init")
            val preferences: Preferences = PrefsUtil(this)
            presenter.init(preferences)
        }
    }

    override fun startAnimation() {
        runOnUiThread {
            main_icon.visibility = View.VISIBLE

            YoYo.with(Techniques.FadeIn)
                .duration(2500)
                .playOn(main_icon)

            thread {
                Thread.sleep(2000)
                runOnUiThread {
                    main_progressBar.visibility = View.VISIBLE
                    textView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun setHintText(message: String) {
        runOnUiThread {
            textView.text = message
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when {
            grantResults.isNotEmpty() && grantResults[1] == permissionGranted -> {
                run()
            }
        }
    }

    override fun checkPermission() {
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
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.SYSTEM_ALERT_WINDOW) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.SYSTEM_ALERT_WINDOW) != permissionGranted ||
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.SYSTEM_ALERT_WINDOW) != permissionGranted
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

    override fun initLocationManager() {
        runOnUiThread {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (!locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER))) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("GPS отключен (Приложение не работает без GPS)")
                    .setCancelable(false)
                    .setPositiveButton("Включить") { _, _ ->
                        startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                val alert = builder.create()
                alert.show()
                thread {
                    while (!locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER))) {
                        // Wait while
                    }
                    LocationListener(locationManager)
                    presenter.checkData()
                }
            } else {
                LocationListener(locationManager)
                presenter.checkData()
            }
        }
    }

    override fun startService(credentials: Credentials, hostPool: HostPool) {
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

    override fun openLoginActivity() {
        runOnUiThread {
            presenter.onDestroy()
            val loginActivity = Intent(this, LoginActivity::class.java)
            startActivity(loginActivity)
        }
    }

    override fun disconnectServer() {
        val remoteMessage1 = RemoteMessage.Builder("Status")
            .addData("command", "serverNotResponse")
            .build()
        NotificationService.createNotification(remoteMessage1, applicationContext)
    }

    override fun openCommonActivity() {

        val remoteMessage1 = RemoteMessage.Builder("Status")
            .addData("command", "connectServer")
            .build()
        NotificationService.createNotification(remoteMessage1, applicationContext)

        runOnUiThread {
            presenter.onDestroy()
            val intent = Intent(this, CommonActivity::class.java)
            startActivity(intent)
            Log.d("LoginActivity", "RegisterSuccess")
        }
    }

    override fun onDestroy() {
        val stopServiceIntent = Intent(applicationContext, NetworkService::class.java)
        stopService(stopServiceIntent)
        System.exit(0)
        super.onDestroy()
    }
}