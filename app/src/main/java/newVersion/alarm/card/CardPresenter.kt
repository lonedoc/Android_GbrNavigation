package newVersion.alarm.card

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import newVersion.common.CurrentTime
import newVersion.commonInterface.Destroyable
import newVersion.commonInterface.Init
import newVersion.models.CardEvent
import newVersion.models.EnableButtons
import oldVersion.workservice.Alarm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

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

    override fun onDestroy() {
        super.onDestroy()
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    fun sendAction(action:String) {
        EventBus.getDefault().post(CardEvent(action = action))
    }


}