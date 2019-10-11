package newVersion.login

import android.util.Log
import android.view.View
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.firebase.iid.FirebaseInstanceId
import java.lang.Thread.sleep
import javax.security.auth.Destroyable
import newVersion.Utils.DataStoreUtils
import newVersion.Utils.newCredetials
import newVersion.commonInterface.Init
import newVersion.login.resource.AdapterIpAddress
import newVersion.models.Auth
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.models.Preferences
import newVersion.network.auth.AuthAPI
import newVersion.network.auth.OnAuthListener
import newVersion.network.auth.RPAuthAPI
import org.greenrobot.eventbus.EventBus
import rubegprotocol.RubegProtocol

@InjectViewState
class LoginPresenter : MvpPresenter<LoginView>(), OnAuthListener, Destroyable, Init {
    override var init: Boolean = false

    override fun isInit(): Boolean {
        return init
    }

    private var preferences: Preferences? = null
    var authAPI: AuthAPI? = null
    var waitingForAuth = false
    lateinit var credentials: Credentials
    fun init(preferences: Preferences?) {
        init = true
        this.preferences = preferences
        val address = preferences?.serverAddress
        val port = preferences?.serverPort
        val imei = preferences?.imei
        val fcmtoken = preferences?.fcmtoken

        if (address != null) {
            if (address.count()> 2) {
                viewState.visibilityAddButton(View.GONE)
            }
            if (address.count() <2) {
                viewState.visibilityRemoveButton(View.GONE)
            }
            viewState.setAddress(address)
        }

        if (port != null && port > -1) {
            viewState.setPort(port.toString())
        }
        if (imei != null && fcmtoken != null) {
            credentials = Credentials(
                imei = imei,
                fcmtoken = fcmtoken
            )
        }
    }

    fun validateAddress(holder: AdapterIpAddress.ViewHolder, address: String?) {
        val regexIPv4 = Regex("((1\\d{1,2}|25[0-5]|2[0-4]\\d|\\d{1,2})\\.){3}(1\\d{1,2}|25[0-5]|2[0-4]\\d|\\d{1,2})")
        return when {
            address.isNullOrBlank() -> {
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

    fun validatePort(portStr: String?): Boolean {
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

    fun submit(addresses: ArrayList<String>?, portStr: String, imei: String?) {

        viewState.showDialog()

        val fcmtoken = initFCMToken()
        val isAddressValid = addresses != null
        val isPortValid = validatePort(portStr)
        val isImeiValid = imei != null
        val isFcmTokenValid = fcmtoken != null

        if (!isAddressValid || !isPortValid || !isImeiValid || !isFcmTokenValid) {
            if (!isFcmTokenValid) {
                viewState.showToastMessage("Устройств не может получить google token, возможно на устройстве не установлены сервисы Google")
            }
            if (!isImeiValid) {
                viewState.showToastMessage("Не удалось получить Imei устройства")
            }
            viewState.closeDialog()
            return
        }

        preferences?.serverAddress = addresses!!
        preferences?.serverPort = portStr.toInt()
        preferences?.imei = imei
        preferences?.fcmtoken = fcmtoken

        credentials = Credentials(
            imei.toString(),
            fcmtoken!!
        )

        val hostPool = HostPool(
            addresses = addresses,
            port = portStr.toInt()
        )

        viewState.startService(credentials = credentials, hostPool = hostPool)
        sleep(1000)
        EventBus.getDefault().post(newCredetials(credentials))
        val protocol = RubegProtocol.sharedInstance

        if (authAPI != null) authAPI!!.onDestroy()

        authAPI = RPAuthAPI(protocol, credentials)
        authAPI!!.onAuthListener = this
        sleep(1000)
        if (protocol.isStarted)
            protocol.stop()

        protocol.configure(hostPool.addresses, hostPool.port)

        waitingForAuth = true

        authAPI!!.sendAuthRequest { success ->
            if (!success) {
                if (addresses.count() == 1) {
                    viewState.closeDialog()

                    if (protocol.isStarted) {
                        protocol.stop()

                        viewState.disconnectServer()
                    }

                    viewState.showToastMessage("Не удалось выполнить запрос. Сервер не отвечает или  неправильно введен IP-адрес сервера")
                }
            }
        }

        protocol.start()
    }

    fun visibilityAddButton(visible: Boolean) {
        if (visible)
            viewState.visibilityAddButton(View.VISIBLE)
        else
            viewState.visibilityAddButton(View.GONE)
    }

    fun visibilityRemoveButton(visible: Boolean) {
        if (visible)
            viewState.visibilityRemoveButton(View.VISIBLE)
        else
            viewState.visibilityRemoveButton(View.GONE)
    }

    fun removeItem(indexItem: Int, count: Int) {
        viewState.removeItem(indexItem, count)
    }

    fun addItem(address: java.util.ArrayList<String>) {

        viewState.addItem(address)
    }

    private fun initFCMToken(): String? {
        var token: String? = null
        var timeOut = 0
        if (!preferences?.containsFcmToken!!) {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                token = it.token
            }
        } else {
            token = preferences?.fcmtoken!!
        }
        while (token == null && timeOut <5000) {
            Log.d("Presenter", "$token")
            timeOut++
            sleep(1)
        }

        return token
    }

    override fun onAuthDataReceived(auth: Auth) {
        if (!waitingForAuth) return

        if (auth.authorized) {
            DataStoreUtils.saveRegistrationData(authInfo = auth.authInfo!!)
            viewState.showToastMessage("Регистрация прошла успешно")
            viewState.closeDialog()
            viewState.openCommonScreen()
        } else {
            viewState.closeDialog()
            val protocol = RubegProtocol.sharedInstance

            if (protocol.isStarted) {
                protocol.stop()
                viewState.disconnectServer()
            }

            val message = if (auth.accessDenied) {
                "Данного пользователя нет в базе данных"
            } else {
                "Нет соединения с сервером(-ами)"
            }

            viewState.showToastMessage(message)
        }

        waitingForAuth = false
    }

    override fun onDestroy() {
        authAPI?.onDestroy()
        init = false
        super.onDestroy()
    }
}