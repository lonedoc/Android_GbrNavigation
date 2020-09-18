package gbr.presentation.presenter.objectinfo

import gbr.presentation.view.objectinfo.ObjectInfoView
import gbr.utils.api.alarm.AlarmAPI
import gbr.utils.api.alarm.RPAlarmAPI
import gbr.utils.data.AlarmInfo
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.common.CurrentTime
import newVersion.models.CardEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rubegprotocol.RubegProtocol

@InjectViewState
class ObjectInfoPresenter:MvpPresenter<ObjectInfoView>() {

    val alarmInfo = AlarmInfo

    var alarmAPI:AlarmAPI? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        val protocol  = RubegProtocol.sharedInstance
        if(alarmAPI!=null)
            alarmAPI?.onDestroy()

        alarmAPI = RPAlarmAPI(protocol = protocol)

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

        if(alarmInfo.lat=="0" && alarmInfo.lon =="0" || alarmInfo.lat==null && alarmInfo.lon==null)
        {
            viewState.setStateArrivedButton(true)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun takeApplyAlarmTime(time:CurrentTime){
        viewState.setObjectTimeApplyAlarm(time.currentTime)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun getEvent(event: CardEvent){
        when (event.action) {
            "report" -> {
                viewState.setStateReportButton(true)
            }
            "reportSend"->{
                viewState.setStateReportButton(false)
            }
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

    private fun setAlarmTimeApply(currentTime: String) {
        viewState.setObjectTimeApplyAlarm(currentTime)
    }

    private fun setTimeArrived(arrivedTime:String){
        viewState.setObjectTimeArrived(arrivedTime)
    }

    override fun onDestroy() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        super.onDestroy()
    }
}