package newVersion.common

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import newVersion.models.Credentials
import newVersion.models.HostPool
import oldVersion.workservice.Alarm
import oldVersion.workservice.GpsStatus
import org.osmdroid.util.GeoPoint

interface CommonView : MvpView {

    fun setTitle(title: String)

    @StateStrategyType(value = SkipStrategy::class)
    fun showToastMessage(message: String)

    fun initMapView()
    fun addOverlays()
    fun setCenter(geoPoint: GeoPoint)
    fun setCenterLoop()
    fun fillStatusBar(statusList: ArrayList<GpsStatus>)

    @StateStrategyType(value = SkipStrategy::class)
    fun openAlarmDialog(alarm: Alarm)
    @StateStrategyType(value = SkipStrategy::class)
    fun createNotification(command: String, status: String)
    @StateStrategyType(value = SkipStrategy::class)
    fun createSettingGpsDialog()
    @StateStrategyType(value = SkipStrategy::class)
    fun createStatusTimer(time: Long)

    fun startService(credentials: Credentials, hostPool: HostPool)
    fun openAlarmActivity(alarm: Alarm)
}