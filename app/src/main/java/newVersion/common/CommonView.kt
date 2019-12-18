package newVersion.common

import android.location.LocationManager
import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.utils.Alarm
import newVersion.utils.GpsStatus
import org.osmdroid.util.GeoPoint

interface CommonView:MvpView {

    @StateStrategyType(value = SkipStrategy::class)
    fun createNotification(command: String, status: String)

    fun fillStatusBar(statusList: ArrayList<GpsStatus>)

    @StateStrategyType(value = SkipStrategy::class)
    fun setTitle(title:String)

    @StateStrategyType(value = SkipStrategy::class)
    fun callSettingGPS(locationManager: LocationManager)

    @StateStrategyType(value = SkipStrategy::class)
    fun startService(credentials: Credentials, hostPool: HostPool)

    @StateStrategyType(value = SkipStrategy::class)
    fun showToastMessage(message:String)

    fun setCenter(geoPoint: GeoPoint)

    @StateStrategyType(value = SkipStrategy::class)
    fun waitCoordinate()

    @StateStrategyType(value = SkipStrategy::class)
    fun createStatusTimer(time: Long)


    fun initMapView()

    fun addOverlays()
}