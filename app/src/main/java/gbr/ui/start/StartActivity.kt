package gbr.ui.start

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import gbr.presentation.presenter.start.StartPresenter
import gbr.ui.whatnew.WhatIsNewFragment
import gbr.utils.PrefsUtils
import gbr.presentation.view.start.StartView
import gbr.ui.login.LoginActivity
import gbr.utils.callbacks.GpsCallback
import gbr.utils.servicess.ProtocolService
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_start.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import okhttp3.internal.wait
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class StartActivity:MvpAppCompatActivity(),StartView,GpsCallback {

    @InjectPresenter
    lateinit var presenter: StartPresenter
    private val permissionGranted = PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        start_rotateLoading.start()
    }

    override fun setText(message: String) {
        start_text.text = message
    }

    override fun whatNew() {
        val dialog = WhatIsNewFragment.newInstance(this)
        dialog.show(supportFragmentManager,"WhatNew?")
    }

    override fun checkPermission() {
        if(ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.READ_PHONE_STATE) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.FOREGROUND_SERVICE) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == permissionGranted &&
            ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) == permissionGranted
        )
        {
            val preferences = PrefsUtils(this)
            presenter.init(preferences,this)
        }


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                permissionGranted
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(grantResults.isEmpty()) return

        when{
            grantResults.isNotEmpty() && grantResults[1] == permissionGranted && grantResults[2] == permissionGranted && grantResults[3] == permissionGranted ->{
                val preferences = PrefsUtils(this)
                presenter.init(preferences,this)
            }
            else->{
                presenter.errorPermission()
            }
        }
    }

    override fun gpsCheck() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        presenter.startGPS(locationManager)
    }

    override fun gpsSetting(
        message: String,
        locationManager: LocationManager
    ) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Включить"){_,_->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                gbr.utils.servicess.LocationListener(locationManager)
            }
            .create().show()
    }

    override fun errorPermissionDialog(errorMessage: String) {

        AlertDialog.Builder(this)
            .setMessage(errorMessage)
            .setCancelable(false)
            .setPositiveButton("Разрешить"){_,_->
                checkPermission()
            }
            .setNegativeButton("Закрыть приложение"){_,_->
                exitProcess(0)
            }
            .create().show()
    }

    override fun loginActivity() {
        val login = Intent(this,LoginActivity::class.java)
        startActivity(login)
    }

    override fun startService() {
        if(ProtocolService.isStarted) return

        val service = Intent(this,ProtocolService::class.java)
        startService(service)
    }

    override fun onDestroy() {
        super.onDestroy()
        val service = Intent(this,ProtocolService::class.java)
        stopService(service)
    }
}