package newVersion.alarm.navigator

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.ColorStateList
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import newVersion.utils.Alarm
import newVersion.servicess.LocationListener.Companion.imHere
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
import java.lang.Exception
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class NavigatorFragment: MvpAppCompatFragment(),NavigatorView {
    override fun addRoadOverlay(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            mMapView!!.overlays.add(roadOverlay)
        }
    }

    override fun removeRoadOverlay(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
          mMapView!!.overlays.remove(roadOverlay)
        }
    }

    override fun createRoad(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            if(!mMapView!!.overlays.contains(roadOverlay))
            mMapView!!.overlays.add(roadOverlay)

            mMapView!!.invalidate()
        }
    }

    override fun clearOverlay(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            mMapView?.overlays?.remove(roadOverlay)
            mMapView?.invalidate()
        }
    }

    override fun recreateTrack(roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            buildTrack = false
            mMapView?.overlays?.remove(roadOverlay)
            mMapView?.invalidate()

            val info = activity!!.intent.getSerializableExtra("info") as Alarm
            presenter.startTrack(info)
        }
    }

    override fun invalidateMapView(road: Road, roadOverlay: Polyline?) {
        activity!!.runOnUiThread {
            mMapView?.overlays?.remove(roadOverlay)
            mMapView?.overlays?.add(roadOverlay)
            mMapView?.invalidate()

        }
    }

    override fun setMarker(name:String?,endPoint: GeoPoint) {
        val marker = Marker(mMapView)
        marker.textLabelBackgroundColor = R.color.viewBackground
        marker.title = name
        marker.textLabelFontSize = 20
        marker.textLabelForegroundColor = R.color.colorPrimaryDark
        marker.icon = ContextCompat.getDrawable(activity!!, R.drawable.ic_arrivedtoobject)
        marker.position = endPoint
        mMapView!!.overlays.add(marker)
    }

    private var buildTrack = false
    override fun buildTrack(distance: Long,waypoints:ArrayList<GeoPoint>,routeServers:ArrayList<String>) {
        if(buildTrack) return

        buildTrack = true

        val roadManager = OSRMRoadManager(context)
        var road: Road? = null

        when{

            routeServers.count()>1->{
                for(i in 0 until routeServers.count())
                {
                    roadManager.setService("http:" + routeServers[i] + "/route/v1/driving/")
                    roadManager.setUserAgent(BuildConfig.APPLICATION_ID)
                    road = roadManager.getRoad(waypoints)

                    if(road?.mStatus != Road.STATUS_OK ||
                        presenter.distance(road) == null
                    )
                    {
                        if(i == routeServers.count() - 1)
                        {
                            showToastMessage("Невозможно проложить путь до объекта, не возможно подключится к серверу прокладки")
                            return
                        }

                    }
                    else
                    {
                        continue
                    }
                }
            }
            routeServers.count()==1->{
                roadManager.setService("http:" + routeServers[0] + "/route/v1/driving/")
                roadManager.setUserAgent(BuildConfig.APPLICATION_ID)
                road = roadManager.getRoad(waypoints)

                if(road?.mStatus != Road.STATUS_OK ||
                    presenter.distance(road) == null
                )
                {
                    showToastMessage("Невозможно проложить путь до объекта, ошибка сервера ${routeServers[0]}")
                    return
                }
            }
        }
        if(road!!.mRouteHigh!!.size < 3 && presenter.distance(road)!!>distance)
        {
            buildTrack = false
            buildTrack(distance,waypoints,routeServers)
            return
        }

        if(imHere==null)
            showToastMessage("Путь проложен от последней известной точки")
        else
            showToastMessage("Путь проложен от вашего нынешнего месторасположения")

        activity!!.runOnUiThread {
            buildTrack = false
            presenter.tracking(road,distance,waypoints[1])
        }
    }


    @InjectPresenter
    lateinit var presenter:NavigatorPresenter

    companion object{
        var isAlive = false
    }
    private var rootView:View? = null


    private var rotationGestureOverlay: RotationGestureOverlay? = null
    private var locationOverlay: MyLocationNewOverlay? = null
    private var scaleBarOverlay: ScaleBarOverlay? = null
    private var mMapView:MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_alarm_navigator,container,false)
        mMapView = rootView?.findViewById(R.id.alarm_navigator_mapView)

        val myLocation: FloatingActionButton = rootView?.findViewById(R.id.alarm_navigator_myLocation)!!
        val followMe:FloatingActionButton = rootView?.findViewById(R.id.alarm_navigator_followMe)!!
        val yandex:FloatingActionButton = rootView?.findViewById(R.id.alarm_navigator_yandex)!!


        myLocation.setOnClickListener {
            if (locationOverlay?.isFollowLocationEnabled!!) {
                locationOverlay?.disableFollowLocation()
                followMe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.viewBackground))
                followMe.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorPrimary))
            }

            presenter.setCenter(locationOverlay?.lastFix!!)

        }

        followMe.setOnClickListener {
            when {
                locationOverlay?.isFollowLocationEnabled!! -> {
                    locationOverlay?.disableFollowLocation()
                    followMe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.viewBackground))
                    followMe.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorPrimary))
                }
                !locationOverlay?.isFollowLocationEnabled!! -> {
                    locationOverlay?.enableFollowLocation()
                    followMe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorPrimary))
                    followMe.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.viewBackground))
                }
            }
        }

        yandex.setOnClickListener {
            val info = activity!!.intent.getSerializableExtra("info") as Alarm

            if(presenter.haveCoordinate(info)){
                val uri: Uri = Uri.parse("yandexnavi://build_route_on_map?lat_to=${info.lat}&lon_to=${info.lon}")
                val intent = Intent(Intent.ACTION_VIEW,uri)
                intent.setPackage("ru.yandex.yandexnavi")
                val packageManager = activity!!.packageManager
                val activities:List<ResolveInfo> = packageManager.queryIntentActivities(intent,0)
                val isIntentSafe = activities.isNotEmpty()
                if(isIntentSafe)
                {
                    activity!!.startActivity(intent)
                }
                else
                {
                    val playMarket = Intent(Intent.ACTION_VIEW)
                    playMarket.data = Uri.parse("market://details?id=ru.yandex.yandexnavi")
                    startActivity(playMarket)
                }
            }
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        isAlive = true
        val locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER)))
        {
            createSettingGpsDialog(locationManager)
        }
        else
        {

            presenter.init()
        }
    }

    override fun onPause() {
        super.onPause()
        isAlive = false
        presenter.onDestroy()
        buildTrack = false
    }

    override fun showToastMessage(message:String){
        activity!!.runOnUiThread {
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show()  
        }
    }

    override fun setCenter(geoPoint: GeoPoint) {
        try {
            mMapView?.controller?.animateTo(geoPoint)
            mMapView?.controller?.setZoom(15.0)

            if(mMapView!!.overlays.size<3){
                val info = activity!!.intent.getSerializableExtra("info") as Alarm
                presenter.startTrack(info)
            }
        }catch (e:Exception){
            Toast.makeText(activity,"Произошла ошибка",Toast.LENGTH_LONG).show()
        }


    }

    private var waitCoordinate = false
    override fun waitCoordinate() {
        if (waitCoordinate) return
        waitCoordinate = true

        thread {
            while(locationOverlay!!.lastFix == null)
            {
                if(!isAlive)
                {
                    waitCoordinate = false
                    return@thread
                }
                sleep(5000)

            }

            activity!!.runOnUiThread {
                mMapView!!.invalidate()
                presenter.setCenter(locationOverlay!!.lastFix)
                waitCoordinate = false
                showToastMessage("Удалось определить ваше последнее месторасположение")

                if(mMapView!!.overlays.size<3){
                    val info = activity!!.intent.getSerializableExtra("info") as Alarm
                    presenter.startTrack(info)
                }
            }
        }
    }

    override fun initMapView() {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        mMapView?.setTileSource(TileSourceFactory.MAPNIK)
        mMapView?.setHasTransientState(true)
        mMapView?.controller?.setZoom(15.0)
        mMapView?.isTilesScaledToDpi = true
        mMapView?.isFlingEnabled = true

        presenter.setCenter(imHere)
    }

    override fun addOverlays() {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        initLocationOverlay()
        initRotationGestureOverlay()
        initScaleBarOverlay()

        if (locationOverlay != null) {
            mMapView?.overlays?.add(locationOverlay)
            locationOverlay?.enableMyLocation()
        }

        if (rotationGestureOverlay != null) {
            mMapView?.overlays?.add(rotationGestureOverlay)
        }

        if (scaleBarOverlay != null) {
            mMapView?.overlays?.add(scaleBarOverlay)
            scaleBarOverlay?.enableScaleBar()
        }
    }

    private fun initLocationOverlay(): MyLocationNewOverlay {
        val gpsMyLocationProvider = GpsMyLocationProvider(context)
        locationOverlay = if (imHere != null) {
            Log.d("CommonActivity", "Init locationOverlay")
            gpsMyLocationProvider.addLocationSource(imHere!!.provider)
            MyLocationNewOverlay(gpsMyLocationProvider, mMapView)
        } else {
            gpsMyLocationProvider.clearLocationSources()
            gpsMyLocationProvider.addLocationSource(LocationManager.GPS_PROVIDER)
            MyLocationNewOverlay(gpsMyLocationProvider, mMapView)
        }
        locationOverlay?.setDirectionArrow(presenter.customIcon(R.drawable.ic_navigator_icon, context!!), presenter.customIcon(R.drawable.ic_navigator_active_icon, context!!))
        locationOverlay?.isDrawAccuracyEnabled = false
        return locationOverlay!!
    }

    private fun initRotationGestureOverlay(): RotationGestureOverlay {
        rotationGestureOverlay = RotationGestureOverlay(mMapView)
        rotationGestureOverlay?.isEnabled = true
        mMapView?.setMultiTouchControls(true)
        return rotationGestureOverlay!!
    }

    private fun initScaleBarOverlay(): ScaleBarOverlay {
        scaleBarOverlay = ScaleBarOverlay(mMapView)
        scaleBarOverlay?.setCentred(true)
        scaleBarOverlay?.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay!!
    }
    
    private fun createSettingGpsDialog(locationManager: LocationManager) {
        val builder = AlertDialog.Builder(context)
            .setMessage("GPS отключен (Приложение не работает без GPS")
            .setCancelable(false)
            .setPositiveButton("Включить")
            {
                _,_->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .create()
        builder.show()
        thread{
            while (!locationManager.isProviderEnabled((LocationManager.GPS_PROVIDER))){
                sleep(2_000)
            }
            presenter.init()
        }
    }


}