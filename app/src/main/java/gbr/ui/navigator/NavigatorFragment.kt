package gbr.ui.navigator

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import gbr.presentation.presenter.navigator.NavigatorPresenter
import gbr.presentation.view.navigator.NavigatorView
import gbr.utils.data.AlarmInfo
import gbr.utils.servicess.ProtocolService
import gbr.utils.servicess.YandexNavigator
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_new_main.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import newVersion.alarm.AlarmActivity
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.ArrayList
import kotlin.concurrent.thread

class NavigatorFragment:MvpAppCompatFragment(),NavigatorView {
    @InjectPresenter
    lateinit var presenter:NavigatorPresenter

    lateinit var locationOverlay:MyLocationNewOverlay
    lateinit var rotationGestureOverlay: RotationGestureOverlay
    lateinit var scaleBarOverlay: ScaleBarOverlay

    lateinit var rootView:View
    val alarmInfo = AlarmInfo
    val yandexNavigator:YandexNavigator = YandexNavigator()

    lateinit var mapView:MapView

    companion object{
        var isAlive = false
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.new_fragment_navigator,container,false)

        val yandexButton = rootView.findViewById<FloatingActionButton>(R.id.navigator_yandex)

        val navitelButton = rootView.findViewById<FloatingActionButton>(R.id.navigator_navitel)

        val myLocation = rootView.findViewById<FloatingActionButton>(R.id.navigator_myLocation)

        val followMe = rootView.findViewById<FloatingActionButton>(R.id.navigator_followMe)

        mapView = rootView.findViewById(R.id.navigator_mapView)

        yandexButton.setOnClickListener {
            yandexNavigator()
        }

        navitelButton.setOnClickListener {
            navitelNavigator()
        }

        myLocation.setOnClickListener {
            if(locationOverlay.myLocation == null)
                showToastMessage("Ваше месторасположение не определенно")
            else
                mapView.controller.setCenter(locationOverlay.myLocation)
        }

