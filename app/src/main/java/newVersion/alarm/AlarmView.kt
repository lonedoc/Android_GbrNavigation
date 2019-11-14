package newVersion.alarm

import moxy.MvpAppCompatFragment
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import newVersion.utils.Alarm

interface AlarmView : MvpView {
    @StateStrategyType(value = SkipStrategy::class)
    fun removeData()
    @StateStrategyType(value = SkipStrategy::class)
    fun setTitle(title: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun openFragment(fragment: MvpAppCompatFragment)
    @StateStrategyType(value = SkipStrategy::class)
    fun startTimer()

    @StateStrategyType(value = SkipStrategy::class)
    fun completeAlarm(alarm:Alarm?)

    @StateStrategyType(value = SkipStrategy::class)
    fun showToastMessage(message: String)

    @StateStrategyType(value = SkipStrategy::class)
    fun showArrivedDialog()

    @StateStrategyType(value = SkipStrategy::class)
    fun showReportDialog()

}