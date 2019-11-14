package newVersion.alarm.card


import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

interface CardView : MvpView {
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectName(name: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectAddress(address: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectCustom(customer: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectInn(inn: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectAlarm(alarm: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectTimeAlarm(timeAlarm: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectTimeApplyAlarm(timeApplyAlarm: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectTimeArrived(timeArrived: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectAdditionally(additionally: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setObjectNumber(number: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setStateReportButton(enable: Boolean)
    @StateStrategyType(value = SkipStrategy::class)
    fun setStateArrivedButton(enable: Boolean)
}