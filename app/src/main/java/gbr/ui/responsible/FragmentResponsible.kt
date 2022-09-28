package gbr.ui.responsible

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gbr.presentation.presenter.responsible.ResponsiblePresenter
import gbr.presentation.view.responsible.ResponsibleView
import gbr.ui.personalcard.FragmentPersonalCard
import gbr.utils.adapters.responsible.AdapterResponsible
import gbr.utils.data.ResponsibleList
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter

class FragmentResponsible:MvpAppCompatFragment(),ResponsibleView {
    @InjectPresenter
    lateinit var presenter: ResponsiblePresenter

    override fun showPersonalCard(
        name: String,
        position:String,
        mobileNumber: String,
        workNumber: String,
        homeNumber: String
    ) {

        val dialog = FragmentPersonalCard.onNewInstance(name,position,mobileNumber,workNumber,homeNumber)
        dialog.show(activity!!.supportFragmentManager,"PersonalCard")
    }

    override fun initRecyclerView(responsibleList: ArrayList<ResponsibleList>) {
        val recyclerView: RecyclerView = rootView?.findViewById(R.id.alarm_responsible_recyclerView)!!
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
            Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
        }
    }

    private var rootView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_alarm_responsible, container, false)

        return rootView
    }

}