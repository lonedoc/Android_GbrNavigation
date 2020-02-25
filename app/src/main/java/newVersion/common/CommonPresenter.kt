package newVersion.common

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.common.alarm.AlarmDialogActivity
import newVersion.commonInterface.Init
import newVersion.models.HostPool
import newVersion.network.alarm.AlarmAPI
import newVersion.network.alarm.OnAlarmListener
import newVersion.network.alarm.RPAlarmAPI
import newVersion.network.status.OnStatusListener
import newVersion.network.status.RPStatusAPI
import newVersion.network.status.StatusAPI
import newVersion.servicess.NetworkService
import newVersion.utils.*
import newVersion.utils.DataStoreUtils.statusList
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.util.GeoPoint
import rubegprotocol.RubegProtocol
import java.lang.Thread.sleep
import kotlin.concurrent.thread

@InjectViewState
class NewCommonPresenter: MvpPresenter<CommonView>(),Init,OnStatusListener,OnAlarmListener {
    override var init:Boolean = false

    private var statusAPI: StatusAPI? = null
    private var alarmApi: AlarmAPI? = null

    private var context:Context? = null

    override fun onStatusDataReceived(status: String, call: String) {
        if(DataStoreUtils.status == status) return

        DataStoreUtils.status = status
        DataStoreUtils.call = call
        fillStatusBar()
        setTitle()

        for (i in 0 until statusList.count()) {
            if (statusList[i].name == status && statusList[i].time != "0") {
                viewState.createStatusTimer(statusList[i].time.toLong())
            }
        }

        viewState.createNotification("gbrstatus", status)
        if(status == "На тревоге")
        {
           onDestroy()
        }
    }

    private fun fillStatusBar() {
        val statusList: ArrayList<GpsStatus> = ArrayList()

        for (i in 0 until DataStoreUtils.statusList.count()) {
            if (DataStoreUtils.statusList[i].name != "На тревоге" && DataStoreUtils.statusList[i].name != DataStoreUtils.status) {
                statusList.add(DataStoreUtils.statusList[i])
            }
        }

        viewState.fillStatusBar(statusList)
    }

    fun sendStatusRequest(status: String) {
        statusAPI?.sendStatusRequest(
            status,
            complete = { success ->
                if (!success) {
                    viewState.showToastMessage("Запрос на смену статуса не был отправлен, запрос будет отправлен повторно")
                    sendStatusRequest(status)
                }
            }
        )
    }

    override fun onAlarmDataReceived(alarm: Alarm) {
        alarmApi?.onDestroy()

        val activity = Intent(context, AlarmDialogActivity::class.java)
        activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.putExtra("info",alarm)
        context?.startActivity(activity)
    }

    fun alarmCheck(namegbr: String) {
        alarmApi?.sendAlarmRequest(
            namegbr,
            complete = {
                if(!it){
                    viewState.showToastMessage("Проверка наличия тревоги не удалось, повторная отправка")
                    alarmCheck(namegbr)
                }
            }
        )
    }

    override fun isInit():Boolean{
        return init
    }



    fun init(){
        setTitle()
        fillStatusBar()
        viewState.initMapView()
        viewState.addOverlays()
    }

    fun initData(
        preferences: PrefsUtil,
        context: Context
    ) {
        if(isInit()) return

        init()

        init = true
        this.context = context


        val credentials = newVersion.models.Credentials(
            preferences.imei.toString(),
            preferences.fcmtoken!!
        )

        val hostPool = HostPool(
            preferences.serverAddress,
            preferences.serverPort
        )

        if(!NetworkService.isServiceStarted)
            viewState.startService(credentials,hostPool)

        val protocol = RubegProtocol.sharedInstance

        if(statusAPI != null) statusAPI?.onDestroy()
        statusAPI = RPStatusAPI(protocol)
        statusAPI?.onStatusListener = this
        sleep(100)

        if(alarmApi != null) alarmApi?.onDestroy()
        alarmApi = RPAlarmAPI(protocol)
        alarmApi?.onAlarmListener = this
        sleep(100)

        alarmCheck(DataStoreUtils.namegbr!!)

        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onEnableLocation(event: providerStatus){
        Log.d("CommonPresenter", event.status)
        when(event.status)
        {
            "disable"->{
                viewState.showToastMessage("GPS был отключен")
                AlertDialog.Builder(context)
                    .setMessage("GPS отключен (Приложение не работает без GPS)")
                    .setCancelable(false)
                    .setPositiveButton("Включить"){
                            _,_ ->
                        context?.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .create()
                    .show()
            }
            "enable"->{
                viewState.setCenter()
            }
            "notInitialized"->{
                viewState.showToastMessage("Ваше месторасположение не определено")
            }
        }

    }

   /* @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun getArrived(event:location) {
        viewState.scrollMap(GeoPoint(event.lat,event.lon))
    }*/
    fun setTitle() {
        if (DataStoreUtils.call != "") {
            viewState.setTitle("${DataStoreUtils.call} ( ${DataStoreUtils.status} ) ver. ${BuildConfig.VERSION_NAME}")
        } else {
            viewState.setTitle("Группа не поставлена на дежурство")
        }
    }

    override fun onDestroy() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        init = false
        if(alarmApi != null) alarmApi?.onDestroy()
        if(statusAPI != null) statusAPI?.onDestroy()
        super.onDestroy()
    }

    fun setCenter(imHere: Location?) {
        if(imHere!=null){
            viewState.setCenter(GeoPoint(imHere))
        }
        else
        {
            viewState.showToastMessage("Ваше месторасположение не определено")
        }
    }

    fun customIcon(drawable: Int, context: Context): Bitmap? {

        val drawableBitmap = ContextCompat.getDrawable(context, drawable)
        val bitmap = Bitmap.createBitmap(
            drawableBitmap?.intrinsicWidth!!,
            drawableBitmap.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawableBitmap.setBounds(0, 0, canvas.width, canvas.height)
        drawableBitmap.draw(canvas)

        return bitmap
    }

}
data class CurrentTime(
    var currentTime: String
)