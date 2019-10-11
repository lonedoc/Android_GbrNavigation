package newVersion.network.alarm

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.lang.Exception
import oldVersion.workservice.Alarm
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol

class RPAlarmAPI(
    private val protocol: RubegProtocol
) : AlarmAPI {

    override var onAlarmListener: OnAlarmListener? = null

    private var unsubrscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendAlarmRequest(namegbr: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "getalarm")
        jsonObject.addProperty("namegbr", namegbr)

        val request = jsonObject.toString()

        protocol.send(request, complete)
    }

    override fun sendAlarmApplyRequest(objectNumber: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "gbrkobra")
        jsonObject.addProperty("command", "alarmp")
        jsonObject.addProperty("number", objectNumber)

        val request = jsonObject.toString()

        protocol.send(request, complete)
    }

    override fun onTextMessageReceived(message: String) {
        val gson = Gson()

        if (!JSONObject(message).has("command")) {
            return
        }
        when (JSONObject(message).getString("command")) {
            "alarm" -> {
                try {
                    val alarm = gson.fromJson(message, Alarm::class.java)
                    if (alarm.name != null)
                        onAlarmListener?.onAlarmDataReceived(alarm)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return
            }
            else -> {
                return
            }
        }
    }

    override fun onDestroy() {
        unsubrscribe()
    }
}