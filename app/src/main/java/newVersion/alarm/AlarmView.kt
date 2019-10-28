package newVersion.alarm

import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import newVersion.utils.Alarm

interface AlarmView : MvpView {

    fun setTitle(title: String)
    fun openFragment(fragment: MvpAppCompatFragment)
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