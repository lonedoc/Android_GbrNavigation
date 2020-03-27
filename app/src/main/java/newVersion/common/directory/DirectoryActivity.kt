package newVersion.common.directory

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import gbr.utils.data.EsInfo
import gbr.utils.data.UsInfo
import kobramob.rubeg38.ru.gbrnavigation.R
import kotlinx.android.synthetic.main.activity_directory.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import newVersion.models.AdapterEsPhone
import newVersion.models.AdapterUsPhone

class DirectoryActivity: MvpAppCompatActivity(),DirectoryView {

    @InjectPresenter
    lateinit var presenter:DirectoryPresenter

    override fun setServicePhone(phone: String, state: Boolean) {
        runOnUiThread {

            if(!state)
            directory_service_phone.setImageResource(R.drawable.ic_phone_disable)

            directory_service_phone.isEnabled = state

            directory_service_phone.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:$phone")
                )
                startActivity(intent)
            }

        }
    }
    override fun setPcsPhone(phone: String, state: Boolean) {
        runOnUiThread {
            if(!state)
                directory_pcs_phone.setImageResource(R.drawable.ic_phone_disable)

            directory_pcs_phone.isEnabled = state

            directory_pcs_phone.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:$phone")
                )
                startActivity(intent)
            }

        }
    }

    override fun setDistanceArrived(distance: String) {
        runOnUiThread {
            directory_distance.text = distance

        }
    }



    override fun showToastMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }
    }

    override fun initUSInfo(usInfo: ArrayList<UsInfo>) {
        alarm_directory_us_phone.layoutManager = LinearLayoutManager(this)
        alarm_directory_us_phone.adapter = AdapterUsPhone(usInfo, this)

        alarm_directory_us_phone.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun initEsInfo(esInfo: ArrayList<EsInfo>) {
        alarm_directory_es_phone.layoutManager = LinearLayoutManager(this)
        alarm_directory_es_phone.adapter = AdapterEsPhone(esInfo, this)

        alarm_directory_es_phone.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun setTitle(title: String) {
        runOnUiThread {
            directory_toolbar.title = title
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directory)
        setSupportActionBar(directory_toolbar)
    }

    override fun onStart() {
        super.onStart()


        presenter.initPCSInfo()
        presenter.initESInfo()
        presenter.initUSInfo()
    }

    override fun onResume() {
        super.onResume()
        presenter.setTitle()
    }
    override fun onBackPressed() {
        super.onBackPressed()
    }
}