package kobramob.rubeg38.ru.gbrnavigation.startactivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.TileSource
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.service.PollingServer
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
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

class StartActivity : AppCompatActivity() {

    companion object {
        const val BROADCAST_ACTION = "kobramob.ruber38.ru.gbrnavigation.startactivity"
    }

    val startActivityModel:StartActivityModel = StartActivityModel()

    private val tileSource: TileSource =
        TileSource()

    private var timer = Timer()
    lateinit var followButton:FloatingActionButton
    lateinit var centerButton:FloatingActionButton

    private lateinit var mMapView:MapView
    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay


    var enableFollowMe = false
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

    private fun dialog_change_map()
    {
        val dialog_cm = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.change_map_dialog,null)
        val spinnerMap = view.findViewById(R.id.spinner_map) as Spinner
        spinnerMap.prompt = "Список карт"
        val arrayMap=ArrayList<String>()
        arrayMap.add("OpenStreetMap")
        arrayMap.add("GoogleRoad")
        arrayMap.add("GoogleSat")
        arrayMap.add("GoogleHybrid")
        val spinnerAdapter: ArrayAdapter<String> = ArrayAdapter(
            this@StartActivity,
            R.layout.simple_spinner_item,
            arrayMap
        )
        var mapType:String = ""
        var mapTile: OnlineTileSourceBase = TileSourceFactory.MAPNIK
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item)
        spinnerMap.adapter = spinnerAdapter
        spinnerMap.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position)
                mapType = item.toString()
            }
        }
        dialog_cm.setView(view)
        dialog_cm.setTitle("Список карт")
        dialog_cm.setPositiveButton("Выбрать")
        {dialog, which ->
            println(mapType)
            when(mapType){
                "OpenStreetMap"->{mapTile = TileSourceFactory.MAPNIK}
                "GoogleRoad"->{mapTile = tileSource.GoogleRoads}
                "GoogleSat"->{mapTile = tileSource.GoogleSat}
                "GoogleHybrid"->{mapTile = tileSource.GoogleHybrid}
            }
            TileSourceFactory.addTileSource(mapTile)
            mMapView.setTileSource(mapTile)
        }.show()

    }

    private fun dialog_server_setting()
    {
        val dialog_ss = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.server_setting_dialog,null)

        val ip_server =view.findViewById(R.id.ip_server) as EditText
        val port_server = view.findViewById(R.id.port_server) as EditText


        dialog_ss.setView(view)
        dialog_ss.setTitle("Настройки сервера")
        dialog_ss.setPositiveButton("Сохранить")
        {dialog, which ->

            if(ip_server.text.toString() == "" || port_server.text.toString() == "")
            {
                Toast.makeText(this,"Все поля должны быть заполнены",Toast.LENGTH_SHORT).show()
                ip_server.setText("")
                port_server.setText("")
                dialog.cancel()
                dialog_server_setting()
            }
        }.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        checkPermission()

        val toolbar:Toolbar = findViewById(R.id.startToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)

        mMapView = findViewById(R.id.startMap)

        initMapView(mMapView)
        addOverlays(mMapView)

        mMapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                //TODO слушатель на карту
              /*  if(enableFollowMe){
                    timer.cancel()
                    followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                    followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                    enableFollowMe = false
                }*/
                /*Toast.makeText(this@StartActivity, "onScroll", Toast.LENGTH_SHORT).show()*/
                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                //TODO слушатель на карту
                if(enableFollowMe){
                    timer.cancel()
                    followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                    followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                    enableFollowMe = false
                }
                return true
            }
        })

        centerButton = findViewById(R.id.my_location)

        centerButton.setOnClickListener {
            try {
                if(enableFollowMe){
                    timer.cancel()
                    followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                    followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                    enableFollowMe = false
                }

                mMapView.controller.animateTo(locationOverlay.myLocation)
                SharedPreferencesState.init(this)
                SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
                SharedPreferencesState.addPropertyFloat("lon",locationOverlay.myLocation.longitude.toFloat())
            }catch (e:Exception)
            {
                Toast.makeText(this,"Ваше месторасположение не определено",Toast.LENGTH_SHORT).show()
            }
        }

        followButton = findViewById(R.id.follow_me)
        followButton.setOnClickListener {
            if(enableFollowMe)
            {

                    /*startActivityModel.stopFollowMe()*/
                    timer.cancel()
                    followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                    followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                    enableFollowMe = false
            }
            else
            {
                    /*startActivityModel.followMe(mMapView,locationOverlay)*/
                    try{
                        oldLocation = locationOverlay.myLocation
                        setCenter()
                        mMapView.controller.setZoom(15.0)
                        mMapView.mapOrientation = - locationOverlay.lastFix.bearing

                        timer = Timer()
                        timer.scheduleAtFixedRate(NavigatorTask(),0,1000)
                        followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                        followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))

                        enableFollowMe = true

                    }catch (e:Exception){}
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.server_setting ->{dialog_server_setting()   }
            R.id.change_map ->{dialog_change_map()}
            R.id.alert_test ->{

                if (Build.VERSION.SDK_INT >= 26) {
                    (this.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    (this.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(1000)
                }

                /*val trevoga = MediaPlayer.create(this,R.raw.jojo)
                trevoga.start()*/

                val alertDialog = AlertDialog.Builder(this)
                val view = layoutInflater.inflate(R.layout.dialog_alarm, null)
                alertDialog.setView(view)
                val dialog: AlertDialog = alertDialog.create()
                dialog.show()

                val acceptAlertButton: Button = view!!.findViewById(R.id.AcceptAlert)
                acceptAlertButton.setOnClickListener {
                    println("accept")
                    dialog.cancel()

                    val objectActivity = Intent(this@StartActivity, ObjectActivity::class.java)
                    startActivity(objectActivity)

                }

                //val et_name = view!!.findViewById(R.id.new_name) as EditText
                /*val uri = Uri.parse("yandexnavi://build_route_on_map?lat_to=55.70&lon_to=37.64")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("ru.yandex.yandexnavi")
                startActivity(intent)*/
            }
        }
        return super.onOptionsItemSelected(item)
    }

    lateinit var oldLocation:GeoPoint

    private inner class NavigatorTask : TimerTask() {

        override fun run() {
            runOnUiThread {
                try{
                    if(oldLocation.distanceToAsDouble(locationOverlay.myLocation)>20.0)
                    {
                        mMapView.mapOrientation = - locationOverlay.lastFix.bearing
                        setCenter()
                        oldLocation = locationOverlay.myLocation
                    }
                }catch (e:Exception){}
            }
        }
    }

    private fun setCenter()
    {
        val density = resources.displayMetrics.densityDpi
        println("Плотность пикселей $density")
        when (density) {
            480->{
                mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                    (2*scaleBarOverlay.screenHeight/4).toDouble(),
                    ((locationOverlay.lastFix.bearing)).toDouble()
                ))
            }
            320->{
                mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                    (2*scaleBarOverlay.screenHeight/4).toDouble(),
                    ((locationOverlay.lastFix.bearing)).toDouble()
                ))
            }
            DisplayMetrics.DENSITY_LOW ->
            {
                mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                    (3*scaleBarOverlay.screenHeight/4).toDouble(),
                    ((locationOverlay.lastFix.bearing)).toDouble()
                ))
            }
            DisplayMetrics.DENSITY_MEDIUM -> {
                mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                    (3*scaleBarOverlay.screenHeight/4).toDouble(),
                    ((locationOverlay.lastFix.bearing)).toDouble()
                ))
            }
            DisplayMetrics.DENSITY_HIGH -> {
                mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                    (3*scaleBarOverlay.screenHeight/4).toDouble(),
                    ((locationOverlay.lastFix.bearing)).toDouble()
                ))
            }
            DisplayMetrics.DENSITY_XHIGH -> {
                mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                    (3*scaleBarOverlay.screenHeight/4).toDouble(),
                    ((locationOverlay.lastFix.bearing)).toDouble()
                ))
            }
            DisplayMetrics.DENSITY_XXHIGH-> {
                mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                    (3*scaleBarOverlay.screenHeight/4).toDouble(),
                    ((locationOverlay.lastFix.bearing)).toDouble()
                ))
            }
            DisplayMetrics.DENSITY_XXXHIGH->
            {
                mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                    (3*scaleBarOverlay.screenHeight/4).toDouble(),
                    ((locationOverlay.lastFix.bearing)).toDouble()
                ))
            }
            DisplayMetrics.DENSITY_TV->{
                mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                    (3*scaleBarOverlay.screenHeight/4).toDouble(),
                    ((locationOverlay.lastFix.bearing)).toDouble()
                ))
            }
            else -> {mMapView.controller.animateTo(locationOverlay.myLocation.destinationPoint(
                (scaleBarOverlay.screenHeight/3).toDouble(),
                ((locationOverlay.lastFix.bearing)).toDouble()
            ))}
        }
    }

    private lateinit var br:BroadcastReceiver
    private  var lon:Double? = 0.0
    private  var lat:Double? = 0.0

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

}
