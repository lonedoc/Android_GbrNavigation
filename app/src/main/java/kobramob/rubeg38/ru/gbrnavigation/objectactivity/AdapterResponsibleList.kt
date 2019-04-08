package kobramob.rubeg38.ru.gbrnavigation.objectactivity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kobramob.rubeg38.ru.gbrnavigation.R
import java.lang.Exception

class AdapterResponsibleList internal constructor(
    private val fioList: ArrayList<String>,
    private val addressList: ArrayList<String>,
    private val positionList: ArrayList<String>,
    private val phoneList: ArrayList<String>,
    private val homeList: ArrayList<String>,
    private val workList: ArrayList<String>,
    private val context: Context?
) : RecyclerView.Adapter<AdapterResponsibleList.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_responsible, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fioList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.textFIO.text = fioList[position]
        holder.textAddress.text = addressList[position]
        holder.textPosition.text = positionList[position]

        if (phoneList[position] == "empty") {
            holder.buttonPhone.visibility = View.GONE
            holder.additionalContainer.visibility = View.VISIBLE
        }

        if (homeList[position] == "empty")
            holder.buttonHome.visibility = View.GONE
        else
            holder.buttonHome.text = "Дом. " + homeList[position]

        if (workList[position] == "empty")
            holder.buttonWork.visibility = View.GONE
        else
            holder.buttonWork.text = "Раб. " + workList[position]

        holder.buttonPhone.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneList[position]))
                context!!.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }
        holder.buttonHome.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + homeList[position]))
                context!!.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }
        holder.buttonWork.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + workList[position]))
                context!!.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }

        holder.parentContainer.setOnClickListener {
            if (holder.additionalContainer.visibility == View.VISIBLE)
                holder.additionalContainer.visibility = View.GONE
            else
                holder.additionalContainer.visibility = View.VISIBLE
        }
/*        holder.parentContainer.setOnClickListener {
                try{
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneList[position]))
                    context!!.startActivity(intent)
                }catch (e:Exception)
                {
                    Toast.makeText(context,"Ваше устройство не поддерживает функцию звонка или не установлена сим-карта",Toast.LENGTH_SHORT).show()
                }
            }*/
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textFIO: TextView = itemView.findViewById(R.id.textFIO)
        var textAddress: TextView = itemView.findViewById(R.id.Address)
        var textPosition: TextView = itemView.findViewById(R.id.Position)
        var buttonPhone: Button = itemView.findViewById(R.id.phone_button)
        var buttonHome: Button = itemView.findViewById(R.id.home_button)
        var buttonWork: Button = itemView.findViewById(R.id.work_button)
        var parentContainer: CardView = itemView.findViewById(R.id.parent_container)
        var additionalContainer: LinearLayout = itemView.findViewById(R.id.additionalInformation)
    }
}