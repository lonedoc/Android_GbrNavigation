package gbr.ui.main

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Intent
import android.content.res.Configuration
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
import com.google.gson.Gson
import com.google.gson.JsonObject
import gbr.presentation.presenter.main.MainPresenter
import gbr.presentation.view.main.MainView
import gbr.ui.status.StatusFragment
import gbr.utils.api.alarm.AlarmAPI
import gbr.utils.api.alarm.OnAlarmListener
import gbr.utils.api.alarm.RPAlarmAPI
import gbr.utils.data.AlarmInformation
import gbr.utils.data.Info
import gbr.utils.data.StatusList
import gbr.utils.servicess.ProtocolService
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_new_main.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import newVersion.common.CommonActivity
import newVersion.common.alarm.AlarmDialogActivity
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import rubegprotocol.RubegProtocol
import java.lang.Exception
import java.util.ArrayList
import kotlin.concurrent.thread

class MainActivity:MvpAppCompatActivity(),MainView,OnAlarmListener {
    @InjectPresenter
    lateinit var presenter: MainPresenter

    companion object{
        var isAlive = false
    }


    lateinit var locationOverlay:MyLocationNewOverlay
    lateinit var rotationGestureOverlay: RotationGestureOverlay
    lateinit var scaleBarOverlay: ScaleBarOverlay

    lateinit var gpsDialog:AlertDialog

    private var alarmAPI: AlarmAPI?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)
        setSupportActionBar(main_toolbar)

        val protocol = RubegProtocol.sharedInstance

        if(alarmAPI!= null) alarmAPI?.onDestroy()
        alarmAPI = RPAlarmAPI(protocol)
        alarmAPI?.onAlarmListener = this

        alertSound = MediaPlayer.create(this, Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext?.packageName + "/" + R.raw.alarm_sound))
    }

    override fun setTitle(title:String) {
        runOnUiThread {
            main_toolbar.title = title
        }

    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()

        val ns: String = NOTIFICATION_SERVICE
        val nMgr = getSystemService(ns) as NotificationManager
        nMgr.cancelAll()

        isAlive = true

        presenter.setTitle()
        presenter.checkAlarm()
    }

    override fun initMapView() {
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

        locationOverlay.enableMyLocation()
        scaleBarOverlay.enableScaleBar()
        Log.d("Location","${locationOverlay.myLocation}")

        gpsDialog = AlertDialog.Builder(this)
            .setTitle("Поиск координат")
            .setCancelable(false)
            .setMessage("Вычисляем где вы находитесь...")
            .show()
        thread{
            while(locationOverlay.myLocation == null)
            {
                //
            }
            presenter.setCenter(locationOverlay.myLocation)
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
                actionButton.colorNormal = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                actionButton.setOnClickListener {
                    if (main_fab_menu.isOpened) {
                        presenter.sendStatusChangeRequest(statusList[i].status)
                        main_fab_menu.close(true)
                    }
                }
                when (statusList[i].status) {
                    "Заправляется" -> { actionButton.setImageResource(R.drawable.ic_refueling) }
                    "Обед" -> { actionButton.setImageResource(R.drawable.ic_dinner) }
                    "Ремонт" -> { actionButton.setImageResource(R.drawable.ic_repairs) }
                    "Свободен" -> { actionButton.setImageResource(R.drawable.ic_freedom) }
                    else -> { actionButton.setImageResource(R.drawable.ic_unknown_status) }
                }
                val sizeNormal = 0
                val sizeMini = 1
                when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                            Configuration.SCREENLAYOUT_SIZE_LARGE -> { actionButton.buttonSize = sizeNormal }
                            Configuration.SCREENLAYOUT_SIZE_XLARGE -> { actionButton.buttonSize = sizeNormal }
                            else -> { actionButton.buttonSize = sizeMini }
                        }
                    }
                    Configuration.ORIENTATION_PORTRAIT -> {
                        when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                            Configuration.SCREENLAYOUT_SIZE_LARGE -> { actionButton.buttonSize = sizeNormal }
                            Configuration.SCREENLAYOUT_SIZE_XLARGE -> { actionButton.buttonSize = sizeNormal }
                            else -> { actionButton.buttonSize = sizeMini }
                        }
                    }
                }
                main_fab_menu.addMenuButton(actionButton)
            }
        }
    }

    override fun setCenter(center:GeoPoint) {
        runOnUiThread {
            if(gpsDialog.isShowing)
                gpsDialog.cancel()

            main_mapView.controller.animateTo(center)
            main_mapView.controller.setZoom(15.0)
        }
    }

    override fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }
    }

    override fun showStatusTimer() {
        Log.d("Shows Dialog","yes")
        val dialog = StatusFragment()
        dialog.show(supportFragmentManager, "StatusTimer")
    }

    private fun initLocationOverlay(): MyLocationNewOverlay {
        val gpsMyLocationProvider = GpsMyLocationProvider(applicationContext)

        val locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider,main_mapView)
        locationOverlay.setDirectionArrow(presenter.customIcon(R.drawable.ic_navigator_icon, applicationContext), presenter.customIcon(R.drawable.ic_navigator_active_icon, applicationContext))
        locationOverlay.isDrawAccuracyEnabled = false

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

    var dialog:AlertDialog? = null
    lateinit var alertSound: MediaPlayer
    override fun onAlarmDataReceived(flag:String,alarm: String) {
        runOnUiThread {
            if(!isAlive)
            {
                val activity = Intent(this, MainActivity::class.java)
                startActivity(activity)
                return@runOnUiThread
            }
            if(alertSound.isPlaying)
                alertSound.stop()

            when(flag){
                "alarm"->{
                    val alarmInformation = Gson().fromJson(alarm,AlarmInformation::class.java)

                    if(alarmInformation.name == null) return@runOnUiThread

                    alertSound.start()
                    val view = layoutInflater.inflate(R.layout.fragment_alarm_dialog,null)

                    val title = view.findViewById(R.id.alarm_text) as TextView
                    title.text = "Тревога на объекте"

                    val objectName = view.findViewById(R.id.object_name) as TextView
                    objectName.text = alarmInformation.name

                    val objectAddresses = view.findViewById(R.id.object_address) as TextView
                    objectAddresses.text = alarmInformation.address

                    val applyAlarm = view.findViewById(R.id.apply_alarm) as Button
                    applyAlarm.setOnClickListener {


                        dialog?.cancel()
                        alertSound.stop()
                    }
                    dialog = AlertDialog.Builder(this)
                        .setView(view)
                        .create()
                    dialog?.show()
                }
                "alarmmob"->{

                    val alarmInformation = Gson().fromJson(alarm,AlarmInformation::class.java)
                    if(alarmInformation.name == null) return@runOnUiThread

                    alertSound.start()
                    val view = layoutInflater.inflate(R.layout.fragment_alarm_dialog,null)

                    val title = view.findViewById(R.id.alarm_text) as TextView
                    title.text = "Тревога на мобильном объекте"

                    val objectName = view.findViewById(R.id.object_name) as TextView
                    objectName.text = alarmInformation.name

                    val objectAddresses = view.findViewById(R.id.object_address) as TextView
                    objectAddresses.visibility = View.GONE

                    val applyAlarm = view.findViewById(R.id.apply_alarm) as Button
                    applyAlarm.setOnClickListener {
                        presenter.giveImage()
                        dialog?.cancel()
                        alertSound.stop()
                    }
                    dialog = AlertDialog.Builder(this)
                        .setView(view)
                        .create()
                    dialog?.show()
                }
                "notalarm"->{
                    try {
                        dialog?.cancel()
                        alertSound.stop()
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        isAlive = false
    }
}