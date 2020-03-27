package gbr.presentation.view.start

import android.location.LocationManager
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType

interface StartView:MvpView {

    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun setText(message: String)

    @StateStrategyType(value =SkipStrategy::class)
    fun checkPermission()

    @StateStrategyType(value = SkipStrategy::class)
    fun whatNew()

    @StateStrategyType(value = SkipStrategy::class)
    fun gpsSetting(message: String, locationManager: LocationManager)

    @StateStrategyType(value = SkipStrategy::class)
    fun errorPermissionDialog(errorMessage: String)

    @StateStrategyType(value = SingleStateStrategy::class)
    fun loginActivity()

    fun startService()

}