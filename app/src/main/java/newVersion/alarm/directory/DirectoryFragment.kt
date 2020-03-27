package newVersion.alarm.directory

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gbr.utils.data.EsInfo
import gbr.utils.data.UsInfo
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_directory.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import newVersion.models.AdapterEsPhone
import newVersion.models.AdapterUsPhone

class DirectoryFragment: MvpAppCompatFragment(),DirectoryFragmentView {

    @InjectPresenter
    lateinit var presenter:DirectoryFragmentPresenter

    companion object{
        var isAlive:Boolean = false
    }
    override fun setServicePhone(phone: String, state: Boolean) {
        activity?.runOnUiThread {

            val servicePhone:ImageButton = rootView?.findViewById(R.id.alarm_directory_service_phone)!!
            if(!state)
                servicePhone.setImageResource(R.drawable.ic_phone_disable)

            servicePhone.isEnabled = state

            servicePhone.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:$phone")
                )
                startActivity(intent)
            }

        }
    }
    override fun setPcsPhone(phone: String, state: Boolean) {
        activity?.runOnUiThread {
            val pcsPhone:ImageButton = rootView?.findViewById(R.id.alarm_directory_pcs_phone)!!
            if(!state)
                pcsPhone.setImageResource(R.drawable.ic_phone_disable)

            pcsPhone.isEnabled = state

            pcsPhone.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:$phone")
                )
                startActivity(intent)
            }

        }
    }

    override fun setDistanceArrived(distance: String) {
        activity?.runOnUiThread {
            val distanceSTR: TextView = rootView?.findViewById(R.id.alarm_directory_distance)!!
            distanceSTR.text = distance

        }
    }



    override fun showToastMessage(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun initUSInfo(usInfo: ArrayList<UsInfo>) {
        val usPhone: RecyclerView = rootView?.findViewById(R.id.alarm_directory_us_phone)!!
        usPhone.layoutManager = LinearLayoutManager(context)
        usPhone.adapter = AdapterUsPhone(usInfo, activity!!)

        usPhone.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun initEsInfo(esInfo: ArrayList<EsInfo>) {
        val esPhone: RecyclerView = rootView?.findViewById(R.id.alarm_directory_es_phone)!!
        esPhone.layoutManager = LinearLayoutManager(context)
        esPhone.adapter = AdapterEsPhone(esInfo, activity!!)

        esPhone.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun setTitle(title: String) {
        activity?.runOnUiThread {
            directory_toolbar.title = title
        }
    }


    var rootView:View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_alarm_directory,container,false)



        return rootView
    }

    override fun onResume() {
        super.onResume()
        isAlive = true
        presenter.initPCSInfo()
        presenter.initESInfo()
        presenter.initUSInfo()
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
        presenter.onDestroy()
    }
}