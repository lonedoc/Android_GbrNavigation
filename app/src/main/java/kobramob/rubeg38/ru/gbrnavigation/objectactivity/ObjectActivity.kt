package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Thread.sleep
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.resource.SharedPreferencesState
import kobramob.rubeg38.ru.gbrnavigation.service.NetworkService
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import kotlin.concurrent.thread
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

    override fun onStart() {
        super.onStart()
        Alive = true
        receiver()
    }

    var closeReceiver = false
    private fun receiver() {
        thread {
            while (Alive) {
                if (!closeReceiver) {
                    if (NetworkService.messageBroker.count() > 0) {
                        receiver@
                        for (i in 0 until NetworkService.messageBroker.count()) {
                            SharedPreferencesState.init(this@ObjectActivity)
                            Log.d("ObjectReceiver", NetworkService.messageBroker[i])
                            val lenght = NetworkService.messageBroker.count()
                            val jsonMessage = JSONObject(NetworkService.messageBroker[i])

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
                                    NetworkService.messageBroker.removeAt(i)
                                }
                                "reconnect" -> {
                                    runOnUiThread {
                                        Toast.makeText(this@ObjectActivity, "Соединение с сервером восстановлено", Toast.LENGTH_LONG).show()
                                    }

                                    NetworkService.messageBroker.removeAt(i)
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
                                    NetworkService.messageBroker.removeAt(i)
                                }
                                "alarmpok" -> {
                                    runOnUiThread {
                                        try {
                                            Toast.makeText(this@ObjectActivity, "Тревога подтвреждена", Toast.LENGTH_LONG).show()
                                        } catch (e: java.lang.Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    NetworkService.messageBroker.removeAt(i)
                                }

                                "notalarm" -> {

                                    closeReceiver = true

                                    runOnUiThread {
                                        Toast.makeText(this@ObjectActivity, "Тревога отменена!", Toast.LENGTH_LONG).show()
                                        SharedPreferencesState.addPropertyString("status", "Свободен")
                                        startActivity(Intent(this@ObjectActivity, StartActivity::class.java))
                                    }
                                    NetworkService.messageBroker.removeAt(i)
                                }
                            }
                            if (lenght> NetworkService.messageBroker.count())
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
    }
}
