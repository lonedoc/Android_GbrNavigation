package gbr.ui.mobnavigator

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import gbr.presentation.presenter.mobnavigator.MobNavigatorPresenter
import gbr.presentation.view.mobnavigator.MobNavigatorView
import gbr.utils.data.AlarmInfo
import gbr.utils.servicess.ProtocolService
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.ArrayList
import javax.inject.Inject
import kotlin.concurrent.thread

class FragmentMobNavigator:MvpAppCompatFragment(), MobNavigatorView {
    @InjectPresenter
    lateinit var presenter: MobNavigatorPresenter

    lateinit var rootView:View
    lateinit var mapView:MapView

    lateinit var locationOverlay: MyLocationNewOverlay
    lateinit var rotationGestureOverlay: RotationGestureOverlay
    lateinit var scaleBarOverlay: ScaleBarOverlay

    companion object{
        var isAlive = false
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_mobalarm_navigator,container,false)

        mapView = rootView.findViewById(R.id.mobalarm_mapView)
        val myLocation:FloatingActionButton = rootView.findViewById(R.id.mobalarm_myLocation)
        val followMe:FloatingActionButton = rootView.findViewById(R.id.mobalarm_followMe)
        marker = Marker(mapView)
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

    var marker:Marker? = null
    override fun setMarker(name: String?, objectLocation: GeoPoint) {
        activity!!.runOnUiThread {

            if(mapView.overlays.contains(marker)) mapView.overlays.remove(marker)
            marker!!.textLabelBackgroundColor = R.color.viewBackground
            marker!!.title = name
            marker!!.textLabelFontSize = 20
            marker!!.textLabelForegroundColor = R.color.colorPrimaryDark
            marker!!.icon = ContextCompat.getDrawable(activity!!, R.drawable.ic_arrivedtoobject)
            marker!!.position = objectLocation
            mapView.overlays.add(marker)
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
}