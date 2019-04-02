package kobramob.rubeg38.ru.gbrnavigation

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.RelativeLayout
import android.widget.Toast
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


class NavigatorActivity : Activity(), LocationListener{



    internal var mMapView: MapView? = null
    internal var mRelativeLayout:RelativeLayout?=null
    private var currentLocation: Location? = null

    lateinit var locationManager:LocationManager

    val tileSource = TileSource()

    private lateinit var myLocation:GeoPoint

    lateinit var mapController:IMapController

    private lateinit var mScaleBarOverlay:ScaleBarOverlay
    private lateinit var mRotationGestureOverlay:RotationGestureOverlay
    private lateinit var mLocationOverlay:MyLocationNewOverlay
    private lateinit var mCompasOverlay:CompassOverlay

    private fun showSettingsAlert() {

        val alertDialog = AlertDialog.Builder(this)

     /*    Setting Dialog Title*/
        alertDialog.setTitle("GPS is settings")

         /*Setting Dialog Message*/
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")

         /*On pressing Settings button*/
        alertDialog.setPositiveButton("Settings") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

      /*   on pressing cancel button*/
        alertDialog.setNegativeButton("Cancel"
        ) { dialog, which -> dialog.cancel() }

       /*  Showing Alert Message*/
        alertDialog.show()
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMapView = findViewById(R.id.mapview)
        mRelativeLayout = findViewById(R.id.parent_container)

        initMyLocation()

    }

    private fun initRotationOverlay()
    {
        mRotationGestureOverlay = RotationGestureOverlay(mMapView)
        mRotationGestureOverlay.isEnabled = true
        mMapView!!.setMultiTouchControls(true)
        mMapView!!.overlays.add(mRotationGestureOverlay)

/*        mMapView!!.setOnGenericMotionListener(object : View.OnGenericMotionListener {
            override fun onGenericMotion(v: View?, event: MotionEvent?): Boolean {
                if (0 != event!!.source and InputDevice.SOURCE_CLASS_POINTER) {
                    when (event.action) {
                        MotionEvent.ACTION_SCROLL -> {
                            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                                mMapView!!.controller.zoomOut()
                            else {
                                val iGeoPoint = mMapView!!.projection.fromPixels(event.x.toInt(), event.y.toInt())
                                mMapView!!.controller.animateTo(iGeoPoint)
                                mMapView!!.controller.zoomIn()
                            }
                            return true
                        }
                    }
                }
                return false
            }
        })*/
    }

    private fun initMyLocation(){

        mMapView!!.setTileSource(TileSourceFactory.MAPNIK)

        mMapView!!.setHasTransientState(true)


        val g = GpsMyLocationProvider(this)
        g.locationUpdateMinTime = 0
        g.locationUpdateMinDistance = 1f
        mLocationOverlay = MyLocationNewOverlay(g,mMapView)



        val dm = resources.displayMetrics

        mScaleBarOverlay = ScaleBarOverlay(mMapView)
        mScaleBarOverlay.setCentred(true)
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)

        initRotationOverlay()

        mMapView!!.controller.setZoom(15.0)
        mMapView!!.isTilesScaledToDpi = true
        mMapView!!.setMultiTouchControls(true)
        mMapView!!.isFlingEnabled = true

        mMapView!!.overlays.add(mScaleBarOverlay)
        mMapView!!.overlays.add(mLocationOverlay)

