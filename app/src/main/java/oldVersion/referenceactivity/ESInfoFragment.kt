package oldVersion.referenceactivity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kobramob.rubeg38.ru.gbrnavigation.R
import oldVersion.resource.DataStore
import newVersion.utils.EsInfo

class ESInfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.esinfo_fragment, container, false)

        val esInfoRecyclerView: RecyclerView = rootView.findViewById(R.id.esinfo_recyclerView)

        println(DataStore.cityCard.esinfo)
        esInfoRecyclerView.layoutManager = LinearLayoutManager(activity)
        esInfoRecyclerView.adapter =
            AdapterEsInfo(DataStore.cityCard.esinfo, context!!)
        esInfoRecyclerView.addItemDecoration(
            DividerItemDecoration(
                esInfoRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        return rootView
    }
}

class AdapterEsInfo(val esinfo: ArrayList<EsInfo>, val context: Context) : RecyclerView.Adapter<AdapterEsInfo.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_esinfo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return esinfo.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameTextView.text = esinfo[position].name
        holder.button.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + esinfo[position].phone))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.esinfo_name)
        val button: Button = itemView.findViewById(R.id.esinfo_phone_button)
    }
}
