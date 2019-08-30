package kobramob.rubeg38.ru.gbrnavigation.objectactivity

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
import kobramob.rubeg38.ru.gbrnavigation.R
import kobramob.rubeg38.ru.gbrnavigation.commonactivity.AlarmObjectInfo
import kobramob.rubeg38.ru.gbrnavigation.workservice.DataStore
import kobramob.rubeg38.ru.gbrnavigation.workservice.RubegNetworkService
import org.json.JSONObject
import kotlin.concurrent.thread

class ObjectFragment : androidx.fragment.app.Fragment() {

    private var objectTimeToArrived:TextView? = null

    private var buttonSendReports:Button? = null
    private var buttonSendArrived:Button? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.object_fragment, container, false)
        val bnv: BottomNavigationView = activity!!.findViewById(R.id.objectMenu)
        bnv.menu.getItem(0).isChecked = true

        val objectName: TextView = rootView.findViewById(R.id.objectName)
        val objectAddress: TextView = rootView.findViewById(R.id.objectAddress)
        val objectAlarm: TextView = rootView.findViewById(R.id.objectAlarm)
        val objectAlarmTime: TextView = rootView.findViewById(R.id.objectAlarmTime)
        val objectAlarmApply:TextView = rootView.findViewById(R.id.objectAlarmApplyTime)
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
            report_spinner.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if(report_spinner.selectedItem !=null){
                        selectedReport = DataStore.reports[p2]
                        Log.d("selected report",selectedReport)
                    }
                }
            }
            alertDialog.setView(view)
            alertDialog.setTitle("Отправка рапорта")
            alertDialog.setPositiveButton("Отправить"){ _: DialogInterface, _: Int ->
                val reportsMessage = JSONObject()
                reportsMessage.put("\$c$", "reports")
                reportsMessage.put("report",selectedReport)
                reportsMessage.put("comment","${report_text.text}")
                reportsMessage.put("namegbr",DataStore.namegbr)
                reportsMessage.put("name",alarmObjectInfo.name)
                reportsMessage.put("number",alarmObjectInfo.number)
                Log.d("Report","$reportsMessage")
                RubegNetworkService.protocol.send("$reportsMessage"){
                        success:Boolean->
                    if(success){
                        activity!!.runOnUiThread {
                            Toast.makeText(context,"Рапорт доставлен",Toast.LENGTH_SHORT).show()
                        }
                    }
                    else
                    {
                        activity!!.runOnUiThread {
                            Toast.makeText(context,"Рапорт не доставлен",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
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
                    RubegNetworkService.protocol.send(message = message.toString()) {
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
                        }
                        else
                        {
                            activity!!.runOnUiThread {
                                Toast.makeText(context, "Прибытие не подтверждено", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                .setNeutralButton("Отложить"){
                        dialogInterface, i ->
                    dialogInterface.cancel()
                }
                .show()
        }


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

        if(ObjectDataStore.timeAlarmApply != null){
            objectAlarmApply.text = "Время принятия тревоги: ${ObjectDataStore.timeAlarmApply}"
        } else {
            objectAlarmApply.visibility = View.GONE
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

    var isAlive = false
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        isAlive = true

        if(ObjectDataStore.timeToArrived != null ){
            objectTimeToArrived?.visibility = View.VISIBLE
            objectTimeToArrived?.text = ObjectDataStore.timeToArrived
        }
        else
        {
            thread{
                do {
                    if(ObjectDataStore.timeToArrived != null){
                        activity!!.runOnUiThread {
                            objectTimeToArrived?.visibility = View.VISIBLE
                            objectTimeToArrived?.text = ObjectDataStore.timeToArrived
                        }
                    }
                }while (isAlive && ObjectDataStore.timeToArrived == null)
            }

        }

        if(ObjectDataStore.arrivedToObjectSend && buttonSendReports != null)
        {
            buttonSendReports?.isEnabled = true

            if(ObjectDataStore.putOffArrivedToObjectSend)
                buttonSendArrived?.isEnabled = true

        }
        else
        {
            thread{
                do{
                    activity!!.runOnUiThread {
                        if(ObjectDataStore.arrivedToObjectSend){
                            buttonSendReports?.isEnabled = true

                            if(ObjectDataStore.putOffArrivedToObjectSend)
                            buttonSendArrived?.isEnabled = true
                        }
                    }
                }while(!buttonSendReports?.isEnabled!! && isAlive)

            }
        }
        if(ObjectDataStore.putOffArrivedToObjectSend && buttonSendArrived != null)
        {

                buttonSendArrived?.isEnabled = true

        }
        else
        {
            thread{
                do{
                    activity!!.runOnUiThread {
                        if(ObjectDataStore.putOffArrivedToObjectSend){
                                buttonSendArrived?.isEnabled = true
                        }
                    }
                }while(!buttonSendArrived?.isEnabled!! && isAlive)

            }
        }

    }

    override fun onPause() {
        super.onPause()
        isAlive = false
    }

}