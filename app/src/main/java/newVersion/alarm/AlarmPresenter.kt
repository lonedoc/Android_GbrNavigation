package newVersion.alarm

import android.content.Context
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import newVersion.Utils.DataStoreUtils
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
import oldVersion.workservice.Alarm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.util.GeoPoint
import rubegprotocol.RubegProtocol

@InjectViewState
class AlarmPresenter : MvpPresenter<AlarmView>(), OnAlarmListener, OnCompleteListener, Destroyable, Init {

    override var init: Boolean = false
    private var alarmApi: AlarmAPI? = null
    private var completeApi: CompleteAPI? = null
    private var alarmInfo: Alarm? = null
    private var context:Context? = null
    private var checkArrivedAlive = false

    override fun isInit(): Boolean {
        return init
    }

    fun init(alarmInfo: Alarm?, applicationContext: Context) {

        this.context = applicationContext

        this.alarmInfo = alarmInfo

        this.init = true

        val protocol = RubegProtocol.sharedInstance

        if (this.alarmApi != null) alarmApi?.onDestroy()
        this.alarmApi = RPAlarmAPI(protocol)
        this.alarmApi?.onAlarmListener = this

        if (this.completeApi != null) completeApi?.onDestroy()
        this.completeApi = RPCompleteAPI(protocol)
        this.completeApi?.onCompleteListener = this

        viewState.startTimer()
        viewState.openFragment(AlarmTabFragment())
        viewState.setTitle("Карточка объекта")

        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)

        arrivedCheck(DataStoreUtils.cityCard?.pcsinfo?.dist,alarmInfo?.lon,alarmInfo?.lat)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun getEvent(event: CardEvent){
        when{
            event.action == "Arrived"->{
                viewState.showArrivedDialog()
            }
            event.action == "Report"->{
                viewState.showReportDialog()
            }
        }
    }


    override fun onCompleteDataReceived(name: String) {
        Log.d("CompletePresenter", name)

        when {
            name != alarmInfo?.name -> return
            name == alarmInfo?.name -> {
                viewState.showToastMessage("Тревога завершена")
                viewState.completeAlarm()
            }
        }
    }

    override fun onAlarmDataReceived(alarm: Alarm) {
        Log.d("AlarmPresenter", "$alarm")
        viewState.showToastMessage("Новая тревога")
        viewState.completeAlarm()
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
            return
        }

        if(lon==null || lat == null)
        {
            viewState.showToastMessage("Ошибка: Не указаны координаты до объекта, автоматическое прибытие отменено")
            return
        }

        if(lon=="0" || lat=="0")
        {
            viewState.showToastMessage("Ошибка: Не указаны координаты до объекта, автоматическое прибытие отменено")
            return
        }

        if(imHere==null)
            viewState.showToastMessage("Не удалось определить ваше месторасположение по GPS, система находится в режиме ожидания ваших координат")

        val endPoint = GeoPoint(lat.toDouble(),lon.toDouble())
        thread{
            while(checkArrivedAlive){
                sleep(5000)

                Log.d("AlarmPresenter","Loop")

                if(imHere == null) continue

                if(endPoint.distanceToAsDouble(GeoPoint(imHere)) > distance) continue

                if (!AlarmActivity.isAlive)
                    viewState.recallActivity(alarmInfo)

                Log.d("AlarmPresenter","${endPoint.distanceToAsDouble(GeoPoint(imHere))}")
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
        init = false
        checkArrivedAlive = false
        alarmApi?.onDestroy()
        completeApi?.onDestroy()

        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        super.onDestroy()
    }

}