package gbr.presentation.presenter.start

import android.location.LocationManager
import android.util.Log
import gbr.utils.models.Preferencess
import gbr.presentation.view.start.StartView
import gbr.utils.PrefsUtils
import gbr.utils.api.access.AccessAPI
import gbr.utils.api.access.OnAccessListener
import gbr.utils.api.access.RPAccessAPI
import gbr.utils.api.serverstatus.OnServerStatusListener
import gbr.utils.api.serverstatus.RPServerStatusAPI
import gbr.utils.api.serverstatus.ServerStatusAPI
import gbr.utils.callbacks.GpsCallback
import gbr.utils.data.ProtocolServiceInfo
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import moxy.InjectViewState
import moxy.MvpPresenter
import gbr.utils.servicess.LocationListener
import gbr.utils.servicess.ProtocolService
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.utils.ProviderStatus
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
            callback.gpsCheck()
            return
        }
        //TODO не забудь раскоментировать дядя
        /*pref.version = BuildConfig.VERSION_NAME*/
        viewState.whatNew()

    }

    fun startGPS(locationManager: LocationManager) {
        viewState.setText("Проверка состояния GPS...")
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            LocationListener(locationManager)
            return
        }

        val message = "GPS отключен. Данное приложение не работает без GPS."
        viewState.gpsSetting(message,locationManager)
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun providerStatus(event:ProviderStatus){
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

    private fun dataChecking() {
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

    override fun onAuthDataReceived(auth: gbr.utils.data.AuthInfo) {

    }

    override fun onAccessDataReceiver(access: Boolean) {
        if(access) return

    }

    override fun onServerStatusDataReceived() {
        Log.d("ServerStatus","NotResponse")
    }

    override fun onDestroy() {
        super.onDestroy()
        authAPI?.onDestroy()
        serverStatusAPI?.onDestroy()
        accessAPI?.onDestroy()
    }
}