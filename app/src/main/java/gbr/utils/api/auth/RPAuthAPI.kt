package gbr.utils.api.auth

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import gbr.utils.data.AuthInfo
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import gbr.utils.data.Credentials
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPAuthAPI(
    private val protocol:RubegProtocol,
    private val credentials: Credentials
):AuthAPI {
    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)
    override var onAuthListener: OnAuthListener? = null

    override fun sendAuthRequest(complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "reg")
        jsonObject.addProperty("id", "0D82F04B-5C16-405B-A75A-E820D62DF911")
        jsonObject.addProperty("password", credentials.imei)
        jsonObject.addProperty("token", credentials.fcmtoken)
        jsonObject.addProperty("ver.", BuildConfig.VERSION_NAME)
        jsonObject.addProperty("keepalive", "5")

        val request = jsonObject.toString()

        Log.d("Auth","$request")
        protocol.send(request, complete)
    }

    override fun onTextMessageReceived(message: String) {

        try{
            if (!JSONObject(message).has("\$c$")) {
                return
            }
        }catch (e:Exception){
            return
        }

        if(JSONObject(message).getString("\$c$") != "regok") return

        val registration =  Gson().fromJson(message, AuthInfo::class.java)
        onAuthListener?.onAuthDataReceived(registration)
        return
    }

    override fun onDestroy() {
        unsubscribe()
    }
}