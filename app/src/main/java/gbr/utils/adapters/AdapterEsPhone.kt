package gbr.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import gbr.utils.data.EsInfo
import kobramob.rubeg38.ru.gbrnavigation.R
import java.util.ArrayList

class AdapterEsPhone(private val esInfo: ArrayList<EsInfo>, val context: Context) :
    RecyclerView.Adapter<AdapterEsPhone.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_directory, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return esInfo.count()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = esInfo[position].name
        holder.phone.text = "Номер: ${esInfo[position].phone}"
        holder.parent.setOnClickListener {
            if(esInfo[position].phone!="")
            {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:${esInfo[position].phone}")
                )
                context.startActivity(intent)
            }
            else
            {
                Toast.makeText(context,"Номер не задан",Toast.LENGTH_SHORT).show()
            }
        }
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.directory_name)
        val phone:TextView = itemView.findViewById(R.id.directory_phone)
        val parent:ConstraintLayout = itemView.findViewById(R.id.directory_parent)
    }

}
