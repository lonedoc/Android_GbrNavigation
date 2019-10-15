package oldVersion.objectactivity.responsiblefragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import kobramob.rubeg38.ru.gbrnavigation.R
import newVersion.Utils.ResponsibleList

class AdapterResponsibleList internal constructor(
    private val responsibleList: ArrayList<ResponsibleList>,
    private val context: Context?
) : RecyclerView.Adapter<AdapterResponsibleList.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_responsible, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return responsibleList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.textFIO.text = responsibleList[position].name
        holder.textAddress.text = responsibleList[position].address
        holder.textPosition.text = responsibleList[position].position

        if (responsibleList[position].phone == "empty" || responsibleList[position].phone == "") {
            holder.buttonPhone.visibility = View.GONE
            holder.additionalContainer.visibility = View.VISIBLE
        }

        if (responsibleList[position].phoneh == "empty" || responsibleList[position].phoneh == "")
            holder.buttonHome.visibility = View.GONE
        else
            holder.buttonHome.text = "Дом. ${responsibleList[position].phoneh}"

        if (responsibleList[position].phonew == "empty" || responsibleList[position].phonew == "")
            holder.buttonWork.visibility = View.GONE
        else
            holder.buttonWork.text = "Раб. ${responsibleList[position].phonew}"

        holder.buttonPhone.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + responsibleList[position].phone))
                context!!.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }
        holder.buttonHome.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + responsibleList[position].phoneh))
                context!!.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }
        holder.buttonWork.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + responsibleList[position].phonew))
                context!!.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }

        holder.parentContainer.setOnClickListener {

            holder.additionalContainer.visibility = if (holder.additionalContainer.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
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