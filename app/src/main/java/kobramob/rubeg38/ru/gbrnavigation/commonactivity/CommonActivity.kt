package kobramob.rubeg38.ru.gbrnavigation.commonactivity

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.github.clans.fab.FloatingActionMenu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Exception
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.R.*
import kobramob.rubeg38.ru.gbrnavigation.R.id.*
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.service.MyLocation
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import kotlinx.android.synthetic.main.activity_common.view.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class CommonActivity : AppCompatActivity() {

    private val sizeNormal = 0
    private val sizeMini = 1
    private lateinit var mMapView: MapView

    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)

        mMapView = findViewById(common_mapView)

        val toolbar: Toolbar = findViewById(common_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Позывной"

        fillFabMenu()

        initMapView()

        Configuration.SCREENLAYOUT_SIZE_XLARGE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            server_setting -> {
                // Смена сервера
            }
            change_map -> {
                // Выбор основной карты
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun fabClick(view: View) {
        val followFab: FloatingActionButton = findViewById(common_followMe)
        when (view.id) {
            common_myLocation -> {
                try {
                    if (locationOverlay.isFollowLocationEnabled) {
                        locationOverlay.disableFollowLocation()
                        followFab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.viewBackground))
                        followFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.colorAccent))
                    }
                    mMapView.controller.animateTo(GeoPoint(MyLocation.imHere))

                    SharedPreferencesState.init(this)
                    SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
                    SharedPreferencesState.addPropertyFloat("lon", locationOverlay.myLocation.longitude.toFloat())
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Ваше месторасположение не определено", Toast.LENGTH_SHORT).show()
                }
            }
            common_followMe -> {

                if (locationOverlay.isFollowLocationEnabled) {
                    locationOverlay.disableFollowLocation()
                    followFab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.viewBackground))
                    followFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.colorAccent))
                } else {
                    locationOverlay.enableFollowLocation()
                    followFab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.colorAccent))
                    followFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.viewBackground))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        locationOverlay.enableMyLocation()
        scaleBarOverlay.enableScaleBar()
    }

    override fun onPause() {
        super.onPause()

        locationOverlay.disableMyLocation()
        scaleBarOverlay.disableScaleBar()

        if (locationOverlay.isFollowLocationEnabled)
            locationOverlay.disableFollowLocation()
    }

    private fun initMapView() {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        mMapView.setTileSource(TileSourceFactory.MAPNIK)
        mMapView.setHasTransientState(true)
        mMapView.controller.setZoom(15.0)
        mMapView.isTilesScaledToDpi = true
        mMapView.isFlingEnabled = true
        mMapView.controller.animateTo(GeoPoint(MyLocation.imHere!!.latitude, MyLocation.imHere!!.longitude))

        mMapView.overlays.add(locationOverlay())
        mMapView.overlays.add(initRotationGestureOverlay())
        mMapView.overlays.add(initScaleBarOverlay())
    }

    private fun locationOverlay(): MyLocationNewOverlay {
        val gpsMyLocationProvider = GpsMyLocationProvider(this)
        gpsMyLocationProvider.addLocationSource(MyLocation.imHere!!.provider)
        locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider, mMapView)
        locationOverlay.setDirectionArrow(customIcon(drawable.ic_navigator_icon), customIcon(drawable.ic_navigator_active_icon))
        locationOverlay.isDrawAccuracyEnabled = false
        return locationOverlay
    }

    private fun initRotationGestureOverlay(): RotationGestureOverlay {
        StartActivity.rotationGestureOverlay = RotationGestureOverlay(mMapView)
        StartActivity.rotationGestureOverlay.isEnabled = true
        mMapView.setMultiTouchControls(true)
        return StartActivity.rotationGestureOverlay
    }

    private fun initScaleBarOverlay(): ScaleBarOverlay {
        scaleBarOverlay = ScaleBarOverlay(mMapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay
    }

    private fun customIcon(drawable: Int): Bitmap? {

        val drawable = ContextCompat.getDrawable(this, drawable)

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun fillFabMenu() {

        val followFab: FloatingActionButton = findViewById(common_followMe)
        val locationFab: FloatingActionButton = findViewById(common_myLocation)
        when (resources.configuration.orientation) {
            ORIENTATION_LANDSCAPE -> {
                when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                    Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                        followFab.size = FloatingActionButton.SIZE_NORMAL
                        locationFab.size = FloatingActionButton.SIZE_NORMAL
                    }
                    Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                        followFab.size = FloatingActionButton.SIZE_NORMAL
                        locationFab.size = FloatingActionButton.SIZE_NORMAL
                    }
                    else -> {
                        followFab.size = FloatingActionButton.SIZE_MINI
                        locationFab.size = FloatingActionButton.SIZE_MINI
                    }
                }
            }
            ORIENTATION_PORTRAIT -> {
                when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                    Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                        followFab.size = FloatingActionButton.SIZE_NORMAL
                        locationFab.size = FloatingActionButton.SIZE_NORMAL
                    }
                    Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                        followFab.size = FloatingActionButton.SIZE_NORMAL
                        locationFab.size = FloatingActionButton.SIZE_NORMAL
                    }
                    else -> {
                        followFab.size = FloatingActionButton.SIZE_MINI
                        locationFab.size = FloatingActionButton.SIZE_MINI
                    }
                }
            }
        }

        val statusList: ArrayList<String> = ArrayList()
        statusList.add("Свободен")
        statusList.add("Обед")
        statusList.add("Ремонт")
        statusList.add("Заправляется")
        statusList.add("Отпуск")

        statusList.sort()

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val heightPixels = metrics.heightPixels
        val widthPixels = metrics.widthPixels
        val densityDpi = metrics.densityDpi
        val xdpi = metrics.xdpi
        val ydpi = metrics.ydpi
        Log.i("CommonActivity", "widthPixels  = $widthPixels")
        Log.i("CommonActivity", "heightPixels = $heightPixels")
        Log.i("CommonActivity", "densityDpi   = $densityDpi")
        Log.i("CommonActivity", "xdpi         = $xdpi")
        Log.i("CommonActivity", "ydpi         = $ydpi")

        val fabMenu: FloatingActionMenu = findViewById(common_fab_menu)
        for (i in 0 until statusList.count()) {
            val actionButton = com.github.clans.fab.FloatingActionButton(this)
            actionButton.labelText = statusList[i]
            actionButton.setOnClickListener {
                if (fabMenu.isOpened) {
                    fabMenu.close(true)
                }
            }
            when (statusList[i]) {
                "Заправляется" -> { actionButton.setImageResource(drawable.ic_refueling) }
                "Обед" -> { actionButton.setImageResource(drawable.ic_dinner) }
                "Ремонт" -> { actionButton.setImageResource(drawable.ic_repairs) }
                "Свободен" -> { actionButton.setImageResource(drawable.ic_freedom) }
                else -> { actionButton.setImageResource(drawable.ic_unknown_status) }
            }
            when (resources.configuration.orientation) {
                ORIENTATION_LANDSCAPE -> {
                    when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                        Configuration.SCREENLAYOUT_SIZE_LARGE -> { actionButton.buttonSize = sizeNormal }
                        Configuration.SCREENLAYOUT_SIZE_XLARGE -> { actionButton.buttonSize = sizeNormal }
                        else -> { actionButton.buttonSize = sizeMini }
                    }
                }
                ORIENTATION_PORTRAIT -> {
                    when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                        Configuration.SCREENLAYOUT_SIZE_LARGE -> { actionButton.buttonSize = sizeNormal }
                        Configuration.SCREENLAYOUT_SIZE_XLARGE -> { actionButton.buttonSize = sizeNormal }
                        else -> { actionButton.buttonSize = sizeMini }
                    }
                }
            }

            fabMenu.addMenuButton(actionButton)
        }
    }
}