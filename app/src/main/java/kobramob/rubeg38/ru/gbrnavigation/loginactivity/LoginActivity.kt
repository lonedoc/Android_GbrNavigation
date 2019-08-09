package kobramob.rubeg38.ru.gbrnavigation.loginactivity

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.redmadrobot.inputmask.MaskedTextChangedListener
import java.lang.Thread.sleep
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.resource.DataBase
import kobramob.rubeg38.ru.gbrnavigation.resource.SPGbrNavigation
import kobramob.rubeg38.ru.gbrnavigation.workservice.MessageEvent
import kobramob.rubeg38.ru.gbrnavigation.workservice.RubegNetworkService
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    companion object {
        var isAlive = false
    }
    private var registration: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val ipText: TextInputEditText = findViewById(R.id.login_ip)
        val portText: TextInputEditText = findViewById(R.id.login_port)

        val listener = MaskedTextChangedListener("[099]{.}[099]{.}[099]{.}[099]", ipText)
        ipText.addTextChangedListener(listener)
        ipText.onFocusChangeListener = listener

        if (getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("ip") &&
            getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("port")
        ) {
            ipText.setText(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("ip", ""))
            portText.setText(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010).toString())
        }

        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {

            when {
                ipText.text.toString() == "" -> {
                    Toast.makeText(this, "Поле ip не может быть пустым", Toast.LENGTH_LONG).show()
                }

                portText.text.toString() == "" -> {
                    Toast.makeText(this, "Поле port не может быть пустым", Toast.LENGTH_LONG).show()
                }

                ipText.text.toString().count() < 7 || ipText.text.toString().count() > 15 -> {
                    ipText.setText("")
                    Toast.makeText(this, "Не правильно введен ip адрес", Toast.LENGTH_LONG).show()
                }

                portText.text.toString().count()> 5 -> {
                    portText.setText("")
                    Toast.makeText(this, "Не правильно введен port сервера", Toast.LENGTH_LONG).show()
                }

                else -> {

                    registration = true

                    val service = Intent(this, RubegNetworkService::class.java)

                    val ip: ArrayList<String> = ArrayList()
                    ip.add(ipText.text.toString())

                    service.putExtra("command", "start")
                    service.putStringArrayListExtra("ip", ip)
                    service.putExtra("port", portText.text.toString().toInt())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(service)
                    } else {
                        startService(service)
                    }

                    val registrationMessage = JSONObject()
                    registrationMessage.put("\$c$", "reg")
                    registrationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                    registrationMessage.put("password", intent.getStringExtra("imei"))
                    thread {
                        sleep(1000)
                        RubegNetworkService.protocol.send(registrationMessage.toString()) {
                            access: Boolean ->
                                if (access) {
                                    runOnUiThread {

                                        SPGbrNavigation.init(this)
                                        SPGbrNavigation.addPropertyString("ip", ipText.text.toString())
                                        SPGbrNavigation.addPropertyInt("port", portText.text.toString().toInt())
                                        SPGbrNavigation.addPropertyString("imei", intent.getStringExtra("imei")!!)

                                        Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    runOnUiThread {
                                        service.putExtra("command", "stop")
                                        service.putStringArrayListExtra("ip", ip)
                                        service.putExtra("port", getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getInt("port", 9010))

                                        startService(service)
                                        Toast.makeText(this, "Приложение не смогло зарегистрироваться на сервере", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.command) {
            "regok" -> {
                Log.d("Registration", "\n " + " " + "\n routeServer ${event.routeServer} \n call ${event.call} \n status ${event.status} \n gbrStatus ${event.gbrStatus}")
                val dbHelper = DataBase(this)
                val db = dbHelper.writableDatabase

                val cursorRoute = db.query("RouteServerList", null, null, null, null, null, null)
                if (cursorRoute.count == 0) {
                    val cv = ContentValues()
                    for (i in 0 until event.routeServer.count()) {
                        cv.put("ip", event.routeServer[i])
                        Log.d("DataBase", "id = " + db.insert("RouteServerList", null, cv))
                    }
                }
                cursorRoute.close()

                val cursorStatus = db.query("StatusList", null, null, null, null, null, null)
                if (cursorStatus.count == 0) {
                    val cv = ContentValues()
                    for (i in 0 until event.gbrStatus.count()) {
                        cv.put("status", event.gbrStatus[i])
                        Log.d("DataBase", "id = " + db.insert("StatusList", null, cv))
                    }
                }
                cursorStatus.close()

                SPGbrNavigation.init(this)
                if(event.call!="")
                SPGbrNavigation.addPropertyString("call", event.call)
                else
                {
                    SPGbrNavigation.addPropertyString("call", "")
                    Toast.makeText(this,"Группа не была поставлена на дежурство в дежурном операторе",Toast.LENGTH_SHORT).show()
                }
                if(event.routeServer.count()>0)
                SPGbrNavigation.addPropertyString("routeserver", event.routeServer[0])
                else
                    SPGbrNavigation.addPropertyString("routeserver", "91.189.160.38:5000")

                val intent = Intent(this@LoginActivity, CommonActivity::class.java)
                intent.putExtra("status", event.status)
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        isAlive = true
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()

        isAlive = false
        EventBus.getDefault().unregister(this)
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
