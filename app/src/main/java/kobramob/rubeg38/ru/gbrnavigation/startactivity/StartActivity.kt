package kobramob.rubeg38.ru.gbrnavigation.startactivity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.SensorEvent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.view.GestureDetector
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import org.osmdroid.api.IMapView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class StartActivity : AppCompatActivity() {

    lateinit var followButton:FloatingActionButton
    lateinit var centerButton:FloatingActionButton

    private lateinit var mMapView:MapView
    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        mMapView = findViewById(R.id.startMap)

        initMapView(mMapView)
        addOverlays(mMapView)

        println("Hello,world")


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

    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        locationOverlay.enableMyLocation()
        scaleBarOverlay.enableScaleBar()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
        locationOverlay.disableMyLocation()
        scaleBarOverlay.disableScaleBar()
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

    private fun  initLocationOverlay(MapView: MapView): MyLocationNewOverlay {
        val gpsMyLocationProvider = GpsMyLocationProvider(this)
        gpsMyLocationProvider.locationUpdateMinDistance = 0f
        gpsMyLocationProvider.locationUpdateMinTime = 0
        locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider,MapView)
        locationOverlay.setDirectionArrow(customIcon(),customIcon())
        locationOverlay.isDrawAccuracyEnabled = false
        return locationOverlay
    }

    private fun addOverlays(MapView:MapView,overlay:Overlay) {
        MapView.overlays.add(overlay)
    }

    private fun customIcon(): Bitmap? {
        return BitmapFactory.decodeResource(
            resources,
            R.drawable.navigator
        )
    }
}
