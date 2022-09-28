package gbr.ui.start

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import gbr.presentation.presenter.start.StartPresenter
import gbr.presentation.view.start.StartView
import gbr.ui.login.LoginActivity
import gbr.ui.main.MainActivity
import gbr.ui.whatnew.WhatIsNewFragment
import gbr.utils.PrefsUtils
import gbr.utils.callbacks.GpsCallback
import gbr.utils.servicess.ProtocolService
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_start.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import java.lang.Thread.sleep
import kotlin.system.exitProcess


class StartActivity:MvpAppCompatActivity(),StartView,GpsCallback {

    @InjectPresenter
    lateinit var presenter: StartPresenter
    private val permissionGranted = PackageManager.PERMISSION_GRANTED

    companion object{
        lateinit var context: Context
        var isAlive = false
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Предупреждение!")
            .setMessage("Приложение имеет доступ к данным о местоположение устройства в фоновом режиме, т.е. даже если оно свернуто. Данные передаются на сервер для отображения месторасположения устройва на ситуационной карте, которая используется в вашем ЧОПе. Сотрудники ЧОПа, имующие доступ к ситуационной карте, видят где находится устройство.")
            .setPositiveButton("Закрыть"){ dialog, which ->
                checkPermission()
                dialog.cancel()
            }
            .setCancelable(false)
            .create()
        dialog.show()

        start_rotateLoading.start()
        context = this
        isAlive = true
        createAlarmChannel()
        createConnectionChannel()
    }

    override fun onResume() {
        super.onResume()
        val ns: String = NOTIFICATION_SERVICE
        val nMgr = getSystemService(ns) as NotificationManager
        nMgr.cancelAll()
    }
    override fun setText(message: String) {
        runOnUiThread {
            start_text.text = message
        }

    }


    fun createAlarmChannel() {
        val sound: Uri =
            Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.alarm_sound)
        val mChannel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel =
                NotificationChannel("Alarm", "Alarm channel", NotificationManager.IMPORTANCE_HIGH)
            mChannel.lightColor = Color.RED
            mChannel.enableLights(true)
            mChannel.description = "Alarm"
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            mChannel.setSound(sound, audioAttributes)
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    fun createConnectionChannel()
    {

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val mChannel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel =
                NotificationChannel("Connection", "Connection after close app", NotificationManager.IMPORTANCE_HIGH)
            mChannel.lightColor = Color.RED
            mChannel.enableLights(true)
            mChannel.description = "Connection"
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            mChannel.setSound(sound, audioAttributes)
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
    override fun whatNew() {
        val dialog = WhatIsNewFragment.newInstance(this)
        dialog.show(supportFragmentManager, "WhatNew?")
    }

    private var presenterInit = false
    override fun checkPermission() {
        if(ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == permissionGranted &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.READ_PHONE_STATE
            ) == permissionGranted &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.FOREGROUND_SERVICE
            ) == permissionGranted &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == permissionGranted &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == permissionGranted &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == permissionGranted
        )
        {
            if(presenterInit) return
            presenterInit = true
            val preferences = PrefsUtils(this)
            presenter.init(preferences, this)
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isEmpty()) return

        when{
            grantResults.isNotEmpty() && grantResults[1] == permissionGranted && grantResults[2] == permissionGranted && grantResults[3] == permissionGranted ->{
                sleep(4000)
                if(presenterInit) return
                presenterInit = true
                val preferences = PrefsUtils(this)
                presenter.init(preferences, this)
            }
            else->{
                presenter.errorPermission()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun gpsCheck() {
        runOnUiThread {
            presenter.dataChecking()
        }

    }

    var stop = false
    override fun gpsSetting(
        message: String
    ) {
        if(stop) presenter.startGPS()

        AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Включить"){ _, _->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                stop = true
            }
            .create().show()

    }

    override fun errorPermissionDialog(errorMessage: String) {

        AlertDialog.Builder(this)
            .setMessage(errorMessage)
            .setCancelable(false)
            .setPositiveButton("Разрешить"){ _, _->
                checkPermission()
            }
            .setNegativeButton("Закрыть приложение"){ _, _->
                exitProcess(0)
            }
            .create().show()
    }

    private var openLogin = false
    override fun loginActivity() {
        runOnUiThread {
            if(openLogin) return@runOnUiThread
            openLogin = true
            val login = Intent(this, LoginActivity::class.java)
            startActivity(login)
        }
    }

    override fun startService() {
        if(ProtocolService.isStarted) return
        runOnUiThread {
            val intent = Intent(this, ProtocolService::class.java)
            startService(intent)
        }

    }

    private var openMain = false
    override fun openMainActivity() {
        Log.d("MainActivity", "Intent")
        if(openMain) return
        openMain = true
        val main = Intent(this, MainActivity::class.java)
        startActivity(main)
    }

    override fun errorMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun stopGpsSetting() {
        stop = false
    }

    override fun onDestroy() {
        val service = Intent(applicationContext, ProtocolService::class.java)
        stopService(service)
        System.exit(0)
        isAlive = false
        stop = false
        super.onDestroy()
    }
}