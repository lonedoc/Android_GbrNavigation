package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.AlarmObjectInfo
import kobramob.rubeg38.ru.gbrnavigation.workservice.MessageEvent
import kobramob.rubeg38.ru.gbrnavigation.workservice.RubegNetworkService
import kotlin.concurrent.thread
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class PlanFragment : androidx.fragment.app.Fragment() {

    companion object {
        val bitmapList: ArrayList<Bitmap> = ArrayList()
        var countInQueue: Int = 0
    }

    private var rootView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.plan_fragment, container, false)

        return rootView
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 2)
    fun onMessageEvent(event: MessageEvent) {
        if (event.byteArray.count()> 0 && event.command == "getfile") {
            val image: Bitmap = BitmapFactory.decodeByteArray(event.byteArray, 0, event.byteArray.count())

            if(!bitmapList.contains(image))
            bitmapList.add(image)
        }
    }

    var waitDownload = true
    override fun onResume() {
        super.onResume()
        val progressBar: ProgressBar = rootView!!.findViewById(R.id.plan_download)
        progressBar.visibility = View.VISIBLE

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        val alarmObjectInfo = activity!!.intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

        if (alarmObjectInfo.planAndPhotoList.count() != 0 && bitmapList.count() == 0 && countInQueue != alarmObjectInfo.planAndPhotoList.count()) {
            when {
                !RubegNetworkService.connectInternet -> {
                    Toast.makeText(
                        context,
                        "Нет соединения с интернетом, невозможно скачать изображения",
                        Toast.LENGTH_LONG
                    ).show()
                }
                !RubegNetworkService.connectServer -> {
                    Toast.makeText(
                        context,
                        "Нет соединения с сервером, невозможно скачать изображения",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {

                    for (i in countInQueue until alarmObjectInfo.planAndPhotoList.count()) {
                        val downloadImage = JSONObject()
                        downloadImage.put("\$c$", "getfile")
                        downloadImage.put("nameinserv", alarmObjectInfo.planAndPhotoList[i])
                        downloadImage.put("name", alarmObjectInfo.planAndPhotoList[i])
                        thread {
                            RubegNetworkService.protocol.send(downloadImage.toString()) { success: Boolean ->
                                if (success) {
                                    waitDownload = false
                                    Log.d("getfile", "waitserver")
                                } else {
                                    activity!!.runOnUiThread {
                                        Toast.makeText(
                                            activity!!,
                                            "Не удалось загрузить изображение ${alarmObjectInfo.planAndPhotoList[i]}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            countInQueue++
                            while (waitDownload) {
                            }
                            waitDownload = true
                        }
                    }
                    thread {
                        while (bitmapList.count() != alarmObjectInfo.planAndPhotoList.count()) {
                        }
                        activity!!.runOnUiThread {

                            val planList: RecyclerView = rootView!!.findViewById(R.id.imageRecyclerView)
                            progressBar.visibility = View.GONE
                            planList.visibility = View.VISIBLE
                            planList.layoutManager = LinearLayoutManager(activity)
                            planList.adapter = AdapterPlanList(bitmapList, context!!)

                            planList.addItemDecoration(
                                DividerItemDecoration(
                                    planList.context,
                                    DividerItemDecoration.VERTICAL
                                )
                            )
                            EventBus.getDefault().unregister(this)
                        }
                    }
                }
            }
        } else {
            activity!!.runOnUiThread {

                val planList: RecyclerView = rootView!!.findViewById(R.id.imageRecyclerView)
                progressBar.visibility = View.GONE
                planList.visibility = View.VISIBLE
                planList.layoutManager = LinearLayoutManager(activity)
                planList.adapter = AdapterPlanList(bitmapList, context!!)

                planList.addItemDecoration(
                    DividerItemDecoration(
                        planList.context,
                        DividerItemDecoration.VERTICAL
                    )
                )
            }
            EventBus.getDefault().unregister(this)
        }
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