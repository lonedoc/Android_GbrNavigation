package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kobramob.rubeg38.ru.gbrnavigation.R

class ObjectFragment : androidx.fragment.app.Fragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.object_fragment, container, false)
        val bnv: BottomNavigationView = activity!!.findViewById(R.id.objectMenu)
        bnv.menu.getItem(0).isChecked = true

        val objectName: TextView = rootView.findViewById(R.id.objectName)
        val objectAddress: TextView = rootView.findViewById(R.id.objectAddress)
        val objectAlarm: TextView = rootView.findViewById(R.id.objectAlarm)
        val objectAlarmTime: TextView = rootView.findViewById(R.id.objectAlarmTime)
        val objectCustomer: TextView = rootView.findViewById(R.id.objectCustomer)
        val objectTIP: TextView = rootView.findViewById(R.id.objectTIP)

        if (activity!!.intent.getStringExtra("name") != "") {
            objectName.text = activity!!.intent.getStringExtra("name")
        } else {
            objectName.visibility = View.GONE
        }

        if (activity!!.intent.getStringExtra("address") != "") {
            objectAddress.text = activity!!.intent.getStringExtra("address")
        } else {
            objectAddress.visibility = View.GONE
        }

        if (activity!!.intent.getStringExtra("zakaz") != "") {
            objectCustomer.text = "Заказчик: ${activity!!.intent.getStringExtra("zakaz")}"
        } else {
            objectCustomer.visibility = View.GONE
        }

        if (activity!!.intent.getIntExtra("inn", 0) != 0) {
            objectTIP.text = "ИНН: ${activity!!.intent.getIntExtra("inn",0)}"
        } else {
            objectTIP.visibility = View.GONE
        }

        if (activity!!.intent.getStringExtra("areaName") != "") {
            objectAlarm.text = activity!!.intent.getStringExtra("areaName")
        } else {
            objectAlarm.visibility = View.GONE
        }

        if (activity!!.intent.getStringExtra("areaAlarmTime") != "") {
            objectAlarmTime.text = "Время тревоги ${activity!!.intent.getStringExtra("areaAlarmTime")}"
        } else {
            objectAlarm.visibility = View.GONE
        }

        return rootView
    }

}