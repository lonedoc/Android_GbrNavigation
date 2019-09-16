package objectactivity

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import kobramob.rubeg38.ru.gbrnavigation.R
import referenceactivity.ReferenceActivity
import commonactivity.AlarmObjectInfo
import commonactivity.CommonActivity
import objectactivity.data.ObjectDataStore
import objectactivity.navigatorfragment.NavigatorFragment
import objectactivity.navigatorfragment.NavigatorFragment.Companion.mMapView
import objectactivity.pager.ObjectTabFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import resource.ControlLifeCycleService
import resource.DataStore
import workservice.*
import java.lang.Exception
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class ObjectActivity : AppCompatActivity() {

    private val objectTabFragment: ObjectTabFragment =
        ObjectTabFragment()
    private val navigatorFragment: NavigatorFragment =
        NavigatorFragment()

    companion object {
        var isAlive = false

        var saveAlarm: AlarmObjectInfo? = null

        var alertCanceled = false
        var proximityAlive = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object)

        NavigatorFragment.arriveToObject = false
        alertCanceled = false
        proximityAlive = false

        if(savedInstanceState != null)
        {

            saveAlarm = intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().register(this)

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            val toolbar: Toolbar = this.findViewById(R.id.toolbar_main_map)
            setSupportActionBar(toolbar)
            supportActionBar!!.title = "Карточка объекта"

            val bnv: BottomNavigationView = findViewById(R.id.objectMenu)
            bnv.menu.getItem(0).isChecked = true

            openFragment(objectTabFragment)
            supportActionBar!!.title = "Карточка объекта"

            bnv.setOnNavigationItemSelectedListener {
                    item ->

                when (item.itemId) {
                    R.id.cardObject -> {
                        openFragment(objectTabFragment)
                        supportActionBar!!.title = "Карточка объекта"
                    }

                    R.id.navigator -> {
                        openFragment(navigatorFragment)
                        supportActionBar!!.title = "Навигатор"
                    }
                }
                true
            }

            try{
                val commonTimer:Chronometer = findViewById(R.id.common_timer)
                commonTimer.base = SystemClock.elapsedRealtime() - ObjectDataStore.timeAlarmApplyLong!!
                commonTimer.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDark))
                commonTimer.setTextColor(ContextCompat.getColor(this,R.color.textWhite))
                commonTimer.setOnChronometerTickListener {
                }
                commonTimer.start()
            }catch (e:Exception){
                e.printStackTrace()
            }

            try{
                val travelTimer:Chronometer = findViewById(R.id.travel_timer)
                travelTimer.base = SystemClock.elapsedRealtime() - ObjectDataStore.timeAlarmApplyLong!!
                travelTimer.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDark))
                travelTimer.setTextColor(ContextCompat.getColor(this,R.color.textWhite))
                travelTimer.setOnChronometerTickListener {

                    val elapsedMillis: Long = (SystemClock.elapsedRealtime()
                            - it.base)

                    ObjectDataStore.timeAlarmApplyLong = elapsedMillis

                    if(NavigatorFragment.arriveToObject){
                        val parentCommonTimer:LinearLayout = findViewById(R.id.parent_common_timer)
                        parentCommonTimer.visibility = View.VISIBLE
                        ObjectDataStore.saveToTimeToArrived((elapsedMillis / 1000).toInt())
                        it.setBackgroundColor(ContextCompat.getColor(this,R.color.arriveToObject))
                        it.stop()
                    }

                }
                travelTimer.start()
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
        else
        {
            saveAlarm = intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().register(this)

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            val toolbar: Toolbar = this.findViewById(R.id.toolbar_main_map)
            setSupportActionBar(toolbar)
            supportActionBar!!.title = "Карточка объекта"

            val bnv: BottomNavigationView = findViewById(R.id.objectMenu)
            bnv.menu.getItem(0).isChecked = true

            openFragment(objectTabFragment)
            supportActionBar!!.title = "Карточка объекта"

            bnv.setOnNavigationItemSelectedListener {
                    item ->

                when (item.itemId) {
                    R.id.cardObject -> {
                        openFragment(objectTabFragment)
                        supportActionBar!!.title = "Карточка объекта"
                    }

                    R.id.navigator -> {
                        openFragment(navigatorFragment)
                        supportActionBar!!.title = "Навигатор"
                    }
                }
                true
            }
            Log.d("Debug","SystemClock.elapsedRealtime() = ${SystemClock.elapsedRealtime()}")
            val commonTimer:Chronometer = findViewById(R.id.common_timer)
            commonTimer.base = SystemClock.elapsedRealtime()
            commonTimer.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDark))
            commonTimer.setTextColor(ContextCompat.getColor(this,R.color.textWhite))
            commonTimer.setOnChronometerTickListener {

            }
            commonTimer.start()

            val travelTimer:Chronometer = findViewById(R.id.travel_timer)
            travelTimer.base = SystemClock.elapsedRealtime()
            travelTimer.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDark))
            travelTimer.setTextColor(ContextCompat.getColor(this,R.color.textWhite))
            travelTimer.setOnChronometerTickListener {

                val elapsedMillis: Long = (SystemClock.elapsedRealtime() - it.base)
                ObjectDataStore.timeAlarmApplyLong = elapsedMillis

                if(NavigatorFragment.arriveToObject){
                    val parentCommonTimer:LinearLayout = findViewById(R.id.parent_common_timer)
                    parentCommonTimer.visibility = View.VISIBLE
                    ObjectDataStore.saveToTimeToArrived((elapsedMillis / 1000).toInt())
                    it.setBackgroundColor(ContextCompat.getColor(this,R.color.arriveToObject))
                    it.stop()
                }

            }
            travelTimer.start()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.object_menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reference ->{
                ///Вызов справочника
                val referenceActivity = Intent(this, ReferenceActivity::class.java)
                referenceActivity.putExtra("objectInfo",
                    saveAlarm
                )
                referenceActivity.putExtra("objectActivity",true)
                startActivity(referenceActivity)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        openFragment(objectTabFragment)
        val bnv: BottomNavigationView = findViewById(R.id.objectMenu)
        bnv.menu.getItem(0).isChecked = true
    }

    override fun onResume() {
        super.onResume()
        Log.d("ObjectActivity", "onResume")
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStart() {
        super.onStart()

        isAlive = true


        if (!ProtocolNetworkService.isServiceStarted) {
            ControlLifeCycleService.reconnectToServer(this)
        }

        val alarmObjectInfo = intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

        if (!proximityAlive && alarmObjectInfo.lat!=0.0 && alarmObjectInfo.lon!=0.0 && !ObjectDataStore.arrivedToObjectSend)
            proximityCheck(alarmObjectInfo)

        Log.d("ObjectActivity", "onStart")
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

    }

    @SuppressLint("InflateParams")
    private fun proximityCheck(alarmObjectInfo: AlarmObjectInfo) {
        thread {

                val location = Location("point A")
                location.latitude = alarmObjectInfo.lat!!
                location.longitude = alarmObjectInfo.lon!!

            val distance = try {
                if(DataStore.cityCard.pcsinfo.dist == "")
                    50
                else {
                    DataStore.cityCard.pcsinfo.dist.toInt()
                }
            }catch (e: Exception){
                e.printStackTrace()
                50
            }

                while (!NavigatorFragment.arriveToObject) {

                    sleep(1000)

                    proximityAlive = true

                    checkAlertState()

                    enableForcedArrival(location,distance)

                    if (LocationService.imHere!!.distanceTo(location) < distance && !NavigatorFragment.arriveToObject && !ObjectDataStore.arrivedToObjectSend) {

                        runOnUiThread {


                            proximityAlive = false

                            NavigatorFragment.arriveToObject = true

                            if (NavigatorFragment.arrived != null) {
                                NavigatorFragment.arrived!!.visibility = View.GONE
                            }

                            when {
                                !ProtocolNetworkService.connectInternet -> {

                                    Toast.makeText(
                                        this,
                                        "Нет соединения с интернетом, невозможно отправить запрос на прибытие",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                !ProtocolNetworkService.connectServer -> {
                                    Toast.makeText(
                                        this,
                                        "Нет соединения с сервером, невозможно отправить запрос на прибытие",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                !isAlive ->{

                                    NavigatorFragment.arriveToObject = true

                                    alertCanceled = true

                                    proximityAlive = false

                                    if (NavigatorFragment.road != null) {
                                        if (NavigatorFragment.road?.mRouteHigh!!.count()> 1)
                                            NavigatorFragment.road?.mRouteHigh!!.clear()
                                    }

                                    val intent = Intent(this, ObjectActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.putExtra("objectInfo",
                                        saveAlarm
                                    )
                                    startActivity(intent)

                                }
                                else -> {

                                    val arrivedDialog = AlertDialog.Builder(this)
                                    arrivedDialog.setTitle("Прибытие")
                                        .setCancelable(false)
                                        .setMessage("Вы прибыли на место")
                                        .setPositiveButton("Подтвердить") {
                                                _, _ ->
                                            ObjectDataStore.arrivedToObjectSend = true
                                            val message = JSONObject()
                                            message.put("\$c$", "gbrkobra")
                                            message.put("command", "alarmpr")
                                            message.put("number", alarmObjectInfo.number)
                                            ProtocolNetworkService.protocol?.send(message = message.toString()) {
                                                    success: Boolean ->
                                                if (success) {
                                                    runOnUiThread {
                                                        if (NavigatorFragment.road != null) {
                                                            if (NavigatorFragment.road?.mRouteHigh!!.count() > 1) {
                                                                if (mMapView != null) {
                                                                    NavigatorFragment.road?.mRouteHigh!!.clear()
                                                                    mMapView!!.overlays.removeAt(
                                                                        mMapView!!.overlays.count() - 1
                                                                    )
                                                                    mMapView!!.invalidate()
                                                                }
                                                            }
                                                        }

                                                        Toast.makeText(this, "Прибытие подтверждено", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                                else
                                                {
                                                    runOnUiThread {
                                                        Toast.makeText(this, "Прибытие не подтверждено", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        }
                                        .setNeutralButton("Отложить"){
                                            dialogInterface, i ->
                                            dialogInterface.cancel()
                                            ObjectDataStore.putOffArrivedToObjectSend = true
                                        }
                                        .show()
                                }
                            }
                        }
                        sleep(50000)
                    }

                }

                if (NavigatorFragment.arrived != null) {
                    runOnUiThread {
                        NavigatorFragment.arrived!!.visibility = View.GONE
                    }
                }
        }
    }

    private fun checkAlertState() {
        if (alertCanceled) {
            proximityAlive = false
            NavigatorFragment.arriveToObject = true
        }
    }

    private fun enableForcedArrival(location: Location, distance: Int) {
        if (LocationService.imHere!!.distanceTo(location)> distance) {
            if (NavigatorFragment.arrived != null) {
                runOnUiThread {
                    NavigatorFragment.arrived!!.visibility = View.GONE
                }
            }
        } else {
            if (NavigatorFragment.arrived != null && !NavigatorFragment.arriveToObject) {
                runOnUiThread {
                    NavigatorFragment.arrived!!.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        Log.d("ObjectActivity", "onStop")

        isAlive = false

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        NavigatorFragment.arriveToObject = true
        alertCanceled = true
        proximityAlive = false
        if (NavigatorFragment.road != null) {
            if (NavigatorFragment.road?.mRouteHigh!!.count()> 1)
                NavigatorFragment.road?.mRouteHigh!!.clear()
        }
       // AppWatcher.objectWatcher.watch(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1, sticky = true)
    fun onMessageEvent(event: MessageEvent) {
        when (event.command) {
            "notalarm" -> {

                EventBus.getDefault().unregister(this)

                Toast.makeText(this, "Тревога завершена/отменена", Toast.LENGTH_SHORT).show()

                EventBus.getDefault().removeAllStickyEvents()

                thread{
                    sleep(500)

                    runOnUiThread {

                        NavigatorFragment.arriveToObject = true
                        alertCanceled = true

                        if (NavigatorFragment.road != null) {
                            if (NavigatorFragment.road?.mRouteHigh!!.count()> 1)
                                NavigatorFragment.road?.mRouteHigh!!.clear()
                        }

                        saveAlarm!!.clear()

                        DataStore.status = event.message
                        ObjectDataStore.clearAllAlarmData()

                        val commonActivity = Intent(this, CommonActivity::class.java)
                        startActivity(commonActivity)
                        finish()
                    }
                }

            }
            "gbrstatus" -> {
                if (event.message != "На тревоге") {

                    Toast.makeText(this, "Тревога отменена (смена статуса)", Toast.LENGTH_SHORT).show()

                    EventBus.getDefault().removeAllStickyEvents()

                    if(EventBus.getDefault().isRegistered(this))
                    EventBus.getDefault().unregister(this)

                    thread{
                        sleep(500)
                        runOnUiThread {

                            NavigatorFragment.arriveToObject = true

                            if (NavigatorFragment.road != null) {
                                if (NavigatorFragment.road?.mRouteHigh!!.count()> 1)
                                    NavigatorFragment.road?.mRouteHigh!!.clear()
                            }

                            alertCanceled = true

                            saveAlarm!!.clear()

                            DataStore.status = event.message

                            ObjectDataStore.clearAllAlarmData()

                            val commonActivity = Intent(this, CommonActivity::class.java)
                            startActivity(commonActivity)
                            finish()
                        }
                    }

                }
            }
        }
    }

}
