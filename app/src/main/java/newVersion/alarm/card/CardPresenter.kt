package newVersion.alarm.card

import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.common.CurrentTime
import newVersion.commonInterface.Destroyable
import newVersion.commonInterface.Init
import newVersion.models.CardEvent
import newVersion.models.EnableButtons
import newVersion.utils.Alarm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

@InjectViewState
class CardPresenter : MvpPresenter<CardView>(), Destroyable, Init {

    override var init: Boolean = false
    override fun isInit(): Boolean {
        return init
    }

    fun init(info: Alarm) {
        setName(info.name)
        setAddress(info.address)
        setNumber(info.number)
        setCustomer(info.zakaz)
        setInn(info.inn)
        setAlarm(info.area?.name)
        setAlarmTime(info.area?.alarmtime)
        setAdditionally(info.additionally)
        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this)
    }



    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun enableButtons(event: EnableButtons) {
        viewState.setStateReportButton(event.enableReport)
        viewState.setStateArrivedButton(event.enableArrived)
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun getCurrentTime(event: CurrentTime){
        setAlarmTimeApply(event.currentTime)
    }

    private fun setAdditionally(additionally: String?) {
        if(additionally!=null){
            viewState.setObjectAdditionally(additionally)
        }
        else
        {
            viewState.setObjectAdditionally("")
        }
    }

    private fun setNumber(number: String?) {
        if (number != null) {
            viewState.setObjectNumber(number)
        } else {
            viewState.setObjectNumber("")
        }
    }
    private fun setName(name: String?) {
        if (name != null) {
            viewState.setObjectName(name)
        } else
            viewState.setObjectName("")
    }
    private fun setAddress(address: String?) {
        if (address != null)
            viewState.setObjectAddress(address)
        else
            viewState.setObjectAddress("")
    }
    private fun setCustomer(zakaz: String?) {
        if (zakaz != null)
            viewState.setObjectCustom(zakaz)
        else
            viewState.setObjectCustom("")
    }
    private fun setInn(inn: String?) {
        if (inn != null)
            viewState.setObjectInn(inn)
        else
            viewState.setObjectInn("")
    }
    private fun setAlarm(alarmName: String?) {
        if (alarmName != null)
            viewState.setObjectAlarm(alarmName)
        else
            viewState.setObjectAlarm("")
    }
    private fun setAlarmTime(alarmTime: String?) {
        if (alarmTime != null)
            viewState.setObjectTimeAlarm(alarmTime)
        else
            viewState.setObjectTimeAlarm("")
    }
    private fun setAlarmTimeApply(currentTime: String) {
        viewState.setObjectTimeApplyAlarm(currentTime)
    }

    private fun setTimeArrived(arrivedTime:String){
        viewState.setObjectTimeArrived(arrivedTime)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    fun sendAction(action:String) {
        EventBus.getDefault().post(CardEvent(action = action))
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun timeArrived(event:ArrivedTime){
        val currentTime: String = SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        val hours = event.arrivedTime / 3600
        val minute = (event.arrivedTime  % 3600) / 60
        val seconds = event.arrivedTime  % 60
        setTimeArrived("$currentTime ($hours:$minute:$seconds)")
    }

}

data class ArrivedTime(var arrivedTime: Int)
