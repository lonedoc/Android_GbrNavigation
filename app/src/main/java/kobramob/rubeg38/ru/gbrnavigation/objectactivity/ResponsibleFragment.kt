package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kobramob.rubeg38.ru.gbrnavigation.R
import org.json.JSONObject

class ResponsibleFragment : androidx.fragment.app.Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView: View = inflater.inflate(R.layout.responsible_fragment, container, false)

        val responsibleList: androidx.recyclerview.widget.RecyclerView = rootView.findViewById(R.id.responsibleRecyclerView)

        val jsonObject = JSONObject(activity!!.intent.getStringExtra("info"))
        val jsonArray1 = jsonObject.getJSONArray("otvl")
        val length = jsonArray1.length()
        val fioList = ArrayList<String>()
        val addressList = ArrayList<String>()
        val positionList = ArrayList<String>()
        val phoneList = ArrayList<String>()
        val homeList = ArrayList<String>()
        val workList = ArrayList<String>()
        for (i in 0 until length) {
            fioList.add(JSONObject(jsonArray1.getString(i)).getString("name"))
            addressList.add(JSONObject(jsonArray1.getString(i)).getString("address"))
            positionList.add(JSONObject(jsonArray1.getString(i)).getString("position"))
            phoneList.add(JSONObject(jsonArray1.getString(i)).getString("phone"))
            homeList.add(JSONObject(jsonArray1.getString(i)).getString("phoneh"))
            workList.add(JSONObject(jsonArray1.getString(i)).getString("phonew"))
        }

        responsibleList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        responsibleList.adapter = AdapterResponsibleList(fioList, addressList, positionList, phoneList, homeList, workList, context)

        responsibleList.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                responsibleList.context,
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
        )

        return rootView
    }
}