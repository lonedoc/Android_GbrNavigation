package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.workservice.MyLocation
import kobramob.rubeg38.ru.gbrnavigation.workservice.RubegNetworkService
import org.json.JSONObject
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread

class NavigatorFragment : androidx.fragment.app.Fragment(), MapEventsReceiver {

    var enableFollowMe = false
    private var timer = Timer()
    lateinit var oldLocation: GeoPoint
    private var lat: Double = 0.0
    private var lon: Double = 0.0

    private lateinit var mMapView: MapView
    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay
    private var progressBarOpen: String = "close"
    private lateinit var cancelDialog: AlertDialog
    private var latitude: Double = 0.toDouble()

    companion object {
        var proximityAlive = false
        var arriveToObject = false
        var alertCanceled = false
        var road: Road? = null
    }
    lateinit var jsonObject: JSONObject


    override fun longPressHelper(p: GeoPoint?): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        if (locationOverlay.isFollowLocationEnabled) {
            val followFab: FloatingActionButton = view!!.findViewById(R.id.navigator_followMe)
            locationOverlay.disableFollowLocation()
            followFab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.viewBackground))
            followFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorPrimary))
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        println("onCreateView")
        val rootView = inflater.inflate(R.layout.navigator_fragment, container, false)

        mMapView = rootView.findViewById(R.id.navigator_mapview)

        initMapView()

        val followMe:FloatingActionButton = rootView.findViewById(R.id.navigator_followMe)
        val myLocation:FloatingActionButton = rootView.findViewById(R.id.navigator_myLocation)
        val yandex:FloatingActionButton = rootView.findViewById(R.id.navigator_yandex)


        myLocation.setOnClickListener {

            try {
                if (locationOverlay.isFollowLocationEnabled) {
                    locationOverlay.disableFollowLocation()
                    followMe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.viewBackground))
                    followMe.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorPrimary))
                }
                mMapView.controller.animateTo(GeoPoint(MyLocation.imHere))

                SharedPreferencesState.init(context!!)
                SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
                SharedPreferencesState.addPropertyFloat("lon", locationOverlay.myLocation.longitude.toFloat())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Ваше месторасположение не определено", Toast.LENGTH_SHORT).show()
            }
        }

        followMe.setOnClickListener {
            if (locationOverlay.isFollowLocationEnabled) {
                locationOverlay.disableFollowLocation()
                followMe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.viewBackground))
                followMe.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorPrimary))
            } else {
                locationOverlay.enableFollowLocation()
                followMe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorPrimary))
                followMe.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.viewBackground))
            }
        }

        yandex.setOnClickListener {
            try {
                val uri = Uri.parse("yandexnavi://build_route_on_map?lat_to=${activity!!.intent.getDoubleExtra("lat", 0.0)}&lon_to=${ activity!!.intent.getDoubleExtra("lon", 0.0)}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("ru.yandex.yandexnavi")
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "На данном устройстве не установлен Яндекс.Навигатор", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        if (!proximityAlive)
            proximityCheck()

        return rootView
    }


    private var enableWhileResume = false

    private fun paveTheWay() {

        Log.d("PaveTheWay","create")

        val roadManager = OSRMRoadManager(context)
        if (context!!.getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("routeserver")) {
            roadManager.setService("http:" + activity!!.getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("routeserver", null) + "/route/v1/driving/")
            roadManager.setUserAgent(BuildConfig.APPLICATION_ID)

            val waypoints = ArrayList<GeoPoint>()
            val startPoint = GeoPoint(
                MyLocation.imHere!!.latitude,
                MyLocation.imHere!!.longitude
            )
            val endPoint = GeoPoint(
                activity!!.intent.getDoubleExtra("lat", 0.0),
                activity!!.intent.getDoubleExtra("lon", 0.0)
            )

            Log.d("endPoint",endPoint.toString())
            waypoints.add(startPoint)
            waypoints.add(endPoint)

            thread {
                try {
                    road = roadManager.getRoad(waypoints)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                if (road!!.mStatus != Road.STATUS_OK) {
                    activity!!.runOnUiThread {
                        Toast.makeText(context, "Невозможно проложить путь до объекта", Toast.LENGTH_LONG).show()
                    }
                } else {
                    if (road!!.mRouteHigh.size> 2) {
                        activity!!.runOnUiThread {
                            val roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                            mMapView.overlays.add(roadOverlay)
                            mMapView.invalidate()
                            tracking(roadOverlay)
                        }
                    } else {
                        if (distance(road!!)> 100) {
                            return@thread
                        } else {
                            // stop Tracking
                            arriveToObject = true
                        }
                    }
                }
            }
        } else {
            Toast.makeText(context, "Нет данных о сервере проложение пути", Toast.LENGTH_SHORT).show()
        }
    }

    private fun tracking(roadOverlay: Polyline) {
        thread {
            var startDistanceBetweenPoints = distance(road!!)
            var newRoadOverlay = roadOverlay
            try{
                while (road!!.mRouteHigh.size> 2) {

                    if (distance(road!!) > startDistanceBetweenPoints + 30) {
                        // Перестроить путь
                        road!!.mRouteHigh.clear()
                        mMapView.overlays.remove(roadOverlay)
                        val handler = Handler()
                        handler.postDelayed(
                            {
                                paveTheWay()
                            },
                            500
                        )
                    } else {
                        if (distance(road!!) <40) {
                            // удалить точку

                                road!!.mRouteHigh.removeAt(1)



                            mMapView.overlays.remove(newRoadOverlay)

                                newRoadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                                mMapView.overlays.add(newRoadOverlay)
                                startDistanceBetweenPoints = distance(road!!)

                            activity!!.runOnUiThread {
                                mMapView.invalidate()
                            }
                            sleep(500)
                        } else {
                            // передвинуть 0 точку

                                    road!!.mRouteHigh.removeAt(0)
                                    road!!.mRouteHigh.add(
                                        0,
                                        GeoPoint(
                                            MyLocation.imHere!!.latitude,
                                            MyLocation.imHere!!.longitude
                                        )
                                    )

                                    mMapView.overlays.remove(newRoadOverlay)


                                    newRoadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                                    mMapView.overlays.add(newRoadOverlay)
                            activity!!.runOnUiThread {
                                    mMapView.invalidate()
                            }
                            sleep(500)
                        }
                    }
                }
            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }

        }
    }

    private fun proximityCheck() {
        thread {
            val location = Location("point A")
            location.latitude = activity!!.intent.getDoubleExtra("lat", 0.0)
            location.longitude = activity!!.intent.getDoubleExtra("lon", 0.0)

            while (!arriveToObject) {
                sleep(1000)
                proximityAlive = true
                if(alertCanceled){
                    proximityAlive = false
                    arriveToObject = true
                }
                Log.d("proximity",(MyLocation.imHere!!.distanceTo(location).toString()))
                if (MyLocation.imHere!!.distanceTo(location) < 100) {
                    proximityAlive = false
                    arriveToObject = true

                    when {
                        !RubegNetworkService.connectInternet -> {
                            Toast.makeText(
                                context,
                                "Нет соединения с интернетом, невозможно отправить запрос на прибытие",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        !RubegNetworkService.connectServer -> {
                            Toast.makeText(
                                context,
                                "Нет соединения с сервером, невозможно отправить запрос на прибытие",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Log.d("proximity", "На месте")
                            activity!!.runOnUiThread {
                                val arrivedDialog = AlertDialog.Builder(activity!!)
                                arrivedDialog.setCancelable(false)
                                arrivedDialog.setTitle("Прибытие")
                                    .setMessage("Вы прибыли на место")
                                    .setPositiveButton("Подтвердить") {
                                            _, _ ->
                                        thread {
                                            val message = JSONObject()
                                            message.put("\$c$", "gbrkobra")
                                            message.put("command", "alarmpr")
                                            message.put("number", activity!!.intent.getStringExtra("number"))
                                            RubegNetworkService.protocol.send(message = message.toString()) {
                                                    success: Boolean ->
                                                if (success) {
                                                    activity!!.runOnUiThread {
                                                        Toast.makeText(activity, "Прибытие подтверждено", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        }

                                    }.show()
                            }
                        }
                    }
                }
            }
        }
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
        val gpsMyLocationProvider = GpsMyLocationProvider(activity)
        gpsMyLocationProvider.addLocationSource(MyLocation.imHere!!.provider)
        locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider, mMapView)
        locationOverlay.setDirectionArrow(customIcon(R.drawable.ic_navigator_icon), customIcon(R.drawable.ic_navigator_active_icon))
        locationOverlay.isDrawAccuracyEnabled = false
        return locationOverlay
    }

    private fun customIcon(drawable: Int): Bitmap? {

        val drawableBitmap = ContextCompat.getDrawable(activity!!, drawable)

        val bitmap = Bitmap.createBitmap(
            drawableBitmap!!.intrinsicWidth,
            drawableBitmap.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawableBitmap.setBounds(0, 0, canvas.width, canvas.height)
        drawableBitmap.draw(canvas)

        return bitmap
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

    override fun onResume() {
        super.onResume()

        mMapView.onResume()

        paveTheWay()

        locationOverlay.enableMyLocation()
        scaleBarOverlay.enableScaleBar()

        if (enableWhileResume) {
            locationOverlay.enableFollowLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        println("onPause")
        mMapView.onPause()

        if (mMapView.overlays.count()> 3)
            mMapView.overlays.removeAt(mMapView.overlays.count() - 1)

        if (road!!.mRouteHigh.size> 1)
            road!!.mRouteHigh.clear()

        if (locationOverlay.isFollowLocationEnabled) {
            locationOverlay.enableFollowLocation()
            enableWhileResume = true
        }

        locationOverlay.disableMyLocation()
        scaleBarOverlay.disableScaleBar()
    }

    override fun onStop() {
        super.onStop()
        println("onStop")
        try {
            SharedPreferencesState.init(context!!)
            SharedPreferencesState.addPropertyFloat("lat", MyLocation.imHere!!.latitude.toFloat())
            SharedPreferencesState.addPropertyFloat("lon", MyLocation.imHere!!.longitude.toFloat())
        } catch (e: Exception) {
        }
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

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy")
    }

}
