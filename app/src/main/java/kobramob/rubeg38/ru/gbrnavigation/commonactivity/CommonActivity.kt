package kobramob.rubeg38.ru.gbrnavigation.commonactivity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
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
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.ObjectActivity
import kobramob.rubeg38.ru.gbrnavigation.resource.DataBase
import kobramob.rubeg38.ru.gbrnavigation.resource.SPGbrNavigation
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.workservice.AlarmEvent
import kobramob.rubeg38.ru.gbrnavigation.workservice.MessageEvent
import kobramob.rubeg38.ru.gbrnavigation.workservice.MyLocation
import kobramob.rubeg38.ru.gbrnavigation.workservice.RubegNetworkService
import kotlin.concurrent.thread
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
import kotlin.system.exitProcess

class CommonActivity : AppCompatActivity() {

    private val sizeNormal = 0
    private val sizeMini = 1
    private lateinit var mMapView: MapView

    private lateinit var rotationGestureOverlay: RotationGestureOverlay
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var scaleBarOverlay: ScaleBarOverlay

    private var alarmObjectInfo:AlarmObjectInfo = AlarmObjectInfo()

    companion object {

        var isAlive = false
        lateinit var alertSound: MediaPlayer
        private var planList: ArrayList<String> = ArrayList()
        private var otvl: ArrayList<String> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)

        alertSound = MediaPlayer.create(this@CommonActivity, raw.trevoga)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        /*if (alertSound.isPlaying) {
            alertSound.stop()
            alertSound.reset()
        }*/

        if (alertSound.isPlaying) {
            alertSound.stop()
            alertSound.reset()
        }

        alarmObjectInfo.print()

        if (alarmObjectInfo.name != "") {

            val alertDialog = AlertDialog.Builder(this@CommonActivity)
            val view = layoutInflater.inflate(R.layout.dialog_alarm, null, false)
            alertDialog.setView(view)

            val dialog = alertDialog.create()
            dialog.setCancelable(false)
            dialog.show()

            val acceptAlertButton: Button = view!!.findViewById(AcceptAlert)
            val dialogObjectName: TextView = view.findViewById(dialog_objectName)
            val dialogObjectAddress: TextView = view.findViewById(dialog_objectAddress)



            dialogObjectName.text = alarmObjectInfo.name
            dialogObjectAddress.text = alarmObjectInfo.address
            alertSound.start()

            acceptAlertButton.setOnClickListener {

                if (alertSound.isPlaying) {
                    alertSound.stop()
                    alertSound.reset()
                }
                if (alertSound.isPlaying) {
                    alertSound.stop()
                    alertSound.reset()
                }
                if (alertSound.isPlaying) {
                    alertSound.stop()
                    alertSound.reset()
                }
                if (alertSound.isPlaying) {
                    alertSound.stop()
                    alertSound.reset()
                }

                when {
                    !RubegNetworkService.connectInternet -> {
                        Toast.makeText(this, "Нет соединения с интернетом, невозможно отправить подтверждение тревоги", Toast.LENGTH_LONG).show()
                        val objectActivity = Intent(this, ObjectActivity::class.java)
                        objectActivity.putExtra("objectInfo", alarmObjectInfo)
                        startActivity(objectActivity)
                        alarmObjectInfo.clear()
                    }
                    !RubegNetworkService.connectServer -> {
                        Toast.makeText(this, "Нет соединения с сервером, невозможно отправить подтверждение тревоги", Toast.LENGTH_LONG).show()
                        val objectActivity = Intent(this, ObjectActivity::class.java)
                        objectActivity.putExtra("objectInfo", alarmObjectInfo)
                        startActivity(objectActivity)
                        alarmObjectInfo.clear()
                    }
                    else -> {
                        thread {
                            val message = JSONObject()
                            message.put("\$c$", "gbrkobra")
                            message.put("command", "alarmp")
                            message.put("number", alarmObjectInfo.number)

                            RubegNetworkService.protocol.request(message.toString()) {
                                success: Boolean, data: ByteArray? ->
                                    if (success && data != null) {
                                        runOnUiThread {
                                            Toast.makeText(this, "Тревога подтверждена", Toast.LENGTH_SHORT).show()
                                        }

                                        Log.d("AlarmGson", "accept")
                                    }
                                }
                            val objectActivity = Intent(this, ObjectActivity::class.java)
                            objectActivity.putExtra("objectInfo", alarmObjectInfo)
                            startActivity(objectActivity)
                            alarmObjectInfo.clear()
                        }
                    }
                }

                dialog.cancel()
            }
        }

