package commonactivity

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.github.clans.fab.FloatingActionMenu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.R.*
import kobramob.rubeg38.ru.gbrnavigation.R.id.*
import referenceactivity.ReferenceActivity
import loginactivity.LoginActivity
import objectactivity.ObjectActivity
import objectactivity.data.ObjectDataStore
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import resource.ControlLifeCycleService
import resource.DataStore
import workservice.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class CommonActivity : AppCompatActivity() {

    private val sizeNormal = 0
    private val sizeMini = 1
    private var mMapView: MapView? =null

    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay

    private var alertAccept:Boolean = false
    private var timer:CountDownTimer? = null

    var dialogStatus:AlertDialog? = null
    var dialogAlarm:AlertDialog? = null

    var alertSound: MediaPlayer = MediaPlayer()

    companion object {
        var isAlive = false
        var exit = false
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)

        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ applicationContext.packageName + "/" + raw.alarm_sound)
        alertSound = MediaPlayer.create(this,soundUri)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        mMapView = findViewById(common_mapView)


        val toolbar: Toolbar = findViewById(common_toolbar)
        setSupportActionBar(toolbar)
        val title: String = "${DataStore.call} ( ${DataStore.status} ) ver.${BuildConfig.VERSION_NAME} "
        supportActionBar?.title = title

        fillFabMenu()

        initMapView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.common_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            server_setting -> {
                val serverSetting = AlertDialog.Builder(this@CommonActivity)
                serverSetting.setTitle("Смена сервера")
                    .setMessage("Вы хотите изменить основной сервер?")
                    .setCancelable(false)
                    .setPositiveButton("Да") { _, _ ->

                        val service = Intent(this, ProtocolNetworkService::class.java)
                        service.putExtra("command", "stop")
                        startService(service)

                        DataStore.clearAllData()

                        val loginActivity = Intent(this, LoginActivity::class.java)
                        loginActivity.putExtra("imei", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                        startActivity(loginActivity)
                    }
                    .setNegativeButton("Нет") { dialog, _ ->
                        dialog.cancel()
                    }.show()
            }
            reference->{
                ///Вызов справочника
                val referenceActivity = Intent(this, ReferenceActivity::class.java)
                startActivity(referenceActivity)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun fabClick(view: View) {
        val followFab: FloatingActionButton = findViewById(common_followMe)
        when (view.id) {
            common_myLocation -> {
                try {
                    if (locationOverlay.isFollowLocationEnabled) {
                        locationOverlay.disableFollowLocation()
                        followFab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.viewBackground))
                        followFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.colorPrimary))
                    }
                    try{
                        mMapView?.controller?.animateTo(GeoPoint(LocationService.imHere))
                    }catch (e:java.lang.Exception){
                        e.printStackTrace()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Ваше месторасположение не определено", Toast.LENGTH_SHORT).show()
                }
            }
            common_followMe -> {

                if (locationOverlay.isFollowLocationEnabled) {
                    locationOverlay.disableFollowLocation()
                    followFab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.viewBackground))
                    followFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.colorPrimary))
                } else {
                    locationOverlay.enableFollowLocation()
                    followFab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.colorPrimary))
                    followFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color.viewBackground))
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN, priority = 0)
    fun onStickyAlertEvent(event: AlarmEvent) {
        when {
            event.command.isNotEmpty() -> {
                alertAccept = true
                val planAndPhoto: ArrayList<String> = ArrayList()
                planAndPhoto.addAll(event.plan)
                planAndPhoto.addAll(event.photo)

                val alarmObjectInfo = AlarmObjectInfo(
                    event.name,
                    event.number,
                    event.lon,
                    event.lat,
                    event.inn,
                    event.zakaz,
                    event.address,
                    event.area.name,
                    event.area.alarmtime,
                    planAndPhoto,
                    event.otvl,
                    DataStore.reports
                )


                alertSound.start()

                val alertDialog = AlertDialog.Builder(this@CommonActivity)
                val view = layoutInflater.inflate(R.layout.dialog_alarm, null, false)
                alertDialog.setView(view)

                try{
                    dialogAlarm =  alertDialog.create()
                    dialogAlarm?.setCancelable(false)
                    dialogAlarm?.show()
                }catch (e:java.lang.Exception){
                    e.printStackTrace()
                }


                val acceptAlertButton: Button = view!!.findViewById(AcceptAlert)
                val dialogObjectName: TextView = view.findViewById(dialog_objectName)
                val dialogObjectAddress: TextView = view.findViewById(dialog_objectAddress)

                dialogObjectName.text = event.name
                dialogObjectAddress.text = event.address
                acceptAlertButton.setOnClickListener {

                    alertSound.stop()

                    if (alertSound.isPlaying) {
                        alertSound.stop()
                        alertSound.reset()
                    }

                    when {
                        !ProtocolNetworkService.connectInternet -> {
                            Toast.makeText(this, "Нет соединения с интернетом, невозможно отправить подтверждение тревоги", Toast.LENGTH_LONG).show()
                            EventBus.getDefault().removeAllStickyEvents()
                            val objectActivity = Intent(this, ObjectActivity::class.java)
                            objectActivity.putExtra("objectInfo", alarmObjectInfo)
                            startActivity(objectActivity)
                        }
                        !ProtocolNetworkService.connectServer -> {
                            Toast.makeText(this, "Нет соединения с сервером, невозможно отправить подтверждение тревоги", Toast.LENGTH_LONG).show()
                            EventBus.getDefault().removeAllStickyEvents()
                            val objectActivity = Intent(this, ObjectActivity::class.java)
                            objectActivity.putExtra("objectInfo", alarmObjectInfo)
                            startActivity(objectActivity)
                        }
                        else -> {
                            thread {
                                val message = JSONObject()
                                message.put("\$c$", "gbrkobra")
                                message.put("command", "alarmp")
                                message.put("number", alarmObjectInfo.number)

                                ProtocolNetworkService.protocol?.request(message.toString()) {
                                    success: Boolean, data: ByteArray? ->
                                        if (success && data != null) {
                                            runOnUiThread {

                                                val currentTime: String = SimpleDateFormat(
                                                    "HH:mm:ss",
                                                    Locale.getDefault()
                                                ).format(Date())

                                                ObjectDataStore.timeAlarmApply(currentTime)

                                                Toast.makeText(this, "Тревога подтверждена в $currentTime", Toast.LENGTH_SHORT).show()

                                            }
                                        } else {
                                            runOnUiThread {
                                                Toast.makeText(this, "Тревога не подтверждена", Toast.LENGTH_SHORT).show()
                                            }

                                        }
                                    }
                                EventBus.getDefault().removeAllStickyEvents()
                                val objectActivity = Intent(this, ObjectActivity::class.java)
                                objectActivity.putExtra("objectInfo", alarmObjectInfo)
                                startActivity(objectActivity)
                            }
                        }
                    }
                    dialogAlarm?.cancel()
                }
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN, priority = 0)
    fun onStickyMessageEvent(event: MessageEvent) {
        when {
            event.command.isNotEmpty() -> {
                when (event.command) {
                    "gbrstatus" -> {
                        try
                        {
                            runOnUiThread {
                                DataStore.status =  event.message
                                val title: String = "${DataStore.call} ( ${DataStore.status} ) ver.${BuildConfig.VERSION_NAME} "
                                supportActionBar?.title = title
                                for(i in 0 until DataStore.statusList.count())
                                {
                                    if(DataStore.statusList[i].name == event.message && DataStore.statusList[i].time!= "0")
                                    {
                                        val stopStatus = {
                                            thread {
                                                this.timer?.cancel()
                                                val statusChangeMessage = JSONObject()
                                                statusChangeMessage.put("\$c$", "gbrkobra")
                                                statusChangeMessage.put("command", "status")
                                                statusChangeMessage.put("newstatus", "Свободен")
                                                ProtocolNetworkService.protocol?.request(statusChangeMessage.toString()) {
                                                        access: Boolean, data: ByteArray? ->
                                                    if (access && data != null) {
                                                        runOnUiThread {
                                                            DataStore.status = JSONObject(String(data)).getString("status")
                                                            val title: String = "${DataStore.call} ( ${DataStore.status} ) ver.${BuildConfig.VERSION_NAME} "
                                                            supportActionBar?.title = title
                                                        }
                                                    } else {
                                                        runOnUiThread {
                                                            Toast.makeText(this, "Смена статуса невозможно, сервер не отвечает", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        try{
                                            if(dialogStatus?.isShowing!!){
                                                dialogStatus?.cancel()
                                            }
                                        }catch (e:java.lang.Exception){
                                            e.printStackTrace()
                                        }

                                        val statusTimerDialog = AlertDialog.Builder(this@CommonActivity)
                                        val view = layoutInflater.inflate(R.layout.status_timer_dialog,null,false)
                                        statusTimerDialog.setView(view)
                                        statusTimerDialog.setTitle(DataStore.statusList[i].name)
                                        statusTimerDialog.setPositiveButton("Завершить"){
                                            dialogInterface, i ->
                                            stopStatus()
                                        }

                                        dialogStatus = statusTimerDialog.create()
                                        dialogStatus?.setCancelable(false)
                                        dialogStatus?.show()

                                        val statusTimer:TextView = view.findViewById(status_timer)

                                        this.timer?.cancel()

                                        this.timer = object : CountDownTimer(DataStore.statusList[i].time.toLong() * 60000, 1000)
                                        {
                                            override fun onFinish() {
                                                dialogStatus?.cancel()
                                                stopStatus()
                                            }

                                            override fun onTick(time: Long) {
                                                println("Tick $time")
                                                updateTimer((time / 1000).toInt())
                                            }

                                            private fun updateTimer(time: Int) {
                                                val hours = time / 3600
                                                val minute = (time % 3600)/60
                                                val seconds = time % 60

                                                val timeRemains = "$hours:$minute:$seconds"
                                                statusTimer.text = timeRemains

                                            }
                                        }

                                        this.timer?.start()
                                    }
                                }
                                if(event.message == "Свободен")
                                {
                                    this.timer?.cancel()
                                    dialogStatus?.cancel()
                                }

                                fillFabMenu()
                            }
                        }
                        catch (e:java.lang.Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isAlive = true
        if (alertSound.isPlaying) {
            alertSound.stop()
            alertSound.reset()
        }



        if (!ProtocolNetworkService.isServiceStarted)
        {
            ControlLifeCycleService.reconnectToServer(this)
            thread{
                while (!ProtocolNetworkService.isServiceStarted){
                }
                runOnUiThread {
                    val message = JSONObject()
                    message.put("\$c$","getalarm")
                    message.put("namegbr", DataStore.namegbr)
                    ProtocolNetworkService.protocol?.send(message = message.toString()){
                        if(it){
                            runOnUiThread {
                                Toast.makeText(this,"Проверка тревоги",Toast.LENGTH_SHORT).show()
                            }

                        }
                        else{
                            runOnUiThread {
                                Toast.makeText(this,"Ошибка при проверке тревоги",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
        else
        {

            try{
                if(dialogAlarm?.isShowing!!){
                    dialogAlarm?.cancel()
                }
            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }

                val message = JSONObject()
                message.put("\$c$","getalarm")
                message.put("namegbr", DataStore.namegbr)
                ProtocolNetworkService.protocol?.send(message = message.toString()){
                    if(it){
                        runOnUiThread {
                            Toast.makeText(this,"Проверка тревоги",Toast.LENGTH_SHORT).show()
                        }

                    }
                    else{
                        runOnUiThread {
                            Toast.makeText(this,"Ошибка при проверке тревоги",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }

        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this)

    }

    override fun onResume() {
        super.onResume()

        if(mMapView != null)
        mMapView?.onResume()

        if (!locationOverlay.isMyLocationEnabled && !scaleBarOverlay.isEnabled) {
            locationOverlay.enableMyLocation()
            scaleBarOverlay.enableScaleBar()
        }
    }

    override fun onPause() {
        super.onPause()

        mMapView?.onPause()

        if(locationOverlay.isMyLocationEnabled && scaleBarOverlay.isEnabled)
        {
            locationOverlay.disableMyLocation()
            scaleBarOverlay.disableScaleBar()
        }

        if (locationOverlay.isFollowLocationEnabled)
        {
            locationOverlay.disableFollowLocation()
        }

    }

    override fun onStop() {
        super.onStop()
        isAlive = false

        if(EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().unregister(this)

        alertAccept = false
    }

    private fun initMapView() {
        org.osmdroid.config.Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        mMapView?.setTileSource(TileSourceFactory.MAPNIK)
        mMapView?.setHasTransientState(true)
        mMapView?.controller?.setZoom(15.0)
        mMapView?.isTilesScaledToDpi = true
        mMapView?.isFlingEnabled = true

        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val buildAlertMessageNoGps = {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setMessage("GPS отключен, хотите ли вы его включить? (Приложение не работает без GPS)")
                .setCancelable(false)
                .setPositiveButton("Да"){_,_ ->
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    thread {
                        while(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        {
                            //gps problem
                        }
                        runOnUiThread {
                            try {
                                mMapView?.controller?.animateTo(GeoPoint(LocationService.imHere!!.latitude, LocationService.imHere!!.longitude))
                            }catch (e:java.lang.Exception){
                                e.printStackTrace()
                            }
                            mMapView?.overlays?.add(locationOverlay())
                            mMapView?.overlays?.add(initRotationGestureOverlay())
                            mMapView?.overlays?.add(initScaleBarOverlay())
                        }
                    }
                }
                .setNegativeButton("No") {
                        dialog,_->
                    dialog.cancel()
                }
            val alert = builder.create()
            alert.show()
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            buildAlertMessageNoGps()
        }
        else
        {

            try {
                mMapView?.controller?.animateTo(
                    GeoPoint(
                        LocationService.imHere!!.latitude,
                        LocationService.imHere!!.longitude
                    )
                )
            }catch (e:Exception){
                e.printStackTrace()
            }
            try {
                mMapView?.overlays?.add(locationOverlay())
            }catch (e:Exception){
                e.printStackTrace()
            }
            try {
                mMapView?.overlays?.add(initRotationGestureOverlay())
            }catch (e:Exception){
                e.printStackTrace()
            }
            try {
                mMapView?.overlays?.add(initScaleBarOverlay())
            }catch (e:Exception){
                e.printStackTrace()
            }

        }

    }

    private fun locationOverlay(): MyLocationNewOverlay {

        val gpsMyLocationProvider = GpsMyLocationProvider(this)
        try {
            gpsMyLocationProvider.addLocationSource(LocationService.imHere!!.provider)
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

        locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider, mMapView)
        locationOverlay.setDirectionArrow(customIcon(drawable.ic_navigator_icon), customIcon(drawable.ic_navigator_active_icon))
        locationOverlay.isDrawAccuracyEnabled = false
        return locationOverlay
    }

    private fun initRotationGestureOverlay(): RotationGestureOverlay {
        rotationGestureOverlay = RotationGestureOverlay(mMapView)
        rotationGestureOverlay.isEnabled = true
        mMapView?.setMultiTouchControls(true)
        return rotationGestureOverlay
    }

    private fun initScaleBarOverlay(): ScaleBarOverlay {
        scaleBarOverlay = ScaleBarOverlay(mMapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        return scaleBarOverlay
    }

    private fun customIcon(drawable: Int): Bitmap? {

        val drawableBitmap = ContextCompat.getDrawable(this, drawable)

        val bitmap = Bitmap.createBitmap(
            drawableBitmap!!.intrinsicWidth,
            drawableBitmap.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawableBitmap.setBounds(0, 0, canvas.width, canvas.height)
        drawableBitmap.draw(canvas)

        return bitmap
    }

    private fun fillFabMenu() {

        val followFab: FloatingActionButton = findViewById(common_followMe)
        val locationFab: FloatingActionButton = findViewById(common_myLocation)

        when (resources.configuration.orientation) {
            ORIENTATION_LANDSCAPE -> {
                when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                    Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                        followFab.size = FloatingActionButton.SIZE_NORMAL
                        locationFab.size = FloatingActionButton.SIZE_NORMAL
                    }
                    Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                        followFab.size = FloatingActionButton.SIZE_NORMAL
                        locationFab.size = FloatingActionButton.SIZE_NORMAL
                    }
                    else -> {
                        followFab.size = FloatingActionButton.SIZE_MINI
                        locationFab.size = FloatingActionButton.SIZE_MINI
                    }
                }
            }
            ORIENTATION_PORTRAIT -> {
                when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                    Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                        followFab.size = FloatingActionButton.SIZE_NORMAL
                        locationFab.size = FloatingActionButton.SIZE_NORMAL
                    }
                    Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                        followFab.size = FloatingActionButton.SIZE_NORMAL
                        locationFab.size = FloatingActionButton.SIZE_NORMAL
                    }
                    else -> {
                        followFab.size = FloatingActionButton.SIZE_MINI
                        locationFab.size = FloatingActionButton.SIZE_MINI
                    }
                }
            }
        }

        val fabMenu: FloatingActionMenu = findViewById(common_fab_menu)

        if(fabMenu.childCount>0)
        fabMenu.removeAllMenuButtons()

        for (i in 0 until  DataStore.statusList.count()) {
            if( DataStore.statusList[i].name != "На тревоге" && DataStore.statusList[i].name != DataStore.status){
                val actionButton = com.github.clans.fab.FloatingActionButton(this)
                actionButton.labelText =  DataStore.statusList[i].name
                actionButton.colorNormal = ContextCompat.getColor(this, color.colorPrimary)
                actionButton.setOnClickListener {
                    if (fabMenu.isOpened) {
                        when {
                            !ProtocolNetworkService.connectInternet -> {
                                Toast.makeText(this, "Нет соединения с интернетом, приложение переходит в автономный режим", Toast.LENGTH_LONG).show()
                            }
                            !ProtocolNetworkService.connectServer -> {
                                Toast.makeText(this, "Нет соединения с сервером, приложение переходит в автономный режим", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                thread {
                                    val statusChangeMessage = JSONObject()
                                    statusChangeMessage.put("\$c$", "gbrkobra")
                                    statusChangeMessage.put("command", "status")
                                    statusChangeMessage.put("newstatus", actionButton.labelText)
                                    ProtocolNetworkService.protocol?.request(statusChangeMessage.toString()) {
                                            access: Boolean, data: ByteArray? ->
                                        if (access && data != null) {
                                            runOnUiThread {
                                                DataStore.status = JSONObject(String(data)).getString("status")
                                                val title: String = "${DataStore.call}   (  ${DataStore.status}   ) ver. ${BuildConfig.VERSION_NAME} "
                                                supportActionBar?.title = title
                                            }
                                        } else {
                                            runOnUiThread {
                                                Toast.makeText(this, "Смена статуса невозможно, сервер не отвечает", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        fabMenu.close(true)
                    }
            }
                when ( DataStore.statusList[i].name) {
                    "Заправляется" -> { actionButton.setImageResource(drawable.ic_refueling) }
                    "Обед" -> { actionButton.setImageResource(drawable.ic_dinner) }
                    "Ремонт" -> { actionButton.setImageResource(drawable.ic_repairs) }
                    "Свободен" -> { actionButton.setImageResource(drawable.ic_freedom) }
                    else -> { actionButton.setImageResource(drawable.ic_unknown_status) }
                }
                when (resources.configuration.orientation) {
                    ORIENTATION_LANDSCAPE -> {
                        when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                            Configuration.SCREENLAYOUT_SIZE_LARGE -> { actionButton.buttonSize = sizeNormal }
                            Configuration.SCREENLAYOUT_SIZE_XLARGE -> { actionButton.buttonSize = sizeNormal }
                            else -> { actionButton.buttonSize = sizeMini }
                        }
                    }
                    ORIENTATION_PORTRAIT -> {
                        when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                            Configuration.SCREENLAYOUT_SIZE_LARGE -> { actionButton.buttonSize = sizeNormal }
                            Configuration.SCREENLAYOUT_SIZE_XLARGE -> { actionButton.buttonSize = sizeNormal }
                            else -> { actionButton.buttonSize = sizeMini }
                        }
                    }
                }

                fabMenu.addMenuButton(actionButton)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if(isFinishing){
            if(!exit)
                ControlLifeCycleService.stopService(this)

            finish()
            exitProcess(0)
        }

    }

    override fun onBackPressed() {
        if (exit) {
            ControlLifeCycleService.stopService(this)
            finishAffinity()
        } else {
            Toast.makeText(this, "Вы точно хотите выйти? Для того чтобы закрыть приложение нажмите еще раз", Toast.LENGTH_LONG).show()
            exit = true
        }
    }
}