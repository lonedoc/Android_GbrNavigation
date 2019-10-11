package newVersion.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import newVersion.Utils.DataStoreUtils
import newVersion.common.alarm.AlarmDialogFragment
import newVersion.commonInterface.Destroyable
import newVersion.commonInterface.Init
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.network.alarm.AlarmAPI
import newVersion.network.alarm.OnAlarmListener
import newVersion.network.alarm.RPAlarmAPI
import newVersion.network.status.OnStatusListener
import newVersion.network.status.RPStatusAPI
import newVersion.network.status.StatusAPI
import newVersion.servicess.NetworkService
import oldVersion.workservice.Alarm
import oldVersion.workservice.GpsStatus
import org.greenrobot.eventbus.EventBus
import org.osmdroid.util.GeoPoint
import rubegprotocol.RubegProtocol

@InjectViewState
class CommonPresenter : MvpPresenter<CommonView>(), OnStatusListener, OnAlarmListener, Init, Destroyable {

    private var context: Context? = null

    fun getContext(context: Context?) {
        this.context = context
    }

    override fun onAlarmDataReceived(alarm: Alarm) {
        Log.d("CommonPresenter", "$alarm")
        sleep(2000)
        if (!AlarmDialogFragment.isAlive)
            if (CommonActivity.isAlive)
                viewState.openAlarmDialog(alarm)
            else {
                val unsleepActivity = Intent(context, CommonActivity::class.java)
                unsleepActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                unsleepActivity.putExtra("activeSound", false)
                context?.startActivity(unsleepActivity)
            }
    }

    fun sendAlarmRequest(namegbr: String) {
        if (waitApply) return
        alarmApi?.sendAlarmRequest(
            namegbr,
            complete = {
                if (!it) {
                    viewState.showToastMessage("Проверка наличия тревоги не удалась")
                } else {
                    viewState.showToastMessage("Проверка наличия тревоги")
                }
            }
        )
    }

    fun sendAlarmApplyRequest(alarm: Alarm) {
        waitApply = true
        alarmApi?.sendAlarmApplyRequest(
            alarm.number!!,
            complete = {
                if (it) {
                    val currentTime: String = SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())
                    viewState.showToastMessage("Тревога принята в $currentTime")
                    EventBus.getDefault().postSticky(CurrentTime(currentTime))
                    waitApply = false
                    onDestroy()
                } else {
                    viewState.showToastMessage("Во время отправки сообщения о принятие тревоги произошел сбой, сообщение будет отправлено еще раз")
                    sendAlarmApplyRequest(alarm)
                }
            }
        )
    }

    private var statusAPI: StatusAPI? = null
    private var alarmApi: AlarmAPI? = null
    override var init: Boolean = false

    private var waitApply = false
    override fun isInit(): Boolean {
        return init
    }

    override fun onStatusDataReceived(status: String, call: String) {
        if (DataStoreUtils.status != status) {
            DataStoreUtils.status = status
            DataStoreUtils.call = call
            fillStatusBar()
            val statusList = DataStoreUtils.statusList
            setTitle()
            viewState.createNotification("gbrstatus", status)

            for (i in 0 until DataStoreUtils.statusList.count()) {
                if (statusList[i].name == status && statusList[i].time != "0") {
                    viewState.createStatusTimer(statusList[i].time.toLong())
                }
            }
        }
    }

    fun sendStatusRequest(status: String) {
        statusAPI?.sendStatusRequest(
            status,
            complete = { success ->
                if (!success) {
                    viewState.showToastMessage("Запрос на смену статуса не был отправлен")
                }
            }
        )
    }

    fun fillStatusBar() {
        val statusList: ArrayList<GpsStatus> = ArrayList()
        for (i in 0 until DataStoreUtils.statusList.count()) {
            if (DataStoreUtils.statusList[i].name != "На тревоге" && DataStoreUtils.statusList[i].name != DataStoreUtils.status) {
                statusList.add(DataStoreUtils.statusList[i])
            }
        }
        viewState.fillStatusBar(statusList)
    }

    fun init(preferences: newVersion.models.Preferences?) {

        init = true
        Log.d("CommonPresenter", "Init")
        val addresses = preferences?.serverAddress
        val port = preferences?.serverPort
        val imei = preferences?.imei
        val fcmtoken = preferences?.fcmtoken

        val credentials = Credentials(
            imei.toString(),
            fcmtoken!!
        )

        val hostPool = HostPool(
            addresses = addresses!!,
            port = port!!.toInt()
        )

        if (!NetworkService.isServiceStarted) {
            viewState.startService(credentials, hostPool)
        }

        viewState.initMapView()

        viewState.addOverlays()

        val protocol = RubegProtocol.sharedInstance
        if (statusAPI != null) statusAPI?.onDestroy()
        statusAPI = RPStatusAPI(protocol)
        statusAPI?.onStatusListener = this

        if (alarmApi != null) alarmApi?.onDestroy()
        alarmApi = RPAlarmAPI(protocol)
        alarmApi?.onAlarmListener = this
    }

    fun setTitle() {
        if (DataStoreUtils.call != "") {
            viewState.setTitle("${DataStoreUtils.call} ( ${DataStoreUtils.status} ) ver. ${BuildConfig.VERSION_NAME}")
        } else {
            viewState.setTitle("Группа не поставлена на дежурство")
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

    fun setCenter(imHere: Location?) {
        if (imHere != null) {
            viewState.setCenter(GeoPoint(imHere))
        } else {
            viewState.showToastMessage("Ваше месторасположение не определено")
            thread {
                sleep(5000)
                viewState.setCenterLoop()
            }
        }
    }

    private var stateGpsCheckStart = false
    fun stateGpsCheck(locationManager: LocationManager) {
        thread {
            Log.d("StateGpsCheck", "$stateGpsCheckStart")
            if (stateGpsCheckStart) return@thread
            stateGpsCheckStart = true
            while (stateGpsCheckStart) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    stateGpsCheckStart = false
                    if (CommonActivity.isAlive)
                        viewState.createSettingGpsDialog()
                    return@thread
                }
            }
        }
    }

    override fun onDestroy() {
        statusAPI?.onDestroy()
        alarmApi?.onDestroy()
        init = false
        super.onDestroy()
    }
}

data class CurrentTime(
    var currentTime: String
)
