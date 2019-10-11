package newVersion.alarm.responsible

import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter

class ResponsibleFragment : MvpAppCompatFragment(), ResponsibleView {

    @InjectPresenter
    lateinit var presenter: ResponsiblePresenter
}