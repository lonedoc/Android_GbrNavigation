package newVersion.alarm.plan

import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter

class PlanFragment : MvpAppCompatFragment(), PlanView {
    @InjectPresenter
    lateinit var presenter: PlanPresenter
}