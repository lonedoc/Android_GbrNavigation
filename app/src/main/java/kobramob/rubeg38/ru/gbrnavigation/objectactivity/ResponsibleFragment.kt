package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kobramob.rubeg38.ru.gbrnavigation.R
import org.json.JSONObject

class ResponsibleFragment : androidx.fragment.app.Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView: View = inflater.inflate(R.layout.responsible_fragment, container, false)

        val responsibleList: RecyclerView = rootView.findViewById(R.id.responsibleRecyclerView)

        val arrayList = activity!!.intent.getStringArrayListExtra("otvl")

        val fioList = ArrayList<String>()
        val addressList = ArrayList<String>()
        val positionList = ArrayList<String>()
        val phoneList = ArrayList<String>()
        val homeList = ArrayList<String>()
        val workList = ArrayList<String>()
        for (i in 0 until arrayList!!.count()) {
            fioList.add(JSONObject(arrayList[i]).getString("name"))
            addressList.add(JSONObject(arrayList[i]).getString("address"))
            positionList.add(JSONObject(arrayList[i]).getString("position"))
            try {
                phoneList.add(JSONObject(arrayList[i]).getString("phone"))
            } catch (e: Exception) {
                phoneList.add("empty")
            }
            try {
                homeList.add(JSONObject(arrayList[i]).getString("phoneh"))
            } catch (e: Exception) {
                homeList.add("empty")
            }
            try {
                workList.add(JSONObject(arrayList[i]).getString("phonew"))
            } catch (e: Exception) {
                workList.add("empty")
            }
        }

        responsibleList.layoutManager = LinearLayoutManager(activity)
        responsibleList.adapter = AdapterResponsibleList(fioList, addressList, positionList, phoneList, homeList, workList, context)

        responsibleList.addItemDecoration(
            DividerItemDecoration(
                responsibleList.context,
                DividerItemDecoration.VERTICAL
            )
        )

        return rootView
    }
}