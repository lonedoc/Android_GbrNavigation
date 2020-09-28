package gbr.presentation.presenter.mobobjectinfo

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.view.View
import gbr.presentation.presenter.alarm.AlarmPresenter
import gbr.presentation.view.mobobjectinfo.MobObjectInfoView
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
import gbr.utils.data.Info
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.common.CurrentTime
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rubegprotocol.RubegProtocol
import java.text.SimpleDateFormat
import java.util.*

@InjectViewState
class MobObjectInfoPresenter:MvpPresenter<MobObjectInfoView>(),OnImageListener{

    var imageAPI:ImageAPI? = null
    val alarmInfo:AlarmInfo = AlarmInfo
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        EventBus.getDefault().register(this)
        val protocol = RubegProtocol.sharedInstance
        imageAPI = RPImageAPI(protocol)
        imageAPI!!.onImageListener = this

        setName()
        setPhone()

        if(alarmInfo.photo.count()>0)
            requestPhoto()
    }

    private fun setPhone() {
        viewState.setPhone(alarmInfo.phone)
    }

    private fun setName() {
        viewState.setName(alarmInfo.name)
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun takeApplyAlarmTime(time:CurrentTime){
        viewState.setObjectTimeApplyAlarm(time.currentTime)
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun takeArrivedTime(time: AlarmPresenter.ArrivedTime){
        viewState.setObjectTimeArrived(time.arrivedTime)
    }

    private fun requestPhoto() {
        imageAPI?.sendImageRequest(alarmInfo.photo[0]){
            if (it)
            {
                imageAPI?.sendReceiveRequest {  }
            }
            else
            {
                requestPhoto()
            }
        }
    }

    override fun onImageDataReceived(imageByte: ByteArray) {
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.count())

        viewState.setImage(bitmap)
    }

    override fun onDestroy() {

        EventBus.getDefault().unregister(this)

        if(imageAPI!=null)
            imageAPI!!.onDestroy()

        super.onDestroy()
    }

}