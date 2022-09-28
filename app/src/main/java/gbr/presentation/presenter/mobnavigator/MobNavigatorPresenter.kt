package gbr.presentation.presenter.mobnavigator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.ContextCompat
import gbr.presentation.presenter.mobalarm.MobAlarmPresenter.Companion.objectLocation
import gbr.presentation.presenter.navigator.NavigatorPresenter
import gbr.presentation.view.mobnavigator.MobNavigatorView
import gbr.ui.mobnavigator.FragmentMobNavigator.Companion.isAlive
import gbr.ui.navigator.NavigatorFragment
import gbr.utils.data.AlarmInfo
import gbr.utils.data.Info
import gbr.utils.servicess.ProtocolService.Companion.currentLocation
import moxy.InjectViewState
import moxy.MvpPresenter
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import kotlin.concurrent.thread

@InjectViewState
class MobNavigatorPresenter:MvpPresenter<MobNavigatorView>() {

    val info:Info = Info
    val alarmInfo:AlarmInfo = AlarmInfo
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.initMapView()
        viewState.initOverlays()

        createRoute()
    }

    fun createRoute() {
        val endPoint = GeoPoint(objectLocation)
        val startPoint = GeoPoint(currentLocation)
        val routeServers = info.routeServers

        viewState.setMarker(alarmInfo.name, objectLocation!!)

        when{
            routeServers!!.count()<1->{
                viewState.showToastMessage("Не указаны сервера для прокладки маршрута")
                return
            }
            startPoint.distanceToAsDouble(endPoint)<100->{
                viewState.showToastMessage("Вы на месте")
                return
            }
            else->{

                val waypoint = ArrayList<GeoPoint>()
                waypoint.add(startPoint)
                waypoint.add(endPoint)

                viewState.buildTrack(100,waypoint,routeServers)
            }
        }
    }

    fun customIcon(drawable: Int, context: Context): Bitmap? {

        val drawableBitmap = ContextCompat.getDrawable(context, drawable)
        val bitmap = Bitmap.createBitmap(
            drawableBitmap?.intrinsicWidth!!,
            drawableBitmap.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawableBitmap.setBounds(0, 0, canvas.width, canvas.height)
        drawableBitmap.draw(canvas)

        return bitmap
    }

    fun setCenter(geoPoint: GeoPoint) {
        viewState.setCenter(geoPoint)
    }

    fun distance(road: Road): Double? {
        if(road.mRouteHigh.size<2) return null

        val firstPoint = road.mRouteHigh[0]
        val secondPoint = road.mRouteHigh[1]

        return firstPoint.distanceToAsDouble(secondPoint)
    }

    fun startTracking(road: Road, distance: Int, geoPoint: GeoPoint) {
        var roadOverlay = RoadManager.buildRoadOverlay(road,0x800000FF.toInt(), 10.0f)
        viewState.initRoadOverlay(roadOverlay)
        var distanceBetweenPoints = distance(road)

        thread {
            while(isAlive)
            {
                val objectLocation = objectLocation
                Log.d("ObjectLocation","${objectLocation!!.distanceToAsDouble(road.mRouteHigh.last())}")
                when{
                    objectLocation.distanceToAsDouble(GeoPoint(currentLocation)) <= distance && distance(road) == null->{
                        viewState.showToastMessage("Вы прибыли на место")
                        viewState.clearOverlay(roadOverlay)
                        return@thread
                    }
                    distance(road)==null->{
                        viewState.recreateTrack(roadOverlay)
                        return@thread
                    }
                    objectLocation.distanceToAsDouble(road.mRouteHigh.last()) > 50->{
                        viewState.setMarker(alarmInfo.name, objectLocation)
                        viewState.recreateTrack(roadOverlay)
                        return@thread
                    }
                    distance(road)!! >= distanceBetweenPoints?.plus(50)!!->{
                        Log.d("DistanceBeetwin","Yes")
                        viewState.recreateTrack(roadOverlay)
                        return@thread
                    }
                    distance(road)!! < 30 ->{
                        viewState.removeRoadOverlay(roadOverlay)
                        road.mRouteHigh.removeAt(1)
                        roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                        viewState.addRoadOverlay(roadOverlay)
                        distanceBetweenPoints = distance(road)
                    }
                    else ->{
                        viewState.removeRoadOverlay(roadOverlay)
                        road.mRouteHigh.removeAt(0)
                        road.mRouteHigh.add(0,GeoPoint(currentLocation))
                        roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                        viewState.addRoadOverlay(roadOverlay)
                    }
                }
                Thread.sleep(500)
            }
        }
    }

}