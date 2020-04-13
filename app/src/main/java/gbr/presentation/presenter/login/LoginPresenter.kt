package gbr.presentation.presenter.login

import gbr.presentation.view.login.LoginView
import moxy.InjectViewState
import moxy.MvpPresenter
@InjectViewState
class LoginPresenter: MvpPresenter<LoginView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.setImei()

        val port:String = "9010"

        viewState.setPort(port)
    }
}