        followMe.setOnClickListener {
            when {
                locationOverlay.myLocation == null -> showToastMessage("Ваше месторасположение не определенно")
                locationOverlay.isFollowLocationEnabled -> {
                    locationOverlay.disableFollowLocation()
                    followMe.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            activity!!,
                            R.color.viewBackground
                        )
                    )
                    followMe.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            activity!!,
                            R.color.colorPrimary
                        )
                    )
                }
                else -> {
                    locationOverlay.enableAutoStop = false

                    locationOverlay.enableFollowLocation()
                    followMe.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            activity!!,
                            R.color.colorPrimary
                        )
                    )
                    followMe.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            activity!!,
                            R.color.viewBackground
                        )
                    )
                }
            }


        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        isAlive = true

    }

    override fun onPause() {
        super.onPause()
        isAlive = false
    }
    override fun onStop() {
        super.onStop()


    }

    override fun initMapView() {
        Log.d("initMapView","Init")
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setHasTransientState(true)
        mapView.controller.setZoom(15.0)
        mapView.isTilesScaledToDpi = true
        mapView.isFlingEnabled = true
    }

    override fun initOverlays() {
        if (mapView.overlays.count() >= 1)
            mapView.overlays.clear()

        locationOverlay = initLocationOverlay()
        rotationGestureOverlay = initRotationGestureOverlay()
        scaleBarOverlay = initScaleBarOverlay()

        mapView.overlays.add(locationOverlay)
        mapView.overlays.add(rotationGestureOverlay)
        mapView.overlays.add(scaleBarOverlay)

        locationOverlay.enableMyLocation()
        scaleBarOverlay.enableScaleBar()
        locationOverlay.isOptionsMenuEnabled = true

        thread{
            while(ProtocolService.currentLocation == null)
            {
                //
            }
            presenter.setCenter(GeoPoint(ProtocolService.currentLocation))
        }
    }

    override fun setCenter(geoPoint: GeoPoint) {
        activity!!.runOnUiThread {
            mapView.controller.animateTo(geoPoint)
            mapView.controller.setZoom(15.0)

        }
    }

    override fun showToastMessage(message: String) {
        activity!!.runOnUiThread {
            Toast.makeText(activity!!,message,Toast.LENGTH_LONG).show()
        }
    }

    override fun setMarket(name: String?, endPoint: GeoPoint) {
        val marker = Marker(mapView)
        marker.textLabelBackgroundColor = R.color.viewBackground
        marker.title = name
        marker.textLabelFontSize = 20
        marker.textLabelForegroundColor = R.color.colorPrimaryDark
        marker.icon = ContextCompat.getDrawable(activity!!, R.drawable.ic_arrivedtoobject)
        marker.position = endPoint
        mapView.overlays.add(marker)
    }

    var buildTrack = false
    override fun buildTrack(
        distance: Int,
        waypoint: ArrayList<GeoPoint>,
        routeServers: ArrayList<String>
    ) {
        thread {
            if(buildTrack) return@thread

            buildTrack = true

            val roadManager = OSRMRoadManager(activity!!)
            var road: Road? = null

            for(i in 0 until routeServers.count())
            {
                roadManager.setService("http:" + routeServers[i] + "/route/v1/driving/")
                roadManager.setUserAgent(BuildConfig.APPLICATION_ID)
                road = roadManager.getRoad(waypoint)

                if(road.mRouteHigh.size<3)
                {
                    if(i == routeServers.count() - 1)
                    {
                        showToastMessage("Невозможно проложить путь до объекта, нет подключения с сервером")
                        return@thread
                    }

                }
                else
                {
                    break
                }
            }
            if(road!!.mRouteHigh!!.size < 3 && presenter.distance(road)!!>distance)
            {
                buildTrack = false
                buildTrack(distance,waypoint,routeServers)
                return@thread
            }

            buildTrack = false
            presenter.startTracking(road,distance,waypoint[1])
        }
    }

    override fun initRoadOverlay(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            if(!mapView.overlays.contains(roadOverlay))
                mapView.overlays.add(roadOverlay)

            mapView.invalidate()
        }
    }

    override fun addRoadOverlay(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            mapView.overlays.add(roadOverlay)
        }
    }

    override fun removeRoadOverlay(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            mapView.overlays.remove(roadOverlay)
        }
    }

    override fun clearOverlay(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            mapView.overlays?.remove(roadOverlay)
            mapView.invalidate()
        }
    }

    override fun recreateTrack(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            buildTrack = false
            mapView.overlays?.remove(roadOverlay)
            mapView.invalidate()

            presenter.createRoute()
        }
    }

    private fun initLocationOverlay(): MyLocationNewOverlay {
        Log.d("LocationOverlay","Init")
        val locationOverlay = MyLocationNewOverlay(mapView)
        locationOverlay.setDirectionArrow(
            presenter.customIcon(
                R.drawable.ic_navigator_icon,
                activity!!
            ), presenter.customIcon(R.drawable.ic_navigator_active_icon, activity!!)
        )
        locationOverlay.isDrawAccuracyEnabled = false

        return locationOverlay
    }

    private fun initRotationGestureOverlay(): RotationGestureOverlay {
        val rotationGestureOverlay = RotationGestureOverlay(mapView)
        rotationGestureOverlay.isEnabled = true
        mapView.setMultiTouchControls(true)
        return rotationGestureOverlay
    }

    private fun initScaleBarOverlay(): ScaleBarOverlay {
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay
    }

    private fun navitelNavigator() {
        val uri = Uri.parse("google.navigation:ll=${alarmInfo.lat},${alarmInfo.lon}")
        var intent = Intent(Intent.ACTION_VIEW,uri)
        intent.setPackage("com.navitel")
        if(intent.resolveActivity(activity!!.packageManager) == null)
        {
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=com.navitel")
        }
        activity!!.startActivity(intent)
    }

    private fun yandexNavigator() {
        yandexNavigator.buildRoute(activity!!,alarmInfo.lat!!,alarmInfo.lon!!)
    }

}