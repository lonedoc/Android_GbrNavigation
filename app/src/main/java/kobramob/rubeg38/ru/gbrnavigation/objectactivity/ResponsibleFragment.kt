package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kobramob.rubeg38.ru.gbrnavigation.R

class ResponsibleFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView: View = inflater.inflate(R.layout.responsible_fragment, container, false)

        val responsibleList: RecyclerView = rootView.findViewById(R.id.responsibleRecyclerView)

        val fioList = ArrayList<String>()
        fioList.add("Кузнецов Евгений Владимирович")
        fioList.add("Геть Алексей Иванович")
        fioList.add("Краснов Ярослав Владимирович")

        val addressList = ArrayList<String>()
        addressList.add("Юбиленая 17 кв.25")
        addressList.add("Юбиленая 45 кв.30")
        addressList.add("Южная 105 кв.22")

        val positionList = ArrayList<String>()
        positionList.add("Директор")
        positionList.add("Программист")
        positionList.add("Уборщик")

        val phoneList = ArrayList<String>()
        phoneList.add("+79041472887")
        phoneList.add("+79645472778")
        phoneList.add("+79041472887")

        val homeList = ArrayList<String>()
        homeList.add("44-74-65")
        homeList.add("empty")
        homeList.add("35-05-90")

        val workList = ArrayList<String>()
        workList.add("38-01-48")
        workList.add("38-01-48")
        workList.add("empty")

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