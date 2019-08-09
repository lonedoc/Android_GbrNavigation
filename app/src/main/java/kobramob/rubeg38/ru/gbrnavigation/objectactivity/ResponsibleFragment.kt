package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.AlarmObjectInfo

class ResponsibleFragment : androidx.fragment.app.Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView: View = inflater.inflate(R.layout.responsible_fragment, container, false)

        val responsibleList: RecyclerView = rootView.findViewById(R.id.responsibleRecyclerView)

        val alarmObjectInfo = activity!!.intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

        responsibleList.layoutManager = LinearLayoutManager(activity)
        responsibleList.adapter = AdapterResponsibleList(alarmObjectInfo.otvlList, context)

        responsibleList.addItemDecoration(
            DividerItemDecoration(
                responsibleList.context,
                DividerItemDecoration.VERTICAL
            )
        )

        return rootView
    }
}