package gbr.presentation.presenter.mobalarm

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import gbr.presentation.view.mobalarm.MobAlarmView
import gbr.ui.main.MainActivity
import gbr.utils.api.alarm.AlarmAPI
import gbr.utils.api.alarm.OnAlarmListener
import gbr.utils.api.alarm.RPAlarmAPI
import gbr.utils.api.status.OnStatusListener
import gbr.utils.api.status.RPStatusAPI
import gbr.utils.api.status.StatusAPI
import gbr.utils.data.AlarmInfo
import gbr.utils.data.Info
import gbr.utils.servicess.ProtocolService.Companion.currentLocation
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.common.CurrentTime
import newVersion.models.CardEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rubegprotocol.RubegProtocol
import java.text.SimpleDateFormat
import java.util.*

@InjectViewState
class MobAlarmPresenter:MvpPresenter<MobAlarmView>(),OnAlarmListener,OnStatusListener {
    val info: Info = Info
    val alarmInfo: AlarmInfo = AlarmInfo
    private var statusAPI: StatusAPI? = null
    private var alarmAPI: AlarmAPI? = null
    lateinit var context: Context


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        val protocol = RubegProtocol.sharedInstance

        if(statusAPI!=null)
            statusAPI?.onDestroy()
        statusAPI = RPStatusAPI(protocol)
        statusAPI?.onStatusListener = this

        if(alarmAPI!=null)
            alarmAPI?.onDestroy()
        alarmAPI= RPAlarmAPI(protocol,"Alarm")
        alarmAPI?.onAlarmListener=this

        viewState.setTitle("Карточка объекта")

        viewState.statePhoto(false)
        viewState.stateArrived(false)
        viewState.stateReport(false)

        if(alarmInfo.lat=="0" && alarmInfo.lon =="0" || alarmInfo.lat==null && alarmInfo.lon==null)
        {
            viewState.showToastMessage("Нет координат объекта, автоприбытие отключено")
            viewState.showBottomBar(View.GONE)
            viewState.stateArrived(true)
        }
        else
        {
            arrivedLoop()
        }

        alarmApply()
    }

    fun context(context:Context){
        this.context = context
    }
    private fun alarmApply() {
        alarmAPI?.sendMobAlarmApplyRequest(
            alarmInfo.number!!,
            currentLocation!!.latitude,
            currentLocation!!.longitude,
            currentLocation!!.speed
        ) {
            if (it) {
                viewState.startTimer(SystemClock.elapsedRealtime())
                val currentTime: String = SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())
                viewState.showToastMessage("Тревога принята в $currentTime")
                EventBus.getDefault().postSticky(CurrentTime(currentTime))
            } else {
                viewState.showToastMessage("Не удалось отправить принятие, повторная отправка")
                alarmApply()
            }
        }
    }


    private fun arrivedLoop() {

    }

    private fun sendArrived() {
        alarmAPI?.sendMobArrivedObject(
            alarmInfo.number!!,
            currentLocation!!.latitude,
            currentLocation!!.longitude,
            currentLocation!!.speed,
        ){
            if(it)
            {
                viewState.showToastMessage("Прибытие отправлено")
                EventBus.getDefault().post(CardEvent("report"))
                viewState.stateArrived(false)
                viewState.stateReport(true)
            }
            else
            {
                viewState.showToastMessage("Не удалось отправить прибытие, повторная отправка")
                sendArrived()
            }
        }
    }

    override fun onAlarmDataReceived(flag: String, alarm: String) {
        when(flag){
            "notalarm"->{
                info.status("Свободен")
                // arrived = true
                AlarmInfo.clearData()
                viewState.showToastMessage("Тревога завершена")
            }
            "alarm"->{
                // arrived = true
                AlarmInfo.clearData()
                viewState.showToastMessage("Отмена тревоги, новая тревога")
            }
            "alarmmob"->{
                // arrived = true
                AlarmInfo.clearData()
                viewState.showToastMessage("Отмена тревоги, новая тревога на мобильном объекте")
            }
        }

        onDestroy()

        val main = Intent(context, MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(main)
    }

    override fun onStatusDataReceived(status: String, call: String) {
        if(status != "Свободен") return

        info.status(status)
        info.nameGBR(call)

        val main = Intent(context, MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(main)

        viewState.showToastMessage("Отмена тревоги смена статуса")
        onDestroy()
    }

    override fun onDestroy() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        alarmAPI?.onDestroy()
        statusAPI?.onDestroy()
        //arrived = true
        super.onDestroy()
    }
}