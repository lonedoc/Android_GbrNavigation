package gbr.presentation.presenter.start

import android.content.Context
import android.util.Log
import gbr.presentation.view.start.StartView
import gbr.utils.api.access.AccessAPI
import gbr.utils.api.access.OnAccessListener
import gbr.utils.api.access.RPAccessAPI
import gbr.utils.api.serverstatus.OnServerStatusListener
import gbr.utils.api.serverstatus.RPServerStatusAPI
import gbr.utils.api.serverstatus.ServerStatusAPI
import gbr.utils.callbacks.GpsCallback
import gbr.utils.data.AuthInfo
import gbr.utils.data.Info
import gbr.utils.data.ProtocolServiceInfo
import gbr.utils.data.ProviderStatus
import gbr.utils.models.Preferencess
import gbr.utils.servicess.ProtocolService.Companion.currentLocation
import gbr.utils.servicess.ProtocolService.Companion.isGPSLocationEnable
import gbr.utils.servicess.ProtocolService.Companion.isInternetLocationEnable
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import moxy.InjectViewState
import moxy.MvpPresenter
import gbr.utils.data.Credentials
import gbr.utils.data.HostPool
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rubegprotocol.RubegProtocol
import java.lang.Thread.sleep
import kotlin.concurrent.thread


@InjectViewState
class StartPresenter:MvpPresenter<StartView>(),OnServerStatusListener,OnAccessListener,gbr.utils.api.auth.OnAuthListener{
    private lateinit var pref:Preferencess

    var errorMessage = "Ошибка"

    var authAPI:gbr.utils.api.auth.AuthAPI? = null
    var accessAPI:AccessAPI? = null
    var serverStatusAPI:ServerStatusAPI? = null

    var context:Context? = null
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.setText("Проверка разрешений...")
        viewState.checkPermission()

        EventBus.getDefault().register(this)
    }

    fun errorPermission() {
        errorMessage = "Были даны не все разрешения, приложение не продолжит работу."
        viewState.errorPermissionDialog(errorMessage)
    }

    fun init(pref: Preferencess,callback:GpsCallback) {

        viewState.startService()

        this.pref = pref

        if(this.pref.version == BuildConfig.VERSION_NAME)
        {
            thread {
                sleep(2000)
                callback.gpsCheck()
            }

            return
        }

             pref.version = BuildConfig.VERSION_NAME
        viewState.whatNew()

    }

    fun startGPS() {

        viewState.setText("Проверка состояния GPS...")


/*        if(isGPSLocationEnable || isInternetLocationEnable)
        {
            viewState.stopGpsSetting()
            thread {
                while (currentLocation==null)
                {
                    //
                }
                Log.d("CurrentLocation","${currentLocation!!.latitude}")
                Log.d("CurrentLocation","${currentLocation!!.longitude}")

            }
            return
        }*/
        dataChecking()
       /* val message = "GPS отключен. Данное приложение не работает без GPS."
        viewState.gpsSetting(message)*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun providerStatus(event: ProviderStatus){
        when(event.status){
            "disable"->{
                errorMessage = "Включите GPS для продолжения работы приложения"
                viewState.setText(errorMessage)
            }
            else->{
                dataChecking()
            }
        }
    }

    fun dataChecking() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        viewState.setText("Проверяем данные...")

        if (pref.containsAddress && pref.containsPort && pref.containsImei && pref.containsFcmToken)
        {
            auth()
            return
        }

        viewState.loginActivity()

    }

    private fun auth() {

        viewState.setText("Авторизация...")

        val credentials = Credentials(
            pref.imei!!,
            pref.fcmtoken!!
        )

        val hostPool = HostPool(
            pref.serverAddress,
            pref.serverPort
        )

        EventBus.getDefault().postSticky(
            ProtocolServiceInfo(
                credentials,
                hostPool
            )
        )

        val protocol = RubegProtocol.sharedInstance

        initAuthAPI(protocol,credentials)

        initAccessAPI(protocol)

        initServerStatusAPI(protocol)

        authAPI!!.sendAuthRequest { success->
            if(!success)
            {
                if(pref.serverAddress.count() != 1 ) return@sendAuthRequest

                if(protocol.isStarted)
                {
                        protocol.stop()
                }

                viewState.loginActivity()
            }
        }

    }

    private fun initAuthAPI(
        protocol: RubegProtocol,
        credentials: Credentials
    ) {
        if(authAPI != null)
            authAPI!!.onDestroy()

        authAPI = gbr.utils.api.auth.RPAuthAPI(
            protocol = protocol,
            credentials = credentials
        )

        authAPI!!.onAuthListener = this
    }

    private fun initAccessAPI(protocol: RubegProtocol) {
        if(accessAPI !=null)
            accessAPI!!.onDestroy()
        accessAPI = RPAccessAPI(protocol)
        accessAPI!!.onAccessListener = this
    }

    private fun initServerStatusAPI(protocol: RubegProtocol) {
        if(serverStatusAPI != null)
            serverStatusAPI!!.onDestroy()
        serverStatusAPI = RPServerStatusAPI(protocol)
        serverStatusAPI!!.onServerStatusListener = this
    }

    override fun onAuthDataReceived(auth: AuthInfo) {
        Log.d("Auth",auth.call)

        Info.status(auth.status)
        Info.call(auth.call)
        if(auth.cityCard.pcsinfo.dist!=null || auth.cityCard.pcsinfo.dist!=""){
            Info.dist(auth.cityCard.pcsinfo.dist.toInt())
        }
        else
        {
            Info.dist=0
        }
        Info.statusList(auth.statusList)
        Info.nameGBR(auth.namegbr)
        Info.routeServers(auth.routeServerList)
        Info.reportList(auth.reportsList)
        sleep(1000)
        viewState.openMainActivity()
    }

    override fun onAccessDataReceiver(access: Boolean) {
        if(access) return

    }

    override fun onServerStatusDataReceived() {
        Log.d("ServerStatus","NotResponse")
        viewState.errorMessage("Сервер не отвечает")
        viewState.loginActivity()
    }

    override fun onDestroy() {
        authAPI?.onDestroy()
        serverStatusAPI?.onDestroy()
        accessAPI?.onDestroy()
        super.onDestroy()
    }
}