package kobramob.rubeg38.ru.gbrnavigation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.telephony.TelephonyManager
import android.widget.Button
import kobramob.rubeg38.ru.gbrnavigation.service.Request
import android.os.Build
import kobramob.rubeg38.ru.gbrnavigation.service.PollingServer
import kobramob.rubeg38.ru.gbrnavigation.startactivity.StartActivity
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    val request: Request = Request()
    val pollingServer: PollingServer = PollingServer()

    lateinit var ipInput: TextInputEditText
    lateinit var portInput: TextInputEditText

    companion object {
        var firstStart = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (getSharedPreferences("state", Context.MODE_PRIVATE).contains("ip") && getSharedPreferences("state", Context.MODE_PRIVATE).contains("port") && getSharedPreferences("state", Context.MODE_PRIVATE).contains("imei")) {
            registerThread()
        }

        checkPermission()

        ipInput = findViewById(R.id.ipInputText)
        portInput = findViewById(R.id.portInputText)

        val buttonEnterToApp: Button = findViewById(R.id.enterToApp)

        val telephonyMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val imei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephonyMgr.imei
        } else {
            telephonyMgr.deviceId
        }

        setOnClickListenerEnterToApp(buttonEnterToApp, imei)
    }

    private fun setOnClickListenerEnterToApp(buttonEnterToApp: Button, imei: String) {

        buttonEnterToApp.setOnClickListener {
            if (ipInput.text.toString() == "" || portInput.text.toString() == "") {
                val dataNotEntered = AlertDialog.Builder(this)
                dataNotEntered.setTitle("Данные не введены")
                    .setMessage("Необходимо заполнить все поля")
                    .setCancelable(false)
                    .setPositiveButton("Ок") { _, _ ->
                        ipInput.setText("")
                        portInput.setText("")
                    }.show()
            } else {
                val registerThread = Runnable {
                    val requestServer = request.register(PollingServer.socket,PollingServer.countSender,0,imei, ipInput.text.toString(), portInput.text.toString().toInt(), "")
                    PollingServer.countSender++
                    runOnUiThread {
                        when (requestServer) {
                            "TimeOut" -> {
                                val dataNotEntered = AlertDialog.Builder(this)
                                dataNotEntered.setTitle("Ошибка")
                                    .setMessage("Сервер не отвечает или данные не верны")
                                    .setCancelable(false)
                                    .setPositiveButton("Ок") { _, _ ->
                                        ipInput.setText("")
                                        portInput.setText("")
                                    }.show()
                            }
                            "{\"\$z\$\":\"s\",\"d\":[{\"reg\":\"0\",\"com\":\"accessdenied\"}]}" -> {
                                val dataNotEntered = AlertDialog.Builder(this)
                                dataNotEntered.setTitle("Ошибка")
                                    .setMessage("Сервер не отвечает или данные не верны")
                                    .setCancelable(false)
                                    .setPositiveButton("Ок") { _, _ ->
                                        ipInput.setText("")
                                        portInput.setText("")
                                    }.show()
                            }
                            else -> {

                                println(requestServer)

                                val jsonObject = JSONObject(requestServer)
                                val jsonArray = jsonObject.getJSONArray("d")

                                SharedPreferencesState.init(this)

                                SharedPreferencesState.addPropertyString(
                                    "imei",
                                    imei
                                )

                                SharedPreferencesState.addPropertyString(
                                    "tid",
                                    JSONObject(jsonArray.getString(0)).getString("tid")
                                )
                                val jsonObject1 = JSONObject(jsonArray.getString(0))
                                val jsonArray1 = jsonObject1.getJSONArray("routeserver")
                                SharedPreferencesState.addPropertyString(
                                    "routeserver",
                                    jsonArray1.getString(0)
                                )
                                SharedPreferencesState.addPropertyString(
                                    "namegbr",
                                    JSONObject(jsonArray.getString(0)).getString("namegbr")
                                )

                                SharedPreferencesState.addPropertyString(
                                    "ip",
                                    ipInput.text.toString()
                                )

                                SharedPreferencesState.addPropertyInt(
                                    "port",
                                    portInput.text.toString().toInt()
                                )

                                startService(Intent(this, PollingServer::class.java))

                                firstStart = true
                                startActivity(Intent(this@LoginActivity, StartActivity::class.java))
                            }
                        }
                    }
                }; Thread(registerThread).start()
            }
        }
    }

    private fun registerThread() {

        val registerThread = Runnable {
            val requestServer = request.register(
                PollingServer.socket,
                PollingServer.countSender,
                0,
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("imei", ""),
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("ip", ""),
                getSharedPreferences("state", Context.MODE_PRIVATE).getInt("port", 0),
                getSharedPreferences("state", Context.MODE_PRIVATE).getString("tid", "")
            )
            PollingServer.countSender++
            println(requestServer)
            runOnUiThread {
                when (requestServer) {
                    "{\"\$z\$\":\"s\",\"d\":[{\"reg\":\"0\",\"com\":\"accessdenied\"}]}" -> {
                        val dataNotEntered = AlertDialog.Builder(this)
                        dataNotEntered.setTitle("Ошибка")
                            .setMessage("Данные не верны")
                            .setCancelable(false)
                            .setPositiveButton("Ок") { _, _ ->
                                registerThread()
                            }.show()
                    }
                    "TimeOut" -> {
                        val dataNotEntered = AlertDialog.Builder(this)
                        dataNotEntered.setTitle("Ошибка")
                            .setMessage("Сервер не отвечает или данные не верны")
                            .setCancelable(false)
                            .setPositiveButton("Ок") { _, _ ->
                                registerThread()
                            }.show()
                    }
                    else -> {

                        val jsonObject = JSONObject(requestServer)
                        val jsonArray = jsonObject.getJSONArray("d")

                        SharedPreferencesState.init(this)
                        SharedPreferencesState.addPropertyString(
                            "tid",
                            JSONObject(jsonArray.getString(0)).getString("tid")
                        )
                        if (JSONObject(jsonArray.getString(0)).getString("namegbr") != getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "")) {
                            SharedPreferencesState.addPropertyString(
                                "namegbr",
                                JSONObject(jsonArray.getString(0)).getString("namegbr")
                            )
                            val title = getSharedPreferences("state", Context.MODE_PRIVATE).getString("namegbr", "") + " (Свободен)"
                            supportActionBar!!.title = title
                        }
                        startService(Intent(this, PollingServer::class.java))
                        startActivity(Intent(this@LoginActivity, StartActivity::class.java))
                    }
                }
            }
        }; Thread(registerThread).start()
    }

    override fun onBackPressed() {
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        val clean = getSharedPreferences("state", Context.MODE_PRIVATE).edit()
        clean.remove("tid")
        clean.apply()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CALL_PHONE,
                    android.Manifest.permission.READ_PHONE_STATE
                ),
                101
            )
        }
    }
}
