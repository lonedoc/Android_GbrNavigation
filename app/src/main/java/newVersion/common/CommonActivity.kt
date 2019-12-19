package newVersion.common

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.messaging.RemoteMessage
import java.lang.Thread.sleep
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kotlin.concurrent.thread
import kotlinx.android.synthetic.main.activity_common.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import newVersion.utils.DataStoreUtils
import newVersion.utils.PrefsUtil
import newVersion.alarm.AlarmActivity
import newVersion.callback.CommonCallback
import newVersion.common.alarm.AlarmDialogFragment
import newVersion.common.directory.DirectoryActivity
import newVersion.common.serverSetting.ServerSettingFragment
import newVersion.common.status.StatusFragment
import newVersion.main.MainActivity
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.servicess.LocationListener
import newVersion.servicess.LocationListener.Companion.imHere
import newVersion.servicess.NetworkService
import newVersion.utils.Alarm
import newVersion.utils.GpsStatus
import newVersion.servicess.NotificationService
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class CommonActivity : MvpAppCompatActivity(), CommonView, CommonCallback {
    companion object {
        var isAlive = false
        private var waitLoop: Boolean = false
    }

    private var dialogAlarm: DialogFragment? = null
    private var rotationGestureOverlay: RotationGestureOverlay? = null
    private var locationOverlay: MyLocationNewOverlay? = null
    private var scaleBarOverlay: ScaleBarOverlay? = null

    @InjectPresenter
    lateinit var presenter: CommonPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        setSupportActionBar(common_toolbar)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.common_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.server_setting -> {
                val dialog = ServerSettingFragment()
                dialog.show(supportFragmentManager, "ChangeServerSetting")
            }
            R.id.directory -> {
                val directory = Intent(this, DirectoryActivity::class.java)
                startActivity(directory)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        isAlive = true
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val preferences = PrefsUtil(applicationContext)
        if (!locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER))) {
            createSettingGpsDialog()
        } else {
            initPresenter(locationManager, preferences)
        }
    }

    private fun initPresenter(
        locationManager: LocationManager,
        preferences: PrefsUtil
    ) {
        if (!presenter.init) {
            Log.d("CommonActivity", "Init")
            presenter.init(preferences)
        }
        presenter.stateGpsCheck(locationManager)
    }

    override fun onResume() {
        super.onResume()

        presenter.setTitle()

        when{
            DataStoreUtils.call == null && DataStoreUtils.namegbr == null->{
                showToastMessage("Ошибка приложения, автоматическая перезагрузка")
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
            intent.hasExtra("alarm")->{
<<<<<<< HEAD
                openAlarmDialog(alarm = intent.getSerializableExtra("alarm") as Alarm)
            }
            else->{
                if (DataStoreUtils.namegbr != null)
                    presenter.sendAlarmRequest(DataStoreUtils.namegbr!!)
            }
        }

        presenter.fillStatusBar()
        presenter.getContext(null)
    }
=======
                presenter.init()
                val activity = Intent(applicationContext, AlarmDialogActivity::class.java)
                activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.putExtra("info",intent.getSerializableExtra("alarm") as Alarm)
                startActivity(activity)
>>>>>>> 52d7edf... ver. 1.9.5

    override fun onStop() {
        super.onStop()
        isAlive = false
        presenter.getContext(applicationContext)
    }

    override fun applyAlarm(alarm: Alarm) {
        openAlarmActivity(alarm)
    }

    override fun setCenterLoop() {
        thread {
            if (waitLoop) return@thread
            waitLoop = true
            while (imHere == null) {
                // wait
                sleep(1000)
            }
            runOnUiThread {
                if(common_mapView.overlays.contains(locationOverlay)) common_mapView.overlays.remove(locationOverlay)

                common_mapView.overlays.add(initLocationOverlay())

                common_mapView.invalidate()

                presenter.setCenter(imHere)

                waitLoop = false
                showToastMessage("Удалось определить ваше последнее месторасположение")
            }
        }
    }

    override fun openAlarmActivity(alarm: Alarm) {

        finish()

        val alarmActivity = Intent(this, AlarmActivity::class.java)

        alarmActivity.putExtra("info", alarm)

        startActivity(alarmActivity)
    }

    override fun openAlarmDialog(alarm: Alarm) {
        if (AlarmDialogFragment.isAlive) {
            return
        } else {
            dialogAlarm = if (intent.getBooleanExtra("activeSound", true))
                AlarmDialogFragment.newInstance(alarm, this, true)
            else
                AlarmDialogFragment.newInstance(alarm, this, false)

            if (intent.hasExtra("activeSound"))
                intent.removeExtra("activeSound")

            dialogAlarm?.show(supportFragmentManager, "AlarmDialogFragment")
        }
    }

    override fun setCenter(geoPoint: GeoPoint) {
        common_mapView.controller.animateTo(geoPoint)
        common_mapView.controller.setZoom(15.0)
    }

    fun fabClick(view: View) {
        when (view.id) {
            R.id.common_myLocation -> {
                if (locationOverlay?.isFollowLocationEnabled!!) {
                    locationOverlay?.disableFollowLocation()
                    common_followMe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.viewBackground))
                    common_followMe.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary))
                }
                if (imHere != null) {
                    presenter.setCenter(imHere)
                } else {
                    showToastMessage("Ваше месторасположение не определено")
                }
            }
            R.id.common_followMe -> {
                when {
                    locationOverlay?.isFollowLocationEnabled!! -> {
                        locationOverlay?.disableFollowLocation()
                        common_followMe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.viewBackground))
                        common_followMe.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary))
                    }
                    !locationOverlay?.isFollowLocationEnabled!! -> {
                        locationOverlay?.enableFollowLocation()
                        common_followMe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary))
                        common_followMe.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.viewBackground))
                    }
                }
            }
        }
    }

