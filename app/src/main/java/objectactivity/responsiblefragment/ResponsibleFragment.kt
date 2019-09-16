package objectactivity.responsiblefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kobramob.rubeg38.ru.gbrnavigation.R
import commonactivity.AlarmObjectInfo

class ResponsibleFragment : androidx.fragment.app.Fragment() {
    private var rootView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.responsible_fragment, container, false)

        return rootView
    }

    override fun onResume() {
        super.onResume()

        val responsibleList: RecyclerView = rootView?.findViewById(R.id.responsibleRecyclerView)!!

        val alarmObjectInfo = activity!!.intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

        if (alarmObjectInfo.otvlList.count()> 0) {
            responsibleList.layoutManager = LinearLayoutManager(activity)
            responsibleList.adapter =
                AdapterResponsibleList(
                    alarmObjectInfo.otvlList,
                    context
                )

            responsibleList.addItemDecoration(
                DividerItemDecoration(
                    responsibleList.context,
                    DividerItemDecoration.VERTICAL
                )
            )
        } else {
            Toast.makeText(activity!!, "Список ответственных пуст", Toast.LENGTH_SHORT).show()
        }
    }
}