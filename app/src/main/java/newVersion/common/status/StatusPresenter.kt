package newVersion.common.status

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.viewstate.MvpViewState
import newVersion.Utils.DataStoreUtils
import newVersion.commonInterface.Destroyable
import newVersion.commonInterface.Init
import newVersion.network.status.OnStatusListener
import newVersion.network.status.StatusApi

@InjectViewState
class StatusPresenter: MvpPresenter<StatusView>(),Destroyable, OnStatusListener,Init {

    var statusApi: StatusApi? = null

    override fun onStatusDataReceived(status: String,call:String) {
        DataStoreUtils.status = status
        DataStoreUtils.call = call
    }

    override var init: Boolean = false

    override fun isInit(): Boolean {
        return init
    }

    fun init(){

    }
    override fun onDestroy() {
        super.onDestroy()
    }
}