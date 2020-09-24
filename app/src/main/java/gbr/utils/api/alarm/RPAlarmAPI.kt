package gbr.utils.api.alarm

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import gbr.utils.data.AlarmInformation
import newVersion.utils.Alarm
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubegprotocol.RubegProtocol
import java.lang.Exception

class RPAlarmAPI(
    private var protocol: RubegProtocol,
    private var activity:String
):AlarmAPI {
    override var onAlarmListener: OnAlarmListener? = null

    private var unsubrscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendAlarmRequest(namegbr: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$","getalarm")
        jsonObject.addProperty("namegbr",namegbr)

        val request = jsonObject.toString()

        Log.d("Alarm",request)

        protocol.send(request,complete)
    }


    override fun sendAlarmApplyRequest(objectNumber: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "gbrkobra")
        jsonObject.addProperty("command", "alarmp")
        jsonObject.addProperty("number", objectNumber)

        val request = jsonObject.toString()

        protocol.send(request, complete)
    }

    override fun sendMobAlarmApplyRequest(objectNumber: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "gbrkobra")
         jsonObject.addProperty("command", "alarmp")
        jsonObject.addProperty("number", objectNumber)

        val request = jsonObject.toString()

        protocol.send(request, complete)
    }

    override fun sendArrivedObject(objectNumber: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "gbrkobra")
        jsonObject.addProperty("command", "alarmpr")
        jsonObject.addProperty("number", objectNumber)

        val request = jsonObject.toString()

        protocol.send(request, complete)
    }

    override fun sendMobArrivedObject(objectNumber: String, complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$", "gbrkobra")
        jsonObject.addProperty("mob","1")
        jsonObject.addProperty("command", "alarmpr")
        jsonObject.addProperty("number", objectNumber)

        val request = jsonObject.toString()

        protocol.send(request, complete)
    }
    override fun sendReport(
        report: String,
        comment: String,
        namegbr: String,
        objectName: String,
        objectNumber: String,
        complete: (Boolean) -> Unit
    ) {
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

        if(!JSONObject(message).has("command")) return

        Log.d("AlarmMessage",message + activity)

        if(JSONObject(message).getString("command")!="alarm" &&
            JSONObject(message).getString("command")!="notalarm" &&
            JSONObject(message).getString("command")!="alarmmob") return



        when (JSONObject(message).getString("command")) {
            "alarm" -> {
                onAlarmListener?.onAlarmDataReceived("alarm",message)
            }
            "notalarm"->{
                onAlarmListener?.onAlarmDataReceived("notalarm",message)
            }
            "alarmmob"->{
                onAlarmListener?.onAlarmDataReceived("alarmmob",message)
            }
        }
    }

    override fun onDestroy() {
        unsubrscribe()
    }
}