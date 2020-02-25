package newVersion.alarm.plan

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import newVersion.utils.Alarm

class PlanFragment : MvpAppCompatFragment(), PlanView {


    @InjectPresenter
    lateinit var presenter: PlanPresenter

    private var rootView:View? = null
    private var recyclerView:RecyclerView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_alarm_plan, container, false)

        return rootView
    }

    override fun onResume() {
        super.onResume()

        val imageInfo = activity!!.intent.getSerializableExtra("info") as? Alarm
        presenter.init(imageInfo!!)

    }
    override fun initRecyclerView(plan:ArrayList<Bitmap?>){
        recyclerView = rootView?.findViewById(R.id.alarm_plan_recyclerView)!!
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter =
            AdapterImage(
                plan,
                presenter,
                context
            )
    }
    override fun addImageToRecyclerView() {
        activity!!.runOnUiThread {
            recyclerView?.adapter?.notifyDataSetChanged()
        }
    }

    override fun showToastMessage(message: String) {
        activity!!.runOnUiThread {
            Toast.makeText(context,message,Toast.LENGTH_LONG).show()
        }
    }
}


