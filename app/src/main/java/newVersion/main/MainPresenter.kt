package newVersion.main

import android.location.LocationManager
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import newVersion.LocationListener
import newVersion.Utils.DataStoreUtils
import newVersion.commonInterface.Init
import newVersion.models.Auth
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.models.Preferences
import newVersion.network.auth.AuthAPI
import newVersion.network.auth.OnAuthListener
import newVersion.network.auth.RPAuthAPI
import org.greenrobot.eventbus.EventBus
import ru.rubeg38.rubegprotocol.RubegProtocol
import java.lang.Thread.sleep
import javax.security.auth.Destroyable
import kotlin.concurrent.thread

@InjectViewState
class MainPresenter:MvpPresenter<MainView>(),OnAuthListener,Destroyable,Init {
    override var init: Boolean = false

    override fun isInit():Boolean {
        return init
    }

    private var  preferences: Preferences? = null
    var authAPI: AuthAPI? = null
    var waitingForAuth = false
    lateinit var credentials: Credentials

    fun init(preferences: Preferences?) {
        Log.d("MainPresenter","Init")
        init = true
        thread {
            this.preferences = preferences

            viewState.startAnimation()
            sleep(2000)

            viewState.setHintText("Проверка данных,подождите...")
            sleep(1000)

            viewState.setHintText("Проверка подключения к GPS")
            viewState.initLocationManager()
        }
    }

    fun checkData(){
        thread {
            val containAddress = preferences?.containsAddress
            val containPort = preferences?.containsPort
            val containImei = preferences?.containsImei
            val containFCMtoken = preferences?.containsFcmToken
            if(!containAddress!! || !containPort!! || !containImei!! || !containFCMtoken!!)
            {

                viewState.setHintText("Регистрация")
                sleep(1000)
                viewState.openLoginActivity()
                return@thread

            }

            viewState.setHintText("Авторизация")
            submit(preferences)
        }

    }
    private fun submit(preferences: Preferences?){
        credentials = Credentials(
            preferences?.imei!!,
            preferences.fcmtoken!!
        )
        val hostPool = HostPool(
            addresses = preferences.serverAddress,
            port = preferences.serverPort
        )

        viewState.startService(credentials,hostPool)
        EventBus.getDefault().post(credentials)
        val protocol = RubegProtocol.sharedInstance
        if(authAPI!=null) authAPI!!.onDestroy()

        authAPI = RPAuthAPI(protocol,credentials)
        authAPI!!.onAuthListener = this

        if(protocol.isStarted)
            protocol.stop()

        protocol.configure(hostPool.addresses,hostPool.port)

        waitingForAuth = true

        authAPI!!.sendAuthRequest { success->
            if(!success){
                if(preferences.serverAddress.count() == 1){
                    if(protocol.isStarted){
                        protocol.stop()
                        viewState.disconnectServer()
                        viewState.openLoginActivity()
                    }

                    viewState.showToastMessage("Не удалось выполнить запрос. Сервер не отвечает или  неправильно введен IP-адрес сервера")
                }
            }
        }

       protocol.start()
    }
    override fun onAuthDataReceived(auth: Auth) {
        if(!waitingForAuth) return

        if(auth.authorized){
            DataStoreUtils.saveRegistrationData(authInfo = auth.authInfo!!)
            viewState.showToastMessage("Авторизация прошла успешно")
        }
        else
        {
            val protocol = RubegProtocol.sharedInstance
            if(protocol.isStarted){
                protocol.stop()
                viewState.disconnectServer()
            }

            val message = if(auth.accessDenied){
                "Данного пользователя нет в базе данных"
            }
            else
            {
                "Нет соединения с сервером(-ами)"
            }
            viewState.showToastMessage(message)
            viewState.openLoginActivity()
        }
        waitingForAuth = false
    }

    override fun onDestroy() {
        init = false
        super.onDestroy()

    }
}