package kobramob.rubeg38.ru.gbrnavigation.loginactivity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.iid.FirebaseInstanceId
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.resource.SPGbrNavigation
import kobramob.rubeg38.ru.gbrnavigation.workservice.ControlLifeCycleService
import kobramob.rubeg38.ru.gbrnavigation.workservice.RegistrationEvent
import kobramob.rubeg38.ru.gbrnavigation.workservice.RubegNetworkService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {

    companion object {
        var isAlive = false
    }

    private var registration: Boolean = false
    private var exit = false

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

                ipText.text.toString() == "" && portText.text.toString() == "" ->{
                    Toast.makeText(this, "Поля ip и port не могут быть пустым", Toast.LENGTH_LONG).show()
                }

                ipText.text.toString().count() < 7 || ipText.text.toString().count() > 15 -> {
                    ipText.setText("")
                    Toast.makeText(this, "Не правильно введен ip адрес", Toast.LENGTH_LONG).show()
                }

                portText.text.toString().count()> 5 -> {
                    portText.setText("")
                    Toast.makeText(this, "Не правильно введен port сервера", Toast.LENGTH_LONG).show()
                }

                else ->{

                    if(!EventBus.getDefault().isRegistered(this)){
                        EventBus.getDefault().register(this)
                    }

                    isAlive = true
                    registration = true

                    thread {

                        runOnUiThread {
                            //ControlLifeCycleService.startService(this,ipText.text.toString(),portText.text.toString().toInt())
                            val service = Intent(this, RubegNetworkService::class.java)

                            service.putExtra("command", "start")
                            service.putStringArrayListExtra("ip", arrayListOf(ipText.text.toString()))
                            service.putExtra("port", portText.text.toString().toInt())

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(service)
                            } else {
                                startService(service)
                            }

                        }


                        println("Hello")

                        val fcmToken = initFCMToken()

                        val registrationMessage = JSONObject()
                        registrationMessage.put("\$c$", "reg")
                        registrationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                        registrationMessage.put("password", intent.getStringExtra("imei"))
                        registrationMessage.put("token",fcmToken )

                        Log.d("Registration",registrationMessage.toString())

                        sleep(1000)
                        RubegNetworkService.protocol.send(registrationMessage.toString()) {
                                access: Boolean ->
                            if (access) {
                                runOnUiThread {

                                    SPGbrNavigation.init(this)
                                    SPGbrNavigation.addPropertyString("ip", ipText.text.toString())
                                    SPGbrNavigation.addPropertyInt("port", portText.text.toString().toInt())
                                    SPGbrNavigation.addPropertyString("imei", intent.getStringExtra("imei")!!)
                                    SPGbrNavigation.addPropertyString("fcmtoken", fcmToken)

                                }
                            } else {
                                runOnUiThread {

                                    Toast.makeText(this, "Приложение не смогло зарегистрироваться на сервере", Toast.LENGTH_LONG).show()

                                    ControlLifeCycleService.stopService(this)

                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun initFCMToken(): String {

        println("Hello 2")
        var token = ""

        if(!getSharedPreferences("gbrStorage",Context.MODE_PRIVATE).contains("fcmtoken")){
            println("Hello 3")
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                token = it.token
            }
        }
        else{
            println("Hello 4")
            token = getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).getString("fcmtoken","").toString()
        }

        while (token == "" ) {
            //init token
        }


        Log.d("FCMToken", token)

        return token
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: RegistrationEvent) {
        when (event.command) {
            "regok" -> {

                Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@LoginActivity, CommonActivity::class.java)
                startActivity(intent)

            }
            "accessdenied"->{

                Toast.makeText(this,"Данного пользователя не существует в базе",Toast.LENGTH_SHORT).show()

                ControlLifeCycleService.stopService(this)

            }
        }
    }

    override fun onStart() {
        super.onStart()

        isAlive = true

        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()

        isAlive = false

        if(EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().unregister(this)

    }

    override fun onBackPressed() {
        if (exit) {
            finish()
            exitProcess(0)
        } else {
            Toast.makeText(this, "Вы точно хотите выйти? Для того чтобы закрыть приложение нажмите еще раз", Toast.LENGTH_LONG).show()
            exit = true
        }
    }
}
