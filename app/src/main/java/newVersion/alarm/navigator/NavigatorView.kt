package newVersion.alarm.navigator

import moxy.MvpView
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline

interface NavigatorView: MvpView {
    @StateStrategyType(value = SkipStrategy::class)
    fun showToastMessage(message:String)
    @StateStrategyType(value = SkipStrategy::class)
    fun initMapView()
    @StateStrategyType(value = SkipStrategy::class)
    fun addOverlays()

    fun setCenter()

    @StateStrategyType(value = SkipStrategy::class)
    fun buildTrack(distance: Long,waypoints:ArrayList<GeoPoint>,routeServers:ArrayList<String>)

    @StateStrategyType(value = SkipStrategy::class)
    fun setMarker(name:String?,endPoint: GeoPoint)

    @StateStrategyType(value = SkipStrategy::class)
    fun clearOverlay(roadOverlay: Polyline?)

    @StateStrategyType(value = SkipStrategy::class)
    fun recreateTrack(roadOverlay: Polyline?)

    @StateStrategyType(value = SkipStrategy::class)
    fun invalidateMapView(road: Road, roadOverlay: Polyline?)

    @StateStrategyType(value = SkipStrategy::class)
    fun createRoad(roadOverlay: Polyline?)

    @StateStrategyType(value = SkipStrategy::class)
    fun removeRoadOverlay(roadOverlay: Polyline?)

    @StateStrategyType(value = SkipStrategy::class)
    fun addRoadOverlay(roadOverlay: Polyline?)

}