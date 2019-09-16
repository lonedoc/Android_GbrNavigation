package objectactivity.planfragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kobramob.rubeg38.ru.gbrnavigation.R
import commonactivity.AlarmObjectInfo
import objectactivity.data.ObjectDataStore
import workservice.ImageEvent
import workservice.ProtocolNetworkService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class PlanFragment : androidx.fragment.app.Fragment() {

    companion object{
        var countInQueue: Int = 0
    }

    private var rootView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.plan_fragment, container, false)
        return rootView
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 2)
    fun onMessageEvent(event: ImageEvent) {
        try{
            if (event.byteArray.count()> 0 && event.command == "getfile") {
                val image: Bitmap = BitmapFactory.decodeByteArray(event.byteArray, 0, event.byteArray.count())

                if (!ObjectDataStore.bitmapList.contains(image))
                    ObjectDataStore.bitmapList.add(image)
            }
        }catch (e:Exception){
            e.printStackTrace()
            ObjectDataStore.bitmapList.clear()
            countInQueue = 0
            downloadImage()
        }
    }

    override fun onResume() {
        super.onResume()

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        downloadImage()
    }

    private fun downloadImage(){
        val downloadProgressBar: ProgressBar = rootView!!.findViewById(R.id.plan_download)
        val planDownloadTextView: TextView = rootView!!.findViewById(R.id.plan_download_text)
        val planListRecyclerView: RecyclerView = rootView!!.findViewById(R.id.imageRecyclerView)

        val alarmObjectInfo = activity!!.intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

        val requestDownloadList = alarmObjectInfo.planAndPhotoList

        if(requestDownloadList.count()>0){
            Log.d("PlanFragment","Download")
            downloadProgressBar.visibility = View.VISIBLE
            if(ObjectDataStore.bitmapList.count() == 0 && countInQueue != requestDownloadList.count())
            {
                when{
                    !ProtocolNetworkService.connectInternet -> {
                        Toast.makeText(
                            context,
                            "Нет соединения с интернетом, невозможно скачать изображения",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    !ProtocolNetworkService.connectServer -> {
                        Toast.makeText(
                            context,
                            "Нет соединения с сервером, невозможно скачать изображения",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else ->
                    {
                        for( i in 0 until requestDownloadList.count()){
                            countInQueue = i
                            val downloadImage = JSONObject()
                            downloadImage.put("\$c$", "getfile")
                            downloadImage.put("nameinserv", requestDownloadList[i])
                            downloadImage.put("name", requestDownloadList[i])
                            ProtocolNetworkService.protocol?.send(downloadImage.toString()) { success: Boolean ->
                                if (success) {
                                    Log.d("PlanFragment","WaitReceiver")
                                } else {
                                    activity!!.runOnUiThread {
                                        Toast.makeText(
                                            activity!!,
                                            "Не удалось загрузить изображение ${alarmObjectInfo.planAndPhotoList[countInQueue -1]}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                        val handler = Handler()
                        thread{
                            Log.d("PlanFragment","ThreadStart")
                            while(ObjectDataStore.bitmapList.count() != requestDownloadList.count())
                            {
                                handler.post {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        downloadProgressBar.setProgress(ProtocolNetworkService.protocol?.percent!!,true)
                                    }
                                    else
                                    {
                                        downloadProgressBar.progress =
                                            ProtocolNetworkService.protocol?.percent!!
                                    }
                                    planDownloadTextView.text = "Скачано изображений ${ObjectDataStore.bitmapList.count()}"
                                }
                                sleep(100)
                            }
                            Log.d("PlanFragment","ThreadStop")
                            try {
                                initRecyclerView(
                                    downloadProgressBar = downloadProgressBar,
                                    planListRecyclerView = planListRecyclerView,
                                    planDownloadTextView = planDownloadTextView
                                )
                            }catch (e:java.lang.Exception){
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            else
            {
                initRecyclerView(
                    downloadProgressBar = downloadProgressBar,
                    planListRecyclerView = planListRecyclerView,
                    planDownloadTextView = planDownloadTextView
                )
            }
        }
        else
        {
            Toast.makeText(activity, "Список план-схем и изображений пуст", Toast.LENGTH_SHORT).show()
            EventBus.getDefault().unregister(this)
        }
    }

    private fun initRecyclerView(downloadProgressBar:ProgressBar, planListRecyclerView:RecyclerView, planDownloadTextView:TextView){
        activity!!.runOnUiThread {
            downloadProgressBar.visibility = View.GONE
            planDownloadTextView.visibility = View.GONE
            planListRecyclerView.visibility = View.VISIBLE
            planListRecyclerView.layoutManager = LinearLayoutManager(activity)
            planListRecyclerView.adapter =
                AdapterPlanList(
                    ObjectDataStore.bitmapList,
                    context!!
                )
            planListRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    planListRecyclerView.context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
        EventBus.getDefault().unregister(this)
    }
}

/*        val imageView = rootView.findViewById(R.id.testImage) as ImageView
        val image: Bitmap = BitmapFactory.decodeByteArray(NetworkService.byteMessageBroker[0], 0, NetworkService.byteMessageBroker[0].count())

        imageView.setImageBitmap(image)

        val message = JSONObject()
        message.put("\$c$", "sendfile")
        message.put("name", "firsttry.png")
        message.put("nameinserv", "firsttry.png")
        networkService.request(message.toString(), null) {
            it: Boolean, data ->
                if (it) {
                    if (JSONObject(String(data!!)).getString("\$c$") == "startrecivefile") {
                        thread {
                            Log.d("Picture", NetworkService.byteMessageBroker[0].count().toString())
                            sleep(2000)
                            val now = System.currentTimeMillis()
                            val bytearray: ByteArray = ByteArray(100000000) { 1.toByte() }

                            networkService.send(bytearray, null) {
                                if (it) {
                                    Log.d("Picture", "send")
                                    Log.d("picture", (System.currentTimeMillis() - now).toString())
                                }
                            }
                        }
                    }
                }
            }*/