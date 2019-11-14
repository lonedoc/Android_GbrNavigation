package newVersion.alarm.navigator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.core.content.ContextCompat
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.utils.Alarm
import newVersion.utils.DataStoreUtils
import newVersion.commonInterface.Init
import newVersion.servicess.LocationListener.Companion.imHere
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import java.lang.Thread.sleep
import kotlin.concurrent.thread

@InjectViewState
class NavigatorPresenter: MvpPresenter<NavigatorView>(),Init {
    override var init: Boolean = false

    fun init(){

        viewState.initMapView()

        viewState.addOverlays()
    }

    fun haveCoordinate(info:Alarm):Boolean{
        if( info.lat==null || info.lon == null)
        {
            viewState.showToastMessage("Не были указаны координаты объекта")
            return false
        }

        if(info.lat=="0" || info.lon=="0" )
        {
            viewState.showToastMessage("Не были указаны координаты объекта")
            return false
        }

        return true
    }

    fun startTrack(info:Alarm) {

        if( info.lat==null || info.lon == null)
        {
            viewState.showToastMessage("Не были указаны координаты объекта")
            return
        }

        if(info.lat=="0" || info.lon=="0" )
        {
            viewState.showToastMessage("Не были указаны координаты объекта")
            return
        }

        val strDistance = DataStoreUtils.cityCard?.pcsinfo?.dist


        val distance = if(strDistance!=null) {
            if (strDistance == "")
                50
            else
                strDistance.toLong()
        }
        else{
            50
        }

        if(imHere == null)
        {
            viewState.showToastMessage("Ваше месторасположение не определено, невозможно построить маршрут, приложение переходит в режим ожидания")
            return
        }

        val lat = info.lat
        val lon = info.lon
        val startPoint = GeoPoint(imHere)



        val endPoint = GeoPoint(lat?.toDouble()!!,lon?.toDouble()!!)

        viewState.setMarker(info.name,endPoint)

        val routeServers = DataStoreUtils.routeServer

        if(routeServers.count()==0)
        {
            viewState.showToastMessage("Не указан сервер для прокладки маршрута, построение не возможно")
            return
        }
        if(startPoint.distanceToAsDouble(endPoint)<distance){
            viewState.showToastMessage("Построение маршрута невозможно, вы прибыли")
            return
        }

        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(startPoint)
        waypoints.add(endPoint)

        thread{
            viewState.buildTrack(distance,waypoints,routeServers)
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

    fun setCenter(imHere: Location?) {
        if (imHere != null) {
            viewState.setCenter(GeoPoint(imHere))
        } else {
            viewState.showToastMessage("Ваше месторасположение не определено, невозможно построить маршрут, приложение переходит в режим ожидания")
            thread {
                sleep(2000)
                viewState.setCenterLoop()
            }
        }
    }

    override fun onDestroy() {
        init = false
        super.onDestroy()
    }

    fun distance(road: Road): Double? {

        if(road.mRouteHigh.size<2) return null

        val firstPoint = road.mRouteHigh[0]
        val secondPoint = road.mRouteHigh[1]

        return firstPoint.distanceToAsDouble(secondPoint)
    }

    fun tracking(
        road: Road?,
        distance: Long,
        endPoint: GeoPoint
    ) {
        thread{
            var distanceBetweenPoints = distance(road!!)
            var roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)

            viewState.createRoad(roadOverlay = roadOverlay)

            while(NavigatorFragment.isAlive){

                if(endPoint.distanceToAsDouble(GeoPoint(imHere)) <= distance && distance(road) == null)
                {
                    viewState.showToastMessage("Вы прибыли на место")
                    viewState.clearOverlay(roadOverlay)
                    return@thread
                }

                if(distance(road)==null)
                {
                    viewState.recreateTrack(roadOverlay)
                    return@thread
                }

                if(distance(road)!! >= distanceBetweenPoints?.plus(30)!!){
                    viewState.recreateTrack(roadOverlay)
                    return@thread
                }

                if(distance(road)!! < 30)
                {
                    viewState.removeRoadOverlay(roadOverlay)
                    road.mRouteHigh.removeAt(1)
                    roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                    viewState.addRoadOverlay(roadOverlay)
                    distanceBetweenPoints = distance(road)

                    continue
                }

                viewState.removeRoadOverlay(roadOverlay)
                road.mRouteHigh.removeAt(0)
                road.mRouteHigh.add(0,GeoPoint(imHere))
                roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                viewState.addRoadOverlay(roadOverlay)

                sleep(500)
            }
        }
    }
}