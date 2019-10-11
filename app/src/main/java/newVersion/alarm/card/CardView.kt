package newVersion.alarm.card

import com.arellomobile.mvp.MvpView

interface CardView : MvpView {
    fun setObjectName(name: String)
    fun setObjectAddress(address: String)
    fun setObjectCustom(customer: String)
    fun setObjectInn(inn: String)
    fun setObjectAlarm(alarm: String)
    fun setObjectTimeAlarm(timeAlarm: String)
    fun setObjectTimeApplyAlarm(timeApplyAlarm: String)
    fun setObjectTimeArrived(timeArrived: String)
    fun setObjectAdditionally(additionally: String)
    fun setObjectNumber(number: String)
    fun setStateReportButton(enable: Boolean)
    fun setStateArrivedButton(enable: Boolean)
}