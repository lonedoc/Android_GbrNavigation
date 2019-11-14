package newVersion.alarm.plan

import android.graphics.Bitmap

import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

interface PlanView : MvpView {
    @StateStrategyType(value = SkipStrategy::class)
    fun showToastMessage(message: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun addImageToRecyclerView()

    fun initRecyclerView(plan:ArrayList<Bitmap?>)
}