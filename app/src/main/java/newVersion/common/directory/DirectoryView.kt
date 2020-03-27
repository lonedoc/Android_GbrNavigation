package newVersion.common.directory

import gbr.utils.data.EsInfo
import gbr.utils.data.UsInfo
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

interface DirectoryView: MvpView {
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