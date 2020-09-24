package gbr.ui.mobobjectinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import gbr.presentation.presenter.mobobjectinfo.MobObjectInfoPresenter
import gbr.presentation.view.mobobjectinfo.MobObjectInfoView
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import newVersion.models.CardEvent
import org.greenrobot.eventbus.EventBus


class FragmentMobObjectInfo: MvpAppCompatFragment(),MobObjectInfoView {
    @InjectPresenter
    lateinit var presenter:MobObjectInfoPresenter
    lateinit var rootView:View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_mobobjectinfo,container,false)

        return rootView
    }
}