        mMapView = findViewById(common_mapView)

        val toolbar: Toolbar = findViewById(common_toolbar)
        setSupportActionBar(toolbar)
        val title: String = getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("call", "Позывной") + " ( " + intent.getStringExtra("status") + " ) "
        supportActionBar?.title = title

        fillFabMenu()

        initMapView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_map, menu)
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

                        val ip: ArrayList<String> = ArrayList()
                        ip.add(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!)

                        val service = Intent(this, RubegNetworkService::class.java)
                        service.putExtra("command", "stop")
                        service.putStringArrayListExtra("ip", ip)
                        service.putExtra("port", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))
                        startService(service)

                        val loginActivity = Intent(this, LoginActivity::class.java)
                        loginActivity.putExtra("imei", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                        startActivity(loginActivity)
                    }
                    .setNegativeButton("Нет") { dialog, _ ->
                        dialog.cancel()
                    }.show()
            }
            change_map -> {
                Toast.makeText(this, "Функция на данный момент не доступна", Toast.LENGTH_SHORT).show()
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
                    mMapView.controller.animateTo(GeoPoint(MyLocation.imHere))

                    SharedPreferencesState.init(this)
                    SharedPreferencesState.addPropertyFloat("lat", locationOverlay.myLocation.latitude.toFloat())
                    SharedPreferencesState.addPropertyFloat("lon", locationOverlay.myLocation.longitude.toFloat())
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

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN, priority = 0)
    fun onStickyAlertEvent(event: AlarmEvent) {
        when {
            event.command.isNotEmpty() -> {

                val planAndPhoto:ArrayList<String> = ArrayList()
                planAndPhoto.addAll(event.plan)
                planAndPhoto.addAll(event.photo)

                alarmObjectInfo = AlarmObjectInfo(event.name,event.number,event.lon,event.lat,event.inn,event.zakaz,event.address,event.area.name,event.area.alarmtime,event.plan,event.otvl)

                alertSound.start()

                val alertDialog = AlertDialog.Builder(this@CommonActivity)
                val view = layoutInflater.inflate(R.layout.dialog_alarm, null, false)
                alertDialog.setView(view)

                val dialog = alertDialog.create()
                dialog.setCancelable(false)
                dialog.show()

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
                        !RubegNetworkService.connectInternet -> {
                            Toast.makeText(this, "Нет соединения с интернетом, невозможно отправить подтверждение тревоги", Toast.LENGTH_LONG).show()
                            val objectActivity = Intent(this, ObjectActivity::class.java)
                            objectActivity.putExtra("objectInfo", alarmObjectInfo)
                            startActivity(objectActivity)
                            alarmObjectInfo.clear()
                        }
                        !RubegNetworkService.connectServer -> {
                            Toast.makeText(this, "Нет соединения с сервером, невозможно отправить подтверждение тревоги", Toast.LENGTH_LONG).show()
                            val objectActivity = Intent(this, ObjectActivity::class.java)
                            objectActivity.putExtra("objectInfo", alarmObjectInfo)
                            startActivity(objectActivity)
                            alarmObjectInfo.clear()
                        }
                        else -> {
                            thread {
                                val message = JSONObject()
                                message.put("\$c$", "gbrkobra")
                                message.put("command", "alarmp")
                                message.put("number", alarmObjectInfo.number)

                                RubegNetworkService.protocol.request(message.toString()) {
                                    success: Boolean, data: ByteArray? ->
                                        if (success && data != null) {
                                            runOnUiThread {

                                                Toast.makeText(this, "Тревога подтверждена", Toast.LENGTH_SHORT).show()
                                            }
                                            Log.d("AlarmGson", "accept")
                                        }
                                    }
                                val objectActivity = Intent(this, ObjectActivity::class.java)
                                objectActivity.putExtra("objectInfo", alarmObjectInfo)
                                startActivity(objectActivity)
                                alarmObjectInfo.clear()
                            }
                        }
                    }
                    dialog.cancel()
                }
            }

        }
    }

    @Subscribe(sticky =true,threadMode = ThreadMode.MAIN,priority = 0)
    fun onStickyMessageEvent(event:MessageEvent){
        when{
            event.command.isNotEmpty() -> {
                when (event.command) {
                    "gbrstatus" -> {
                        runOnUiThread {
                            SPGbrNavigation.init(this)
                            SPGbrNavigation.addPropertyString("status", event.message)
                            val title: String = getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("call", "") + " ( " + event.message + " ) "
                            supportActionBar?.title = title
                        }
                    }
                    "disconnect" -> {
                        if (event.message == "lost") {
                            // Dialog
                            Toast.makeText(this, "Нет соединения с сервером, приложение переходит в автономный режим", Toast.LENGTH_LONG).show()
                        }
                    }
                    "internet" -> {
                        if (event.message == "lost") {
                            // Dialog
                            Toast.makeText(this, "Нет соединения с интернетом, приложение переходит в автономный режим", Toast.LENGTH_LONG).show()
                        }
                    }
                    "reconnectInternet" -> {
                        if (event.message == "true") {
                            Toast.makeText(this, "Интернет соединение восстановлено", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Интернет соединение не восстановлено", Toast.LENGTH_LONG).show()
                        }
                    }
                    "reconnectServer" -> {
                        if (event.message == "true") {
                            Toast.makeText(this, "Соединение с сервером восстановлено", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Соединение с сервером не восстановлено", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        isAlive = true

        if(!RubegNetworkService.isServiceStarted){
            val ip: ArrayList<String> = ArrayList()
            ip.add(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!)

            val service = Intent(this, RubegNetworkService::class.java)
            service.putExtra("command", "start")
            service.putStringArrayListExtra("ip", ip)
            service.putExtra("port", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))

            startService(service)
            thread {
                Thread.sleep(500)
                val authorizationMessage = JSONObject()
                authorizationMessage.put("\$c$", "reg")
                authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                authorizationMessage.put(
                    "password",
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
                )
                RubegNetworkService.protocol.send(authorizationMessage.toString()) { success: Boolean ->
                    if (success) {
                        runOnUiThread {
                            Toast.makeText(this, "Восстановление связи прошло успешно", Context.MODE_PRIVATE).show()
                        }
                    } else {
                        runOnUiThread {
                            val ipList: ArrayList<String> = ArrayList()
                            ipList.add(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!)
                            service.putExtra("command", "stop")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(service)
                            } else {
                                startService(service)
                            }

                            Toast.makeText(this, "Приложение не смогло восстановить соединение", Toast.LENGTH_LONG).show()
                            val loginActivity = Intent(this, LoginActivity::class.java)
                            loginActivity.putExtra("imei", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", ""))
                            startActivity(loginActivity)
                        }
                    }
                }
            }
        }

        EventBus.getDefault().register(this)
        locationOverlay.enableMyLocation()
        scaleBarOverlay.enableScaleBar()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        if (!locationOverlay.isMyLocationEnabled && !scaleBarOverlay.isEnabled) {
            locationOverlay.enableMyLocation()
            scaleBarOverlay.enableScaleBar()
        }
    }

    override fun onPause() {
        super.onPause()

        mMapView.onPause()
        locationOverlay.disableMyLocation()
        scaleBarOverlay.disableScaleBar()

        if (locationOverlay.isFollowLocationEnabled)
            locationOverlay.disableFollowLocation()
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
        EventBus.getDefault().unregister(this)
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
        val gpsMyLocationProvider = GpsMyLocationProvider(this)
        gpsMyLocationProvider.addLocationSource(MyLocation.imHere!!.provider)
        locationOverlay = MyLocationNewOverlay(gpsMyLocationProvider, mMapView)
        locationOverlay.setDirectionArrow(customIcon(drawable.ic_navigator_icon), customIcon(drawable.ic_navigator_active_icon))
        locationOverlay.isDrawAccuracyEnabled = false
        return locationOverlay
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

        val statusList: ArrayList<String> = ArrayList()

        val dbHelper = DataBase(this)
        val db = dbHelper.writableDatabase

        val cursorStatus = db.query("StatusList", null, null, null, null, null, null)
        if (cursorStatus.moveToFirst()) {

            val status = cursorStatus.getColumnIndex("status")
            do {
                if (cursorStatus.getString(status) != "На тревоге") {
                    statusList.add(cursorStatus.getString(status))
                }
            } while (cursorStatus.moveToNext())
        }
        cursorStatus.close()

        statusList.sort()

        val fabMenu: FloatingActionMenu = findViewById(common_fab_menu)
        for (i in 0 until statusList.count()) {
            val actionButton = com.github.clans.fab.FloatingActionButton(this)
            actionButton.labelText = statusList[i]
            actionButton.colorNormal = ContextCompat.getColor(this, color.colorPrimary)
            actionButton.setOnClickListener {
                if (fabMenu.isOpened) {
                    when {
                        !RubegNetworkService.connectInternet -> {
                            Toast.makeText(this, "Нет соединения с интернетом, приложение переходит в автономный режим", Toast.LENGTH_LONG).show()
                        }
                        !RubegNetworkService.connectServer -> {
                            Toast.makeText(this, "Нет соединения с сервером, приложение переходит в автономный режим", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            thread {
                                val statusChangeMessage = JSONObject()
                                statusChangeMessage.put("\$c$", "gbrkobra")
                                statusChangeMessage.put("command", "status")
                                statusChangeMessage.put("newstatus", actionButton.labelText)

                                RubegNetworkService.protocol.request(statusChangeMessage.toString()) {
                                    access: Boolean, data: ByteArray? ->
                                        if (access && data != null) {
                                            runOnUiThread {
                                                SPGbrNavigation.init(this)
                                                SPGbrNavigation.addPropertyString("status", JSONObject(String(data)).getString("status"))
                                                val title: String = getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("call", "") + " ( " + JSONObject(String(data)).getString("status") + " ) "
                                                supportActionBar?.title = title
                                            }
                                        } else {
                                            runOnUiThread {
                                                Toast.makeText(this, "Сообщение не дошло", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                            }
                        }
                    }
                    fabMenu.close(true)
                }
            }
            when (statusList[i]) {
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

    private var exit = false
    override fun onBackPressed() {
        if (exit) {
            exitProcess(0)
        } else {
            Toast.makeText(this, "Вы точно хотите выйти? Для того чтобы закрыть приложение нажмите еще раз", Toast.LENGTH_LONG).show()
            exit = true
        }
    }
}

/*val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val heightPixels = metrics.heightPixels
        val widthPixels = metrics.widthPixels
        val densityDpi = metrics.densityDpi
        val xdpi = metrics.xdpi
        val ydpi = metrics.ydpi
        Log.i("CommonActivity", "widthPixels  = $widthPixels")
        Log.i("CommonActivity", "heightPixels = $heightPixels")
        Log.i("CommonActivity", "densityDpi   = $densityDpi")
        Log.i("CommonActivity", "xdpi         = $xdpi")
        Log.i("CommonActivity", "ydpi         = $ydpi")*/