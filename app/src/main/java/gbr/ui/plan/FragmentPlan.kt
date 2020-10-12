package gbr.ui.plan

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gbr.presentation.presenter.plan.PlanPresenter
import gbr.presentation.view.plan.PlanView
import gbr.utils.data.AlarmInfo
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter


class FragmentPlan:MvpAppCompatFragment(),PlanView {
    @InjectPresenter
    lateinit var presenter:PlanPresenter

    private var rootView:View? = null
    private var recyclerView: RecyclerView? = null

    private var alarmInfo:AlarmInfo = AlarmInfo
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_alarm_plan, container, false)

        return rootView
    }

    override fun initRecyclerView(){
        recyclerView = rootView?.findViewById(R.id.alarm_plan_recyclerView)!!
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter =
            gbr.utils.adapters.plan.AdapterImage(
                alarmInfo.downloadPhoto,
                presenter,
                context
            )
    }
    override fun addImageToRecyclerView() {
        activity!!.runOnUiThread {
            recyclerView?.adapter?.notifyDataSetChanged()
        }
    }

}