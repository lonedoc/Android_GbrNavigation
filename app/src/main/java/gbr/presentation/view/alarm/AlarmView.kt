package gbr.presentation.view.alarm

import moxy.MvpAppCompatFragment
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.AddToEndStrategy
import moxy.viewstate.strategy.StateStrategyType

interface AlarmView:MvpView {

    fun showToastMessage(message:String)

    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun startTimer(elapsedRealtime: Long)
    fun openFragment(fragment: MvpAppCompatFragment)

    fun showBottomBar(view: Int)
}