package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.*
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.AlarmObjectInfo
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.loginactivity.LoginActivity
import kobramob.rubeg38.ru.gbrnavigation.objectactivity.NavigatorFragment.Companion.mMapView
import kobramob.rubeg38.ru.gbrnavigation.workservice.DataStore
import kobramob.rubeg38.ru.gbrnavigation.workservice.MessageEvent
import kobramob.rubeg38.ru.gbrnavigation.workservice.MyLocation
import kobramob.rubeg38.ru.gbrnavigation.workservice.RubegNetworkService
import kotlin.concurrent.thread
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.Thread.sleep

class ObjectActivity : AppCompatActivity() {

    private val tabFragment: TabFragment = TabFragment()
    private val navigatorFragment: NavigatorFragment = NavigatorFragment()
    /*private val networkService = NetworkService()*/

    companion object {
        var isAlive = false
        const val BROADCAST_ACTION = "kobramob.ruber38.ru.gbrnavigation.objectactivity"

        var saveAlarm: AlarmObjectInfo? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object)

        NavigatorFragment.arriveToObject = false
        NavigatorFragment.alertCanceled = false
        NavigatorFragment.proximityAlive = false

        saveAlarm = intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val toolbar: Toolbar = this.findViewById(R.id.toolbar_main_map)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Карточка объекта"

        val bnv: BottomNavigationView = findViewById(R.id.objectMenu)
        bnv.menu.getItem(0).isChecked = true

        openFragment(tabFragment)
        supportActionBar!!.title = "Карточка объекта"

        bnv.setOnNavigationItemSelectedListener {
            item ->

                when (item.itemId) {
                    R.id.cardObject -> {
                        openFragment(tabFragment)
                        supportActionBar!!.title = "Карточка объекта"
                    }

                    R.id.navigator -> {
                        openFragment(navigatorFragment)
                        supportActionBar!!.title = "Навигатор"
                    }
                }
                true
            }
    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        openFragment(tabFragment)
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

