package kobramob.rubeg38.ru.gbrnavigation.startactivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.service.PollingServer
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.lang.Exception

class StartActivity : AppCompatActivity() {

    companion object {
        const val BROADCAST_ACTION = "kobramob.ruber38.ru.gbrnavigation.startactivity"
    }

    lateinit var followButton:FloatingActionButton
    lateinit var centerButton:FloatingActionButton

    private lateinit var mMapView:MapView
    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED      ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                101
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        checkPermission()

        mMapView = findViewById(R.id.startMap)

        initMapView(mMapView)
        addOverlays(mMapView)

        mMapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                //TODO слушатель на карту
                //Toast.makeText(getActivity(), "onScroll", Toast.LENGTH_SHORT).show();
                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                //TODO слушатель на карту
                return true
            }
        })

        centerButton = findViewById(R.id.my_location)

        centerButton.setOnClickListener {
            try {
                mMapView.controller.animateTo(locationOverlay.myLocation)
                SharedPreferencesState.init(this)
                SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
                SharedPreferencesState.addPropertyFloat("lon",locationOverlay.myLocation.longitude.toFloat())
            }catch (e:Exception)
            {
                Toast.makeText(this,"Ваше месторасположение не определено",Toast.LENGTH_SHORT).show()
            }
        }

        try {
            mMapView.controller.animateTo(
                GeoPoint(
                    getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lat",0f).toDouble(),
                    getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lon",0f).toDouble())
            )
        }
        catch (e:Exception)
        {
            Toast.makeText(this,"Ваше месторасположение не определено",Toast.LENGTH_SHORT).show()
        }



    }

    private lateinit var br:BroadcastReceiver
    private fun broadcastReceiver (){
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val request = intent?.getStringExtra("test")
                println(request)
            }
        }
        val intentFilter = IntentFilter(BROADCAST_ACTION)
        registerReceiver(br,intentFilter)
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        locationOverlay.enableMyLocation()
        scaleBarOverlay.enableScaleBar()
        broadcastReceiver()
        startService(Intent(this,PollingServer::class.java))
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
        locationOverlay.disableMyLocation()
        scaleBarOverlay.disableScaleBar()
    }

    override fun onStop() {
        super.onStop()
        SharedPreferencesState.init(this)
        SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
        SharedPreferencesState.addPropertyFloat("lon",locationOverlay.myLocation.longitude.toFloat())
        unregisterReceiver(br)
        stopService(Intent(this,PollingServer::class.java))
    }

    private fun initMapView(MapView:MapView){
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        MapView.setTileSource(TileSourceFactory.MAPNIK)
        MapView.setHasTransientState(true)
        MapView.controller.setZoom(15.0)
        MapView.isTilesScaledToDpi = true
        MapView.isFlingEnabled = true


    }

    private fun addOverlays(MapView:MapView){
        addOverlays(MapView,initLocationOverlay(MapView))
        addOverlays(MapView,initScaleBarOverlay(MapView))
        addOverlays(MapView,initRotationGestureOverlay(MapView))

    }

    private fun initRotationGestureOverlay(MapView: MapView): RotationGestureOverlay {
        rotationGestureOverlay = RotationGestureOverlay(MapView)
        rotationGestureOverlay.isEnabled = true
        MapView.setMultiTouchControls(true)
        return rotationGestureOverlay
    }

    private fun initScaleBarOverlay(MapView:MapView): ScaleBarOverlay {
        scaleBarOverlay= ScaleBarOverlay(MapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels/2,10)
        return scaleBarOverlay
    }

    private fun initLocationOverlay(MapView: MapView): MyLocationNewOverlay {
        val gpsMyLocationProvider = GpsMyLocationProvider(this)
        gpsMyLocationProvider.locationUpdateMinDistance = 0f
        gpsMyLocationProvider.locationUpdateMinTime = 0
        locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider,MapView)
        locationOverlay.setDirectionArrow(customIcon(R.drawable.navigator),customIcon(R.drawable.navigator))
        locationOverlay.isDrawAccuracyEnabled = false
        return locationOverlay
    }

    private fun addOverlays(MapView:MapView,overlay:Overlay) {
        MapView.overlays.add(overlay)
    }

    private fun customIcon(drawable:Int): Bitmap? {
        return BitmapFactory.decodeResource(
            resources,
            drawable
        )
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}
