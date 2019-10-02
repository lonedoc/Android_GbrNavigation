package newVersion.common

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.firebase.messaging.RemoteMessage
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_common.*
import newVersion.LocationListener
import newVersion.NetworkService
import newVersion.Utils.PrefsUtil
import newVersion.models.Credentials
import newVersion.models.HostPool
import oldVersion.workservice.NotificationService
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.concurrent.thread


class CommonActivity : MvpAppCompatActivity(), CommonView {

    companion object{
        var isAlive = false
    }

    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay

    @InjectPresenter
    lateinit var presenter: CommonPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        setSupportActionBar(common_toolbar)
    }

    override fun onStart() {
        super.onStart()

        isAlive = true

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val preferences = PrefsUtil(applicationContext)
        if (!locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER))) {
            createSettingGpsDialog()
        }
        else
        {
           initPresenter(locationManager,preferences)
        }
    }

    private fun initPresenter(
        locationManager: LocationManager,
        preferences: PrefsUtil
    )
    {
        if(!presenter.init)
        {
            Log.d("CommonActivity","Init")
            presenter.init(preferences)
        }
        presenter.stateGpsCheck(locationManager)
    }

    override fun onResume() {
        super.onResume()
        presenter.setTitle()
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
    }
    override fun setCenter(geoPoint: GeoPoint) {
        common_mapView.controller.animateTo(geoPoint)
        common_mapView.controller.setZoom(15.0)
    }

    fun fabClick(view: View) {
        when(view.id){
            R.id.common_myLocation->{
                presenter.setCenter(LocationListener.imHere)
            }
            R.id.common_followMe->{

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.common_menu,menu)
        return true
    }

    override fun setTitle(title: String) {
        runOnUiThread {
            common_toolbar.title = title
        }
    }

    override fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
        }
    }

    override fun initMapView(){
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        common_mapView.setTileSource(TileSourceFactory.MAPNIK)
        common_mapView.setHasTransientState(true)
        common_mapView.controller.setZoom(15.0)
        common_mapView.isTilesScaledToDpi = true
        common_mapView.isFlingEnabled = true

        presenter.setCenter(LocationListener.imHere)
    }

    override fun addOverlays() {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        if(!common_mapView.overlays.contains(initLocationOverlay()))
        {
            common_mapView.overlays.add(initLocationOverlay())
            locationOverlay.enableMyLocation()
        }

        if(!common_mapView.overlays.contains(initRotationGestureOverlay()))
        {
            common_mapView.overlays.add(initRotationGestureOverlay())

        }

        if(!common_mapView.overlays.contains(initScaleBarOverlay()))
        {
            common_mapView.overlays.add(initScaleBarOverlay())
            scaleBarOverlay.enableScaleBar()
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
                    initPresenter(locationManager,preferences)
                }
            }
        }
    }

    private fun initLocationOverlay(): MyLocationNewOverlay {
        val gpsMyLocationProvider = GpsMyLocationProvider(applicationContext)
        locationOverlay = if(LocationListener.imHere!=null){
            Log.d("CommonActivity","Init locationOverlay")
            gpsMyLocationProvider.addLocationSource(LocationListener.imHere!!.provider)
            MyLocationNewOverlay(gpsMyLocationProvider,common_mapView)
        } else {
            MyLocationNewOverlay(common_mapView)
        }
        locationOverlay.setDirectionArrow(presenter.customIcon(R.drawable.ic_navigator_icon,applicationContext),presenter.customIcon(R.drawable.ic_navigator_active_icon,applicationContext))
        locationOverlay.isDrawAccuracyEnabled = false
        return locationOverlay
    }

    private fun initRotationGestureOverlay(): RotationGestureOverlay {
        rotationGestureOverlay = RotationGestureOverlay(common_mapView)
        rotationGestureOverlay.isEnabled = true
        common_mapView.setMultiTouchControls(true)
        return rotationGestureOverlay
    }

    private fun initScaleBarOverlay(): ScaleBarOverlay {
        scaleBarOverlay = ScaleBarOverlay(common_mapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay
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

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}