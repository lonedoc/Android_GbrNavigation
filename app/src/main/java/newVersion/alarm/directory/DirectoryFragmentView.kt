package newVersion.alarm.directory

import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import newVersion.utils.EsInfo
import newVersion.utils.UsInfo

interface DirectoryFragmentView: MvpView {
    @StateStrategyType(value = SkipStrategy::class)
    fun setTitle(title:String)
    @StateStrategyType(value = SkipStrategy::class)
    fun showToastMessage(message:String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setDistanceArrived(distance:String)
    @StateStrategyType(value = SkipStrategy::class)
    fun setPcsPhone(phone: String, state: Boolean)
    @StateStrategyType(value = SkipStrategy::class)
    fun setServicePhone(phone:String, state: Boolean)
    @StateStrategyType(value = SkipStrategy::class)
    fun initUSInfo(usInfo:ArrayList<UsInfo>)
    @StateStrategyType(value = SkipStrategy::class)
    fun initEsInfo(esInfo:ArrayList<EsInfo>)
}