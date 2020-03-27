package newVersion.alarm.navigator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.ContextCompat
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.utils.Alarm
import newVersion.utils.DataStoreUtils
import newVersion.commonInterface.Init
import gbr.utils.servicess.LocationListener.Companion.imHere
import newVersion.utils.Location
import newVersion.utils.ProviderStatus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import java.lang.Thread.sleep
import kotlin.concurrent.thread

@InjectViewState
class   NavigatorPresenter: MvpPresenter<NavigatorView>(),Init {

    override var init: Boolean = false
    private var info:Alarm? = null

    fun init(info: Alarm?){

        this.info = info

        viewState.initMapView()

        viewState.addOverlays()

        if(!EventBus.getDefault().isRegistered(this))
        {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND,sticky = true)
    fun onEnableLocation(event: ProviderStatus){
        when(event.status)
        {
            "enable"->{
                viewState.setCenter()
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onTracking(event:Location){
        if(!tracking||distance == null||road == null||endPoint == null) return

        var distanceBetweenPoints = distance(road!!)
        var roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)

        viewState.createRoad(roadOverlay = roadOverlay)

        while(NavigatorFragment.isAlive){

            if(endPoint?.distanceToAsDouble(GeoPoint(imHere))!! <= this.distance!! && distance(road!!) == null)
            {
                viewState.showToastMessage("Вы прибыли на место")

                viewState.clearOverlay(roadOverlay)
                return
            }

            if(distance(road!!)==null)
            {
                tracking = false
                viewState.recreateTrack(roadOverlay)
                return
            }

            if(distance(road!!)!! >= distanceBetweenPoints?.plus(30)!!){
                tracking = false
                viewState.recreateTrack(roadOverlay)
                return
            }

            if(distance(road!!)!! < 30)
            {
                viewState.removeRoadOverlay(roadOverlay)
                road!!.mRouteHigh.removeAt(1)
                roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
                viewState.addRoadOverlay(roadOverlay)
                distanceBetweenPoints = distance(road!!)

                continue
            }

            viewState.removeRoadOverlay(roadOverlay)
            road!!.mRouteHigh.removeAt(0)
            road!!.mRouteHigh.add(0,GeoPoint(imHere))
            roadOverlay = RoadManager.buildRoadOverlay(road, 0x800000FF.toInt(), 10.0f)
            viewState.addRoadOverlay(roadOverlay)

            sleep(500)
        }
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

    fun startTrack(info: Alarm, myLocation: GeoPoint) {

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

        val lat = info.lat
        val lon = info.lon

        val endPoint = GeoPoint(lat?.toDouble()!!,lon?.toDouble()!!)

        viewState.setMarker(info.name,endPoint)

        val routeServers = DataStoreUtils.routeServer

        if(routeServers.count()==0) {
            viewState.showToastMessage("Не указан сервер для прокладки маршрута, построение не возможно")
            return
        }

        if(myLocation.distanceToAsDouble(endPoint)<distance) {
            viewState.showToastMessage("Построение маршрута невозможно, вы прибыли")
            return
        }

        val waypoint = ArrayList<GeoPoint>()
        waypoint.add(myLocation)
        waypoint.add(endPoint)
        Log.d("NavigatorPresenter","$waypoint")
        thread{
            /*viewState.buildTrack(distance,waypoint,routeServers)*/
        }
    }

    fun distance(road: Road): Double? {

        if(road.mRouteHigh.size<2) return null

        val firstPoint = road.mRouteHigh[0]
        val secondPoint = road.mRouteHigh[1]

        return firstPoint.distanceToAsDouble(secondPoint)
    }

    var tracking = false
    var road:Road? = null
    private var distance:Long? = null
    var endPoint:GeoPoint? = null

    fun tracking(
        road: Road?,
        distance: Long,
        endPoint: GeoPoint
    ) {
        this.road = road
        this.distance = distance
        this.endPoint = endPoint
        this.tracking = true
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

    override fun onDestroy() {
        init = false

        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        super.onDestroy()
    }
}