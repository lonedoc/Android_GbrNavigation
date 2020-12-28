package gbr.presentation.view.main

import gbr.utils.data.AlarmInformation
import gbr.utils.data.StatusList
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.AddToEndStrategy
import org.osmdroid.util.GeoPoint
import java.util.ArrayList

interface MainView:MvpView {

    fun setTitle(title:String)

    @StateStrategyType(value = AddToEndStrategy::class)
    fun initMapView()

    @StateStrategyType(value = AddToEndStrategy::class)
    fun initOverlays()

    @StateStrategyType(value = AddToEndStrategy::class)
    fun fillStatusBar(statusList: ArrayList<StatusList>)

    @StateStrategyType(value = AddToEndStrategy::class)
    fun setCenter(center:GeoPoint)

    fun showToastMessage(message: String)

    @StateStrategyType(value =SkipStrategy::class)
    fun showStatusTimer()
    @StateStrategyType(value =SkipStrategy::class)
    fun showAlarmDialog(alarmInformation: AlarmInformation)
    @StateStrategyType(value =SkipStrategy::class)
    fun showMobAlarmDialog(alarmInformation: AlarmInformation)
    @StateStrategyType(value =SkipStrategy::class)
    fun cancelAlarm()
    @StateStrategyType(value =SkipStrategy::class)
    fun reopenActivity()
}