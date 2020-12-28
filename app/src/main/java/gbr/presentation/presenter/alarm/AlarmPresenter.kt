package gbr.presentation.presenter.alarm

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.view.View
import gbr.presentation.presenter.navigator.NavigatorPresenter
import gbr.presentation.view.alarm.AlarmView
import gbr.ui.main.MainActivity
import gbr.utils.api.alarm.AlarmAPI
import gbr.utils.api.alarm.OnAlarmListener
import gbr.utils.api.alarm.RPAlarmAPI
import gbr.utils.api.image.ImageAPI
import gbr.utils.api.image.OnImageListener
import gbr.utils.api.image.RPImageAPI
import gbr.utils.api.status.OnStatusListener
import gbr.utils.api.status.RPStatusAPI
import gbr.utils.api.status.StatusAPI
import gbr.utils.data.AlarmInfo
import gbr.utils.data.CurrentTime
import gbr.utils.data.Info
import gbr.utils.servicess.ProtocolService
import gbr.utils.servicess.ProtocolService.Companion.currentLocation
import moxy.InjectViewState
import moxy.MvpPresenter
import gbr.utils.data.RefreshPlan
import org.greenrobot.eventbus.EventBus
import org.osmdroid.util.GeoPoint
import rubegprotocol.RubegProtocol
import java.lang.Exception
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

@InjectViewState
class AlarmPresenter: MvpPresenter<AlarmView>(),OnStatusListener,OnAlarmListener, OnImageListener {

    val info: Info = Info
    val alarmInfo:AlarmInfo = AlarmInfo
    private var statusAPI: StatusAPI? = null
    private var alarmAPI: AlarmAPI?=null
    private var imageAPI: ImageAPI?=null
    lateinit var context: Context

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        val protocol = RubegProtocol.sharedInstance

        if(statusAPI!=null)
            statusAPI?.onDestroy()
        statusAPI = RPStatusAPI(protocol)
        statusAPI?.onStatusListener = this

        if(imageAPI!=null)
            imageAPI?.onDestroy()
        imageAPI = RPImageAPI(protocol)
        imageAPI?.onImageListener = this

        if(alarmAPI!=null)
            alarmAPI?.onDestroy()
        alarmAPI=RPAlarmAPI(protocol,"Alarm")
        alarmAPI?.onAlarmListener=this

        viewState.setTitle("Карточка объекта")

        viewState.statePhoto(false)
        viewState.stateArrived(false)
        viewState.stateReport(false)


        if(alarmInfo.lat=="0" && alarmInfo.lon =="0" || alarmInfo.lat==null && alarmInfo.lon==null)
        {
            viewState.showToastMessage("Нет координат объекта, автоприбытие отключено")
            viewState.showBottomBar(View.GONE)
            viewState.stateArrived(true)
        }
        else
        {
            arrivedLoop()
        }

        alarmApply()
        downloadImage()
    }

    private fun downloadImage()
    {
        val photo = alarmInfo.photo + alarmInfo.plan
        thread{
            if(photo.count() == 0)
            {
                viewState.showToastMessage("Нет изображений для загруки")
                return@thread
            }
            for(i in 0 until photo.count())
            {
                sendImageRequest(photo[i])
            }
        }
    }
    private fun sendImageRequest(photoName: String) {
        imageAPI?.sendImageRequest(photoName){
            if(!it){
                sendImageRequest(photoName)
            }
        }
    }
    var arrived = false
    private fun arrivedLoop() {
        thread {
            while(!arrived)
            {
                val myCoordinate = ProtocolService.coordinate
                sleep(3000)

                if(myCoordinate==null) continue

                if(alarmInfo.lat==null || alarmInfo.lon == null) continue

                if(myCoordinate.lat==0.toDouble() && myCoordinate.lon==0.toDouble()) continue

                val distance = 100

                val endPoint = GeoPoint(alarmInfo.lat!!.toDouble(),alarmInfo.lon!!.toDouble())


                if(endPoint.distanceToAsDouble(GeoPoint(myCoordinate.lat,myCoordinate.lon))>distance) continue

                arrived = true

                sendArrived()
            }
        }

    }

    private fun alarmApply() {
        if(alarmInfo.number == null) return
        try{
            alarmAPI?.sendAlarmApplyRequest(
                alarmInfo.number!!,
                currentLocation!!.latitude,
                currentLocation!!.longitude,
                currentLocation!!.speed
            ) {
                if (it) {
                    viewState.startTimer(SystemClock.elapsedRealtime())
                    val currentTime: String = SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())
                    viewState.showToastMessage("Тревога принята в $currentTime")
                    EventBus.getDefault().postSticky(CurrentTime(currentTime))
                } else {
                    viewState.showToastMessage("Не удалось отправить принятие, повторная отправка")
                    alarmApply()
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            alarmApply()
        }
    }

    fun sendArrived(){
        alarmAPI?.sendArrivedObject(
            alarmInfo.number!!,
                  currentLocation!!.latitude,
        currentLocation!!.longitude,
        currentLocation!!.speed
        ){
            if(it)
            {
                viewState.showToastMessage("Прибытие отправлено")
                viewState.stateArrived(false)
                val currentTime: String = SimpleDateFormat(
                    "HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())
                EventBus.getDefault().post(ArrivedTime(currentTime))
                viewState.stateReport(true)
            }
            else
            {
                viewState.showToastMessage("Не удалось отправить прибытие, повторная отправка")
                sendArrived()
            }
        }
    }

    data class ArrivedTime(var arrivedTime: String)

    fun sendReports(report:String,comment:String){
        alarmAPI?.sendReport(
            report,
            comment,
            info.nameGBR!!,
            alarmInfo.name!!,
            alarmInfo.number!!
        ){
            if(it){
                viewState.stateReport(false)
            }
            else
            {
                viewState.showToastMessage("Не удалось отправить рапорт, повторная отправка")
                sendReports(report,comment)
            }
        }
    }

    override fun onStatusDataReceived(status: String, call: String) {
        if(status != "Свободен") return

        info.status(status)
        info.nameGBR(call)

        val main = Intent(context,MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(main)

        viewState.showToastMessage("Отмена тревоги смена статуса")
        onDestroy()
    }

    override fun onAlarmDataReceived(flag: String, alarm: String) {

        when(flag){
            "notalarm"->{
                info.status("Свободен")
                arrived = true
                AlarmInfo.clearData()
                viewState.showToastMessage("Тревога завершена")
            }
            "alarm"->{
                arrived = true
                NavigatorPresenter.arrived = true
                AlarmInfo.clearData()
                viewState.showToastMessage("Отмена тревоги, новая тревога")
            }
            "alarmmob"->{
                arrived = true
                NavigatorPresenter.arrived = true
                AlarmInfo.clearData()
                viewState.showToastMessage("Отмена тревоги, новая тревога на мобильном объекте")
            }
        }

        onDestroy()

        val main = Intent(context,MainActivity::class.java)
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(main)
    }

    fun context(applicationContext: Context?) {
        this.context = applicationContext!!
    }

    override fun onDestroy() {
        alarmAPI?.onDestroy()
        statusAPI?.onDestroy()
        imageAPI?.onDestroy()
        arrived = true
        super.onDestroy()
    }

    override fun onImageDataReceived(imageByte: ByteArray) {
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.count())
        alarmInfo.downloadPhoto.add(bitmap)

        EventBus.getDefault().post(
            RefreshPlan(
                true
            )
        )
    }
}