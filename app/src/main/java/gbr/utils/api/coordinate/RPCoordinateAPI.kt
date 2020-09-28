package gbr.utils.api.coordinate

import android.util.Log
import org.json.JSONObject
import ru.rubeg38.rubegprotocol.TextMessageWatcher
import rubeg38.myalarmbutton.utils.api.coordinate.CoordinateAPI
import rubeg38.myalarmbutton.utils.api.coordinate.OnCoordinateListener
import rubegprotocol.RubegProtocol

class RPCoordinateAPI(
    private var protocol:RubegProtocol
): CoordinateAPI {

    override var onCoordinateListener: OnCoordinateListener? = null
    override fun sendCoordinateRequest(
        lat: String,
        lon: String,
        imei:String,
        speed: Int,
        satelliteCount: Int,
        accuracy: Float
    ) {
        val message = JSONObject()
        message.put("\$c$", "gbrkobra")
        message.put("command", "location")
        message.put("id",imei)
        message.put("lat", lat)
        message.put("lon", lon)
        message.put("speed",speed)
        message.put("accuracy",accuracy)
        message.put("gpsCount",satelliteCount)


        Log.d("Coordinate",message.toString())
        protocol.send(message.toString()){
        }
    }

    private var unsubscribe = protocol.subscribe(this as TextMessageWatcher)


    override fun onTextMessageReceived(message: String) {
        if(JSONObject(message).getString("\$c$") != "gbrkobra") return

        if(JSONObject(message).getString("command") != "alarmpos") return

        onCoordinateListener?.onCoordinateListener(JSONObject(message).getString("lat"),JSONObject(message).getString("lon"))
    }

    override fun onDestroy() {
        unsubscribe()
    }

}