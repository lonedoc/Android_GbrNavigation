package newVersion.alarm.plan

import android.graphics.Bitmap
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.utils.Alarm
import newVersion.commonInterface.Destroyable
import newVersion.commonInterface.Init
import newVersion.models.RefreshPlan
import newVersion.network.image.ImageAPI
import newVersion.network.image.OnImageListener
import newVersion.network.image.RPImageAPI
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rubegprotocol.RubegProtocol
import java.lang.Thread.sleep

@InjectViewState
class PlanPresenter : MvpPresenter<PlanView>(), OnImageListener,Init,Destroyable {
    override fun onImageDataReceived(imageByte: ByteArray) {
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refresh(event:RefreshPlan)
    {
        if(event.boolean)
            viewState.addImageToRecyclerView()
    }

    override var init: Boolean = false
    private var imageApi:ImageAPI? = null


    companion object{
        var countQueueImageInDownload:Int = 0
        var plan:ArrayList<Bitmap?> = ArrayList()
    }

    fun init(imageInfo:Alarm){
        init = true

        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this)

        val planName = imageInfo.plan
        val photoName = imageInfo.photo
        val imageName:ArrayList<String> = ArrayList()

        if(planName.count()>0){
            imageName.addAll(planName)
        }

        if(photoName.count()>0){
            imageName.addAll(photoName)
        }

        if(imageName.count()==0){
            viewState.showToastMessage("Нет изображений для загрузки")
            return
        }

        if(imageName.count() == countQueueImageInDownload){
            viewState.initRecyclerView(plan)
            return
        }

        for(i in 0 until imageName.count()){
            plan.add(i,null)

        }

        viewState.initRecyclerView(plan)

        val protocol = RubegProtocol.sharedInstance

        if(this.imageApi != null) return
        this.imageApi = RPImageAPI(protocol = protocol)
        this.imageApi?.onImageListener = this

        for(i in 0 until imageName.count()){

            imageDownload(image = imageName[i],count = imageName.count())
            countQueueImageInDownload++
        }
        imageName.clear()
    }

    private var imageDownloadStart = false
    private fun imageDownload(image:String,count:Int){
        if(imageDownloadStart) return

        imageApi?.sendImageRequest(image){
            if(!it){
                sleep(1000)
                imageDownloadStart = false
            }
        }

        if(countQueueImageInDownload == count)
            imageDownloadStart = true

    }
    override fun isInit(): Boolean {
        return init
    }

    override fun onDestroy() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        init = false
        imageApi?.onDestroy()
        super.onDestroy()
    }
}