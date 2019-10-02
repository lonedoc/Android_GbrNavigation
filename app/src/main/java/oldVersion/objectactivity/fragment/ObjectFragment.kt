package oldVersion.objectactivity.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlin.concurrent.thread
import oldVersion.commonactivity.AlarmObjectInfo
import oldVersion.objectactivity.data.ObjectDataStore
import oldVersion.objectactivity.navigatorfragment.NavigatorFragment
import oldVersion.resource.DataStore
import oldVersion.workservice.ProtocolNetworkService
import org.json.JSONObject

class ObjectFragment : androidx.fragment.app.Fragment() {

    private var objectTimeToArrived: TextView? = null

    private var buttonSendReports: Button? = null
    private var buttonSendArrived: Button? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.object_fragment, container, false)
        val bnv: BottomNavigationView = activity!!.findViewById(R.id.objectMenu)
        bnv.menu.getItem(0).isChecked = true

        val objectName: TextView = rootView.findViewById(R.id.objectName)
        val objectAddress: TextView = rootView.findViewById(R.id.objectAddress)
        val objectAlarm: TextView = rootView.findViewById(R.id.objectAlarm)
        val objectAlarmTime: TextView = rootView.findViewById(R.id.objectAlarmTime)
        val objectAlarmApply: TextView = rootView.findViewById(R.id.objectAlarmApplyTime)
        objectTimeToArrived = rootView.findViewById(R.id.objectTimeToArrived)
        val objectCustomer: TextView = rootView.findViewById(R.id.objectCustomer)
        val objectTIP: TextView = rootView.findViewById(R.id.objectTIP)
        buttonSendReports = rootView.findViewById(R.id.button_send_reports)
        buttonSendArrived = rootView.findViewById(R.id.button_send_arrived)
        val alarmObjectInfo = activity!!.intent.getSerializableExtra("objectInfo") as AlarmObjectInfo

        buttonSendReports?.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context)
            val view = layoutInflater.inflate(R.layout.dialog_reports, null, false)
            val report_spinner: Spinner = view.findViewById(R.id.reports_spinner)
            val report_text: EditText = view.findViewById(R.id.report_EditText)
            report_spinner.prompt = "Список рапортов"
            report_spinner.adapter = ArrayAdapter(
                context!!,
                R.layout.report_spinner_item,
                DataStore.reports
            )
            var selectedReport = ""
            report_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (report_spinner.selectedItem != null) {
                        selectedReport = DataStore.reports[p2]
                        Log.d("selected report", selectedReport)
                    }
                }
            }
            alertDialog.setView(view)
            alertDialog.setTitle("Отправка рапорта")
            alertDialog.setPositiveButton("Отправить") { _: DialogInterface, _: Int ->
                val reportsMessage = JSONObject()
                reportsMessage.put("\$c$", "reports")
                reportsMessage.put("report", selectedReport)
                reportsMessage.put("comment", "${report_text.text}")
                reportsMessage.put("namegbr", DataStore.namegbr)
                reportsMessage.put("name", alarmObjectInfo.name)
                reportsMessage.put("number", alarmObjectInfo.number)
                Log.d("Report", "$reportsMessage")
                ProtocolNetworkService.protocol?.send("$reportsMessage") {
                    success: Boolean ->
                        if (success) {
                            activity!!.runOnUiThread {
                                Toast.makeText(context, "Рапорт доставлен", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            activity!!.runOnUiThread {
                                Toast.makeText(context, "Рапорт не доставлен", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
            alertDialog.setNeutralButton("Отложить") {
                dialog: DialogInterface, i ->
                    dialog.cancel()
                }
            val dialog = alertDialog.create()
            dialog.setCancelable(false)
            dialog.show()
        }

        buttonSendArrived?.setOnClickListener {

            val arrivedDialog = androidx.appcompat.app.AlertDialog.Builder(context!!)
            arrivedDialog.setTitle("Прибытие")
                .setCancelable(false)
                .setMessage("Вы прибыли на место")
                .setPositiveButton("Подтвердить") {
                    _, _ ->
                        ObjectDataStore.arrivedToObjectSend = true
                        val message = JSONObject()
                        message.put("\$c$", "gbrkobra")
                        message.put("command", "alarmpr")
                        message.put("number", alarmObjectInfo.number)
                        ProtocolNetworkService.protocol?.send(message = message.toString()) {
                            success: Boolean ->
                                if (success) {
                                    activity!!.runOnUiThread {
                                        if (NavigatorFragment.road != null) {
                                            if (NavigatorFragment.road!!.mRouteHigh.count() > 1) {
                                                if (NavigatorFragment.mMapView != null) {
                                                    NavigatorFragment.road!!.mRouteHigh.clear()
                                                    NavigatorFragment.mMapView!!.overlays.removeAt(
                                                        NavigatorFragment.mMapView!!.overlays.count() - 1
                                                    )
                                                    NavigatorFragment.mMapView!!.invalidate()
                                                }
                                            }
                                        }

                                        Toast.makeText(context, "Прибытие подтверждено", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    activity!!.runOnUiThread {
                                        Toast.makeText(context, "Прибытие не подтверждено", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    }
                .setNeutralButton("Отложить") {
                    dialogInterface, i ->
                        dialogInterface.cancel()
                    }
                .show()
        }

        try {
            if (alarmObjectInfo.name != " ") {
                objectName.text = alarmObjectInfo.name
            } else {
                objectName.visibility = View.GONE
            }
        } catch (e: Exception) {
            objectName.visibility = View.GONE
        }

        try {
            if (alarmObjectInfo.address != " ") {
                objectAddress.text = alarmObjectInfo.address
            } else {
                objectAddress.visibility = View.GONE
            }
        } catch (e: Exception) {
            objectAddress.visibility = View.GONE
        }

        try {
            if (alarmObjectInfo.zakaz != " ") {
                objectCustomer.text = "Заказчик: ${alarmObjectInfo.zakaz}"
            } else {
                objectCustomer.visibility = View.GONE
            }
        } catch (e: Exception) {
            objectCustomer.visibility = View.GONE
        }

        try {
            if (ObjectDataStore.timeAlarmApply != null) {
                objectAlarmApply.text = "Время принятия тревоги: ${ObjectDataStore.timeAlarmApply}"
            } else {
                objectAlarmApply.visibility = View.GONE
            }
        } catch (e: Exception) {
            objectAlarmApply.visibility = View.GONE
        }

        try {
            if (alarmObjectInfo.areaAlarmTime != "") {
                objectAlarmTime.text = "Время тревоги ${alarmObjectInfo.areaAlarmTime}"
            } else {
                objectAlarm.visibility = View.GONE
            }
        } catch (e: Exception) {
            objectAlarmTime.visibility = View.GONE
        }

        try {

            if (alarmObjectInfo.inn != "") {
                objectTIP.text = "ИНН: ${alarmObjectInfo.inn}"
            } else {
                objectTIP.visibility = View.GONE
            }
        } catch (e: Exception) {
            objectTIP.visibility = View.GONE
        }

        try {
            if (alarmObjectInfo.areaName != "") {
                objectAlarm.text = alarmObjectInfo.areaName
            } else {
                objectAlarm.visibility = View.GONE
            }
        } catch (e: Exception) {
            objectAlarm.visibility = View.GONE
        }

        return rootView
    }

    var isAlive = false
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        isAlive = true
        Log.d("ObjectFragment", "$isAlive")

        thread {
            if (ObjectDataStore.timeToArrived != null) {
                activity!!.runOnUiThread {
                    objectTimeToArrived?.visibility = View.VISIBLE
                    objectTimeToArrived?.text = ObjectDataStore.timeToArrived
                }
            } else {
                Log.d("ObjectFragment", "StartThread 1")
                do {
                    if (ObjectDataStore.timeToArrived != null) {
                        activity!!.runOnUiThread {
                            objectTimeToArrived?.visibility = View.VISIBLE
                            objectTimeToArrived?.text = ObjectDataStore.timeToArrived
                        }
                    }
                } while (isAlive && ObjectDataStore.timeToArrived == null)
                Log.d("ObjectFragment", "StopThread 1")
            }
        }
        thread {
            if (ObjectDataStore.arrivedToObjectSend && buttonSendReports != null) {
                activity!!.runOnUiThread {
                    buttonSendReports?.isEnabled = true

                    if (ObjectDataStore.putOffArrivedToObjectSend)
                        buttonSendArrived?.isEnabled = true
                }
            } else {
                Log.d("ObjectFragment", "StartThread 2")
                do {

                    if (ObjectDataStore.arrivedToObjectSend) {
                        activity!!.runOnUiThread {
                            buttonSendReports?.isEnabled = true

                            if (ObjectDataStore.putOffArrivedToObjectSend)
                                buttonSendArrived?.isEnabled = true
                        }
                    }
                } while (!buttonSendReports?.isEnabled!! && isAlive)
                Log.d("ObjectFragment", "StopThread 2")
            }
        }
        thread {
            if (ObjectDataStore.putOffArrivedToObjectSend && buttonSendArrived != null) {
                activity!!.runOnUiThread {
                    buttonSendArrived?.isEnabled = true
                }
            } else {
                Log.d("ObjectFragment", "StartThread 3")
                do {
                    if (ObjectDataStore.putOffArrivedToObjectSend) {
                        activity!!.runOnUiThread {
                            buttonSendArrived?.isEnabled = true
                        }
                    }
                } while (!buttonSendArrived?.isEnabled!! && isAlive)
                Log.d("ObjectFragment", "StopThread 3")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isAlive = false
        Log.d("ObjectFragment", "$isAlive")
    }
}