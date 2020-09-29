package gbr.presentation.presenter.mobobjectinfo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import gbr.presentation.presenter.alarm.AlarmPresenter
import gbr.presentation.view.mobobjectinfo.MobObjectInfoView
import gbr.utils.api.image.ImageAPI
import gbr.utils.api.image.OnImageListener
import gbr.utils.api.image.RPImageAPI
import gbr.utils.data.AlarmInfo
import gbr.utils.data.CurrentTime
import moxy.InjectViewState
import moxy.MvpPresenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rubegprotocol.RubegProtocol

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
    fun takeApplyAlarmTime(time: CurrentTime){
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