        mMapView!!.isVerticalMapRepetitionEnabled = true
        mMapView!!.isHorizontalMapRepetitionEnabled = true


    }


    private var oldDistance:Float = 0F

    private var stopUpdateRoad:Boolean = false

    private fun createRoad()
    {

        val roadManager = OSRMRoadManager(this)
        roadManager.setService("http:192.168.1.95:5000/route/v1/driving/")
        roadManager.setUserAgent(BuildConfig.APPLICATION_ID)

        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(myLocation)

        val endPoint = GeoPoint(56.31585, 101.75473)
        waypoints.add(endPoint)



        val thread = Runnable {

            stopUpdateRoad = false

            val road = roadManager.getRoad(waypoints)
            var roadOverlay = RoadManager.buildRoadOverlay(road,0x800000FF.toInt(),10.0f)
            runOnUiThread {

                println(road.mRouteHigh.size)
                if(road.mRouteHigh.size>2)
                {
                    mMapView!!.overlays.add(roadOverlay)

                    road.mRouteHigh.add(0,myLocation)

                    oldDistance = distance(road)

                    val updateRoadThread = object : Thread() {
                        override fun run() {
                            while (!stopUpdateRoad) {
                                try {
                                    Thread.sleep(100)
                                    runOnUiThread {
                                        if(!stopUpdateRoad)
                                        {

                                            if(road.mRouteHigh.size>2)
                                            {
                                                mMapView!!.mapOrientation = -getRotationAngle(road)

                                                if(distance(road)>oldDistance+50)
                                                {

                                                    road.mRouteHigh.clear()
                                                    stopUpdateRoad=true

                                                    val handler = Handler()
                                                    handler.postDelayed(
                                                        {mMapView!!.overlays.removeAt(mMapView!!.overlays.size-1)
                                                            createRoad() },
                                                        500
                                                    )

                                                }
                                                else
                                                {

                                                    if(distance(road)<55)
                                                    {
                                                        println("УДАЛИЛ ТОЧКУ")
                                                        road.mRouteHigh.removeAt(1)
                                                        mMapView!!.overlays.removeAt(mMapView!!.overlays.size-1)
                                                        roadOverlay = RoadManager.buildRoadOverlay(road,0x800000FF.toInt(),10.0f)
                                                        mMapView!!.overlays.add(roadOverlay)
                                                        oldDistance=distance(road)
                                                        /* if(mLocationOverlay.isFollowLocationEnabled)
                                                         {*/


                                                         /*}
                                                          else
                                                              mMapView!!.mapOrientation = 0F*/

                                                    }
                                                    else
                                                    {
                                                        mMapView!!.controller.setCenter(road.mRouteHigh[0])

                                                        when {
                                                            -getRotationAngle(road)<-100 -> mMapView!!.controller.setCenter( mMapView!!.projection.fromPixels(mMapView!!.width,2*mMapView!!.height/3))

                                                            -getRotationAngle(road)<-80 -> mMapView!!.controller.setCenter( mMapView!!.projection.fromPixels(mMapView!!.width,mMapView!!.height/3))

                                                            -getRotationAngle(road)<-65 -> mMapView!!.controller.setCenter( mMapView!!.projection.fromPixels(mMapView!!.width,mMapView!!.height/3))

                                                            -getRotationAngle(road)<-50 -> mMapView!!.controller.setCenter(mMapView!!.projection.fromPixels(3*mMapView!!.width/4,mMapView!!.height/3))

                                                            -getRotationAngle(road)<-25 -> mMapView!!.controller.setCenter( mMapView!!.projection.fromPixels(2*mMapView!!.width/3,mMapView!!.height/3))

                                                            -getRotationAngle(road)<0 -> mMapView!!.controller.setCenter( mMapView!!.projection.fromPixels(mMapView!!.width/2,mMapView!!.height/3))

                                                            -getRotationAngle(road)>80 -> mMapView!!.controller.setCenter( mMapView!!.projection.fromPixels(0,mMapView!!.height/3))

                                                            -getRotationAngle(road)>65 -> mMapView!!.controller.setCenter( mMapView!!.projection.fromPixels(0,mMapView!!.height/3))

                                                            -getRotationAngle(road)>25-> mMapView!!.controller.setCenter( mMapView!!.projection.fromPixels(mMapView!!.width/3,mMapView!!.height/3))

                                                            -getRotationAngle(road)>0 -> mMapView!!.controller.setCenter( mMapView!!.projection.fromPixels(mMapView!!.width/3,mMapView!!.height/3))
                                                        }



                                                        road.mRouteHigh.removeAt(0)
                                                        road.mRouteHigh.add(0,myLocation)
                                                        mMapView!!.overlays.removeAt(mMapView!!.overlays.size-1)
                                                        roadOverlay = RoadManager.buildRoadOverlay(road,0x800000FF.toInt(),10.0f)
                                                        mMapView!!.overlays.add(roadOverlay)
                                                        mMapView!!.invalidate()
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                road.mRouteHigh.clear()
                                                stopUpdateRoad=true
                                                Toast.makeText(this@NavigatorActivity, "Конец пути", Toast.LENGTH_SHORT).show()
                                                mMapView!!.overlays.removeAt(mMapView!!.overlays.size-1)
                                            }
                                        }



                                    }
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }

                            }
                        }
                    }

                    updateRoadThread.start()
                }
                else
                    createRoad()

            }
        }
        Thread(thread).start()
    }

    private fun portrait(road:Road)
    {

        println("Направление " + (-getRotationAngle(road)))
        when
        {
            -getRotationAngle(road)<-100 ->
                centerMap(
                    road.mRouteHigh[0],
                    mMapView!!.width,
                    mMapView!!.height
                )
            -getRotationAngle(road)<-80 ->
                centerMap(
                    road.mRouteHigh[0],
                    mMapView!!.width,
                    mMapView!!.height/3
                )
            -getRotationAngle(road)<-65 ->
                centerMap(
                    road.mRouteHigh[0],
                    mMapView!!.width,
                    mMapView!!.height/3
                )
            -getRotationAngle(road)<-50 ->
                centerMap(
                    road.mRouteHigh[0],
                    3*mMapView!!.width/4,
                    mMapView!!.height/3
                )
            -getRotationAngle(road)<-25 ->
                centerMap(
                    road.mRouteHigh[0],
                    -mMapView!!.height/2,
                    0

                )
            -getRotationAngle(road)<0 ->
                centerMap(
                    road.mRouteHigh[0],
                    mMapView!!.width/2,
                    mMapView!!.height/3
                )
            -getRotationAngle(road)>80 ->
                centerMap(
                    road.mRouteHigh[0],
                    -mMapView!!.width,
                    mMapView!!.height/3
                )
            -getRotationAngle(road)>65 ->
                centerMap(
                    road.mRouteHigh[0],
                    0,
                    mMapView!!.height/3
                )
            -getRotationAngle(road)>25 ->
                centerMap(
                    road.mRouteHigh[0],
                    mMapView!!.width/3,
                    mMapView!!.height/3
                )
            -getRotationAngle(road)>0 ->
                centerMap(
                    road.mRouteHigh[0],
                    mMapView!!.width/3,
                    mMapView!!.height/3
                )
        }
    }


    private fun centerMap(center: GeoPoint, offX: Int, offY: Int) {
        val tl = mMapView!!.projection.fromPixels(0, 0)
        val br = mMapView!!.projection.fromPixels(mMapView!!.width, mMapView!!.height)

        val newLon = offX * (br.longitude - tl.longitude) / mMapView!!.width + center.longitude
        val newLat = offY * (br.latitude - tl.latitude) / mMapView!!.height + center.latitude

        mMapView!!.controller.setCenter(GeoPoint(newLat, newLon))

    }

    fun width(): Float {
        return  mScaleBarOverlay.xdpi
    }

    fun height(): Float {
        return mScaleBarOverlay.ydpi
    }

    fun getRotationAngle(road:Road): Float {
        val x1 = road.mRouteHigh[0].latitude
        val y1 = road.mRouteHigh[0].longitude
        val x2 = road.mRouteHigh[1].latitude
        val y2 =  road.mRouteHigh[1].longitude

        val xDiff = (x2 - x1).toFloat()
        val yDiff = (y2 - y1).toFloat()

        return (Math.atan2(yDiff.toDouble(), xDiff.toDouble()) * 180.0 / Math.PI).toFloat()

    }

    private fun distance(road:Road): Float {
        val locationA = Location("point A")
        locationA.latitude = road.mRouteHigh[0].latitude
        locationA.longitude = road.mRouteHigh[0].longitude
        val locationB = Location("point B")
        locationB.latitude = road.mRouteHigh[1].latitude
        locationB.longitude = road.mRouteHigh[1].longitude
        return locationA.distanceTo(locationB)
    }

    var createRoad:Int = 0

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED      ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
        }
    }

    private fun getLocation() {
        val time:Float = 0F
        try {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
            } catch (ex: Exception) {
            }


            try {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
            } catch (ex: Exception) {
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        }

    }

    override fun onLocationChanged(location: Location) {

        currentLocation = location

        myLocation = GeoPoint(location.latitude,location.longitude)

        mLocationOverlay.enableMyLocation()
        if(createRoad==0)
        {
            mMapView!!.controller.setCenter(myLocation)
            createRoad()
            createRoad=1
        }

       /* Toast.makeText(this@NavigatorActivity, "Longitude " + location.longitude + " Latitude " + location.latitude, Toast.LENGTH_SHORT).show()*/
    }

    override fun onProviderDisabled(provider: String) {
        /*Toast.makeText(this@NavigatorActivity, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show()*/
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {
        /*Toast.makeText(
            this, "Enabled new provider!$provider",
            Toast.LENGTH_SHORT
        ).show()*/
    }


    public override fun onResume() {
        super.onResume()

        getLocation()

        /*mLocationOverlay.enableFollowLocation()*/


        mScaleBarOverlay.disableScaleBar()

        if(stopUpdateRoad)
        {
            createRoad()
        }

    }

    public override fun onPause() {
        super.onPause()

        locationManager.removeUpdates(this)

        /*mLocationOverlay.disableFollowLocation()*/
        mLocationOverlay.disableMyLocation()
        mScaleBarOverlay.enableScaleBar()

        stopUpdateRoad=true
        mMapView!!.overlays.removeAt(mMapView!!.overlays.size-1)
    }


}

