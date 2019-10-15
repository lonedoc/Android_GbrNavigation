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
import java.lang.Exception
import java.util.ArrayList
import kobramob.rubeg38.ru.gbrnavigation.R
import oldVersion.resource.DataStore
import newVersion.Utils.UsInfo

class USInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.usinfo_fragment, container, false)

        val usInfoRecyclerView: RecyclerView = rootView.findViewById(R.id.usinfo_recyclerView)

        usInfoRecyclerView.layoutManager = LinearLayoutManager(activity)
        usInfoRecyclerView.adapter =
            AdapterUsInfo(DataStore.cityCard.usinfo, context!!)
        usInfoRecyclerView.addItemDecoration(
            DividerItemDecoration(
                usInfoRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        return rootView
    }
}

class AdapterUsInfo(val usinfo: ArrayList<UsInfo>, val context: Context) :
    RecyclerView.Adapter<AdapterUsInfo.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_usinfo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return usinfo.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameTextView.text = usinfo[position].name
        holder.button.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + usinfo[position].phone))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.usinfo_name)
        val button: Button = itemView.findViewById(R.id.usinfo_phone_button)
    }
}
