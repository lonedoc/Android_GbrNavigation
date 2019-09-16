package referenceactivity

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
import kobramob.rubeg38.ru.gbrnavigation.R
import resource.DataStore
import java.lang.Exception

class PCSInfoFragment:Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView:View = inflater.inflate(R.layout.pcsinfo_fragment,container,false)

        val pcsInfoRecyclerView:RecyclerView = rootView.findViewById(R.id.pcsinfo_recyclerView)

        val pcsinfoList:ArrayList<Pair<String,String>> = ArrayList()

        if(DataStore.cityCard.pcsinfo.operatorphone != "")
        {
            pcsinfoList.add(Pair("Дежурный оператор", DataStore.cityCard.pcsinfo.operatorphone))
        }

        if(DataStore.cityCard.pcsinfo.servicecenterphone != ""){
            pcsinfoList.add(Pair("Сервисный центр", DataStore.cityCard.pcsinfo.servicecenterphone))
        }

        if(pcsinfoList.count()>0)
        {
            pcsInfoRecyclerView.layoutManager = LinearLayoutManager(activity)
            pcsInfoRecyclerView.adapter = AdapterPcsInfo(pcsinfoList, context!!)

            pcsInfoRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    pcsInfoRecyclerView.context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        return rootView
    }
}

class AdapterPcsInfo(val pcsinfoList: ArrayList<Pair<String, String>>,val context: Context) :
    RecyclerView.Adapter<AdapterPcsInfo.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_pcsinfo,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pcsinfoList.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameTextView.text = pcsinfoList[position].first
        if(pcsinfoList[position].second != ""){
            holder.button.setOnClickListener{
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + pcsinfoList[position].second))
                    context.startActivity(intent)
                }catch (e:Exception){
                    Toast.makeText(context,"Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else
        {
            holder.button.visibility = View.GONE
        }
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val nameTextView: TextView = itemView.findViewById(R.id.pcsinfo_name)
        val button: Button = itemView.findViewById(R.id.pcsinfo_phone_button)
    }
}
