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
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.CommonActivity
import kobramob.rubeg38.ru.gbrnavigation.service.RubegNetworkService
import org.json.JSONObject
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val ipText: TextInputEditText = findViewById(R.id.login_ip)
        val portText:TextInputEditText = findViewById(R.id.login_port)

        val listener = MaskedTextChangedListener("[099]{.}[099]{.}[099]{.}[099]", ipText)
        ipText.addTextChangedListener(listener)
        ipText.onFocusChangeListener = listener

        if(getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("ip")
            && getSharedPreferences("gbrStorage", Context.MODE_PRIVATE).contains("port"))
        {
            ipText.setText(getSharedPreferences("gbrStorage",Context.MODE_PRIVATE).getString("ip",""))
            portText.setText(getSharedPreferences("gbrStorage",Context.MODE_PRIVATE).getInt("port",9010).toString())
        }

        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {

            when{
                ipText.text.toString() == "" ->{
                    //TODO ip не может быть пустым
                    Toast.makeText(this,"Вы точно хотите выйти? Для того чтобы закрыть приложение нажмите еще раз",Toast.LENGTH_LONG).show()
                }
                portText.text.toString() == "" ->{
                    //TODO port не может быть пустым
                    Toast.makeText(this,"Вы точно хотите выйти? Для того чтобы закрыть приложение нажмите еще раз",Toast.LENGTH_LONG).show()
                }
                ipText.text.toString().count() < 7 || ipText.text.toString().count() >15 ->{
                    //TODO Не правильно введен ip
                }
                portText.text.toString().count()>5 ->{
                    //TODO не правильно введен port
                }
                else->{
                    //TODO Интент для запуска сервера
                    val service = Intent(this,RubegNetworkService::class.java)

                    val ip:ArrayList<String> = ArrayList()
                    ip.add(ipText.text.toString())
                    //TODO передаешь данные сервису для протокола
                    service.putStringArrayListExtra("ip",ip)
                    service.putExtra("port",portText.text.toString().toInt())

                    //TODO запуск сервиса
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        startForegroundService(service)
                    }
                    else
                    {
                        startService(service)
                    }

                    val registrationMessage = JSONObject()
                    registrationMessage.put("\$c$","reg")
                    registrationMessage.put("id","0D82F04B-5C16-405B-A75A-E820D62DF911")
                    registrationMessage.put("password",intent.getStringExtra("imei"))

                    thread {
                        sleep(1000)
                        RubegNetworkService.protocol.request(registrationMessage.toString()){
                                access:Boolean,response:ByteArray? ->
                            if(access && response != null){
                                runOnUiThread {
                                    Log.d("Registration",String(response))
                                    val intent = Intent(this@LoginActivity, CommonActivity::class.java)
                                    startActivity(intent)
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    private var exit = false
    override fun onBackPressed() {
        if(exit)
        {
            exitProcess(0)
        }
        else
        {
            Toast.makeText(this,"Вы точно хотите выйти? Для того чтобы закрыть приложение нажмите еще раз",Toast.LENGTH_LONG).show()
            exit = true
        }
    }
}
