package gbr.presentation.view.mobalarm

import moxy.MvpView

interface MobAlarmView:MvpView {
    fun showToastMessage(message: String)
    fun startTimer(elapsedRealtime: Long)
    fun showBottomBar(view: Int)
    fun setTitle(title: String)
}