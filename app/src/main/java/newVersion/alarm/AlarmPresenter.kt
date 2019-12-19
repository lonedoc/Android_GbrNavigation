package newVersion.alarm

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.utils.DataStoreUtils
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import newVersion.alarm.pager.AlarmTabFragment
import newVersion.commonInterface.Destroyable
import newVersion.commonInterface.Init
import newVersion.models.CardEvent
import newVersion.models.EnableButtons
import newVersion.network.alarm.AlarmAPI
import newVersion.network.alarm.OnAlarmListener
import newVersion.network.alarm.RPAlarmAPI
import newVersion.network.complete.CompleteAPI
import newVersion.network.complete.OnCompleteListener
import newVersion.network.complete.RPCompleteAPI
import newVersion.servicess.LocationListener.Companion.imHere
import newVersion.utils.Alarm
import newVersion.alarm.AlarmActivity.Companion.elapsedMillis
import newVersion.alarm.plan.PlanPresenter.Companion.plan
import newVersion.common.CommonActivity
import newVersion.common.CurrentTime
import newVersion.models.RefreshPlan
import newVersion.network.image.ImageAPI
import newVersion.network.image.OnImageListener
import newVersion.network.image.RPImageAPI
import newVersion.network.status.OnStatusListener
import newVersion.network.status.RPStatusAPI
import newVersion.network.status.StatusAPI
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.util.GeoPoint
import rubegprotocol.RubegProtocol
import java.text.SimpleDateFormat
import java.util.*

@InjectViewState
class AlarmPresenter : MvpPresenter<AlarmView>(),OnStatusListener, OnImageListener, OnAlarmListener, OnCompleteListener, Destroyable, Init {

    override fun onStatusDataReceived(status: String, call: String) {
        if(status == "Свободен" && !skipStatus)
            viewState.completeAlarm(null)
        else
            skipStatus = false
    }

    override fun onImageDataReceived(imageByte: ByteArray) {
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.count())
        for(i in 0 until plan.count())
        {
            if(plan[i]==null){
                plan[i]=bitmap
                break
            }
        }

