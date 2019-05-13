package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.Toolbar
import android.support.v4.app.Fragment
import android.widget.Toast
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity

class ObjectActivity : AppCompatActivity() {

    private val tabFragment: TabFragment = TabFragment()
    private val navigatorFragment: NavigatorFragment = NavigatorFragment()

    companion object {
        const val BROADCAST_ACTION = "kobramob.ruber38.ru.gbrnavigation.objectactivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object)

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
        broadcastReceiver()
    }

    private lateinit var br: BroadcastReceiver
    private fun broadcastReceiver() {
        println("broadcastReciever")
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val info = intent?.getStringExtra("info")
                val status = intent?.getStringExtra("status")
                println(info)
                val accessDenied = intent?.getBooleanExtra("accessDenied",false)
                if(accessDenied!!)
                {
                    //Dialog Window
                    Toast.makeText(this@ObjectActivity,"Связь с Сервером потеряна!",Toast.LENGTH_LONG).show()
                }
                if(status!="На тревоге")
                {
                    SharedPreferencesState.init(this@ObjectActivity)
                    if (status != null) {
                        SharedPreferencesState.addPropertyString("status", status)
                    }
                    Toast.makeText(this@ObjectActivity, "Тревога отменена!", Toast.LENGTH_LONG).show()
                    unregisterReceiver(br)
                    startActivity(Intent(this@ObjectActivity, StartActivity::class.java))
                }
                if (info == "alarmprok") {
                    Toast.makeText(this@ObjectActivity, "Прибытие отмечено!!", Toast.LENGTH_LONG).show()
                } else {
                    if (info == "notalarm") {
                        unregisterReceiver(br)
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
        println("onStop")
    }
}
