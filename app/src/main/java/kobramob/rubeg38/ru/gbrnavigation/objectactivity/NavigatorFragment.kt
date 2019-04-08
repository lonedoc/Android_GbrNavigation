package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kobramob.rubeg38.ru.gbrnavigation.R
import android.support.v4.app.Fragment
import android.util.DisplayMetrics
import android.widget.Toast
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.SharedPreferencesState
import kotlinx.android.synthetic.main.navigator_fragment.*
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
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
import java.util.*

class NavigatorFragment : Fragment() {

    var enableFollowMe = false
    private var timer = Timer()
    lateinit var oldLocation: GeoPoint
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.navigator_fragment, container, false)

        initMapView(rootView)
        addOverlays()
        createRoad()

        val bnv: BottomNavigationView = activity!!.findViewById(R.id.objectMenu)
        bnv.menu.getItem(1).isChecked = true

        mMapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                // TODO слушатель на карту
                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                // TODO слушатель на карту
                if (enableFollowMe) {
                    timer.cancel()
                    follow_me_fragment.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                    follow_me_fragment.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                    enableFollowMe = false
                }
                return true
            }
        })

        val follow_me: FloatingActionButton = rootView.findViewById(R.id.follow_me_fragment)
        val my_location: FloatingActionButton = rootView.findViewById(R.id.my_location_fragment)

        follow_me.setOnClickListener {
            if (enableFollowMe) {

                /*startActivityModel.stopFollowMe()*/
                timer.cancel()
                follow_me.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                follow_me.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                enableFollowMe = false
            } else {
                /*startActivityModel.followMe(mMapView,locationOverlay)*/
                try {
                    oldLocation = locationOverlay.myLocation
                    setCenter()
                    mMapView.controller.setZoom(15.0)
                    mMapView.mapOrientation = - locationOverlay.lastFix.bearing

                    timer = Timer()
                    timer.scheduleAtFixedRate(NavigatorTask(), 0, 1000)
                    follow_me.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                    follow_me.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))

                    enableFollowMe = true
                } catch (e: Exception) {}
            }
        }

        my_location.setOnClickListener {
            if (enableFollowMe) {
                timer.cancel()
                follow_me.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                follow_me.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                enableFollowMe = false
            }

            mMapView.controller.animateTo(locationOverlay.myLocation)
            SharedPreferencesState.init(context!!)
            SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
            SharedPreferencesState.addPropertyFloat("lon", locationOverlay.myLocation.longitude.toFloat())
        }

        val yandex_api: FloatingActionButton = rootView.findViewById(R.id.yandex_api)
        yandex_api.setOnClickListener {
            val uri = Uri.parse("yandexnavi://build_route_on_map?lat_to=" + 56.31585 + "&lon_to=" + 101.75473 + "")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("ru.yandex.yandexnavi")
            startActivity(intent)
        }

        return rootView
    }

    private lateinit var mMapView: MapView
    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay

    private fun createRoad() {
        val roadManager = OSRMRoadManager(context)
        roadManager.setService("http:192.168.1.95:5000/route/v1/driving/")
        roadManager.setUserAgent(BuildConfig.APPLICATION_ID)
        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(
            GeoPoint(
                context!!.getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lat", 0f).toDouble(),
                context!!.getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lon", 0f).toDouble()
            )
        )
        waypoints.add(GeoPoint(56.31585, 101.75473))
        val createRoad = Runnable {
            val road = roadManager.getRoad(waypoints)
            if (road.mRouteHigh.size != 2) {
                val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                activity!!.runOnUiThread {
                    mMapView.overlays.add(roadOverlay)
                }
            } else {
                createRoad()
            }
        }
        Thread(createRoad).start()
    }

    private inner class NavigatorTask : TimerTask() {

        override fun run() {
            activity!!.runOnUiThread {
                try {
                    if (oldLocation.distanceToAsDouble(locationOverlay.myLocation)> 20.0) {
                        mMapView.mapOrientation = - locationOverlay.lastFix.bearing
                        setCenter()
                        oldLocation = locationOverlay.myLocation
                    }
                } catch (e: Exception) {}
            }
        }
    }

    private fun setCenter() {
        val density = resources.displayMetrics.densityDpi
        println("Плотность пикселей $density")
        when (density) {
            480 -> {
                mMapView.controller.animateTo(
                    locationOverlay.myLocation.destinationPoint(
                        (2*scaleBarOverlay.screenHeight / 4).toDouble(),
                        ((locationOverlay.lastFix.bearing)).toDouble()
                    )
                )
            }
            320 -> {
                mMapView.controller.animateTo(
                    locationOverlay.myLocation.destinationPoint(
                        (2*scaleBarOverlay.screenHeight / 4).toDouble(),
                        ((locationOverlay.lastFix.bearing)).toDouble()
                    )
                )
            }
            DisplayMetrics.DENSITY_LOW ->
                {
                    mMapView.controller.animateTo(
                        locationOverlay.myLocation.destinationPoint(
                            (3*scaleBarOverlay.screenHeight / 4).toDouble(),
                            ((locationOverlay.lastFix.bearing)).toDouble()
                        )
                    )
                }
            DisplayMetrics.DENSITY_MEDIUM -> {
                mMapView.controller.animateTo(
                    locationOverlay.myLocation.destinationPoint(
                        (3*scaleBarOverlay.screenHeight / 4).toDouble(),
                        ((locationOverlay.lastFix.bearing)).toDouble()
                    )
                )
            }
            DisplayMetrics.DENSITY_HIGH -> {
                mMapView.controller.animateTo(
                    locationOverlay.myLocation.destinationPoint(
                        (3*scaleBarOverlay.screenHeight / 4).toDouble(),
                        ((locationOverlay.lastFix.bearing)).toDouble()
                    )
                )
            }
            DisplayMetrics.DENSITY_XHIGH -> {
                mMapView.controller.animateTo(
                    locationOverlay.myLocation.destinationPoint(
                        (3*scaleBarOverlay.screenHeight / 4).toDouble(),
                        ((locationOverlay.lastFix.bearing)).toDouble()
                    )
                )
            }
            DisplayMetrics.DENSITY_XXHIGH -> {
                mMapView.controller.animateTo(
                    locationOverlay.myLocation.destinationPoint(
                        (3*scaleBarOverlay.screenHeight / 4).toDouble(),
                        ((locationOverlay.lastFix.bearing)).toDouble()
                    )
                )
            }
            DisplayMetrics.DENSITY_XXXHIGH ->
                {
                    mMapView.controller.animateTo(
                        locationOverlay.myLocation.destinationPoint(
                            (3*scaleBarOverlay.screenHeight / 4).toDouble(),
                            ((locationOverlay.lastFix.bearing)).toDouble()
                        )
                    )
                }
            DisplayMetrics.DENSITY_TV -> {
                mMapView.controller.animateTo(
                    locationOverlay.myLocation.destinationPoint(
                        (3*scaleBarOverlay.screenHeight / 4).toDouble(),
                        ((locationOverlay.lastFix.bearing)).toDouble()
                    )
                )
            }
            else -> {
                mMapView.controller.animateTo(
                    locationOverlay.myLocation.destinationPoint(
                        (scaleBarOverlay.screenHeight / 3).toDouble(),
                        ((locationOverlay.lastFix.bearing)).toDouble()
                    )
                )
            }
        }
    }

    private fun initMapView(rootView: View) {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        mMapView = rootView.findViewById(R.id.navigator_view)
        mMapView.setTileSource(TileSourceFactory.MAPNIK)
        mMapView.setHasTransientState(true)
        mMapView.controller.setZoom(15.0)
        mMapView.isTilesScaledToDpi = true
        mMapView.isFlingEnabled = true

        try {
            mMapView.controller.animateTo(
                GeoPoint(
                    context!!.getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lat", 0f).toDouble(),
                    context!!.getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lon", 0f).toDouble()
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Ваше месторасположение не определено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addOverlays() {
        addOverlays(initLocationOverlay())
        addOverlays(initRotationGestureOverlay())
        addOverlays(initScaleBarOverlay())
    }

    private fun initRotationGestureOverlay(): RotationGestureOverlay {
        rotationGestureOverlay = RotationGestureOverlay(mMapView)
        rotationGestureOverlay.isEnabled = true
        mMapView.setMultiTouchControls(true)
        return rotationGestureOverlay
    }

    private fun initScaleBarOverlay(): ScaleBarOverlay {
        scaleBarOverlay = ScaleBarOverlay(mMapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay
    }

    private fun initLocationOverlay(): MyLocationNewOverlay {
        val gpsMyLocationProvider = GpsMyLocationProvider(context)
        gpsMyLocationProvider.locationUpdateMinDistance = 0f
        gpsMyLocationProvider.locationUpdateMinTime = 0
        locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider, mMapView)
        locationOverlay.setDirectionArrow(customIcon(R.drawable.navigator), customIcon(R.drawable.navigator))
        locationOverlay.isDrawAccuracyEnabled = false
        return locationOverlay
    }

    private fun addOverlays(overlay: Overlay) {
        mMapView.overlays.add(overlay)
    }

    private fun customIcon(drawable: Int): Bitmap? {
        return BitmapFactory.decodeResource(
            resources,
            drawable
        )
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        locationOverlay.enableMyLocation()
        scaleBarOverlay.enableScaleBar()
    }

    override fun onStop() {
        super.onStop()
        mMapView.onPause()
        SharedPreferencesState.init(context!!)
        SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
        SharedPreferencesState.addPropertyFloat("lon", locationOverlay.myLocation.longitude.toFloat())
        //  locationOverlay.disableMyLocation()
        scaleBarOverlay.disableScaleBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}