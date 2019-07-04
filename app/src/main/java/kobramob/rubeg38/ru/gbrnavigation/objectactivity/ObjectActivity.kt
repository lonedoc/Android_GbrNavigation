package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.Toolbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.service.NetworkService
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import org.json.JSONObject

class ObjectActivity : AppCompatActivity() {

    private val tabFragment: TabFragment = TabFragment()
    private val navigatorFragment: NavigatorFragment = NavigatorFragment()
    private val networkService = NetworkService()

    companion object {
        var Alive = false
        const val BROADCAST_ACTION = "kobramob.ruber38.ru.gbrnavigation.objectactivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object)

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
                        NavigatorFragment.firstTime = true
                        openFragment(navigatorFragment)
                        supportActionBar!!.title = "Навигатор"
                    }
                }
                true
            }
    }

    private fun openFragment(fragment: Fragment) {
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

    override fun onStart() {
        super.onStart()
        Alive = true
        broadcastReceiver()
    }

    var connectionLostBoolean = false
    var showDialog = false
    private lateinit var br: BroadcastReceiver
    private fun broadcastReceiver() {
        println("broadcastReciever")
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = intent?.getStringExtra("status")
                val notAlarm = intent?.getBooleanExtra("notAlarm",false)
                val regok = intent?.getStringExtra("regok")
                val connectionLost = intent?.getBooleanExtra("connectionLost",false)

                if(connectionLost!!){
                    //Dialog
                    if(!showDialog){
                        showDialog = true
                        val dialog = AlertDialog.Builder(this@ObjectActivity)
                        dialog.setTitle("Потеряно соединение с сервером")
                            .setMessage("СОЕДИНЕНИЕ С СЕРВЕРОМ ПРИЛОЖЕНИЕ ПЕРЕХОДИТ В АВТОНОМНЫЙ РЕЖИМ")
                            .setPositiveButton("Принял") { dialog, _ ->
                                dialog.cancel()
                            }
                        dialog.show()
                    }

                    connectionLostBoolean = true
                    val message = JSONObject()
                    message.put("\$c$", "reg")
                    message.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                    message.put("password", getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei",""))
                    networkService.send(message.toString(),null){
                            success:Boolean->
                        if (success){
                            Log.d("Connected","true")

                        }
                    }

                }
                if(regok!=null){
                    val dialog = AlertDialog.Builder(this@ObjectActivity)
                    dialog.setTitle("Соединение с сервером восстановлено")
                        .setMessage("ПОТЕРЯНА СОЕДИНЕНИЕ С СЕРВЕРОМ ВОССТАНОВЛЕНО")
                        .setPositiveButton("Принял") { dialog, _ ->
                            dialog.cancel()
                        }
                    dialog.show()
                    showDialog = false
                    connectionLostBoolean = false
                }

                if(!connectionLostBoolean){
                    if (status != "На тревоге") {
                        SharedPreferencesState.init(this@ObjectActivity)
                        if (status != null) {
                            SharedPreferencesState.addPropertyString("status", status)
                        }
                        Toast.makeText(this@ObjectActivity, "Тревога отменена(смена статуса)!", Toast.LENGTH_LONG).show()
                        unregisterReceiver(br)

                        startActivity(Intent(this@ObjectActivity, StartActivity::class.java))
                    }

                    if (notAlarm!!) {
                        Toast.makeText(this@ObjectActivity, "Тревога отменена!", Toast.LENGTH_LONG).show()

                        try {
                            unregisterReceiver(br)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        startActivity(Intent(this@ObjectActivity, StartActivity::class.java))
                    }
                }

            }
        }
        val intentFilter = IntentFilter(BROADCAST_ACTION)
        registerReceiver(br, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        Alive = false
        println("onStop")
    }
}
