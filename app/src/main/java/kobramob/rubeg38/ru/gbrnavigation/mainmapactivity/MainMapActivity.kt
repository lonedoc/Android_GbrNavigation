package kobramob.rubeg38.ru.gbrnavigation.mainmapactivity

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.TileSource
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import kotlin.collections.ArrayList

class MainMapActivity :
    AppCompatActivity(),
    LocationListener,
    MapEventsReceiver,
    SensorEventListener,
    SoundPool.OnLoadCompleteListener {
    override fun onLoadComplete(soundPool: SoundPool?, sampleId: Int, status: Int) {
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (followMeEnable) {
            timer.cancel()
            timer2.cancel()
            followMeEnable = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (followMeEnable) {
            timer.cancel()
            timer2.cancel()
            followMeEnable = false
        }
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return true
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        if (followMeEnable) {
            /*myFollowMe.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
            myFollowMe.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))*/
            timer.cancel()
            followMeEnable = false
        }
        return true
    }

    private lateinit var mMapView: MapView
    private lateinit var mScaleBarOverlay: ScaleBarOverlay
    private lateinit var mRotationGestureOverlay: RotationGestureOverlay
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var mMapEvent: MapEventsOverlay

    private lateinit var locationManager: LocationManager
    private lateinit var currentLocation: Location
    private lateinit var myLocation: GeoPoint
    private lateinit var oldLocation: GeoPoint

    private val tileSource: TileSource =
        TileSource()
    private var firstStart = true
    private var followMeEnable = false

/*    private lateinit var myLocationButton:FloatingActionButton
    private lateinit var myFollowMe:FloatingActionButton*/

    private lateinit var timer: Timer
    private lateinit var timer2: Timer

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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

    private fun dialog_test_alert() {
        // Тут диалог для тестовой тревоги.
        // На тревоге должна быть иконка, имя объекта, адрес.
        // Далее открывается карточка объекта
    }

    private fun dialog_change_map() {
        val dialog_cm = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.change_map_dialog, null)
        val spinnerMap = view.findViewById(R.id.spinner_map) as Spinner
        spinnerMap.prompt = "Список карт"
        val arrayMap = ArrayList<String>()
        arrayMap.add("OpenStreetMap")
        arrayMap.add("GoogleRoad")
        arrayMap.add("GoogleSat")
        arrayMap.add("GoogleHybrid")
        val spinnerAdapter: ArrayAdapter<String> = ArrayAdapter(
            this@MainMapActivity,
            R.layout.simple_spinner_item,
            arrayMap
        )
        var mapType: String = ""
        var mapTile: OnlineTileSourceBase = TileSourceFactory.MAPNIK
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerMap.adapter = spinnerAdapter
        spinnerMap.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position)
                mapType = item.toString()
            }
        }
        dialog_cm.setView(view)
        dialog_cm.setTitle("Список карт")
        dialog_cm.setPositiveButton("Выбрать") { dialog, which ->
            println(mapType)
            when (mapType) {
                "OpenStreetMap" -> { mapTile = TileSourceFactory.MAPNIK }
                "GoogleRoad" -> { mapTile = tileSource.GoogleRoads }
                "GoogleSat" -> { mapTile = tileSource.GoogleSat }
                "GoogleHybrid" -> { mapTile = tileSource.GoogleHybrid }
            }
            TileSourceFactory.addTileSource(mapTile)
            mMapView.setTileSource(mapTile)
        }.show()
    }

    private fun dialog_server_setting() {
        val dialog_ss = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.server_setting_dialog, null)

        val ip_server = view.findViewById(R.id.ip_server) as EditText
        val port_server = view.findViewById(R.id.port_server) as EditText

        dialog_ss.setView(view)
        dialog_ss.setTitle("Настройки сервера")
        dialog_ss.setPositiveButton("Сохранить") { dialog, which ->

            if (ip_server.text.toString() == "" || port_server.text.toString() == "") {
                Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
                ip_server.setText("")
                port_server.setText("")
                dialog.cancel()
                dialog_server_setting()
            }
        }.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_map)

        checkPermission()

        getLocation()

        val toolbar: Toolbar = this.findViewById(R.id.toolbar_main_map)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)

        mMapView = findViewById(R.id.main_map)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        mMapView.setTileSource(TileSourceFactory.MAPNIK)

        mMapView.setHasTransientState(true)

        mMapView.controller.setZoom(15.0)
        mMapView.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude, -TileSystem.MaxLatitude, 0)
        mMapView.isTilesScaledToDpi = true
        mMapView.setMultiTouchControls(true)
        mMapView.isFlingEnabled = true

        mMapView.isVerticalMapRepetitionEnabled = true
        mMapView.isHorizontalMapRepetitionEnabled = true

        myLocationOverlay()
        mScaleBarOverlay()
        mRotationOverlay()

