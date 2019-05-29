package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kobramob.rubeg38.ru.gbrnavigation.R
import org.json.JSONObject
import java.lang.Exception

class ObjectFragment : Fragment() {
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

        val infoObject = activity!!.intent.getStringExtra("info")

        val jsonObject = JSONObject(infoObject)
        val jsonArray = jsonObject.getJSONArray("d")

        objectName.text = JSONObject(jsonArray.getString(0)).getString("name")
        objectAddress.text = JSONObject(jsonArray.getString(0)).getString("address")
        try {
            objectCustomer.text = "Заказчик: " + JSONObject(jsonArray.getString(0)).getString("zakaz")
        } catch (e: Exception) {
            objectCustomer.text = "Заказчик: "
        }
        try {
            objectTIP.text = "ИНН: " + JSONObject(jsonArray.getString(0)).getString("inn")
        } catch (e: Exception) {
            objectTIP.text = "ИНН: "
        }

        val jsonObject1 = JSONObject(jsonArray.getString(0))
        val jsonObject2 = JSONObject(jsonObject1.getString("area"))

        objectAlarm.text = jsonObject2.getString("name")
        objectAlarmTime.text = "Время тревоги: " + jsonObject2.getString("alarmtime")

        return rootView
    }
}