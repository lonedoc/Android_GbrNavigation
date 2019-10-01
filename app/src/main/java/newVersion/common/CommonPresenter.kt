package newVersion.common

import com.arellomobile.mvp.MvpPresenter
import newVersion.commonInterface.Destroyable
import newVersion.commonInterface.Init

class CommonPresenter: MvpPresenter<CommonView>(),Init,Destroyable {
    override var init: Boolean = false
    override fun isInit(): Boolean {
        return init
    }

}