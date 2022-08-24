package gbr.presentation.presenter.login

import android.util.Log
import android.view.View
import com.google.firebase.iid.FirebaseInstanceId
import gbr.presentation.view.login.LoginView
import gbr.utils.PrefsUtils
import gbr.utils.adapters.login.AdapterIpAddresses
import gbr.utils.api.access.AccessAPI
import gbr.utils.api.access.OnAccessListener
import gbr.utils.api.access.RPAccessAPI
import gbr.utils.api.serverstatus.OnServerStatusListener
import gbr.utils.api.serverstatus.RPServerStatusAPI
import gbr.utils.api.serverstatus.ServerStatusAPI
import gbr.utils.data.AuthInfo
import gbr.utils.data.Info
import gbr.utils.data.ProtocolServiceInfo
import moxy.InjectViewState
import moxy.MvpPresenter
import gbr.utils.data.Credentials
import gbr.utils.data.HostPool
import org.greenrobot.eventbus.EventBus
import rubegprotocol.RubegProtocol
import java.lang.Thread.sleep
import java.util.ArrayList

@InjectViewState
class LoginPresenter: MvpPresenter<LoginView>(), OnServerStatusListener, OnAccessListener,gbr.utils.api.auth.OnAuthListener {

    lateinit var preferences: PrefsUtils

    var authAPI:gbr.utils.api.auth.AuthAPI? = null
    var accessAPI: AccessAPI? = null
    var serverStatusAPI: ServerStatusAPI? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        val port = "9010"

