package newVersion.network.status

import com.google.gson.Gson
import com.google.gson.JsonObject
import newVersion.utils.StatusGson
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPStatusAPI(
    private val protocol: RubegProtocol
) : StatusAPI {
    override var onStatusListener: OnStatusListener? = null

    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendStatusRequest(status: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "gbrkobra")
        jsonObject.addProperty("command", "status")
        jsonObject.addProperty("newstatus", status)
        val request = jsonObject.toString()
        protocol.send(request, complete)
    }

    override fun onTextMessageReceived(message: String) {
        val gson = Gson()

        if (!JSONObject(message).has("command")) {
            return
        }
        when (JSONObject(message).getString("command")) {
            "gbrstatus" -> {
                val status = gson.fromJson(message, StatusGson::class.java)
                if (status.status != "") {
                    onStatusListener?.onStatusDataReceived(status = status.status, call = status.call)
                }
            }
        }
    }

    override fun onDestroy() {
        unsubscribe()
    }
}