package gbr.presentation.presenter.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import gbr.presentation.view.main.MainView
import gbr.ui.main.MainActivity
import gbr.utils.api.alarm.AlarmAPI
import gbr.utils.api.alarm.OnAlarmListener
import gbr.utils.api.alarm.RPAlarmAPI
import gbr.utils.api.status.OnStatusListener
import gbr.utils.api.status.RPStatusAPI
import gbr.utils.api.status.StatusAPI
import gbr.utils.data.AlarmInfo
import gbr.utils.data.AlarmInformation
import gbr.utils.data.Info
import gbr.utils.data.StatusList
import kobramob.rubeg38.ru.gbrnavigation.BuildConfig
import moxy.InjectViewState
import moxy.MvpPresenter

import org.osmdroid.util.GeoPoint
import rubegprotocol.RubegProtocol

@InjectViewState
class MainPresenter:MvpPresenter<MainView>(),OnStatusListener,OnAlarmListener {

    val info:Info = Info
    private var statusAPI:StatusAPI? = null
    private var alarmAPI:AlarmAPI?=null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        val protocol = RubegProtocol.sharedInstance


        if(statusAPI!=null)
            statusAPI?.onDestroy()

        statusAPI = RPStatusAPI(protocol)
        statusAPI?.onStatusListener = this


        if(alarmAPI!=null)
            alarmAPI?.onDestroy()

        alarmAPI = RPAlarmAPI(protocol,"Main")
        alarmAPI?.onAlarmListener = this

        viewState.initMapView()
        viewState.initOverlays()

        if(info.statusList == null)
        {
            viewState.showToastMessage("Нет списка статусов")
        }
        else
        {
            val newStatusList:ArrayList<StatusList> = ArrayList()

            for(i in 0 until info.statusList!!.count())
            {
                if(info.statusList!![i].status != info.status && info.statusList!![i].status != "На тревоге")
                    newStatusList.add(info.statusList!![i])
            }
            viewState.fillStatusBar(newStatusList)
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

    fun setTitle() {
        val title = if(info.call == null)
            "Группа не подставлена на дежурство"
        else
            "${info.call} (${info.status}) v.${BuildConfig.VERSION_NAME}"

        for(i in 0 until info.statusList!!.count())
        {
            if(info.statusList!![i].status == info.status && info.statusList!![i].time != "0"){
                viewState.showStatusTimer()
                break
            }
        }

        viewState.setTitle(title)
    }

    fun setCenter(lastFix: GeoPoint?) {
        if(lastFix!=null)
        viewState.setCenter(lastFix)
        else
        {
            viewState.showToastMessage("Не удалось определить ваше месторасположение")
        }

    }

    fun sendStatusChangeRequest(status: String) {
        statusAPI?.sendStatusRequest(
            status,
            complete = { success ->
                if (!success) {
                    viewState.showToastMessage("Запрос на смену статуса не был отправлен, запрос будет отправлен повторно")
                    sendStatusChangeRequest(status)
                }
            }
        )
    }

    override fun onStatusDataReceived(status: String, call: String) {
        if (status == info.status) return

        info.status(status)
        info.call(call)

        val title = "$call (${status}) v.${BuildConfig.VERSION_NAME}"
        viewState.setTitle(title)

        val newStatusList:ArrayList<StatusList> = ArrayList()

        for(i in 0 until info.statusList!!.count())
        {
            if(info.statusList!![i].status != info.status && info.statusList!![i].status != "На тревоге")
                newStatusList.add(info.statusList!![i])
        }
        viewState.fillStatusBar(newStatusList)

        for(i in 0 until info.statusList!!.count())
        {
            if(info.statusList!![i].status == status && info.statusList!![i].time != "0"){
                viewState.showStatusTimer()
                break
            }
        }
    }

    fun checkAlarm(){
        info.nameGBR?.let {
            alarmAPI!!.sendAlarmRequest(it){acknowledgement:Boolean->
                if (!acknowledgement){
                    checkAlarm()
                }
            }
        }
    }

    override fun onAlarmDataReceived(flag: String, alarm: String) {
            when{
                !MainActivity.isAlive->{
                    info.status("На тревоге")
                    val main = Intent(context,MainActivity::class.java)
                    main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(main)
                    onDestroy()
                }
                flag == "notalarm"->{
                    viewState.cancelAlarm()
                }
                flag == "alarm"->{
                    val alarmInfo = Gson().fromJson(alarm, AlarmInformation::class.java)
                    AlarmInfo.initAllData(alarmInfo)
                    if(alarmInfo.name == null) return

                    viewState.showAlarmDialog(alarmInfo)
                }
                flag == "alarmmob"->{
                    val alarmInfo = Gson().fromJson(alarm, AlarmInformation::class.java)
                    AlarmInfo.initAllData(alarmInfo)
                    if(alarmInfo.name == null) return

                    viewState.showMobAlarmDialog(alarmInfo)
                }
            }
    }

    override fun onDestroy() {
        if(alarmAPI!=null)
            alarmAPI!!.onDestroy()

        if(statusAPI!=null)
            statusAPI!!.onDestroy()

        super.onDestroy()
    }

    lateinit var context:Context
    fun context(applicationContext: Context?) {
        this.context = applicationContext!!
    }
}