/*private val locationListener = object : LocationListener {

    override fun onLocationChanged(location: Location) {
        showLocation(location)
    }

    override fun onProviderDisabled(provider: String) {
        checkEnabled()
    }

    override fun onProviderEnabled(provider: String) {
        checkEnabled()

        if(ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            showLocation(locationManager.getLastKnownLocation(provider))
        }

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        if (provider == LocationManager.NETWORK_PROVIDER) {
            println("Status: $status")
        }
    }
}

private fun showLocation(location:Location?) {
    if (location == null)
        return
    if (location.provider == LocationManager.NETWORK_PROVIDER)
    {
        println(formatLocation(location))
    }
}

private fun formatLocation(location:Location?):String {
    return if (location == null) "" else {
        createMap(location)
        String.format(
            "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3\$tF %3\$tT",
            location.latitude, location.longitude, Date(
                location.time
            )
        )

    }

}

private fun checkEnabled() {
    println(("Enabled: " + locationManager
        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)))
    if(!locationManager
            .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    )
    {
        showSettingsAlert()
    }
}*/


/*  if(ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {

        }
        else
        {
            showSettingsAlert()
        }*/
/*  locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                100,
                1.toFloat(),
                locationListener
            )*/
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

               ** val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                mFusedLocationClient.lastLocation
                    .addOnSuccessListener(this
                    ) { location ->
                         Got last known location. In some rare situations, this can be null.
                        if (location != null) {
                            println(location.latitude)
                            println(location.longitude)
                            mMapView = findViewById(R.id.mapview)
                            createMap(location)
                        }
                    }**

                **locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    100,
                    1.toFloat(),
                    locationListener
                )**


            } else {
                showSettingsAlert()
            }
        } else
        {
            val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mFusedLocationClient.lastLocation
                .addOnSuccessListener(this
                ) { location ->
                     Got last known location. In some rare situations, this can be null.
                    if (location != null) {
                        println(location.latitude)
                        println(location.longitude)
                        mMapView = findViewById(R.id.mapview)
                        createMap(location)
                    }
                }
        }*/



