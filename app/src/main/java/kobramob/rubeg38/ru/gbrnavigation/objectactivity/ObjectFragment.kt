package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.AlarmObjectInfo

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

        val alarmObjectInfo = activity!!.intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

        if (alarmObjectInfo.name != " ") {
            objectName.text = alarmObjectInfo.name
        } else {
            objectName.visibility = View.GONE
        }

        if (alarmObjectInfo.address != " ") {
            objectAddress.text = alarmObjectInfo.address
        } else {
            objectAddress.visibility = View.GONE
        }

        if (alarmObjectInfo.zakaz != " ") {
            objectCustomer.text = "Заказчик: ${alarmObjectInfo.zakaz}"
        } else {
            objectCustomer.visibility = View.GONE
        }

        if (alarmObjectInfo.inn != 0L) {
            objectTIP.text = "ИНН: ${alarmObjectInfo.inn}"
        } else {
            objectTIP.visibility = View.GONE
        }

        if (alarmObjectInfo.areaName != "") {
            objectAlarm.text = alarmObjectInfo.areaName
        } else {
            objectAlarm.visibility = View.GONE
        }

        if (alarmObjectInfo.areaAlarmTime != "") {
            objectAlarmTime.text = "Время тревоги ${alarmObjectInfo.areaAlarmTime}"
        } else {
            objectAlarm.visibility = View.GONE
        }

        return rootView
    }
}