<<<<<<< HEAD
=======
    override fun setCenter(geoPoint:GeoPoint) {
        common_mapView.controller.animateTo(geoPoint)
        common_mapView.controller.setZoom(15.0)
    }

    var waitCoordinate:Boolean = false

    override fun waitCoordinate() {
        if(waitCoordinate) return
        waitCoordinate = true
        thread{
            while(locationOverlay!!.lastFix == null)
            {
                if(AlarmActivity.isAlive)
                {
                    waitCoordinate = false
                    return@thread
                }
                sleep(5000)
            }

            runOnUiThread {
                common_mapView.invalidate()

                presenter.setCenter(locationOverlay!!.lastFix)

                waitCoordinate = false
                showToastMessage("Удалось определить ваше последнее месторасположение")
            }
        }
    }

>>>>>>> 52d7edf... ver. 1.9.5
    override fun fillStatusBar(statusList: ArrayList<GpsStatus>) {
        runOnUiThread {
            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                        Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                            common_followMe.size = FloatingActionButton.SIZE_NORMAL
                            common_myLocation.size = FloatingActionButton.SIZE_NORMAL
                        }
                        Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                            common_followMe.size = FloatingActionButton.SIZE_NORMAL
                            common_myLocation.size = FloatingActionButton.SIZE_NORMAL
                        }
                        else -> {
                            common_followMe.size = FloatingActionButton.SIZE_MINI
                            common_myLocation.size = FloatingActionButton.SIZE_MINI
                        }
                    }
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                        Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                            common_followMe.size = FloatingActionButton.SIZE_NORMAL
                            common_myLocation.size = FloatingActionButton.SIZE_NORMAL
                        }
                        Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                            common_followMe.size = FloatingActionButton.SIZE_NORMAL
                            common_myLocation.size = FloatingActionButton.SIZE_NORMAL
                        }
                        else -> {
                            common_followMe.size = FloatingActionButton.SIZE_MINI
                            common_myLocation.size = FloatingActionButton.SIZE_MINI
                        }
                    }
                }
            }

            if (common_fab_menu.childCount> 0) {
                common_fab_menu.removeAllMenuButtons()
            }

            for (i in 0 until statusList.count()) {
                val actionButton = com.github.clans.fab.FloatingActionButton(applicationContext)
                actionButton.labelText = statusList[i].name
                actionButton.colorNormal = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                actionButton.setOnClickListener {
                    if (common_fab_menu.isOpened) {
                        presenter.sendStatusRequest(statusList[i].name)
                        common_fab_menu.close(true)
                    }
                }
                when (statusList[i].name) {
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
                common_fab_menu.addMenuButton(actionButton)
            }
        }
    }

    override fun createStatusTimer(time: Long) {
        val dialog = StatusFragment.newInstance(time)
        dialog.show(supportFragmentManager, "StatusTimer")
    }

    override fun setTitle(title: String) {
        runOnUiThread {
            common_toolbar.title = title
        }
    }

    override fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun initMapView() {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        common_mapView.setTileSource(TileSourceFactory.MAPNIK)
        common_mapView.setHasTransientState(true)
        common_mapView.controller.setZoom(15.0)
        common_mapView.isTilesScaledToDpi = true
        common_mapView.isFlingEnabled = true

        presenter.setCenter(imHere)
    }

    override fun addOverlays() {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        initLocationOverlay()
        initRotationGestureOverlay()
        initScaleBarOverlay()

        if (locationOverlay != null) {
            common_mapView.overlays.add(locationOverlay)
            locationOverlay?.enableMyLocation()
        }

        if (rotationGestureOverlay != null) {
            common_mapView.overlays.add(rotationGestureOverlay)
        }

        if (scaleBarOverlay != null) {
            common_mapView.overlays.add(scaleBarOverlay)
            scaleBarOverlay?.enableScaleBar()
        }
    }

    override fun createSettingGpsDialog() {
        runOnUiThread {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val preferences = PrefsUtil(applicationContext)

            val builder = AlertDialog.Builder(this)
            builder.setMessage("GPS отключен (Приложение не работает без GPS)")
                .setCancelable(false)
                .setPositiveButton("Включить") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            val alert = builder.create()
            alert.show()
            thread {
                while (!locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER))) {
                    // Wait while
                }
                runOnUiThread {
                    LocationListener(locationManager)
                    initPresenter(locationManager, preferences)
                }
            }
        }
    }

    private fun initLocationOverlay(): MyLocationNewOverlay {
        if(common_mapView.overlays.contains(locationOverlay)) common_mapView.overlays.remove(locationOverlay)
        val gpsMyLocationProvider = GpsMyLocationProvider(applicationContext)
        locationOverlay = if (imHere != null) {
            Log.d("CommonActivity", "Init locationOverlay")
            gpsMyLocationProvider.addLocationSource(imHere!!.provider)
            MyLocationNewOverlay(gpsMyLocationProvider, common_mapView)
        } else {
            gpsMyLocationProvider.clearLocationSources()
            gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER)
            Log.d("CommonActivity","${gpsMyLocationProvider.locationSources}")
            MyLocationNewOverlay(gpsMyLocationProvider, common_mapView)
        }
        locationOverlay?.setDirectionArrow(presenter.customIcon(R.drawable.ic_navigator_icon, applicationContext), presenter.customIcon(R.drawable.ic_navigator_active_icon, applicationContext))
        locationOverlay?.isDrawAccuracyEnabled = false
        return locationOverlay!!
    }

    private fun initRotationGestureOverlay(): RotationGestureOverlay {
        rotationGestureOverlay = RotationGestureOverlay(common_mapView)
        rotationGestureOverlay?.isEnabled = true
        common_mapView.setMultiTouchControls(true)
        return rotationGestureOverlay!!
    }

    private fun initScaleBarOverlay(): ScaleBarOverlay {
        scaleBarOverlay = ScaleBarOverlay(common_mapView)
        scaleBarOverlay?.setCentred(true)
        scaleBarOverlay?.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay!!
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

    override fun createNotification(command: String, status: String) {
        if (status != "На тревоге") {
            val remoteMessage: RemoteMessage = RemoteMessage.Builder("Status")
                .addData("command", command)
                .addData("status", status)
                .build()
            NotificationService.createNotification(remoteMessage, applicationContext)
        }
    }

    override fun onBackPressed() {
    }
}