package gbr.ui.mobnavigator

import gbr.presentation.presenter.mobnavigator.MobNavigatorPresenter
import gbr.presentation.view.mobnavigator.MobNavigatorView
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class FragmentMobNavigator:MvpAppCompatFragment(), MobNavigatorView {
    @InjectPresenter
    lateinit var presenter: MobNavigatorPresenter
}