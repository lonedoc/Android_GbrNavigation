package gbr.ui.login

import android.os.Bundle
import gbr.presentation.presenter.login.LoginPresenter
import gbr.presentation.view.login.LoginView
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter

class LoginActivity:MvpAppCompatActivity(),LoginView {

    @InjectPresenter
    lateinit var presenter:LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_login)

    }

}