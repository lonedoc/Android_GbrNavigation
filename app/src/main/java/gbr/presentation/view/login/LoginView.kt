package gbr.presentation.view.login

import moxy.MvpView

interface LoginView:MvpView {
    fun setImei()

    fun setPort(port:String)
}