        viewState.setPort(port)

    }

    fun init(preferences: PrefsUtils) {
        this.preferences = preferences

        if(preferences.imei==null || preferences.imei=="")
            viewState.setImei()
        else
            viewState.setImei(preferences.imei!!)

        val address = preferences.serverAddress
        val port = preferences.serverPort
        val imei = preferences.imei
        val fcmtoken = preferences.fcmtoken

        if(address.count()>2)
            viewState.visibilityAddButton(View.GONE)

        if(address.count()<2)
            viewState.visibilityRemoveButton(View.GONE)

        if(address.count() == 0)
            address.add("")

        viewState.setAddress(address)


        if(port != 9010 && port > -1)
        {
            viewState.setPort(port.toString())
        }
    }

    fun validateAddress(holder: AdapterIpAddresses.ViewHolder, address: String) {
        val regexIPv4 = Regex("((1\\d{1,2}|25[0-5]|2[0-4]\\d|\\d{1,2})\\.){3}(1\\d{1,2}|25[0-5]|2[0-4]\\d|\\d{1,2})")
        return when {
            address.isBlank() -> {
                holder.ipAddressLayoutView.error = ("Поле не должно быть пустым")
            }
            !regexIPv4.matches(address) -> {
                holder.ipAddressLayoutView.error = ("Неверный формат IPv4")
            }
            else -> {
                holder.ipAddressLayoutView.error = (null)
            }
        }
    }

    fun validateAddresses(addresses: ArrayList<String>): ArrayList<String>? {
        var isBlank = false
        var isRegexIPv4 = false

        val regexIPv4 = Regex("((1\\d{1,2}|25[0-5]|2[0-4]\\d|\\d{1,2})\\.){3}(1\\d{1,2}|25[0-5]|2[0-4]\\d|\\d{1,2})")
        for (i in 0 until addresses.count()) {
            if (addresses[i].isBlank()) {
                isBlank = true
                continue
            }
            if (!regexIPv4.matches((addresses[i]))) {
                isRegexIPv4 = true
                continue
            }
        }

        return when {
            isBlank -> {
                Log.d("Presenter", "Blank")
                viewState.showToastMessage("Одно из полей незаполнено")
                null
            }
            isRegexIPv4 -> {
                Log.d("Presenter", "IPv4")
                viewState.showToastMessage("Одно из полей не соответствует формату IPv4")
                null
            }
            else -> {
                addresses
            }
        }
    }

    fun validatePort(portStr: String?):Boolean {
        val port = portStr?.toIntOrNull()
        return when {
            portStr.isNullOrBlank() -> {
                viewState.setPortTextViewError("Поле не должно быть пустым")
                false
            }
            portStr.contains(Regex("[\\D]")) -> {
                viewState.setPortTextViewError("Поле не должно содержать ничего кроме цифр")
                false
            }
            port == null || port> 0xFFFF -> {
                viewState.setPortTextViewError("Номер порта не должен превышать 65535")
                false
            }
            else -> {
                viewState.setPortTextViewError(null)
                true
            }
        }
    }

    fun validateImei(imeiStr: String?):Boolean {
        val imei = imeiStr?.toIntOrNull()
        return when{
            imeiStr.isNullOrBlank()->{
                viewState.setImeiTextViewError("Поле не должно быть пустым")
                false
            }
            imeiStr.contains(Regex("[\\D]")) -> {
                viewState.setImeiTextViewError("Поле не должно содержать ничего кроме цифр")
                false
            }
            imeiStr.length>15->{
                viewState.setImeiTextViewError("Превышено количество символом")
                false
            }
            imeiStr.length<15->{
                viewState.setImeiTextViewError("Осталось символов ${15-imeiStr.length}")
                true
            }
            else->{
                viewState.setImeiTextViewError(null)
                true
            }
        }
    }

    fun visibility(count: Int) {
        when(count){
            1->{
                viewState.visibilityAddButton(View.VISIBLE)
                viewState.visibilityRemoveButton(View.GONE)
            }
            2->{
                viewState.visibilityAddButton(View.VISIBLE)
                viewState.visibilityRemoveButton(View.VISIBLE)
            }
            3 -> {
                viewState.visibilityAddButton(View.GONE)
                viewState.visibilityRemoveButton(View.VISIBLE)
            }
        }
    }

    fun submit(addresses: ArrayList<String>, port: String, imei: String) {
        // показать диалог

        viewState.showDialog()

        val fcmToken = initFCMToken()
        val isAddressValid = validateAddresses(addresses)!=null
        val isPortValid = validatePort(port)
        val isImeiValid = validateImei(imei)
        val isFcmTokenValid = fcmToken!=null

        if (!isAddressValid || !isPortValid || !isImeiValid || !isFcmTokenValid) {
            if (!isFcmTokenValid) {
                viewState.showToastMessage("Устройств не может получить google token, возможно на устройстве не установлены сервисы Google")
            }
            if (!isImeiValid) {
                viewState.showToastMessage("Не удалось получить Imei устройства")
            }
            //закрыть диалог
            viewState.closeDialog()
            return
        }

        preferences.serverAddress = addresses
        preferences.serverPort = port.toInt()
        preferences.imei = imei
        preferences.fcmtoken = fcmToken

        val credentials = Credentials(
            imei,
            fcmToken!!
        )

        val hostPool = HostPool(
            addresses,
            port.toInt()
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
                if(preferences.serverAddress.count() != 1 ) return@sendAuthRequest

                if(protocol.isStarted)
                {
                    protocol.stop()
                }

               // Выдать ошибку что не удалось подключится
                viewState.closeDialog()
                viewState.showToastMessage("Соединение с сервером не удалось")
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

    private fun initFCMToken(): String? {
        var token: String? = null
        var timeOut = 0
        if (!preferences.containsFcmToken) {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                token = it.token
            }
        } else {
            token = preferences.fcmtoken!!
        }
        while (token == null && timeOut <10000) {

            timeOut++
            Thread.sleep(1)
        }

        return token
    }

    override fun onServerStatusDataReceived() {
        Log.d("ServerStatus","NotResponse")
    }

    override fun onAccessDataReceiver(access: Boolean) {
      if (access) return
    }

    override fun onAuthDataReceived(auth: AuthInfo) {
        sleep(2000)
        viewState.closeDialog()

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
        viewState.mainActivity()
    }

}