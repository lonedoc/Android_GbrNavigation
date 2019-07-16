package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception
import kobramob.rubeg38.ru.gbrnavigation.R
import org.json.JSONObject

class ObjectFragment : androidx.fragment.app.Fragment() {
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

        objectName.text = jsonObject.getString("name")
        objectAddress.text = jsonObject.getString("address")
        try {
            objectCustomer.text = "Заказчик: " + jsonObject.getString("zakaz")
        } catch (e: Exception) {
            objectCustomer.text = "Заказчик: "
        }
        try {
            objectTIP.text = "ИНН: " + jsonObject.getString("inn")
        } catch (e: Exception) {
            objectTIP.text = "ИНН: "
        }

        val jsonObject1 = jsonObject
        val jsonObject2 = JSONObject(jsonObject1.getString("area"))

        objectAlarm.text = jsonObject2.getString("name")
        objectAlarmTime.text = "Время тревоги: " + jsonObject2.getString("alarmtime")

        return rootView
    }
}