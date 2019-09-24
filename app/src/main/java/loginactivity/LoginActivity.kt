package loginactivity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.iid.FirebaseInstanceId
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kobramob.rubeg38.ru.gbrnavigation.R
import commonactivity.CommonActivity
import kobramob.rubeg38.ru.networkprotocol.RubegProtocol
import resource.SPGbrNavigation
import resource.ControlLifeCycleService
import resource.DataStore
import workservice.RegistrationEvent
import workservice.ProtocolNetworkService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    companion object {
        var isAlive = false
    }

    private var registration: Boolean = false
    private var exit = false
    lateinit var dialog:AlertDialog
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
                           /* ControlLifeCycleService.startService(this,ipText.text.toString(),portText.text.toString().toInt())*/
                            val service = Intent(this, ProtocolNetworkService::class.java)

                            service.putExtra("command", "start")
                            service.putStringArrayListExtra("ip", arrayListOf(ipText.text.toString()))
                            service.putExtra("port", portText.text.toString().toInt())

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(service)
                            } else {
                                startService(service)
                            }
                            val accessLogin = AlertDialog.Builder(this@LoginActivity)
                            val view = layoutInflater.inflate(R.layout.progress_bar, null, false)
                            accessLogin.setView(view)
                            dialog = accessLogin.create()
                            try{

                                dialog.setCancelable(false)
                                dialog.show()
                            }catch (e:java.lang.Exception){
                                e.printStackTrace()
                            }

                        }

                        println("Hello")

                        val fcmToken = initFCMToken()

                        val registrationMessage = JSONObject()
                        registrationMessage.put("\$c$", "reg")
                        registrationMessage.put("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
                        registrationMessage.put("password", intent.getStringExtra("imei"))
                        registrationMessage.put("token",fcmToken )
                        registrationMessage.put("keepalive","10")
                        Log.d("Registration",registrationMessage.toString())

                        sleep(1000)
                        ProtocolNetworkService.protocol?.send(registrationMessage.toString()) {
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
                                    if(dialog.isShowing){
                                        dialog.cancel()
                                    }
                                    Toast.makeText(this, "Приложение не смогло зарегистрироваться на сервере", Toast.LENGTH_LONG).show()
                                    ControlLifeCycleService.stopService(applicationContext)
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun initFCMToken(): String {

        var token = ""

        if(!getSharedPreferences("gbrStorage",Context.MODE_PRIVATE).contains("fcmtoken")){
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                token = it.token
            }
        }
        else{
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

                if(dialog.isShowing){
                    dialog.cancel()
                }

                Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@LoginActivity, CommonActivity::class.java)
                startActivity(intent)
                finish()
                
            }
            "accessdenied"->{

                if(dialog.isShowing){
                    dialog.cancel()
                }
                Toast.makeText(this,"Данного пользователя не существует в базе",Toast.LENGTH_SHORT).show()

                ControlLifeCycleService.stopService(applicationContext)

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
            ControlLifeCycleService.stopService(applicationContext)
            DataStore.clearAllData()
            android.os.Process.killProcess(android.os.Process.myPid())
        } else {
            Toast.makeText(this, "Вы точно хотите выйти? Для того чтобы закрыть приложение нажмите еще раз", Toast.LENGTH_LONG).show()
            exit = true
        }
    }
}
