package kobramob.rubeg38.ru.gbrnavigation.startactivity

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputEditText
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.github.clans.fab.FloatingActionMenu
import kobramob.rubeg38.ru.gbrnavigation.*
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.NavigatorFragment
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.resource.TileSource
import kobramob.rubeg38.ru.gbrnavigation.service.PollingServer
import kobramob.rubeg38.ru.gbrnavigation.service.Request
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.events.MapEventsReceiver
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

class StartActivity : AppCompatActivity(), MapEventsReceiver {

    private var progressBarOpen: String = "close"
    private lateinit var cancelDialog: AlertDialog
    @SuppressLint("InflateParams")
    private fun showProgressBar() {
        if (progressBarOpen == "close") {
            val dialog = AlertDialog.Builder(this)
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
            followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
            followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
            enableFollowMe = false
        }
        return true
    }

    companion object {
        const val BROADCAST_ACTION = "kobramob.ruber38.ru.gbrnavigation.startactivity"
        lateinit var rotationGestureOverlay: RotationGestureOverlay
        lateinit var locationOverlay: MyLocationNewOverlay
    }

    private val startActivityModel: StartActivityModel = StartActivityModel()

    private val request: Request = Request()
    private val tileSource: TileSource =
        TileSource()

    private var timer = Timer()
    lateinit var followButton: FloatingActionButton
    lateinit var centerButton: FloatingActionButton

