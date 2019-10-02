package newVersion.network.status

import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import oldVersion.workservice.NotificationService
import oldVersion.workservice.StatusGson
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.RubegProtocol
import ru.rubeg38.rubegprotocol.TextMessageWatcher

class RPStatusApi(
    private val protocol: RubegProtocol
) :StatusApi{
    override var OnStatusListener: OnStatusListener? = null

    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)

    override fun sendStatusRequest(status:String,complete: (Boolean) -> Unit) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("\$c$","gbrkobra")
        jsonObject.addProperty("command","status")
        jsonObject.addProperty("newstatus",status)
        val request = jsonObject.toString()
        protocol.send(request,complete)
    }

    override fun onTextMessageReceived(message: String) {
        Log.d("StatusMessage", message)
        val gson = Gson()

        if(!JSONObject(message).has("command")){
            return
        }
        when(JSONObject(message).getString("command")){
            "gbrstatus"->{
                val status = gson.fromJson(message,StatusGson::class.java)
                if(status.status!=""){
                    OnStatusListener?.onStatusDataReceived(status = status.status,call= status.call)
                }
            }
        }
    }

    override fun onDestroy() {
        unsubscribe()
    }
}