package objectactivity.responsiblefragment

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
import workservice.OtvlList

class AdapterResponsibleList internal constructor(
    private val otvlList: ArrayList<OtvlList>,
    private val context: Context?
) : RecyclerView.Adapter<AdapterResponsibleList.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_responsible, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return otvlList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.textFIO.text = otvlList[position].name
        holder.textAddress.text = otvlList[position].address
        holder.textPosition.text = otvlList[position].position

        if (otvlList[position].phone == "empty" || otvlList[position].phone == "") {
            holder.buttonPhone.visibility = View.GONE
            holder.additionalContainer.visibility = View.VISIBLE
        }

        if (otvlList[position].phoneh == "empty" || otvlList[position].phoneh == "")
            holder.buttonHome.visibility = View.GONE
        else
            holder.buttonHome.text = "Дом. ${otvlList[position].phoneh}"

        if (otvlList[position].phonew == "empty" || otvlList[position].phonew == "")
            holder.buttonWork.visibility = View.GONE
        else
            holder.buttonWork.text = "Раб. ${otvlList[position].phonew}"

        holder.buttonPhone.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + otvlList[position].phone))
                context!!.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }
        holder.buttonHome.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + otvlList[position].phoneh))
                context!!.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Ваше устройство не поддерживает функцию звонка или не установлена сим-карта", Toast.LENGTH_SHORT).show()
            }
        }
        holder.buttonWork.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + otvlList[position].phonew))
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