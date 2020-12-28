package gbr.presentation.view.objectinfo

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndStrategy
import moxy.viewstate.strategy.StateStrategyType

interface ObjectInfoView:MvpView {
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectName(name: String)
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectAddress(address: String)
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectNumber(number: String)
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectCustom(zakaz: String)
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectInn(inn: String)
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectAlarm(alarmName: String)
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectTimeAlarm(alarmTime: String)
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectAdditionally(additionally: String)
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectTimeApplyAlarm(currentTime: String)
    @StateStrategyType(value = AddToEndStrategy::class)
    fun setObjectTimeArrived(arrivedTime: String)
}