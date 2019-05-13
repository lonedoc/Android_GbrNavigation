package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kobramob.rubeg38.ru.gbrnavigation.R
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.DisplayMetrics
import android.widget.LinearLayout
import android.widget.Toast
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.service.PollingServer
import kobramob.rubeg38.ru.gbrnavigation.service.Request
import kotlinx.android.synthetic.main.navigator_fragment.*
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
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

class NavigatorFragment : Fragment(), MapEventsReceiver {

    private val request: Request = Request()
    var enableFollowMe = false
    private var timer = Timer()
    lateinit var oldLocation: GeoPoint
    var lat: Double = 0.0
    var lon: Double = 0.0

    private lateinit var mMapView: MapView
    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay
    private var arrivedToObject: Boolean = false
    private var progressBarOpen: String = "close"
    private lateinit var cancelDialog: AlertDialog
    var latitude: Double = 0.toDouble()

    companion object {
        var firstTime = true
    }
    lateinit var jsonArray: JSONArray

    private fun showProgressBar() {
        if (progressBarOpen == "close") {
            val dialog = AlertDialog.Builder(activity!!)
            val dialogView = layoutInflater.inflate(R.layout.progress_bar_location, null)
            dialog.setView(dialogView)
            dialog.setCancelable(false)
            cancelDialog = dialog.create()
            cancelDialog.show()
            progressBarOpen = "open"
        }
    }