/*        myLocationButton = findViewById(R.id.my_location)
        myFollowMe = findViewById(R.id.follow_me)

        myLocationButton.setOnClickListener {
            try{
                mMapView.controller.setCenter(myLocation)
                mMapView.controller.setZoom(15.0)
            }catch (e:java.lang.Exception)
            {
                Toast.makeText(this,"Ваше месторасположение не определенно",Toast.LENGTH_SHORT).show()
            }

        }

        myFollowMe.setOnClickListener {
            followMeEnable = if(followMeEnable) {
                println("Выключаем таймер")
                timer.cancel()
                myFollowMe.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                myFollowMe.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                false
            } else {
                try{
                    println("Запускаем таймер")
                    myFollowMe.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.clickButton))
                    myFollowMe.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                    oldLocation = myLocation
                    mMapView.controller.setZoom(15.0)

                    mMapView.mapOrientation = -currentLocation.bearing

                    val width = this@MainMapActivity.windowManager.defaultDisplay.width
                    val height = this@MainMapActivity.windowManager.defaultDisplay.height

                    println(width)
                    println(height)

                    val isLandscape = width > height

                    if(isLandscape)
                    {
                        //Альбомная
                        setCenterMap()
                    }
                    else
                    {
                        //Портретная
                        setCenterMap()
                    }

                    timer = Timer()
                    timer.scheduleAtFixedRate(followMeTask(),0,1000)
                    true
                }catch (e:java.lang.Exception) {
                    myFollowMe.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                    myFollowMe.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                    timer.cancel()
                    Toast.makeText(this,"Ваше месторасположение не определенно",Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }*/
    }

    fun setCenterMap() {
        val density = resources.displayMetrics.densityDpi
        when (density) {
            DisplayMetrics.DENSITY_LOW ->
                {
                    mMapView.controller.animateTo(
                        myLocation.destinationPoint(
                            (3*mScaleBarOverlay.screenHeight / 4).toDouble(),
                            ((currentLocation.bearing)).toDouble()
                        )
                    )
                }
            DisplayMetrics.DENSITY_MEDIUM -> {
                mMapView.controller.animateTo(
                    myLocation.destinationPoint(
                        (3*mScaleBarOverlay.screenHeight / 4).toDouble(),
                        ((currentLocation.bearing)).toDouble()
                    )
                )
            }
            DisplayMetrics.DENSITY_HIGH -> {
                mMapView.controller.animateTo(
                    myLocation.destinationPoint(
                        (3*mScaleBarOverlay.screenHeight / 4).toDouble(),
                        ((currentLocation.bearing)).toDouble()
                    )
                )
            }
            DisplayMetrics.DENSITY_XHIGH -> {
                mMapView.controller.animateTo(
                    myLocation.destinationPoint(
                        (3*mScaleBarOverlay.screenHeight / 4).toDouble(),
                        ((currentLocation.bearing)).toDouble()
                    )
                )
            }
            DisplayMetrics.DENSITY_XXHIGH -> {
                mMapView.controller.animateTo(
                    myLocation.destinationPoint(
                        (3*mScaleBarOverlay.screenHeight / 4).toDouble(),
                        ((currentLocation.bearing)).toDouble()
                    )
                )
            }
            DisplayMetrics.DENSITY_XXXHIGH ->
                {
                    mMapView.controller.animateTo(
                        myLocation.destinationPoint(
                            (3*mScaleBarOverlay.screenHeight / 4).toDouble(),
                            ((currentLocation.bearing)).toDouble()
                        )
                    )
                }
            DisplayMetrics.DENSITY_TV -> {
                mMapView.controller.animateTo(
                    myLocation.destinationPoint(
                        (3*mScaleBarOverlay.screenHeight / 4).toDouble(),
                        ((currentLocation.bearing)).toDouble()
                    )
                )
            }
            else -> {
                mMapView.controller.animateTo(
                    myLocation.destinationPoint(
                        (mScaleBarOverlay.screenHeight / 3).toDouble(),
                        ((currentLocation.bearing)).toDouble()
                    )
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.server_setting -> { dialog_server_setting() }
            R.id.change_map -> { dialog_change_map() }

            // val et_name = view!!.findViewById(R.id.new_name) as EditText
                /*val uri = Uri.parse("yandexnavi://build_route_on_map?lat_to=55.70&lon_to=37.64")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("ru.yandex.yandexnavi")
                startActivity(intent)*/
        }
        return super.onOptionsItemSelected(item)
    }

    // TODO 3 потока, 1 переставляет точку, 2 обновляет угол направления, 3 поворачивает камеру
    private inner class followMeTask : TimerTask() {

        override fun run() {
            if (oldLocation.distanceToAsDouble(myLocation)> 20.0) {
                oldLocation = myLocation

                setCenterMap()

                mMapView.mapOrientation = -currentLocation.bearing
            }
        }

        fun getRotationAngle(oldLocation: GeoPoint, myLocation: GeoPoint): Float {
            val x1 = oldLocation.latitude
            val y1 = oldLocation.longitude
            val x2 = myLocation.latitude
            val y2 = myLocation.longitude

            val xDiff = (x2 - x1).toFloat()
            val yDiff = (y2 - y1).toFloat()

            return (Math.atan2(yDiff.toDouble(), xDiff.toDouble()) * 180.0 / Math.PI).toFloat()
        }
    }

    /*mMapView.mapOrientation = -currentLocation.bearing

                        val width = this@MainMapActivity.windowManager.defaultDisplay.width
                        val height = this@MainMapActivity.windowManager.defaultDisplay.height

                        val isLandscape = width > height
                        if(isLandscape)
                        {
                            setCenterMap()
                        }
                        else
                        {
                            setCenterMap()
                        }
                        oldLocation=myLocation*/

    override fun onResume() {
        super.onResume()
        //
        mScaleBarOverlay.enableScaleBar()
        mLocationOverlay.enableMyLocation()
        mMapView.onResume()
        if (followMeEnable) {
            timer = Timer()
            timer2 = Timer()
            oldLocation = myLocation
            timer.scheduleAtFixedRate(followMeTask(), 0, 1000)
        }
    }

    override fun onPause() {
        super.onPause()
        // locationManager.removeUpdates(this)
        mLocationOverlay.disableMyLocation()
        mScaleBarOverlay.disableScaleBar()
        mMapView.onPause()
        if (followMeEnable) {
            timer.cancel()
        }
    }

    private fun myLocationOverlay() {
        val g = GpsMyLocationProvider(this)
        g.locationUpdateMinTime = 0
        g.locationUpdateMinDistance = 1f
        mLocationOverlay = MyLocationNewOverlay(g, mMapView)

        mMapView.overlays.add(mLocationOverlay)
        mMapEvent = MapEventsOverlay(this)
        mMapView.overlays.add(mMapEvent)

        val icon = BitmapFactory.decodeResource(
            this.resources,
            R.drawable.navigator
        )

        mLocationOverlay.setDirectionArrow(icon, icon)

        mMapView.setOnGenericMotionListener(object : View.OnGenericMotionListener {
            override fun onGenericMotion(v: View?, event: MotionEvent?): Boolean {
                if (0 != event!!.source and InputDevice.SOURCE_CLASS_POINTER) {
                    when (event.action) {
                        MotionEvent.ACTION_SCROLL -> {
                            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                                mMapView.controller.zoomOut()
                            else {
                                val iGeoPoint = mMapView.projection.fromPixels(event.x.toInt(), event.y.toInt())
                                mMapView.controller.animateTo(iGeoPoint)
                                mMapView.controller.zoomIn()
                            }
                            return true
                        }
                        else -> {
                            println("Ну рабой уже сука")
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    private fun mScaleBarOverlay() {
        val dm = resources.displayMetrics
        mScaleBarOverlay = ScaleBarOverlay(mMapView)
        mScaleBarOverlay.setCentred(true)
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)

        mMapView.overlays.add(mScaleBarOverlay)
    }

    private fun mRotationOverlay() {
        mRotationGestureOverlay = RotationGestureOverlay(mMapView)
        mRotationGestureOverlay.isEnabled = true
        mMapView.setMultiTouchControls(true)
        mMapView.overlays.add(mRotationGestureOverlay)
    }

    private fun getLocation() {
        val time: Float = 0F
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

        myLocation = GeoPoint(location.latitude, location.longitude)

        println(myLocation)

        if (firstStart) {
            mMapView.controller.setCenter(myLocation)
            firstStart = false
        }
    }

    override fun onProviderDisabled(provider: String) {
        /*Toast.makeText(this@NavigatorActivity, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show()*/
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
    }

    override fun onProviderEnabled(provider: String) {
    }
}

// var oldMapOrientation:Int = 0
// var newMapOrientation:Int = 0

/*  private inner class rotationCamera: TimerTask() {
      override fun run() {
          if(oldLocation.distanceToAsDouble(myLocation)>20.0)
          {
              runOnUiThread {


                  if(oldMapOrientation!=newMapOrientation)
                  {


                      if(oldMapOrientation>270 && newMapOrientation<90)
                      {
                          oldMapOrientation+=1
                          if(oldMapOrientation>360)
                              oldMapOrientation=0
                      }
                      else
                      {
                          if(oldMapOrientation<90 && newMapOrientation>270)
                          {
                              oldMapOrientation-=1
                              if(oldMapOrientation<0)
                                  oldMapOrientation=360
                          }
                          else
                          {
                              if(oldMapOrientation<newMapOrientation)
                              {
                                  oldMapOrientation+=1
                              }
                              else
                              {
                                  oldMapOrientation-=1
                              }
                          }
                      }
                      println("Now $oldMapOrientation new $newMapOrientation")


                      runOnUiThread {
                          mMapView.controller.setCenter(myLocation)
                          mMapView.mapOrientation = (-oldMapOrientation).toFloat()
                          mMapView.controller.animateTo(myLocation.destinationPoint(
                          (3*mScaleBarOverlay.screenHeight/4).toDouble(),
                          ((oldMapOrientation)).toDouble()
                          ))
                          *//*mMapView.controller.animateTo(myLocation)
                            mMapView.mapOrientation = (-oldMapOrientation).toFloat()*//*
                        }
                    }
                    else
                    {
                        if(newMapOrientation!=currentLocation.bearing.toInt())
                        {
                            newMapOrientation = currentLocation.bearing.toInt()
                        }

                        runOnUiThread {
                            mMapView.controller.setCenter(myLocation)
                            mMapView.mapOrientation = (-oldMapOrientation).toFloat()
                            mMapView.controller.animateTo(myLocation.destinationPoint(
                                (3*mScaleBarOverlay.screenHeight/4).toDouble(),
                                ((oldMapOrientation)).toDouble()
                            ))
                        *//*mMapView.controller.animateTo(myLocation)

                            mMapView.mapOrientation = (-oldMapOrientation).toFloat()*//*
                        }
                    }
                }
            }
        }
    }*/