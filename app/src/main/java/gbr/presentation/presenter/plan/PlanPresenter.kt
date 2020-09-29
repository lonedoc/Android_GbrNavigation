package gbr.presentation.presenter.plan

import android.graphics.Bitmap
import gbr.presentation.view.plan.PlanView
import gbr.utils.data.AlarmInfo
import moxy.InjectViewState
import moxy.MvpPresenter
import newVersion.alarm.plan.PlanPresenter
import newVersion.models.RefreshPlan
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@InjectViewState
class PlanPresenter:MvpPresenter<PlanView>() {

    val alarmInfo:AlarmInfo = AlarmInfo
    var plan:ArrayList<Bitmap> = alarmInfo.downloadPhoto
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        EventBus.getDefault().register(this)

        viewState.initRecyclerView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refresh(event: RefreshPlan)
    {
        if(event.boolean)
            viewState.addImageToRecyclerView()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}