        if (!RubegNetworkService.isServiceStarted) {
            val ip: ArrayList<String> = ArrayList()
            ip.add(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", "")!!)

            val service = Intent(this, RubegNetworkService::class.java)
            service.putExtra("command", "start")
            service.putStringArrayListExtra("ip", ip)
            service.putExtra("port", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))

            startService(service)
            thread {
                sleep(500)
                val authorizationMessage = JSONObject()
                authorizationMessage.put("\$c$", "reg")
                authorizationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                authorizationMessage.put(
                    "password",
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("imei", "")
                )
                authorizationMessage.put(
                    "token",
                    getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("fcmtoken", "")
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
                            service.putStringArrayListExtra("ip", ipList)
                            service.putExtra("port", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))
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

        if (CommonActivity.alertSound.isPlaying) {
            CommonActivity.alertSound.stop()
            CommonActivity.alertSound.reset()
        }

        if (!NavigatorFragment.proximityAlive)
            proximityCheck()

        isAlive = true
        Log.d("ObjectActivity", "onStart")
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }
    @SuppressLint("InflateParams")
    private fun proximityCheck() {
        thread {
            val alarmObjectInfo = intent.getSerializableExtra("objectInfo") as AlarmObjectInfo
            if(alarmObjectInfo.lat!=0.0 && alarmObjectInfo.lon!=0.0){
                val location = Location("point A")
                location.latitude = alarmObjectInfo.lat!!
                location.longitude = alarmObjectInfo.lon!!
                while (!NavigatorFragment.arriveToObject) {
                    sleep(1000)
                    NavigatorFragment.proximityAlive = true
                    if (NavigatorFragment.alertCanceled) {
                        NavigatorFragment.proximityAlive = false
                        NavigatorFragment.arriveToObject = true
                        PlanFragment.countInQueue = 0
                    }
                    Log.d("proximity", (MyLocation.imHere!!.distanceTo(location).toString()))
                    if (MyLocation.imHere!!.distanceTo(location)> 500) {
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
                    if (MyLocation.imHere!!.distanceTo(location) < 200 && !NavigatorFragment.arriveToObject) {
                        runOnUiThread {
                            if (NavigatorFragment.arrived != null) {
                                NavigatorFragment.arrived!!.visibility = View.GONE
                            }
                            NavigatorFragment.proximityAlive = false
                            NavigatorFragment.arriveToObject = true

                            when {
                                !RubegNetworkService.connectInternet -> {

                                    Toast.makeText(
                                        this,
                                        "Нет соединения с интернетом, невозможно отправить запрос на прибытие",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                !RubegNetworkService.connectServer -> {
                                    Toast.makeText(
                                        this,
                                        "Нет соединения с сервером, невозможно отправить запрос на прибытие",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                !isAlive ->{

                                    PlanFragment.bitmapList.clear()
                                    NavigatorFragment.arriveToObject = true
                                    NavigatorFragment.alertCanceled = true
                                    NavigatorFragment.proximityAlive = false
                                    NavigatorFragment.road!!.mRouteHigh.clear()
                                    PlanFragment.countInQueue = 0

                                    val intent = Intent(this,ObjectActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.putExtra("objectInfo", saveAlarm)
                                    startActivity(intent)
                                }
                                else -> {
                                    val arrivedDialog = AlertDialog.Builder(this)
                                    arrivedDialog.setCancelable(false)
                                    arrivedDialog.setTitle("Прибытие")
                                        .setMessage("Вы прибыли на место")
                                        .setPositiveButton("Подтвердить") {
                                                _, _ ->
                                            val message = JSONObject()
                                            message.put("\$c$", "gbrkobra")
                                            message.put("command", "alarmpr")
                                            message.put("number", alarmObjectInfo.number)
                                            RubegNetworkService.protocol.send(message = message.toString()) {
                                                    success: Boolean ->
                                                if (success) {
                                                    runOnUiThread {
                                                        if (NavigatorFragment.road!!.mRouteHigh.count()> 1) {
                                                            if (mMapView != null) {
                                                                NavigatorFragment.road!!.mRouteHigh.clear()
                                                                mMapView!!.overlays.removeAt(mMapView!!.overlays.count() - 1)
                                                                mMapView!!.invalidate()
                                                            }
                                                        }
                                                        Toast.makeText(this, "Прибытие подтверждено", Toast.LENGTH_LONG).show()
                                                        val alertDialog = AlertDialog.Builder(this)
                                                        val view = layoutInflater.inflate(R.layout.dialog_reports, null, false)
                                                        val report_spinner: Spinner = view.findViewById(R.id.reports_spinner)
                                                        val report_text: EditText = view.findViewById(R.id.report_EditText)
                                                        report_spinner.prompt = "Список рапортов"
                                                        report_spinner.adapter = ArrayAdapter(
                                                            this,
                                                            R.layout.report_spinner_item,
                                                            DataStore.reports
                                                        )
                                                        var selectedReport = ""
                                                        report_spinner.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
                                                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                                            }

                                                            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                                                                if(report_spinner.selectedItem !=null){
                                                                    selectedReport = DataStore.reports[p2]
                                                                    Log.d("selected report",selectedReport)
                                                                }
                                                            }
                                                        }

                                                        alertDialog.setView(view)
                                                        alertDialog.setTitle("Отправка рапорта")
                                                        alertDialog.setPositiveButton("Отправить"){ _: DialogInterface, _: Int ->
                                                            val reportsMessage = JSONObject()
                                                            reportsMessage.put("\$c$", "reports")
                                                            reportsMessage.put("report",selectedReport)
                                                            reportsMessage.put("comment","${report_text.text}")
                                                            reportsMessage.put("namegbr",DataStore.namegbr)
                                                            reportsMessage.put("name",alarmObjectInfo.name)
                                                            reportsMessage.put("number",alarmObjectInfo.number)
                                                            Log.d("Report","$reportsMessage")
                                                            RubegNetworkService.protocol.send("$reportsMessage"){
                                                                    success:Boolean->
                                                                if(success){
                                                                    runOnUiThread {
                                                                        Toast.makeText(this,"Рапорт доставлен",Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                                else
                                                                {
                                                                    runOnUiThread {
                                                                        Toast.makeText(this,"Рапорт не доставлен",Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        val dialog = alertDialog.create()
                                                        dialog.setCancelable(false)
                                                        dialog.show()

                                                    }
                                                }
                                                else
                                                {
                                                    runOnUiThread {
                                                        Toast.makeText(this, "Прибытие не подтверждено", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        }.show()
                                }
                            }
                        }
                    }
                }
                if (NavigatorFragment.arrived != null) {
                    runOnUiThread {
                        NavigatorFragment.arrived!!.visibility = View.GONE
                    }
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
        PlanFragment.bitmapList.clear()
        PlanFragment.countInQueue = 0
        NavigatorFragment.arriveToObject = true
        NavigatorFragment.alertCanceled = true
        NavigatorFragment.proximityAlive = false
        if (NavigatorFragment.road != null) {
            if (NavigatorFragment.road!!.mRouteHigh.count()> 1)
                NavigatorFragment.road!!.mRouteHigh.clear()
        }
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
                        PlanFragment.bitmapList.clear()
                        PlanFragment.countInQueue = 0
                        NavigatorFragment.arriveToObject = true
                        NavigatorFragment.alertCanceled = true
                        NavigatorFragment.proximityAlive = false
                        if (NavigatorFragment.road != null) {
                            if (NavigatorFragment.road!!.mRouteHigh.count()> 1)
                                NavigatorFragment.road!!.mRouteHigh.clear()
                        }
                        saveAlarm!!.clear()
                        PlanFragment.countInQueue = 0
                        val commonActivity = Intent(this, CommonActivity::class.java)
                        commonActivity.putExtra("status", event.message)
                        startActivity(commonActivity)
                    }
                }
            }
            "gbrstatus" -> {
                if (event.message != "На тревоге") {
                    Toast.makeText(this, "Тревога отменена (смена статуса)", Toast.LENGTH_SHORT).show()
                    EventBus.getDefault().removeAllStickyEvents()
                    EventBus.getDefault().unregister(this)
                    thread{
                        sleep(500)
                        runOnUiThread {
                            PlanFragment.bitmapList.clear()
                            PlanFragment.countInQueue = 0
                            NavigatorFragment.arriveToObject = true
                            NavigatorFragment.alertCanceled = true
                            NavigatorFragment.proximityAlive = false
                            if (NavigatorFragment.road != null) {
                                if (NavigatorFragment.road!!.mRouteHigh.count()> 1)
                                    NavigatorFragment.road!!.mRouteHigh.clear()
                            }
                            saveAlarm!!.clear()
                            PlanFragment.countInQueue = 0
                            val commonActivity = Intent(this, CommonActivity::class.java)
                            commonActivity.putExtra("status", event.message)
                            startActivity(commonActivity)
                        }
                    }

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

/*    var closeReceiver = false
    private fun receiver() {
        thread {
            while (Alive) {
                if (!closeReceiver) {
                    if (NetworkService.stringMessageBroker.count() > 0) {
                        receiver@
                        for (i in 0 until NetworkService.stringMessageBroker.count()) {
                            SharedPreferencesState.init(this@ObjectActivity)
                            Log.d("ObjectReceiver", NetworkService.stringMessageBroker[i])
                            val lenght = NetworkService.stringMessageBroker.count()
                            val jsonMessage = JSONObject(NetworkService.stringMessageBroker[i])

                            when (jsonMessage.getString("command")) {
                                "disconnect" -> {
                                    val sendMessage = JSONObject()
                                    sendMessage.put("\$c$", "reg")
                                    sendMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                                    sendMessage.put("password", getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", ""))

                                    networkService.send(sendMessage.toString(), null) {
                                        success: Boolean ->
                                            if (success) {
                                                Log.d("Connected", "true")
                                            } else {
                                                Log.d("Connected", "false")
                                            }
                                        }

                                    runOnUiThread {
                                        val dialog = AlertDialog.Builder(this@ObjectActivity)
                                        dialog.setTitle("Потеряно соединение с сервером")
                                            .setMessage("СОЕДИНЕНИЕ С СЕРВЕРОМ ПОТЕРЯНО ПРИЛОЖЕНИЕ ПЕРЕХОДИТ В АВТОНОМНЫЙ РЕЖИМ")
                                            .setPositiveButton("Принял") { dialog, _ ->
                                                dialog.cancel()
                                            }
                                        dialog.show()
                                    }
                                    NetworkService.stringMessageBroker.removeAt(i)
                                }
                                "reconnect" -> {
                                    runOnUiThread {
                                        Toast.makeText(this@ObjectActivity, "Соединение с сервером восстановлено", Toast.LENGTH_LONG).show()
                                    }

                                    NetworkService.stringMessageBroker.removeAt(i)
                                }
                                "gbrstatus" -> {
                                    if (jsonMessage.getString("status") != "На тревоге") {
                                        SharedPreferencesState.init(this@ObjectActivity)
                                        if (jsonMessage.getString("status") != null) {
                                            SharedPreferencesState.addPropertyString("status", jsonMessage.getString("status"))
                                        }

                                        runOnUiThread {
                                            Toast.makeText(this@ObjectActivity, "Тревога отменена(смена статуса)!", Toast.LENGTH_LONG).show()

                                            startActivity(Intent(this@ObjectActivity, StartActivity::class.java))
                                        }
                                    }
                                    NetworkService.stringMessageBroker.removeAt(i)
                                }
                                "alarmpok" -> {
                                    runOnUiThread {
                                        try {
                                            Toast.makeText(this@ObjectActivity, "Тревога подтвреждена", Toast.LENGTH_LONG).show()
                                        } catch (e: java.lang.Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    NetworkService.stringMessageBroker.removeAt(i)
                                }

                                "notalarm" -> {

                                    closeReceiver = true

                                    runOnUiThread {
                                        Toast.makeText(this@ObjectActivity, "Тревога отменена!", Toast.LENGTH_LONG).show()
                                        SharedPreferencesState.addPropertyString("status", "Свободен")
                                        startActivity(Intent(this@ObjectActivity, StartActivity::class.java))
                                    }
                                    NetworkService.stringMessageBroker.removeAt(i)
                                }
                            }
                            if (lenght> NetworkService.stringMessageBroker.count())
                                break
                        }
                    }
                }

                sleep(100)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Alive = false
        println("onStop")
    }*/
}
