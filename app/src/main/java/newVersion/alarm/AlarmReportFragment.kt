package newVersion.alarm

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import kobramob.rubeg38.ru.gbrnavigation.R
import moxy.MvpAppCompatDialogFragment
import newVersion.callback.ReportCallback

class AlarmReportFragment: MvpAppCompatDialogFragment() {

    companion object{
        lateinit var callback: ReportCallback
        fun newInstance(reports:ArrayList<String>,callback:ReportCallback):AlarmReportFragment{
            this.callback = callback
            val frag = AlarmReportFragment()
            val args = Bundle()
            args.putStringArrayList("report",reports)
            frag.arguments = args
            return frag
        }
    }
    var rootView: View? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = layoutInflater.inflate(R.layout.fragment_alarm_report_dialog,null)

        val spinner: Spinner = rootView?.findViewById(R.id.reports_spinner)!!
        val editText:EditText = rootView?.findViewById(R.id.report_edittext)!!

        var selectedReport = "не выбран"

        spinner.prompt = "Отправка рапорта"
        spinner.adapter = arguments?.getStringArrayList("report")?.let {
            ArrayAdapter(
                context!!,
                R.layout.report_spinner_item,
                it
            )
        }
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedReport = arguments?.getStringArrayList("report")?.let { it[p2] }.toString()
            }
        }

        isCancelable = false

        return builder
            .setView(rootView)
            .setTitle("Отправка рапорта")
            .setNeutralButton("Отложить"){
                    _, _ ->
                callback.reportNotSend()
            }
            .setPositiveButton("Отправить"){
                    _, _ ->
                callback.sendReport(selectedReport,editText.text.toString())
            }
            .show()
    }
}