    private fun closeProgressBar() {
        if (progressBarOpen == "open") {
            cancelDialog.cancel()
            progressBarOpen = "close"
        }
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        if (enableFollowMe) {
            timer.cancel()
            follow_me_fragment.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
            follow_me_fragment.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
            enableFollowMe = false
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        println("onCreateView")
        val rootView = inflater.inflate(R.layout.navigator_fragment, container, false)

        val jsonObject = JSONObject(activity!!.intent.getStringExtra("info"))
        jsonArray = jsonObject.getJSONArray("d")
        lat = JSONObject(jsonArray.getString(0)).getDouble("lat")
        lon = JSONObject(jsonArray.getString(0)).getDouble("lon")
        initMapView(rootView)

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

            try {
            } catch (e: Exception) { Toast.makeText(activity!!, "Место расположение не определено", Toast.LENGTH_SHORT).show() }
            mMapView.controller.animateTo(locationOverlay.myLocation)
            SharedPreferencesState.init(context!!)
            SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
            SharedPreferencesState.addPropertyFloat("lon", locationOverlay.myLocation.longitude.toFloat())
        }

        val yandex_api: FloatingActionButton = rootView.findViewById(R.id.yandex_api)
        yandex_api.setOnClickListener {
            if(lat!=0.0 && lon!=0.0) {
                try {
                    val uri = Uri.parse("yandexnavi://build_route_on_map?lat_to=$lat&lon_to=$lon")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("ru.yandex.yandexnavi")
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(activity, "На данном устройстве не установлен Яндекс.Навигатор", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            else
            {
                Toast.makeText(activity!!,"Не были указаны координаты до конечного объекта",Toast.LENGTH_LONG).show()
            }
        }

        return rootView
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



    private fun createRoad(){

        val roadManager = OSRMRoadManager(context)

        roadManager.setService("http:" + activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getString("routeserver", "") + "/route/v1/driving/")
        roadManager.setUserAgent(BuildConfig.APPLICATION_ID)

        val waypoints = ArrayList<GeoPoint>()

        val startPoint = GeoPoint(
            activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lat", 0f).toDouble(),
            activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lon", 0f).toDouble()
        )
        val endPoint = GeoPoint(lat, lon)

        waypoints.add(startPoint)

        waypoints.add(endPoint)
        val createRoad = Runnable {
            val road = roadManager.getRoad(waypoints)
            if(road.mRouteHigh.size>2){
                val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                activity!!.runOnUiThread {
                    mMapView.overlays.add(0, roadOverlay)
                    road.mRouteHigh.add(0, startPoint)
                    //tracking
                    tracking(road)
                }
            }
            else
            {
                if(distance(road)>60)
                createRoad()
                else
                {
                    tracking(road)
                    //tracking
                }
            }
        }
        Thread(createRoad).start()
    }

    private fun tracking(road: Road) {
        val updateTrackingThread = object : Thread() {
            override fun run() {
                super.run()
                try {
                    var oldDistance = distance(road)
                    while (!arrivedToObject) {
                        println(arrivedToObject)
                        sleep(100)
                        if (road.mRouteHigh.size> 2) {
                            if (distance(road)> oldDistance + 40) {
                                activity!!.runOnUiThread {
                                    road.mRouteHigh.clear()
                                    arrivedToObject = true
                                    mMapView.overlays.removeAt(0)
                                    val handler = Handler()
                                    handler.postDelayed(
                                        {
                                            createRoad()
                                        },
                                        500
                                    )
                                }
                            } else {
                                activity!!.runOnUiThread {
                                    if (distance(road) <40) {
                                        road.mRouteHigh.removeAt(1)
                                        if(mMapView.overlays.size>0)
                                            mMapView.overlays.removeAt(0)
                                        val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                                        mMapView.overlays.add(0, roadOverlay)
                                        oldDistance = distance(road)
                                        mMapView.invalidate()
                                    } else {
                                        try{
                                            road.mRouteHigh.removeAt(0)
                                            road.mRouteHigh.add(0, locationOverlay.myLocation)
                                            if(mMapView.overlays.size>0)
                                                mMapView.overlays.removeAt(0)
                                            val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                                            mMapView.overlays.add(0, roadOverlay)
                                            mMapView.invalidate()
                                        }catch (e:Exception){
                                            e.printStackTrace()
                                        }

                                    }
                                }
                            }
                        } else {
                            if (road.mRouteHigh.size> 0) {
                                if (distance(road) < 30.0) {

                                    road.mRouteHigh.clear()
                                    activity!!.runOnUiThread {
                                        val alertDialog = AlertDialog.Builder(activity!!)
                                        val view = layoutInflater.inflate(R.layout.arrived_dialog, null)
                                        alertDialog.setView(view)
                                        val dialog: AlertDialog = alertDialog.create()
                                        dialog.show()
                                        val arrive_button: LinearLayout = view!!.findViewById(R.id.arrived_button)
                                        arrive_button.setOnClickListener {
                                            val arriveThread = Runnable {
                                                arrivedToObject(jsonArray)
                                                dialog.cancel()
                                            }; Thread(arriveThread).start()
                                        }
                                        mMapView.overlays.removeAt(0)
                                        arrivedToObject = true
                                    }
                                } else {
                                    activity!!.runOnUiThread {
                                        road.mRouteHigh.removeAt(0)
                                        road.mRouteHigh.add(0, locationOverlay.myLocation)
                                        mMapView.overlays.removeAt(0)
                                        val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                                        mMapView.overlays.add(0, roadOverlay)
                                        mMapView.invalidate()
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }; updateTrackingThread.start()
    }

    private fun arrivedToObject(jsonArray: JSONArray) {
        val arrivedToObject = Runnable {
            request.arrivedToObject(
                PollingServer.socket,
                PollingServer.countSender,
                JSONObject(jsonArray.getString(0)).getString("number"),
                0,
                activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", ""),
                activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 0),
                activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", "")
            )
        }; Thread(arrivedToObject).start()
        PollingServer.countSender++
    }

    private fun distance(road: Road): Float {
        return try {
            val locationA = Location("point A")
            locationA.latitude = road.mRouteHigh[0].latitude
            locationA.longitude = road.mRouteHigh[0].longitude
            val locationB = Location("point B")
            locationB.latitude = road.mRouteHigh[1].latitude
            locationB.longitude = road.mRouteHigh[1].longitude
            locationA.distanceTo(locationB)
        } catch (e: Exception) {
            0f
        }
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
        mMapView.onResume()
        println(mMapView.overlays.size)
        if (firstTime) {
            if(mMapView.overlays.size==0){
                addOverlays()
            }
            scaleBarOverlay.enableScaleBar()
            locationOverlay.enableMyLocation()

            showProgressBar()

            val tread = Runnable {
                latitude = 0.toDouble()
                println("Thread start")
                do {
                    try { latitude = locationOverlay.lastFix.latitude
                    } catch (e: Exception) {}
                } while (latitude == 0.toDouble())
                activity!!.runOnUiThread {
                    if (latitude != 0.toDouble()) {
                        try {
                            closeProgressBar()
                            mMapView.controller.animateTo(GeoPoint(locationOverlay.lastFix.latitude, locationOverlay.lastFix.longitude))

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try{
                            if(lat!=0.0 && lon!=0.0){
                                createRoad()
                            }
                            else
                            {
                                Toast.makeText(activity!!,"Не были указаны координаты до конечного объекта",Toast.LENGTH_SHORT).show()
                            }

                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }; Thread(tread).start()

            firstTime = false
        }
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
        mMapView.onPause()
        scaleBarOverlay.disableScaleBar()
        //firstTime = true
    }

    override fun onStop() {
        super.onStop()
        println("onStop")
        try {
            SharedPreferencesState.init(context!!)
            SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
            SharedPreferencesState.addPropertyFloat("lon", locationOverlay.myLocation.longitude.toFloat())
        } catch (e: Exception) {
        }
    }

    private fun setCenter() {
        when (resources.displayMetrics.densityDpi) {
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

    private fun initScaleBarOverlay(): ScaleBarOverlay {
        scaleBarOverlay = ScaleBarOverlay(mMapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay
    }

    private fun initRotationGestureOverlay(): RotationGestureOverlay {
        rotationGestureOverlay = RotationGestureOverlay(mMapView)
        rotationGestureOverlay.isEnabled = true
        mMapView.setMultiTouchControls(true)
        return rotationGestureOverlay
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy")
    }
}

/*    private fun createRoad() {

        val roadManager = OSRMRoadManager(context)

        roadManager.setService("http:" + activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getString("routeserver", "") + "/route/v1/driving/")
        roadManager.setUserAgent(BuildConfig.APPLICATION_ID)

        val waypoints = ArrayList<GeoPoint>()

        val startPoint: GeoPoint = GeoPoint(
            activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lat", 0f).toDouble(),
            activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lon", 0f).toDouble()
        )
        val endPoint: GeoPoint = GeoPoint(lat, lon)
        waypoints.add(
            startPoint
        )
        waypoints.add(endPoint)

        val createRoad = Runnable {
            val road = roadManager.getRoad(waypoints)
            if (road.mRouteHigh.size != 2) {
                val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                activity!!.runOnUiThread {
                    mMapView.overlays.add(0, roadOverlay)
                    road.mRouteHigh.add(0, startPoint)
                    arrivedToObject = false
                    tracking(road)
                }
            } else {
                try {
                    if (distance(road)> 50)
                        createRoad()
                    else {
                        tracking(road)
                        addOverlays()
                    }
                } catch (e: Exception) { tracking(road) }
            }
        }
        Thread(createRoad).start()
    }

    private fun tracking(road: Road) {
        val updateTrackingThread = object : Thread() {
            override fun run() {
                super.run()
                try {
                    var oldDistance = distance(road)
                    while (!arrivedToObject) {
                        sleep(100)
                        if (road.mRouteHigh.size> 2) {
                            if (distance(road)> oldDistance + 40) {
                                activity!!.runOnUiThread {
                                    road.mRouteHigh.clear()
                                    arrivedToObject = true
                                    mMapView.overlays.removeAt(0)
                                    val handler = Handler()
                                    handler.postDelayed(
                                        {
                                            createRoad()
                                        },
                                        500
                                    )
                                }
                            } else {
                                activity!!.runOnUiThread {
                                    if (distance(road) <40) {
                                        road.mRouteHigh.removeAt(1)
                                        if(mMapView.overlays.size>0)
                                        mMapView.overlays.removeAt(0)
                                        val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                                        mMapView.overlays.add(0, roadOverlay)
                                        oldDistance = distance(road)
                                        mMapView.invalidate()
                                    } else {
                                        try{
                                            road.mRouteHigh.removeAt(0)
                                            road.mRouteHigh.add(0, locationOverlay.myLocation)
                                            if(mMapView.overlays.size>0)
                                            mMapView.overlays.removeAt(0)
                                            val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                                            mMapView.overlays.add(0, roadOverlay)
                                            mMapView.invalidate()
                                        }catch (e:Exception){
                                            e.printStackTrace()
                                        }

                                    }
                                }
                            }
                        } else {
                            if (road.mRouteHigh.size> 0) {
                                if (distance(road) < 30.0) {

                                    road.mRouteHigh.clear()
                                    activity!!.runOnUiThread {
                                        val alertDialog = AlertDialog.Builder(activity!!)
                                        val view = layoutInflater.inflate(R.layout.arrived_dialog, null)
                                        alertDialog.setView(view)
                                        val dialog: AlertDialog = alertDialog.create()
                                        dialog.show()
                                        val arrive_button: Button = view!!.findViewById(R.id.arrived_button)
                                        arrive_button.setOnClickListener {
                                            val arriveThread = Runnable {
                                                arrivedToObject(jsonArray)
                                                dialog.cancel()
                                            }; Thread(arriveThread).start()
                                        }
                                        mMapView.overlays.removeAt(0)
                                        arrivedToObject = true
                                    }
                                } else {
                                    activity!!.runOnUiThread {
                                        road.mRouteHigh.removeAt(0)
                                        road.mRouteHigh.add(0, locationOverlay.myLocation)
                                        mMapView.overlays.removeAt(0)
                                        val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                                        mMapView.overlays.add(0, roadOverlay)
                                        mMapView.invalidate()
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }; updateTrackingThread.start()
    }

    private fun arrivedToObject(jsonArray: JSONArray) {
        val arrivedToObject = Runnable {
            request.arrivedToObject(
                PollingServer.socket,
                PollingServer.countSender,
                JSONObject(jsonArray.getString(0)).getString("number"),
                0,
                activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", ""),
                activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 0),
                activity!!.getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", "")
            )
        }; Thread(arrivedToObject).start()
        PollingServer.countSender++
    }

    */