/*do {
                     for(i in 0 until road.mRouteHigh.size-1)
                     {
                         if(startPoint == road.mRouteHigh[i])
                         {
                             road.mRouteHigh.removeAt(i)
                         }
                         j++
                     }
                 } while(j!=road.mRouteHigh.size-1)*/
/*if(startPoint == road.mRouteHigh[1])
{
    road.mRouteHigh.removeAt(1)
}*/
/*println(sqrt((startPoint.latitude-road.mRouteHigh[1].latitude)*(startPoint.latitude-road.mRouteHigh[1].latitude)+(startPoint.longitude-road.mRouteHigh[1].longitude)*(startPoint.longitude-road.mRouteHigh[1].longitude)))
*/
/* if(roadOverlay.isCloseTo(startPoint,30.0,mMapView))
             {
                 println("delete")
                 road.mRouteHigh.removeAt(0)
                 road.mRouteHigh.add(0,GeoPoint(56.19481,101.83738))
                 roadOverlay = RoadManager.buildRoadOverlay(road,0x800000FF.toInt(),10.0f)
                 mMapView!!.overlays.removeAt(1)
                 mMapView!!.overlays.add(roadOverlay)
                 mMapView!!.invalidate()
             }*/
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                mFusedLocationClient.lastLocation
                    .addOnSuccessListener(this
                    ) { location ->
                         Got last known location. In some rare situations, this can be null.
                        if (location != null) {
                            println(location.latitude)
                            println(location.longitude)
                            mMapView = findViewById(R.id.mapview)
                            createMap(location)
                        }
                    }
            }
            else
            {
                showSettingsAlert()
            }
        }
        else
        {
*/
/*
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                10,
                0.toFloat(),
                locationListener
            )**

        }
*/

/** mMapView!!.invalidate()**

TODO удаление точки с карты ( надо придумать как это прикрутить к месту расположения )*/


/* TileSourceFactory.addTileSource(YandexMaps)
 mMapView!!.setTileSource(YandexMaps)
 road.mRouteHigh.removeAt(0)
 road.mRouteHigh.add(0, GeoPoint(56.15675,101.66760))
 roadOverlay = RoadManager.buildRoadOverlay(road,0x800000FF.toInt(),10.0f)
 mMapView!!.overlays.clear()
 mMapView!!.overlays.add(roadOverlay)*/

/*TileSourceFactory.addTileSource(GoogleSat)
TileSourceFactory.addTileSource(GoogleHybrid)
TileSourceFactory.addTileSource(YandexMaps)
*/
/* val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this),mMapView)
 mLocationOverlay.enableMyLocation()
 mLocationOverlay.enableFollowLocation()
 mLocationOverlay.isOptionsMenuEnabled = true
 val bitmap = BitmapFactory.decodeResource(resources, R.drawable.baseline_arrow_upward_black_48)
 mLocationOverlay.setPersonIcon(bitmap)
 mMapView!!.overlays.add(mLocationOverlay)*/
/*roadManager.setService("http:91.189.160.72:5000/route/v1/driving/")*/

/*TODO месторасположение 2 способ*/
/*        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.lastLocation
            .addOnSuccessListener(this
            ) { location ->
                 Got last known location. In some rare situations, this can be null.
                if (location != null) {
                    println(location.latitude)
                    println(location.longitude)
                }
            }*/


/*val thread = Runnable {
            val mURL =
                URL("http:192.168.1.95:5000/route/v1/driving/101.67805,56.31401;101.7520000000,56.3177000000?alternatives=true&overview=false&steps=true")
            val response = StringBuffer()
            with(mURL.openConnection() as HttpURLConnection) {
                BufferedReader(InputStreamReader(inputStream) as Reader?).use {


                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                    println(response.toString())
                }
                **var road:Road
                road = response
            val roadOverlay = RoadManager.buildRoadOverlay(response)**
                runOnUiThread {
**                    waypoints.clear()
                    val jRoutes = JSONObject(response.toString())
                    val jaRoutes = jRoutes.getJSONArray("routes")

                    val jLegs = JSONObject(jaRoutes[0].toString())
                    val jaLegs = jLegs.getJSONArray("legs")

                    val jSteps = JSONObject(jaLegs[0].toString())
                    val jaSteps = jSteps.getJSONArray("steps")

                    for(i in 0 until jaSteps.length())
                    {
                        val jIntersection = JSONObject(jaSteps[i].toString())
                        val jaIntersection = jIntersection.getJSONArray("intersections")
                        println("i= $i")
                        for(j in 0 until jaIntersection.length())
                        {
                            val jLocation = JSONObject(jaIntersection[j].toString())
                            val jaLocation= jLocation.getJSONArray("location")
                            println("j= $j")
                            val x:Double = jaLocation[0] as Double
                            val y:Double = jaLocation[1] as Double
                            waypoints.add(GeoPoint(y,x))
                        }
                    }
                    ****val jCoordinates = jGeometry.getJSONObject("geometry")
                    val jOverlay = jCoordinates.getJSONArray("coordinates")
                    for(i in 0 until jOverlay.length())
                    {
                        val coordinates = jOverlay.getJSONArray(i)
                        val x:Double = coordinates[0] as Double
                        val y:Double = coordinates[1] as Double
                        waypoints.add(GeoPoint(y,x))
                    }****
                    val roadOverlay:Polyline = Polyline(mMapView)
                    roadOverlay.color = 0x800000FF.toInt()
                    roadOverlay.width = 5.0f
                    roadOverlay.setPoints(waypoints)**

                    mMapView!!.overlays.add(roadOverlay)
                    mMapView!!.invalidate()
                }
            }
        }*/