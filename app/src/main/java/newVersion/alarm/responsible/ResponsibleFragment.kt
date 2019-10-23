package newVersion.alarm.responsible

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kobramob.rubeg38.ru.gbrnavigation.R
import newVersion.utils.Alarm
import newVersion.utils.ResponsibleList
import newVersion.alarm.responsible.personalCard.PersonalCardFragment

class ResponsibleFragment : MvpAppCompatFragment(), ResponsibleView {
    override fun showPersonalCard(
        name: String,
        position:String,
        mobileNumber: String,
        workNumber: String,
        homeNumber: String
    ) {
        val dialog = PersonalCardFragment.onNewInstance(name,position,mobileNumber,workNumber,homeNumber)
        dialog.show(activity!!.supportFragmentManager,"PersonalCard")
    }

    override fun initRecyclerView(responsibleList: ArrayList<ResponsibleList>) {
        val recyclerView:RecyclerView = rootView?.findViewById(R.id.alarm_responsible_recyclerView)!!
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter =
            AdapterResponsible(
                responsibleList,
                presenter
            )

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun showToastMessage(message: String) {
        activity!!.runOnUiThread {
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
        }
    }

    @InjectPresenter
    lateinit var presenter: ResponsiblePresenter

    private var rootView:View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_alarm_responsible, container, false)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        if(activity?.intent?.hasExtra("info")!!){
            val alarmInfo = activity?.intent?.getSerializableExtra("info") as Alarm
            if(!presenter.init){
                presenter.init(alarmInfo)
            }
        }
    }

    override fun onPause() {
        presenter.onDestroy()
        super.onPause()
    }
}