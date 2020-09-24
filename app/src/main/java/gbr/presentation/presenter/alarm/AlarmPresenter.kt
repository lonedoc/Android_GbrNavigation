package gbr.presentation.presenter.alarm

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.View
import gbr.presentation.view.alarm.AlarmView
import gbr.ui.alarm.AlarmActivity
import gbr.ui.main.MainActivity
import gbr.ui.pager.AlarmTabFragment
import gbr.utils.api.alarm.AlarmAPI
import gbr.utils.api.alarm.OnAlarmListener
import gbr.utils.api.alarm.RPAlarmAPI
import gbr.utils.api.status.OnStatusListener
import gbr.utils.api.status.RPStatusAPI
import gbr.utils.api.status.StatusAPI
import gbr.utils.data.AlarmInfo
import gbr.utils.data.AlarmInformation
import gbr.utils.data.Info
import gbr.utils.data.ProtocolServiceInfo
import gbr.utils.servicess.ProtocolService
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.common.CurrentTime
import newVersion.models.CardEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.util.GeoPoint
import rubegprotocol.RubegProtocol
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

@InjectViewState
class AlarmPresenter: MvpPresenter<AlarmView>(),OnStatusListener,OnAlarmListener {

    val info: Info = Info
    val alarmInfo:AlarmInfo = AlarmInfo
    private var statusAPI: StatusAPI? = null
    private var alarmAPI: AlarmAPI?=null
    lateinit var context: Context

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()


        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        val protocol = RubegProtocol.sharedInstance

        if(statusAPI!=null)
            statusAPI?.onDestroy()
        statusAPI = RPStatusAPI(protocol)
        statusAPI?.onStatusListener = this

        if(alarmAPI!=null)
            alarmAPI?.onDestroy()
        alarmAPI=RPAlarmAPI(protocol,"Alarm")
        alarmAPI?.onAlarmListener=this

        viewState.setTitle("Карточка объекта")

        if(alarmInfo.lat=="0" && alarmInfo.lon =="0" || alarmInfo.lat==null && alarmInfo.lon==null)
        {
            viewState.showToastMessage("Нет координат объекта, автоприбытие отключено")
            viewState.showBottomBar(View.GONE)
        }
        else
        {
            arrivedLoop()
        }

        alarmApply()
    }

    var arrived = false
    private fun arrivedLoop() {
        thread {
            while(!arrived)
            {
                val myCoordinate = ProtocolService.coordinate
                sleep(3000)

                Log.d("Loop","Worked")
                if(myCoordinate==null) continue

                if(alarmInfo.lat==null || alarmInfo.lon == null) continue

                if(myCoordinate.lat==0.toDouble() && myCoordinate.lon==0.toDouble()) continue

                val distance = 100

                val endPoint = GeoPoint(alarmInfo.lat!!.toDouble(),alarmInfo.lon!!.toDouble())

                Log.d("Loop","${endPoint.distanceToAsDouble(GeoPoint(myCoordinate.lat,myCoordinate.lon))}")
                if(endPoint.distanceToAsDouble(GeoPoint(myCoordinate.lat,myCoordinate.lon))>distance) continue

                viewState.showToastMessage("Вы прибыли на место")

                arrived = true

                sendArrived()
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun getEvent(event: CardEvent){
        when (event.action) {
            "Arrived" -> {
                sendArrived()
            }
            "Report" -> {

            }
        }
    }

    private fun alarmApply() {
        alarmAPI?.sendAlarmApplyRequest(alarmInfo.number!!){
            if(it){
                viewState.startTimer(SystemClock.elapsedRealtime())
                val currentTime: String = SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())
                viewState.showToastMessage("Тревога принята в $currentTime")
                EventBus.getDefault().postSticky(CurrentTime(currentTime))
            }
            else{
                viewState.showToastMessage("Не удалось отправить принятие, повторная отправка")
                alarmApply()
            }
        }
    }

    private fun sendArrived(){
        alarmAPI?.sendArrivedObject(alarmInfo.number!!){
            if(it)
            {
                EventBus.getDefault().post(CardEvent("report"))
            }
            else
            {
                viewState.showToastMessage("Не удалось отправить прибытие, повторная отправка")
                sendArrived()
            }
        }
    }

    fun sendReports(report:String,comment:String){
        alarmAPI?.sendReport(report,comment,info.nameGBR!!,alarmInfo.name!!,alarmInfo.number!!){
            if(it){
                EventBus.getDefault().post(CardEvent("reportSend"))
            }
            else
            {
                viewState.showToastMessage("Не удалось отправить рапорт, повторная отправка")
                sendReports(report,comment)
            }
        }
    }

    override fun onStatusDataReceived(status: String, call: String) {
        if(status != "Свободен") return

        info.status(status)
        info.nameGBR(call)

        val main = Intent(context,MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(main)

        viewState.showToastMessage("Отмена тревоги смена статуса")
        onDestroy()
    }

    override fun onAlarmDataReceived(flag: String, alarm: String) {

        when(flag){
            "notalarm"->{
                info.status("Свободен")
                arrived = true
                AlarmInfo.clearData()
                viewState.showToastMessage("Тревога завершена")
            }
            "alarm"->{
                arrived = true
                AlarmInfo.clearData()
                viewState.showToastMessage("Отмена тревоги, новая тревога")
            }
            "alarmmob"->{
                arrived = true
                AlarmInfo.clearData()
                viewState.showToastMessage("Отмена тревоги, новая тревога на мобильном объекте")
            }
        }

        onDestroy()

        val main = Intent(context,MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(main)
    }

    fun context(applicationContext: Context?) {
        this.context = applicationContext!!
    }

    override fun onDestroy() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        alarmAPI?.onDestroy()
        statusAPI?.onDestroy()
        arrived = true
        super.onDestroy()
    }
}