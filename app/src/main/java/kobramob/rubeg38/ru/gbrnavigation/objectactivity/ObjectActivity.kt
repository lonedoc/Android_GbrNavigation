package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.workservice.MessageEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ObjectActivity : AppCompatActivity() {

    private val tabFragment: TabFragment = TabFragment()
    private val navigatorFragment: NavigatorFragment = NavigatorFragment()
    /*private val networkService = NetworkService()*/

    companion object {
        var isAlive = false
        const val BROADCAST_ACTION = "kobramob.ruber38.ru.gbrnavigation.objectactivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object)

        if(!EventBus.getDefault().isRegistered(this))
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
        Log.d("ObjectActivity","onResume")
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }
    override fun onStart() {
        super.onStart()
        if(CommonActivity.alertSound.isPlaying){
            CommonActivity.alertSound.stop()
            CommonActivity.alertSound.reset()
        }

        isAlive = true
        Log.d("ObjectActivity","onStart")
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        Log.d("ObjectActivity","onStop")
        isAlive = false
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN,priority = 1,sticky = true)
    fun onMessageEvent(event:MessageEvent){
        when(event.command){
            "notalarm"->{
                PlanFragment.bitmapList.clear()
                NavigatorFragment.arriveToObject = true
                NavigatorFragment.alertCanceled = true
                NavigatorFragment.proximityAlive = false
                NavigatorFragment.road!!.mRouteHigh.clear()
                Toast.makeText(this,"Тревога завершена/отменена",Toast.LENGTH_SHORT).show()
                val commonActivity = Intent(this,CommonActivity::class.java)
                startActivity(commonActivity)
            }
            "gbrstatus" -> {
                if(event.message!="На тревоге")
                {
                    PlanFragment.bitmapList.clear()
                    PlanFragment.countInQueue = 0
                    NavigatorFragment.arriveToObject = true
                    NavigatorFragment.alertCanceled = true
                    NavigatorFragment.proximityAlive = false
                    if(NavigatorFragment.road!=null)
                    {
                        if(NavigatorFragment.road!!.mRouteHigh.count()>1)
                            NavigatorFragment.road!!.mRouteHigh.clear()
                    }

                    Toast.makeText(this, "Тревога отменена (смена статуса)", Toast.LENGTH_SHORT).show()
                    val commonActivity = Intent(this, CommonActivity::class.java)
                    commonActivity.putExtra("status", event.message)
                    startActivity(commonActivity)
                }
            }
            "disconnect" -> {
                if(event.message == "lost"){
                    //Dialog
                    Toast.makeText(this,"Нет соединения с сервером, приложение переходит в автономный режим",Toast.LENGTH_LONG).show()
                }
            }
            "internet" -> {
                if(event.message == "lost"){
                    //Dialog
                    Toast.makeText(this,"Нет соединения с интернетом, приложение переходит в автономный режим",Toast.LENGTH_LONG).show()
                }
            }
            "reconnectInternet"->{
                if(event.message == "true"){
                    Toast.makeText(this,"Интернет соединение восстановлено",Toast.LENGTH_LONG).show()
                }
                else
                {
                    Toast.makeText(this,"Интернет соединение не восстановлено",Toast.LENGTH_LONG).show()
                }
            }
            "reconnectServer"->{
                if(event.message == "true"){
                    Toast.makeText(this,"Соединение с сервером восстановлено",Toast.LENGTH_LONG).show()
                }
                else
                {
                    Toast.makeText(this,"Соединение с сервером не восстановлено",Toast.LENGTH_LONG).show()
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