    private lateinit var mMapView: MapView
    private lateinit var scaleBarOverlay: ScaleBarOverlay
    var enableFollowMe = false

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CALL_PHONE,
                    android.Manifest.permission.READ_PHONE_STATE
                ),
                101
            )
        }
    }

    private fun dialogChangeMap() {
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
            this@StartActivity,
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

    private fun dialogServerSetting() {
        val dialog_ss = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.server_setting_dialog, null)

        val ip_server = view.findViewById(R.id.ip_server) as TextInputEditText
        val port_server = view.findViewById(R.id.port_server) as TextInputEditText

        dialog_ss.setView(view)
        dialog_ss.setTitle("Настройки сервера")
        dialog_ss.setPositiveButton("Сохранить") { dialog, _ ->

            if (ip_server.text.toString() == "" || port_server.text.toString() == "") {
                Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
                ip_server.setText("")
                port_server.setText("")
                dialog.cancel()
                dialogServerSetting()
            } else {
                SharedPreferencesState.init(this)
                SharedPreferencesState.addPropertyString(
                    "ip",
                    ip_server.text.toString()
                )

                SharedPreferencesState.addPropertyInt(
                    "port",
                    port_server.text.toString().toInt()
                )
                unregisterReceiver(br)
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }.show()
    }

    lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        NavigatorFragment.firstTime = true

        checkPermission()

        val actionMenu: FloatingActionMenu = findViewById(R.id.parent_menu)
        actionMenu.isIconAnimated = false
        toolbar = findViewById(R.id.startToolbar)
        centerButton = findViewById(R.id.my_location)
        followButton = findViewById(R.id.follow_me)
        mMapView = findViewById(R.id.startMap)

        setSupportActionBar(toolbar)
        if (getSharedPreferences("state", Context.MODE_PRIVATE).contains("status")) {
            val title =
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "") + " " +
                    getSharedPreferences("state", Context.MODE_PRIVATE).getString("call", "") +
                    " (" + getSharedPreferences("state", Context.MODE_PRIVATE).getString("status", "") + ")"
            supportActionBar!!.title = title
        } else {
            val title =
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "") + " " +
                    getSharedPreferences("state", Context.MODE_PRIVATE).getString("call", "") +
                    " (Свободен)"
            supportActionBar!!.title = title
        }

        initFloatingMenu()
        initMapView(mMapView)

        addOverlays(mMapView)

        clickListenerCenterButton(centerButton)

        clickFollowButton(followButton)

        centerMap()
    }

    private fun centerMap() {
        try {
            mMapView.controller.animateTo(
                GeoPoint(
                    getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lat", 0f).toDouble(),
                    getSharedPreferences("state", Context.MODE_PRIVATE).getFloat("lon", 0f).toDouble()
                )
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Ваше месторасположение не определено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clickFollowButton(followButton: FloatingActionButton?) {
        followButton!!.setOnClickListener {
            if (enableFollowMe) {

                /*startActivityModel.stopFollowMe()*/
                timer.cancel()
                followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
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
                    followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                    followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))

                    enableFollowMe = true
                } catch (e: Exception) {}
            }
        }
    }

    private fun clickListenerCenterButton(centerButton: FloatingActionButton?) {
        centerButton!!.setOnClickListener {
            try {
                if (enableFollowMe) {
                    timer.cancel()
                    followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                    followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                    enableFollowMe = false
                }

                mMapView.controller.animateTo(locationOverlay.myLocation)
                SharedPreferencesState.init(this)
                SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
                SharedPreferencesState.addPropertyFloat("lon", locationOverlay.myLocation.longitude.toFloat())
            } catch (e: Exception) {
                Toast.makeText(this, "Ваше месторасположение не определено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.server_setting -> { dialogServerSetting() }
            R.id.change_map -> { dialogChangeMap() }
        }
        return super.onOptionsItemSelected(item)
    }

    lateinit var oldLocation: GeoPoint

    private inner class NavigatorTask : TimerTask() {

        override fun run() {
            runOnUiThread {
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

    private lateinit var br: BroadcastReceiver

    private fun broadcastReceiver() {
        br = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context?, intent: Intent?) {
                /*val request = intent?.getStringExtra("test")
                if(locationOverlay.lastFix.speed!=0f)
                intent!!.putExtra("speed",locationOverlay.lastFix.speed)
                println(request)*/
                val status = intent?.getStringExtra("status")
                val accessDenied = intent?.getBooleanExtra("accessDenied",false)
                if(accessDenied!!)
                {
                    //Dialog Window
                    Toast.makeText(this@StartActivity,"Связь с Сервером потеряна!",Toast.LENGTH_LONG).show()
                }
                SharedPreferencesState.init(this@StartActivity)
                if (status != null) {
                    SharedPreferencesState.addPropertyString("status", status)
                }
                if (status == "alarm") {
                    val serverResponse = intent.getStringExtra("info")
                    val jsonObject = JSONObject(serverResponse)
                    val jsonArray = jsonObject.getJSONArray("d")

                    if (Build.VERSION.SDK_INT >= 26) {
                        (this@StartActivity.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        (this@StartActivity.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(1000)
                    }

                    val trevoga = MediaPlayer.create(this@StartActivity, R.raw.trevoga)
                    trevoga.start()

                    val alertDialog = AlertDialog.Builder(this@StartActivity)
                    val view = layoutInflater.inflate(R.layout.dialog_alarm, null)
                    alertDialog.setView(view)
                    val dialog: AlertDialog = alertDialog.create()
                    dialog.show()

                    val acceptAlertButton: Button = view!!.findViewById(R.id.AcceptAlert)
                    val dialog_objectName: TextView = view.findViewById(R.id.dialog_objectName)
                    val dialog_objectAddress: TextView = view.findViewById(R.id.dialog_objectAddress)
                    dialog_objectName.text = JSONObject(jsonArray.getString(0)).getString("name")
                    dialog_objectAddress.text = JSONObject(jsonArray.getString(0)).getString("address")

                    acceptAlertButton.setOnClickListener {
                        trevoga.stop()
                        acceptAlarm(jsonArray)
                        unregisterReceiver(br)
                        val objectActivity = Intent(this@StartActivity, ObjectActivity::class.java)
                        objectActivity.putExtra("info", serverResponse)
                        startActivity(objectActivity)
                        dialog.cancel()
                    }
                } else {
                    val title =
                        getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "") + " " +
                            getSharedPreferences("state", Context.MODE_PRIVATE).getString("call", "") +
                            " (" + getSharedPreferences("state", Context.MODE_PRIVATE).getString("status", "") + ")"
                    supportActionBar!!.title = title
                }
            }
        }
        val intentFilter = IntentFilter(BROADCAST_ACTION)
        registerReceiver(br, intentFilter)
    }

    private fun acceptAlarm(jsonArray: JSONArray) {
        val acceptAlarm = Runnable {
            request.acceptAlarm(
                PollingServer.socket,
                PollingServer.countSender,
                JSONObject(jsonArray.getString(0)).getString("number"),
                0,
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", ""),
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 0),
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", "")
            )
        }; Thread(acceptAlarm).start()
        PollingServer.countSender++
    }

    var latiduet: Double = 0.toDouble()
    override fun onResume() {
        super.onResume()

        broadcastReceiver()

        mMapView.onResume()
        if (!locationOverlay.isEnabled) {
            locationOverlay.enableMyLocation()
            scaleBarOverlay.enableScaleBar()
        }

        showProgressBar()

        val tread = Runnable {
            do {
                try {
                    latiduet = locationOverlay.lastFix.latitude
                } catch (e: Exception) {}
            } while (latiduet == 0.toDouble())
            runOnUiThread {
                if (latiduet != 0.toDouble()) {
                    closeProgressBar()
                    try{
                        mMapView.controller.animateTo(GeoPoint(locationOverlay.lastFix.latitude, locationOverlay.lastFix.longitude))
                    }catch (e:Exception){}
                }
            }
        }; Thread(tread).start()
    }

    private fun initMapView(MapView: MapView) {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        MapView.setTileSource(TileSourceFactory.MAPNIK)
        MapView.setHasTransientState(true)
        MapView.controller.setZoom(15.0)
        MapView.isTilesScaledToDpi = true
        MapView.isFlingEnabled = true

        MapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                // TODO слушатель на карту
                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                // TODO слушатель на карту
                if (enableFollowMe) {
                    timer.cancel()
                    followButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.textWhite))
                    followButton.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.textDark))
                    enableFollowMe = false
                }
                return true
            }
        })
    }

    private fun addOverlays(MapView: MapView) {
        addOverlays(MapView, initLocationOverlay(MapView))
        addOverlays(MapView, initScaleBarOverlay(MapView))
        addOverlays(MapView, initRotationGestureOverlay(MapView))
    }

    private fun initRotationGestureOverlay(MapView: MapView): RotationGestureOverlay {
        rotationGestureOverlay = RotationGestureOverlay(MapView)
        rotationGestureOverlay.isEnabled = true
        MapView.setMultiTouchControls(true)
        return rotationGestureOverlay
    }

    private fun initScaleBarOverlay(MapView: MapView): ScaleBarOverlay {
        scaleBarOverlay = ScaleBarOverlay(MapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay
    }

    private fun initLocationOverlay(MapView: MapView): MyLocationNewOverlay {
        val gpsMyLocationProvider = GpsMyLocationProvider(this)
        gpsMyLocationProvider.locationUpdateMinDistance = 0f
        gpsMyLocationProvider.locationUpdateMinTime = 0
        locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider, MapView)
        locationOverlay.setDirectionArrow(customIcon(R.drawable.navigator), customIcon(R.drawable.navigator))
        locationOverlay.isDrawAccuracyEnabled = false
        return locationOverlay
    }

    private fun addOverlays(MapView: MapView, overlay: Overlay) {
        MapView.overlays.add(overlay)
    }

    private fun customIcon(drawable: Int): Bitmap? {
        return BitmapFactory.decodeResource(
            resources,
            drawable
        )
    }

    private fun initFloatingMenu() {
        val floatingActionButton1: com.github.clans.fab.FloatingActionButton = findViewById(R.id.change_status_dinner)
        val floatingActionButton2: com.github.clans.fab.FloatingActionButton = findViewById(R.id.change_status_refueling)
        val floatingActionButton3: com.github.clans.fab.FloatingActionButton = findViewById(R.id.change_status_repairs)
        floatingActionButton1.setOnClickListener {
            statusChanged(floatingActionButton1)
        }
        floatingActionButton2.setOnClickListener {
            statusChanged(floatingActionButton2)
        }
        floatingActionButton3.setOnClickListener {
            statusChanged(floatingActionButton3)
        }
    }

    private fun statusChanged(floatingActionButton: com.github.clans.fab.FloatingActionButton) {
        val statusChanged = Runnable {
            request.changeStatus(
                PollingServer.socket,
                floatingActionButton.labelText,
                PollingServer.countSender,
                0,
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", ""),
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 0),
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", "")
            )
            PollingServer.countSender++
        }
        Thread(statusChanged).start()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
        locationOverlay.disableMyLocation()
        scaleBarOverlay.disableScaleBar()
    }

    override fun onStop() {
        super.onStop()
        try {

            SharedPreferencesState.init(this)
            SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
            SharedPreferencesState.addPropertyFloat("lon", locationOverlay.myLocation.longitude.toFloat())
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
