package newVersion.alarm.plan

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

interface PlanView : MvpView {
    @StateStrategyType(value = SkipStrategy::class)
    fun showToastMessage(message: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun addImageToRecyclerView()

    fun initRecyclerView(plan:ArrayList<Bitmap?>)
}