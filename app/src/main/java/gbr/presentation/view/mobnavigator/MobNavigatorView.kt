package gbr.presentation.view.mobnavigator

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.util.ArrayList

interface MobNavigatorView:MvpView {
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun initMapView()
    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun initOverlays()

    fun setMarker(name: String?, objectLocation: GeoPoint)

    fun setCenter(geoPoint: GeoPoint)


    fun showToastMessage(message: String)

    @StateStrategyType(value = AddToEndSingleStrategy::class)
    fun buildTrack(distance: Int, waypoint: ArrayList<GeoPoint>, routeServers: ArrayList<String>)

    fun initRoadOverlay(roadOverlay: Polyline?)

    fun removeRoadOverlay(roadOverlay: Polyline?)

    fun addRoadOverlay(roadOverlay: Polyline?)

    fun clearOverlay(roadOverlay: Polyline?)

    fun recreateTrack(roadOverlay: Polyline?)

}