package gbr.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import gbr.presentation.presenter.main.MainPresenter
import gbr.presentation.view.main.MainView
import gbr.ui.alarm.AlarmActivity
import gbr.ui.mobalarm.MobAlarmActivity
import gbr.ui.status.StatusFragment
import gbr.utils.data.AlarmInformation
import gbr.utils.data.StatusList
import gbr.utils.servicess.ProtocolService
import gbr.utils.servicess.ProtocolService.Companion.currentLocation
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_new_main.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import kotlin.concurrent.thread

class MainActivity:MvpAppCompatActivity(),MainView {
    @InjectPresenter
    lateinit var presenter: MainPresenter

    companion object{
        var isAlive = false
    }

    lateinit var lm:LocationManager
    lateinit var locationOverlay:MyLocationNewOverlay
    lateinit var rotationGestureOverlay: RotationGestureOverlay
    lateinit var scaleBarOverlay: ScaleBarOverlay

    lateinit var gpsDialog:AlertDialog

    lateinit var alertSound: MediaPlayer

    var dialog:AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)
        setSupportActionBar(main_toolbar)

        presenter.context(applicationContext)
        this.alertSound = MediaPlayer.create(
            this,
            Uri.parse("android.resource://" + application.packageName + "/" + R.raw.alarm_sound)
        )


        main_myLocation.setOnClickListener {
            if(locationOverlay.myLocation == null)
                showToastMessage("Ваше месторасположение не определенно")
            else
                main_mapView.controller.setCenter(locationOverlay.myLocation)
        }

        main_followMe.setOnClickListener {
            when {
                locationOverlay.myLocation == null -> showToastMessage("Ваше месторасположение не определенно")
                locationOverlay.isFollowLocationEnabled -> {
                    locationOverlay.disableFollowLocation()
                    main_followMe.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this,
                            R.color.viewBackground
                        )
                    )
                    main_followMe.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this,
                            R.color.colorPrimary
                        )
                    )
                }
                else -> {
                    locationOverlay.enableAutoStop = false

                    locationOverlay.enableFollowLocation()
                    main_followMe.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this,
                            R.color.colorPrimary
                        )
                    )
                    main_followMe.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this,
                            R.color.viewBackground
                        )
                    )
                }
            }


        }


    }

    override fun setTitle(title: String) {
        runOnUiThread {
            main_toolbar.title = title
        }

    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        val ns: String = NOTIFICATION_SERVICE
        val nMgr = getSystemService(ns) as NotificationManager
        nMgr.cancelAll()

        isAlive = true

        locationOverlay.enableMyLocation()

        scaleBarOverlay.enableScaleBar()
    }

    override fun onResume() {
        super.onResume()

        presenter.checkAlarm()
        presenter.setTitle()
    }
    override fun onStop() {
        super.onStop()

        isAlive = false

        locationOverlay.disableMyLocation()

        scaleBarOverlay.disableScaleBar()
    }

    override fun initMapView() {
        Log.d("initMapView","Init")
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        main_mapView.setTileSource(TileSourceFactory.MAPNIK)
        main_mapView.setHasTransientState(true)
        main_mapView.controller.setZoom(15.0)
        main_mapView.isTilesScaledToDpi = true
        main_mapView.isFlingEnabled = true
    }

    override fun initOverlays() {
        if (main_mapView.overlays.count() >= 1)
            main_mapView.overlays.clear()

        locationOverlay = initLocationOverlay()
        rotationGestureOverlay = initRotationGestureOverlay()
        scaleBarOverlay = initScaleBarOverlay()

        main_mapView.overlays.add(locationOverlay)
        main_mapView.overlays.add(rotationGestureOverlay)
        main_mapView.overlays.add(scaleBarOverlay)

        locationOverlay.isOptionsMenuEnabled = true
        gpsDialog = AlertDialog.Builder(this)
            .setTitle("Поиск координат")
            .setCancelable(false)
            .setMessage("Вычисляем где вы находитесь...")
            .show()

        thread{
            while(currentLocation == null)
            {
                //
            }
            presenter.setCenter(GeoPoint(currentLocation))
        }
    }

    override fun fillStatusBar(statusList: ArrayList<StatusList>) {
        runOnUiThread {
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                        Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                            main_followMe.size = FloatingActionButton.SIZE_NORMAL
                            main_myLocation.size = FloatingActionButton.SIZE_NORMAL
                        }
                        Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                            main_followMe.size = FloatingActionButton.SIZE_NORMAL
                            main_myLocation.size = FloatingActionButton.SIZE_NORMAL
                        }
                        else -> {
                            main_followMe.size = FloatingActionButton.SIZE_MINI
                            main_myLocation.size = FloatingActionButton.SIZE_MINI
                        }
                    }
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                        Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                            main_followMe.size = FloatingActionButton.SIZE_NORMAL
                            main_myLocation.size = FloatingActionButton.SIZE_NORMAL
                        }
                        Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                            main_followMe.size = FloatingActionButton.SIZE_NORMAL
                            main_myLocation.size = FloatingActionButton.SIZE_NORMAL
                        }
                        else -> {
                            main_followMe.size = FloatingActionButton.SIZE_MINI
                            main_myLocation.size = FloatingActionButton.SIZE_MINI
                        }
                    }
                }
            }

            if (main_fab_menu.childCount> 0) {
                main_fab_menu.removeAllMenuButtons()
            }

            for (i in 0 until statusList.count()) {
                val actionButton = com.github.clans.fab.FloatingActionButton(applicationContext)
                actionButton.labelText = statusList[i].status
                actionButton.colorNormal = ContextCompat.getColor(
                    applicationContext,
                    R.color.colorPrimary
                )
                actionButton.setOnClickListener {
                    if (main_fab_menu.isOpened) {
                        presenter.sendStatusChangeRequest(statusList[i].status)
                        main_fab_menu.close(true)
                    }
                }
                when (statusList[i].status) {
                    "Заправляется" -> {
                        actionButton.setImageResource(R.drawable.ic_refueling)
                    }
                    "Обед" -> {
                        actionButton.setImageResource(R.drawable.ic_dinner)
                    }
                    "Ремонт" -> {
                        actionButton.setImageResource(R.drawable.ic_repairs)
                    }
                    "Свободен" -> {
                        actionButton.setImageResource(R.drawable.ic_freedom)
                    }
                    else -> { actionButton.setImageResource(R.drawable.ic_unknown_status) }
                }
                val sizeNormal = 0
                val sizeMini = 1
                when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                            Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                                actionButton.buttonSize = sizeNormal
                            }
                            Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                                actionButton.buttonSize = sizeNormal
                            }
                            else -> {
                                actionButton.buttonSize = sizeMini
                            }
                        }
                    }
                    Configuration.ORIENTATION_PORTRAIT -> {
                        when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                            Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                                actionButton.buttonSize = sizeNormal
                            }
                            Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                                actionButton.buttonSize = sizeNormal
                            }
                            else -> {
                                actionButton.buttonSize = sizeMini
                            }
                        }
                    }
                }
                main_fab_menu.addMenuButton(actionButton)
            }
        }
    }

    override fun setCenter(center: GeoPoint) {
        runOnUiThread {
            if(gpsDialog.isShowing)
                gpsDialog.cancel()

            main_mapView.controller.animateTo(center)
            main_mapView.controller.setZoom(15.0)

        }
    }

    override fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun showStatusTimer() {
        Log.d("Shows Dialog", "yes")
        val dialog = StatusFragment()
        dialog.show(supportFragmentManager, "StatusTimer")
    }

    override fun showAlarmDialog(alarmInformation: AlarmInformation) {
        runOnUiThread {
            alertSound.start()
            val view = layoutInflater.inflate(R.layout.fragment_alarm_dialog, null)

            val title = view.findViewById(R.id.alarm_text) as TextView
            title.text = "Тревога на объекте"

            val objectName = view.findViewById(R.id.object_name) as TextView
            objectName.text = alarmInformation.name

            val objectAddresses = view.findViewById(R.id.object_address) as TextView
            objectAddresses.text = alarmInformation.address

            val applyAlarm = view.findViewById(R.id.apply_alarm) as Button
            applyAlarm.setOnClickListener {

                presenter.onDestroy()

                dialog?.cancel()

                alertSound.stop()

                val intentAlarm = Intent(this, AlarmActivity::class.java)
                startActivity(intentAlarm)
            }

            dialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(view)
                .create()
            dialog?.show()
        }
    }

    override fun showMobAlarmDialog(alarmInformation: AlarmInformation) {
        runOnUiThread {
            alertSound.start()
            val view = layoutInflater.inflate(R.layout.fragment_alarm_dialog, null)

            val title = view.findViewById(R.id.alarm_text) as TextView
            title.text = "Тревога на мобильном объекте"

            val objectName = view.findViewById(R.id.object_name) as TextView
            objectName.text = alarmInformation.name

            val objectAddresses = view.findViewById(R.id.object_address) as TextView
            objectAddresses.visibility = View.GONE

            val applyAlarm = view.findViewById(R.id.apply_alarm) as Button
            applyAlarm.setOnClickListener {
                presenter.onDestroy()
                dialog?.cancel()
                alertSound.stop()

                val intentAlarm = Intent(this, MobAlarmActivity::class.java)
                startActivity(intentAlarm)
            }

            dialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(view)
                .create()
            dialog?.show()
        }

    }

    override fun cancelAlarm() {
        runOnUiThread {
            try {
                alertSound.stop()
            }catch (e: Exception){
                e.printStackTrace()
            }

            dialog?.cancel()
            this.alertSound = MediaPlayer.create(
                this,
                Uri.parse("android.resource://" + application.packageName + "/" + R.raw.alarm_sound)
            )
        }
    }

    override fun reopenActivity() {
        runOnUiThread {

        }
    }

    private fun initLocationOverlay(): MyLocationNewOverlay {
        Log.d("LocationOverlay","Init")
        val locationOverlay = MyLocationNewOverlay( main_mapView)
        locationOverlay.setDirectionArrow(
            presenter.customIcon(
                R.drawable.ic_navigator_icon,
                applicationContext
            ), presenter.customIcon(R.drawable.ic_navigator_active_icon, applicationContext)
        )
        locationOverlay.isDrawAccuracyEnabled = false
        Log.d("LocationOverlay","Init")
        return locationOverlay
    }

    private fun initRotationGestureOverlay(): RotationGestureOverlay {
        val rotationGestureOverlay = RotationGestureOverlay(main_mapView)
        rotationGestureOverlay.isEnabled = true
        main_mapView.setMultiTouchControls(true)
        return rotationGestureOverlay
    }

    private fun initScaleBarOverlay(): ScaleBarOverlay {
        val scaleBarOverlay = ScaleBarOverlay(main_mapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay
    }

    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}