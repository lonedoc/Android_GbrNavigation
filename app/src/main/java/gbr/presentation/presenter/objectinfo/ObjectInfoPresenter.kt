package gbr.presentation.presenter.objectinfo

import gbr.presentation.presenter.alarm.AlarmPresenter
import gbr.presentation.view.objectinfo.ObjectInfoView
import gbr.utils.data.AlarmInfo
import gbr.utils.data.CurrentTime
import moxy.InjectViewState
import moxy.MvpPresenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@InjectViewState
class ObjectInfoPresenter:MvpPresenter<ObjectInfoView>() {

    val alarmInfo = AlarmInfo


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        setName(alarmInfo.name)
        setAddress(alarmInfo.address)
        setNumber(alarmInfo.number)
        setCustomer(alarmInfo.zakaz)
        setInn(alarmInfo.inn)
        setAlarm(alarmInfo.area?.name)
        setAlarmTime(alarmInfo.area?.alarmtime)
        setAdditionally(alarmInfo.additionally)

    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun takeApplyAlarmTime(time: CurrentTime){
        viewState.setObjectTimeApplyAlarm(time.currentTime)
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun takeArrivedTime(time: AlarmPresenter.ArrivedTime){
        viewState.setObjectTimeArrived(time.arrivedTime)
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

    private fun setNumber(number: String?) {
        if (number != null) {
            viewState.setObjectNumber(number)
        } else {
            viewState.setObjectNumber("")
        }
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
    private fun setAdditionally(additionally: String?) {
        if(additionally!=null){
            viewState.setObjectAdditionally(additionally)
        }
        else
        {
            viewState.setObjectAdditionally("")
        }
    }

    override fun onDestroy() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        super.onDestroy()
    }
}