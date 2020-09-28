package gbr.presentation.view.mobobjectinfo

import android.graphics.Bitmap
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

interface MobObjectInfoView:MvpView {
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setImage(bitmap: Bitmap)
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setPhone(phone: String?)
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setName(name: String?)
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setObjectTimeApplyAlarm(currentTime: String)
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setObjectTimeArrived(arrivedTime: String)

}