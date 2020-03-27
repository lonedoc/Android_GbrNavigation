package newVersion.common

import android.location.LocationManager
import gbr.utils.data.StatusList
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.utils.Alarm
import org.osmdroid.util.GeoPoint

interface CommonView:MvpView {

    @StateStrategyType(value = SkipStrategy::class)
    fun createNotification(command: String, status: String)

    fun fillStatusBar(statusList: ArrayList<StatusList>)

    @StateStrategyType(value = SkipStrategy::class)
    fun setTitle(title:String)

    @StateStrategyType(value = SkipStrategy::class)
    fun startService(credentials: Credentials, hostPool: HostPool)

    @StateStrategyType(value = SkipStrategy::class)
    fun showToastMessage(message:String)

    fun setCenter(geoPoint: GeoPoint)

    fun setCenter()

    @StateStrategyType(value = SkipStrategy::class)
    fun createStatusTimer(time: Long)

    @StateStrategyType(value = SkipStrategy::class)
    fun initMapView()
    @StateStrategyType(value = SkipStrategy::class)
    fun addOverlays()

/*    @StateStrategyType(value = SkipStrategy::class)
    fun scrollMap(location:GeoPoint)*/
}