        EventBus.getDefault().post(
            RefreshPlan(
                true
            )
        )

    }

    var skipStatus = false
    override fun onCompleteDataReceived(name: String) {
        Log.d("AlarmPresenter", name)
        Log.d("AlarmPresenter","${name == alarmInfo?.name}")
        when {
            name != alarmInfo?.name -> {
                skipStatus = true
                 }
            name == alarmInfo?.name -> {
                if(AlarmActivity.isAlive)
                {
                    viewState.showToastMessage("Тревога завершена")
                    DataStoreUtils.status = "Свободен"
                    viewState.completeAlarm(null)
                }
                else
                {
                    viewState.showToastMessage("Тревога завершена")
                    DataStoreUtils.status = "Свободен"
                    viewState.removeData()
                    onDestroy()
                    val intent = Intent(context, CommonActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context?.startActivity(intent)
                }
            }
            name =="oldVersion"->{
                viewState.showToastMessage("Для стабильной работы приложения необходимо обновить Сервер сообщений")
                DataStoreUtils.status = "Свободен"
                viewState.completeAlarm(null)
            }
        }
    }

    override fun onAlarmDataReceived(alarm: Alarm) {
        Log.d("AlarmPresenter", "$alarm")

        if(alarm.name!=alarmInfo?.name)
            when{
                !AlarmActivity.isAlive ->{
                    Log.d("AlarmPresenter","New alarm")
                    viewState.showToastMessage("Тревога завершена")
                    elapsedMillis = null
                    val intent = Intent(context, CommonActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("alarm",alarm)
                    context?.startActivity(intent)
                    onDestroy()
                }
                else->{
                    viewState.showToastMessage("Новая тревога")
                    viewState.completeAlarm(alarm)
                }
            }

    }

    override var init: Boolean = false

    private var alarmInfo: Alarm? = null
    private var context:Context? = null
    private var checkArrivedAlive = false

    private var imageApi: ImageAPI? = null
    private var alarmApi: AlarmAPI? = null
    private var completeApi: CompleteAPI? = null
    private var statusApi:StatusAPI? = null

    override fun isInit(): Boolean {
        return init
    }

    fun sendAlarmApplyRequest(alarm: Alarm) {
        alarmApi?.sendAlarmApplyRequest(
            alarm.number!!,
            complete = {
                if (it) {
                    val currentTime: String = SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date())
                    viewState.showToastMessage("Тревога принята в $currentTime")
                    EventBus.getDefault().postSticky(CurrentTime(currentTime))
                } else {
                    viewState.showToastMessage("Во время отправки сообщения о принятие тревоги произошел сбой, сообщение будет отправлено еще раз")
                    sendAlarmApplyRequest(alarm)
                }
            }
        )
    }

    fun init(info: Alarm?, applicationContext: Context) {

        Log.d("AlarmPResenter","$info")

        this.context = applicationContext

        this.alarmInfo = info

        this.init = true

        val protocol = RubegProtocol.sharedInstance

        if(this.imageApi != null) imageApi?.onDestroy()
        this.imageApi = RPImageAPI(protocol = protocol)
        this.imageApi?.onImageListener = this
        sleep(2)


        if(this.statusApi != null) statusApi?.onDestroy()
        this.statusApi = RPStatusAPI(protocol = protocol)
        this.statusApi?.onStatusListener = this
        sleep(2)

        if (this.alarmApi != null) alarmApi?.onDestroy()
        this.alarmApi = RPAlarmAPI(protocol = protocol)
        this.alarmApi?.onAlarmListener = this
        sleep(2)

        if (this.completeApi != null) completeApi?.onDestroy()
        this.completeApi = RPCompleteAPI(protocol = protocol)
        this.completeApi?.onCompleteListener = this
        sleep(2)

        viewState.startTimer()
        viewState.openFragment(AlarmTabFragment())
        viewState.setTitle("Карточка объекта")

        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this)

        changeStateButton(enableArrived = false,enableReport = false)
        arrivedCheck(DataStoreUtils.cityCard?.pcsinfo?.dist, info?.lon, info?.lat)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun getEvent(event: CardEvent){
        when{
            event.action == "Arrived"->{
                changeStateButton(enableArrived = true, enableReport = false)
                viewState.showArrivedDialog()
            }
            event.action == "Report"->{
                changeStateButton(enableArrived = false, enableReport = true)
                viewState.showReportDialog()
            }
        }
    }

    fun sendArrived() {
        when {
            alarmInfo?.number == null -> {
                viewState.showToastMessage("Сообщение о прибытие не может быть отправлено, так как в базе не был указан номер объекта")
                return
            }
            alarmInfo?.number != null -> {
                changeStateButton(enableArrived = false, enableReport = true)
                completeApi?.sendArrivedObject(alarmInfo?.number!!) {
                    if (it) {
                        viewState.showToastMessage("Сообщение о прибытие было доставлено, теперь вы можете отправить рапорт")
                    } else {
                        viewState.showToastMessage("Сообщение о прибытие не было доставлено, отправка повторится через несколько секунд")
                        sleep(1000)
                        sendArrived()
                    }
                }
                return
            }
        }
    }

    fun sendReport(report:String,comment:String){
        val namegbr = DataStoreUtils.namegbr
        val name = alarmInfo?.name
        val number = alarmInfo?.number

        if(name==null){
            viewState.showToastMessage("Невозможно отправить рапорт, потому что не было указано Имя Объекта")
            return
        }
        if(number==null){
            viewState.showToastMessage("Невозможно отправить рапорт, потому что не был указан Номер Объекта")
        }
        if(namegbr==null)
        {
            viewState.showToastMessage("Невозможно отправить рапорт, потому что не был указан Имя ГБР")
            return
        }

        changeStateButton(enableArrived = false, enableReport = false)

        completeApi?.sendReport(
            report = report,
            comment = comment,
            namegbr = namegbr,
            objectName = name,
            objectNumber = number.toString()
        ){
            if(it){
                viewState.showToastMessage("Рапорт был отправлен на сервер")

            }
            else{
                viewState.showToastMessage("Рапорт не был отправлен на сервер, отправка повторится через несколько секунд")
                sleep(1000)
                sendReport(report,comment)
            }
        }
    }

    private fun arrivedCheck(strDistance: String?, lon:String?, lat:String?){
        if(checkArrivedAlive) return

        checkArrivedAlive = true

        val distance = if(strDistance!=null)
            if(strDistance=="")
                50
            else
                strDistance.toLong()
        else{
            viewState.showToastMessage("Ошибка: Дистанция для прибытия не была указана, автоматическое прибытие отключено")
            changeStateButton(enableArrived = true, enableReport = false)
            return
        }

        if(lon==null || lat == null)
        {
            viewState.showToastMessage("Ошибка: Не указаны координаты до объекта, автоматическое прибытие отменено")
            changeStateButton(enableArrived = true, enableReport = false)
            return
        }

        if(lon=="0" || lat=="0")
        {
            viewState.showToastMessage("Ошибка: Не указаны координаты до объекта, автоматическое прибытие отменено")
            changeStateButton(enableArrived = true, enableReport = false)
            return
        }

        if(imHere==null)
        {
            viewState.showToastMessage("Не удалось определить ваше месторасположение по GPS, система находится в режиме ожидания ваших координат")
            changeStateButton(enableArrived = true, enableReport = false)
        }


        val endPoint = GeoPoint(lat.toDouble(),lon.toDouble())
        thread{
            while(checkArrivedAlive){
                sleep(5000)
                if(imHere == null) continue

                if(endPoint.distanceToAsDouble(GeoPoint(imHere)) > distance) continue

                if (!AlarmActivity.isAlive)
                {
                    val recallActivity = Intent( context,AlarmActivity::class.java)
                    recallActivity.putExtra("info",alarmInfo)
                    recallActivity.putExtra("elapsedMillis",elapsedMillis)
                    recallActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context?.startActivity(recallActivity)
                    return@thread
                }
                changeStateButton(enableArrived = true, enableReport = false)
                viewState.showArrivedDialog()

                checkArrivedAlive = false
            }
        }
    }

    fun changeStateButton(enableArrived: Boolean, enableReport: Boolean) {
        EventBus.getDefault().postSticky(
            EnableButtons(
                enableArrived,
                enableReport
            )
        )
    }

    override fun onDestroy() {

       /* if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)*/

        init = false

        checkArrivedAlive = false

        alarmApi?.onDestroy()
        completeApi?.onDestroy()
        imageApi?.onDestroy()
        statusApi?.onDestroy()

        super.onDestroy()
    }


}