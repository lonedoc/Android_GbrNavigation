package newVersion.network.complete

import com.google.gson.JsonObject
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPCompleteAPI(
    private val protocol: RubegProtocol
) : CompleteAPI {

    override var onCompleteListener: OnCompleteListener? = null
    private var unsubrscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendArrivedObject(objectNumber: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "gbrkobra")
        jsonObject.addProperty("command", "alarmpr")
        jsonObject.addProperty("number", objectNumber)

        val request = jsonObject.toString()

        protocol.send(request, complete)
    }

    override fun sendReport(report: String, comment: String, namegbr: String, objectName: String, objectNumber: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "reports")
        jsonObject.addProperty("report", report)
        jsonObject.addProperty("comment", comment)
        jsonObject.addProperty("namegbr", namegbr)
        jsonObject.addProperty("name", objectName)
        jsonObject.addProperty("number", objectNumber)

        val request = jsonObject.toString()

        protocol.send(request, complete)
    }

    override fun onTextMessageReceived(message: String) {
        if (!JSONObject(message).has("command")) return

        when (JSONObject(message).getString("command")) {
            "notalarm" -> {
                onCompleteListener?.onCompleteDataReceived(JSONObject(message).getString("name"))
                return
            }
            else -> { return }
        }
    }

    override fun onDestroy() {
        unsubrscribe()
    }
}