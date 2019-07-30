package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.service.NetworkService
import org.json.JSONObject
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class PlanFragment : androidx.fragment.app.Fragment() {

    val networkService:NetworkService = NetworkService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.plan_fragment, container, false)

        val imageView = rootView.findViewById(R.id.testImage) as ImageView
        val image:Bitmap = BitmapFactory.decodeByteArray(NetworkService.byteMessageBroker[0],0,NetworkService.byteMessageBroker[0].count())

        imageView.setImageBitmap(image)

        val message = JSONObject()
        message.put("\$c$","sendfile")
        message.put("name","firsttry.png")
        message.put("nameinserv","firsttry.png")
        networkService.request(message.toString(),null){
            it:Boolean,data->
            if(it){
                if(JSONObject(String(data!!)).getString("\$c$") == "startrecivefile")
                {
                    thread{
                        Log.d("Picture",NetworkService.byteMessageBroker[0].count().toString())
                        sleep(2000)
                        val now = System.currentTimeMillis()
                        val bytearray:ByteArray = ByteArray(100000000) {1.toByte()}

                        networkService.send(bytearray,null){
                            if(it){
                                Log.d("Picture","send")
                                Log.d("picture",(System.currentTimeMillis()-now).toString())
                            }
                        }
                    }

                }
            }
        }
        return rootView
    }
}