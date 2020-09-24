package gbr.presentation.view.objectinfo

import moxy.MvpView

interface ObjectInfoView:MvpView {
    fun setObjectName(name: String)
    fun setObjectAddress(address: String)
    fun setObjectNumber(number: String)
    fun setObjectCustom(zakaz: String)
    fun setObjectInn(inn: String)
    fun setObjectAlarm(alarmName: String)
    fun setObjectTimeAlarm(alarmTime: String)
    fun setObjectAdditionally(additionally: String)
    fun setObjectTimeApplyAlarm(currentTime: String)
    fun setObjectTimeArrived(arrivedTime: String)
}