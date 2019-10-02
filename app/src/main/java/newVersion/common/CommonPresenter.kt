package newVersion.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.firebase.messaging.RemoteMessage
import newVersion.NetworkService
import newVersion.Utils.DataStoreUtils
import newVersion.commonInterface.Destroyable
import newVersion.commonInterface.Init
import newVersion.models.Credentials
import newVersion.models.HostPool
import newVersion.network.status.OnStatusListener
import newVersion.network.status.RPStatusApi
import newVersion.network.status.StatusApi
import oldVersion.commonactivity.CommonActivity
import oldVersion.workservice.NotificationService
import org.osmdroid.util.GeoPoint
import ru.rubeg38.rubegprotocol.RubegProtocol
import java.util.prefs.Preferences
import kotlin.concurrent.thread

@InjectViewState
class CommonPresenter : MvpPresenter<CommonView>(),OnStatusListener, Init, Destroyable {

    override fun onStatusDataReceived(status: String,call:String) {
        DataStoreUtils.status = status
        DataStoreUtils.call = call
        setTitle()
        viewState.createNotification("gbrstatus",status)
    }

    var statusApi:StatusApi? = null
    override var init: Boolean = false
    override fun isInit(): Boolean {
        return init
    }

    fun init(preferences: newVersion.models.Preferences?){
        init = true
        Log.d("CommonPresenter","Init")
        val addresses = preferences?.serverAddress
        val port = preferences?.serverPort
        val imei = preferences?.imei
        val fcmtoken = preferences?.fcmtoken

        val credentials = Credentials(
            imei.toString(),
            fcmtoken!!
        )

        val hostPool = HostPool(
            addresses = addresses!!,
            port = port!!.toInt()
        )

        if(!NetworkService.isServiceStarted){
            viewState.startService(credentials,hostPool)
        }

        viewState.initMapView()

        viewState.addOverlays()

        val protocol = RubegProtocol.sharedInstance
        if(statusApi!=null) statusApi?.onDestroy()
        statusApi = RPStatusApi(protocol)
        statusApi?.OnStatusListener = this

    }

    fun setTitle(){
        if(DataStoreUtils.call != ""){
            viewState.setTitle("${DataStoreUtils.call} ( ${DataStoreUtils.status} )")
        }
        else
        {
            viewState.setTitle("Группа не поставлена на дежурство")
        }
    }

    fun customIcon(drawable: Int,context:Context): Bitmap? {

        val drawableBitmap = ContextCompat.getDrawable(context,drawable)
        val bitmap = Bitmap.createBitmap(
            drawableBitmap?.intrinsicWidth!!,
            drawableBitmap.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawableBitmap.setBounds(0,0,canvas.width,canvas.height)
        drawableBitmap.draw(canvas)

        return bitmap
    }

    fun setCenter(imHere: Location?) {
        if(imHere!=null){
            viewState.setCenter(GeoPoint(imHere))
        }
        else
        {
            viewState.showToastMessage("Ваше месторасположение не определено")
        }
    }

    private var stateGpsCheckStart = false
    fun stateGpsCheck(locationManager: LocationManager) {
        thread{
            Log.d("StateGpsCheck","$stateGpsCheckStart")
            if(stateGpsCheckStart) return@thread
            stateGpsCheckStart = true
            while(stateGpsCheckStart){
                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    stateGpsCheckStart = false
                    if(newVersion.common.CommonActivity.isAlive )
                    viewState.createSettingGpsDialog()
                    return@thread
                }
            }
        }
    }

    override fun onDestroy() {
        statusApi?.onDestroy()
        super.onDestroy()
    }
}