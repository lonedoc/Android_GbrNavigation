package gbr.presentation.view.plan

import android.graphics.Bitmap
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndStrategy
import moxy.viewstate.strategy.StateStrategyType
import java.util.ArrayList

interface PlanView:MvpView {

    @StateStrategyType(value = AddToEndStrategy::class)
    fun initRecyclerView()

    fun addImageToRecyclerView()

}