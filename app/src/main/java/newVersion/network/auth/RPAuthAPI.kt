package newVersion.network.auth

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import newVersion.models.Auth
import newVersion.models.AuthInfo
import newVersion.models.Credentials
import newVersion.Utils.RegistrationGson
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPAuthAPI(
    private val protocol: RubegProtocol,
    private val credentials: Credentials
) : AuthAPI {
    override var onAuthListener: OnAuthListener? = null

    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendAuthRequest(complete: (Boolean) -> Unit) {

        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "reg")
        jsonObject.addProperty("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        jsonObject.addProperty("password", credentials.imei)
        jsonObject.addProperty("token", credentials.fcmtoken)
        jsonObject.addProperty("ver.", BuildConfig.VERSION_NAME)
        jsonObject.addProperty("keepalive", "10")

        val request = jsonObject.toString()

        Log.d("Auth", request)
        protocol.send(request, complete)
    }

    override fun onTextMessageReceived(message: String) {
        Log.d("AuthMessage", message)

        val gson = Gson()

        if (!JSONObject(message).has("\$c$")) {
            return
        }

        when (JSONObject(message).getString("\$c$")) {
            "regok" ->
                {
                    val registration = gson.fromJson(message, RegistrationGson::class.java)

                    val authInfo = AuthInfo(
                        token = registration.tid,
                        nameGbr = registration.namegbr,
                        call = registration.call,
                        status = registration.status,
                        gpsStatus = registration.gpsstatus,
                        routeServer = registration.routeserver,
                        report = registration.reports,
                        cityCard = registration.citycard
                    )

                    val auth = Auth(
                        authInfo = authInfo,
                        authorized = true,
                        accessDenied = false
                    )

                    onAuthListener?.onAuthDataReceived(auth)
                }
            "accessdenied" -> {
                val auth = Auth(
                    null,
                    false,
                    accessDenied = true
                )
                onAuthListener?.onAuthDataReceived(auth)
                return
            }
            "ServerNotResponse" -> {
                val auth = Auth(
                    null,
                    false,
                    false
                )
                onAuthListener?.onAuthDataReceived(auth)
                return
            }
        }
    }
    override fun onDestroy() {
        unsubscribe()
    }
}